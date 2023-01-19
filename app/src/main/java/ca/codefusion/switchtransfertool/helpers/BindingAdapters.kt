package ca.codefusion.switchtransfertool.helpers

import android.net.Uri
import android.widget.ImageView
import androidx.databinding.BindingAdapter
import ca.codefusion.switchtransfertool.R
import com.bumptech.glide.Glide

@BindingAdapter("android:glideSrc")
fun setSrc(imageView: ImageView, uri: Uri) {
        Glide.with(imageView.context)
            .load(uri)
            .placeholder(R.drawable.ic_image)
            .into(imageView)
}