package moe.fuqiuluo.mamu.floating.data.local

import android.widget.EditText

/**
 * 输入框历史记录管理器
 * 用于保存和恢复各个对话框输入框的上次输入内容
 */
object InputHistoryManager {
    
    // 各输入框的历史记录
    private val historyMap = mutableMapOf<String, String>()
    
    // 预定义的输入框Key
    object Keys {
        const val SEARCH_VALUE = "search_value"
        const val MODULE_ADDRESS = "module_address"
        const val OFFSET_CALCULATOR_BASE = "offset_calculator_base"
        const val OFFSET_CALCULATOR_OFFSET = "offset_calculator_offset"
        const val BATCH_MODIFY_VALUE = "batch_modify_value"
        const val POINTER_SCAN_ADDRESS = "pointer_scan_address"
        const val POINTER_SCAN_DEPTH = "pointer_scan_depth"
        const val POINTER_SCAN_OFFSET = "pointer_scan_offset"
        const val FUZZY_SEARCH_VALUE = "fuzzy_search_value"
        const val EXPORT_FILENAME = "export_filename"
        const val IMPORT_FILENAME = "import_filename"
    }
    
    /**
     * 保存输入内容
     */
    fun save(key: String, value: String) {
        historyMap[key] = value
    }
    
    /**
     * 获取上次输入内容
     */
    fun get(key: String): String {
        return historyMap[key] ?: ""
    }
    
    /**
     * 清除指定key的历史
     */
    fun clear(key: String) {
        historyMap.remove(key)
    }
    
    /**
     * 清除所有历史
     */
    fun clearAll() {
        historyMap.clear()
    }
    
    /**
     * 恢复输入框内容并全选（如果有内容）
     * @param editText 输入框
     * @param key 历史记录key
     * @param defaultValue 默认值（如果没有历史记录）
     */
    fun restoreAndSelectAll(editText: EditText, key: String, defaultValue: String = "") {
        val savedValue = get(key)
        val valueToSet = savedValue.ifEmpty { defaultValue }
        
        if (valueToSet.isNotEmpty()) {
            editText.setText(valueToSet)
            // 全选文本，方便用户直接删除或替换
            editText.selectAll()
        }
    }
    
    /**
     * 恢复输入框内容，光标移到末尾
     * @param editText 输入框
     * @param key 历史记录key
     * @param defaultValue 默认值（如果没有历史记录）
     */
    fun restoreWithCursorAtEnd(editText: EditText, key: String, defaultValue: String = "") {
        val savedValue = get(key)
        val valueToSet = savedValue.ifEmpty { defaultValue }
        
        if (valueToSet.isNotEmpty()) {
            editText.setText(valueToSet)
            editText.setSelection(valueToSet.length)
        }
    }
    
    /**
     * 保存输入框当前内容
     * @param editText 输入框
     * @param key 历史记录key
     */
    fun saveFromEditText(editText: EditText, key: String) {
        save(key, editText.text.toString())
    }
}
