package moe.fuqiuluo.mamu.floating.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import moe.fuqiuluo.mamu.databinding.ItemProcessListBinding
import moe.fuqiuluo.mamu.floating.model.DisplayProcessInfo
import moe.fuqiuluo.mamu.utils.ByteFormatUtils.formatBytes

class ProcessListAdapter(
    private val context: Context,
    private val processList: List<DisplayProcessInfo>
): BaseAdapter() {
    override fun getCount(): Int = processList.size
    override fun getItem(position: Int): DisplayProcessInfo = processList[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding = when {
            convertView != null -> convertView.tag as? ItemProcessListBinding
                ?: ItemProcessListBinding.bind(convertView)
            else -> ItemProcessListBinding.inflate(LayoutInflater.from(context), parent, false).also {
                it.root.tag = it
            }
        }

        processList[position].let { processInfo ->
            binding.apply {
                processRss.text = formatBytes(processInfo.rss * 4096, 0)
                processName.text = processInfo.validName
                processDetails.text = "[${processInfo.pid}]"
                processIcon.setImageDrawable(processInfo.icon)
            }
        }

        return binding.root
    }
}