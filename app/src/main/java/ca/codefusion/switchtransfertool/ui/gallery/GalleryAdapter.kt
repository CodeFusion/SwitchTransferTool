package ca.codefusion.switchtransfertool.ui.gallery

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ca.codefusion.switchtransfertool.data.models.MediaFile

class GalleryAdapter(private val list: List<MediaFile>)
    : RecyclerView.Adapter<MediaViewHolder>() {

    private var recyclerViewItemListener: ((Int) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MediaViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaFile = list[position]
        holder.itemView.setOnClickListener {
            recyclerViewItemListener?.invoke(position)
        }
        holder.bind(mediaFile)
    }

    fun setItemClickListener(listener: (position: Int) -> Unit) {
        recyclerViewItemListener = listener
    }

    override fun getItemCount(): Int {
        return list.size
    }
}