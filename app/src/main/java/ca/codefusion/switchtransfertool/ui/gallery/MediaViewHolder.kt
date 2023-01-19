package ca.codefusion.switchtransfertool.ui.gallery

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.data.models.MediaFile
import com.bumptech.glide.Glide


class MediaViewHolder(inflater: LayoutInflater, parent: ViewGroup)
    : RecyclerView.ViewHolder(inflater.inflate(
        R.layout.view_holder_media, parent, false
    )){

    fun bind(mediaFile: MediaFile) {
        if (mediaFile.type == MediaFile.MediaType.VIDEO ) {
            Glide.with(itemView)
                .load(mediaFile.contentUri)
                .placeholder(R.drawable.ic_video)
                .centerCrop()
                .into(itemView.findViewById(R.id.ivMedia))
            val durationView = itemView.findViewById<TextView>(R.id.ivDuration)
            durationView.text = itemView.context.getString(R.string.seconds_abbr, mediaFile.duration)
            durationView.visibility = View.VISIBLE
        } else {
            Glide.with(itemView)
                .load(mediaFile.contentUri)
                .placeholder(R.drawable.ic_image)
                .centerCrop()
                .into(itemView.findViewById(R.id.ivMedia))
            val durationView = itemView.findViewById<TextView>(R.id.ivDuration)
            durationView.visibility = View.GONE
        }
    }
}