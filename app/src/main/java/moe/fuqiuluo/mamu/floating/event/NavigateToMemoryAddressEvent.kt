package moe.fuqiuluo.mamu.floating.event

/**
 * 导航到内存地址事件
 * 用于从搜索界面跳转到内存预览界面指定地址
 */
data class NavigateToMemoryAddressEvent(
    val address: Long
)
