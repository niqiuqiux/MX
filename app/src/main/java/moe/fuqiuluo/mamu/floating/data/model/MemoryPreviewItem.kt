package moe.fuqiuluo.mamu.floating.data.model

/**
 * 内存预览列表项
 */
sealed class MemoryPreviewItem {
    /**
     * 内存数据行
     * @param address 内存地址
     * @param formattedValues 格式化后的值列表
     * @param memoryRange 内存区域类型（包含代码和颜色）
     * @param isHighlighted 是否高亮显示（用于标记跳转目标地址）
     * @param displayText 预构建的显示文本
     */
    data class MemoryRow(
        val address: Long,
        val formattedValues: List<FormattedValue>,
        val memoryRange: MemoryRange? = null,
        val isHighlighted: Boolean = false,
        val displayText: CharSequence? = null
    ) : MemoryPreviewItem()

    /**
     * 分页导航项
     * @param targetAddress 目标地址
     * @param isNext true表示下一页，false表示上一页
     */
    data class PageNavigation(
        val targetAddress: Long,
        val isNext: Boolean
    ) : MemoryPreviewItem()
}

/**
 * 格式化后的值
 * @param format 格式类型
 * @param value 显示的值
 * @param color 显示颜色（如果为null则使用format的默认颜色）
 */
data class FormattedValue(
    val format: MemoryDisplayFormat,
    val value: String,
    val color: Int? = null
)
