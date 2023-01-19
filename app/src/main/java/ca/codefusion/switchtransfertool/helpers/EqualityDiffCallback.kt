package ca.codefusion.switchtransfertool.helpers

import androidx.recyclerview.widget.DiffUtil

class EqualityDiffCallback<T>(
    private val oldContent: List<T>,
    private val newContent: List<T>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldContent[oldItemPosition] == newContent[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldContent.size
    }

    override fun getNewListSize(): Int {
        return newContent.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldContent[oldItemPosition] == newContent[newItemPosition]
    }

}