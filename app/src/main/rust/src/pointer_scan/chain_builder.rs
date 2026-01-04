//! 第二阶段：指针链构造器
//!
//! 本模块从目标地址反向构建指针链，追溯到静态模块。
//! 使用第一阶段构建的指针库来查找所有可能的路径。
//!
//! ## 算法
//! - `build_pointer_chains`: 主入口，调用分层BFS算法
//! - `build_pointer_chains_layered_bfs`: **分层BFS + rayon并行**

use crate::pointer_scan::storage::MmapQueue;
use crate::pointer_scan::types::{PointerChain, PointerChainStep, PointerData, PointerScanConfig, VmStaticData};
use anyhow::Result;
use log::{debug, info, warn};
use rayon::prelude::*;
use std::cmp::Ordering;
use std::sync::atomic::{AtomicBool, AtomicUsize, Ordering as AtomicOrdering};

/// 在 MmapQueue<PointerData> 中二分查找值在 [min, max) 范围内的指针。
/// 返回 (起始索引, 结束索引)。
fn find_range_in_pointer_queue(queue: &MmapQueue<PointerData>, min_value: u64, max_value: u64) -> (usize, usize) {
    let count = queue.len();
    if count == 0 {
        return (0, 0);
    }

    // 辅助函数：从归档的 PointerData 获取 value 字段
    let get_value = |index: usize| -> Option<u64> { queue.get(index).map(|archived| archived.value.to_native()) };

    // 二分查找：下界
    let mut left = 0;
    let mut right = count;
    while left < right {
        let mid = left + (right - left) / 2;
        match get_value(mid) {
            Some(val) if val < min_value => left = mid + 1,
            Some(_) => right = mid,
            None => break,
        }
    }
    let start_idx = left;

    // 二分查找：上界
    left = start_idx;
    right = count;
    while left < right {
        let mid = left + (right - left) / 2;
        match get_value(mid) {
            Some(val) if val < max_value => left = mid + 1,
            Some(_) => right = mid,
            None => break,
        }
    }
    let end_idx = left;

    (start_idx, end_idx)
}

/// 查找所有指向 [target - max_offset, target + max_offset] 范围的指针。
/// 返回 Vec<(指针地址, 有符号偏移)>，其中 有符号偏移 = target - 指针值。
/// 正偏移：指针指向target下方
/// 负偏移：指针指向target上方
fn find_pointers_to_range(pointer_lib: &MmapQueue<PointerData>, target: u64, max_offset: u32) -> Vec<(u64, i64)> {
    let min_value = target.saturating_sub(max_offset as u64);
    let max_value = target.saturating_add(max_offset as u64 + 1); // 上界不包含

    let (start_idx, end_idx) = find_range_in_pointer_queue(pointer_lib, min_value, max_value);

    let mut results = Vec::with_capacity(end_idx - start_idx);

    for i in start_idx..end_idx {
        if let Some(archived) = pointer_lib.get(i) {
            let ptr_address = archived.address.to_native();
            let ptr_value = archived.value.to_native();
            // 有符号偏移：正值表示指针指向target下方
            let offset = (target as i64).wrapping_sub(ptr_value as i64);
            // ptr_address这个位置有个指针值，把它读出来然后加上offset得到target
            results.push((ptr_address, offset));
        }
    }

    results
}

/// 检查地址是否属于静态模块。
/// 如果找到，返回 (模块名, 模块索引, 基址偏移)。
fn classify_pointer(address: u64, static_modules: &[VmStaticData]) -> Option<(String, u32, u64)> {
    for module in static_modules {
        if module.contains(address) {
            let offset = module.offset_from_base(address);
            return Some((module.name.clone(), module.index, offset));
        }
    }
    None
}

/// 第二阶段：使用分层BFS从目标地址构建指针链。
///
/// 这是主入口函数，使用并行分层BFS算法。
/// 层内并行、层间串行：每个深度层级内部并行处理，层级之间顺序处理。
///
/// # 参数
/// * `pointer_lib` - 第一阶段构建的已排序指针库
/// * `static_modules` - 静态模块列表（代码段）
/// * `config` - 扫描配置
/// * `progress_callback` - 进度回调 (当前深度, 已找到链数)
/// * `check_cancelled` - 检查是否取消的函数
///
/// # 返回
/// 完整指针链的向量
pub fn build_pointer_chains<F, C>(
    pointer_lib: &MmapQueue<PointerData>,
    static_modules: &[VmStaticData],
    config: &PointerScanConfig,
    progress_callback: F,
    check_cancelled: C,
) -> Result<Vec<PointerChain>>
where
    F: Fn(u32, i64) + Sync,
    C: Fn() -> bool + Sync,
{
    build_pointer_chains_layered_bfs(pointer_lib, static_modules, config, progress_callback, check_cancelled)
}

/// BFS遍历的路径节点。
/// 存储当前目标地址和从target到此节点的偏移历史。
#[derive(Clone)]
struct PathNode {
    /// 当前正在搜索指向此地址的指针
    current_target: u64,
    /// 偏移历史：offsets[0] 是从深度0到深度1的偏移，依此类推。
    /// 构建链时需要反转以获得 root->target 的顺序
    offset_history: Vec<i64>,
}

impl PathNode {
    fn new(target: u64) -> Self {
        Self {
            current_target: target,
            offset_history: Vec::new(),
        }
    }

    fn with_capacity(target: u64, capacity: usize) -> Self {
        Self {
            current_target: target,
            offset_history: Vec::with_capacity(capacity),
        }
    }

    fn depth(&self) -> usize {
        self.offset_history.len()
    }

    /// 创建子节点，带有给定的指针地址和偏移
    fn child(&self, ptr_address: u64, offset: i64) -> Self {
        let mut new_history = self.offset_history.clone();
        new_history.push(offset);
        Self {
            current_target: ptr_address,
            offset_history: new_history,
        }
    }
}

/// 散射阶段发现的候选指针
struct Candidate {
    /// 指向父节点目标的指针地址
    ptr_address: u64,
    /// 从指针值到父节点目标的偏移
    offset: i64,
    /// 父PathNode在当前层中的索引
    parent_idx: usize,
}

/// 每层最大候选数，防止内存爆炸
const MAX_CANDIDATES_PER_LAYER: usize = 30_000_000;

/// 使用分层BFS + rayon并行构建指针链。
///
/// 算法流程：
/// 1. 从目标地址初始化CurrentLayer
/// 2. 对于每个深度层级：
///    a. 散射阶段：并行扫描CurrentLayer中所有节点，查找候选指针
///    b. 检查静态根并构建完整链
///    c. 将非静态候选移动到NextLayer
/// 3. 重复直到达到max_depth或没有更多候选
///
/// 注意：不使用全局visited，允许同一个中间节点出现在不同的路径中。
/// 例如：a->b->c 和 a->d->b->c 可以同时存在，因为虽然都经过b，但路径不同。
pub fn build_pointer_chains_layered_bfs<F, C>(
    pointer_lib: &MmapQueue<PointerData>,
    static_modules: &[VmStaticData],
    config: &PointerScanConfig,
    progress_callback: F,
    check_cancelled: C,
) -> Result<Vec<PointerChain>>
where
    F: Fn(u32, i64) + Sync,
    C: Fn() -> bool + Sync,
{
    info!(
        "构建指针链 (分层BFS) 目标=0x{:X}, 最大深度={}, 最大偏移=0x{:X}",
        config.target_address, config.max_depth, config.max_offset
    );

    let mut results: Vec<PointerChain> = Vec::new();

    // 用目标地址初始化
    let mut current_layer = vec![PathNode::new(config.target_address)];

    let cancelled = AtomicBool::new(false);
    let chains_found = AtomicUsize::new(0);

    for depth in 0..config.max_depth {
        if check_cancelled() {
            cancelled.store(true, AtomicOrdering::Relaxed);
            break;
        }

        if current_layer.is_empty() {
            debug!("深度 {} 没有更多候选", depth);
            break;
        }

        info!("处理深度 {}, 当前层 {} 个节点", depth, current_layer.len());

        // 并行扫描：每个线程处理current_layer的一个分块
        // 并将候选收集到线程局部缓冲区
        let candidates: Vec<Candidate> = current_layer
            .par_iter()
            .enumerate()
            .flat_map(|(parent_idx, node)| {
                if cancelled.load(AtomicOrdering::Relaxed) {
                    return Vec::new();
                }

                let pointers = find_pointers_to_range(pointer_lib, node.current_target, config.max_offset);

                pointers
                    .into_iter()
                    .map(|(ptr_address, offset)| Candidate {
                        ptr_address,
                        offset,
                        parent_idx,
                    })
                    .collect::<Vec<_>>()
            })
            .collect();

        if cancelled.load(AtomicOrdering::Relaxed) {
            break;
        }

        debug!("散射阶段在深度 {} 发现 {} 个候选", depth, candidates.len());

        // 直接遍历所有候选，无需去重：
        // - 同一个 parent 的候选中，ptr_address 本来就唯一
        // - 不同 parent 的相同 ptr_address 代表不同路径，都应保留
        let mut next_layer: Vec<PathNode> = Vec::new();

        for candidate in candidates {
            let parent = &current_layer[candidate.parent_idx];

            // 避免回到原始target形成循环
            if candidate.ptr_address == config.target_address {
                continue;
            }

            // 检查此指针是否来自静态模块
            if let Some((module_name, module_index, base_offset)) = classify_pointer(candidate.ptr_address, static_modules) {
                // 找到一条完整链！
                let mut chain = PointerChain::with_capacity(config.target_address, parent.depth() + 2);

                // 添加静态根
                chain.push(PointerChainStep::static_root(module_name, module_index, base_offset as i64));

                // 添加从静态指针到其目标的偏移
                if candidate.offset != 0 {
                    chain.push(PointerChainStep::dynamic_offset(candidate.offset));
                }

                // 按反序添加中间偏移 (parent -> ... -> target)
                for &offset in parent.offset_history.iter().rev() {
                    chain.push(PointerChainStep::dynamic_offset(offset));
                }

                results.push(chain);
                chains_found.fetch_add(1, AtomicOrdering::Relaxed);
            }

            // 如果未达到最大深度，继续向上搜索
            if depth + 1 < config.max_depth {
                // 只将非静态指针添加到下一层（或者如果不是scan_static_only则全部添加）
                if !config.scan_static_only || classify_pointer(candidate.ptr_address, static_modules).is_none() {
                    next_layer.push(parent.child(candidate.ptr_address, candidate.offset));
                }
            }
        }

        // 剪枝：如果候选过多，只保留一部分
        if next_layer.len() > MAX_CANDIDATES_PER_LAYER {
            warn!("[候选裁剪] 在深度 {} 将候选从 {} 剪枝到 {}", depth, next_layer.len(), MAX_CANDIDATES_PER_LAYER);
            next_layer.truncate(MAX_CANDIDATES_PER_LAYER);
        }

        // 报告进度
        progress_callback(depth + 1, chains_found.load(AtomicOrdering::Relaxed) as i64);

        current_layer = next_layer;
    }

    // 最终进度报告
    progress_callback(config.max_depth, results.len() as i64);

    info!("指针链构建 (分层BFS) 完成。找到 {} 条链", results.len());

    // 按深度排序（短链优先），然后按模块名排序
    results.par_sort_by(|a, b| {
        let depth_cmp = a.depth().cmp(&b.depth());
        if depth_cmp != Ordering::Equal {
            return depth_cmp;
        }
        let a_name = a.steps.first().and_then(|s| s.module_name.as_ref());
        let b_name = b.steps.first().and_then(|s| s.module_name.as_ref());
        a_name.cmp(&b_name)
    });

    Ok(results)
}
