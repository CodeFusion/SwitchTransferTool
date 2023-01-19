package ca.codefusion.switchtransfertool.ui.gallery

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ca.codefusion.switchtransfertool.R
import ca.codefusion.switchtransfertool.databinding.GalleryImageCellBinding
import ca.codefusion.switchtransfertool.helpers.EqualityDiffCallback
import ca.codefusion.switchtransfertool.ui.main.MainViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber
import java.lang.IllegalArgumentException

private typealias ImageItemViewModel = MainViewModel.GalleryItemViewModel.ImageItemViewModel

class ImageGalleryAdapter(): RecyclerView.Adapter<ImageGalleryAdapter.GalleryViewHolder>() {

    var viewModels = listOf<MainViewModel.GalleryItemViewModel>()
        set(newValue) {
            val oldValue = field
            field = newValue
            DiffUtil.calculateDiff(EqualityDiffCallback(oldValue, newValue))
                .dispatchUpdatesTo(this)
            notifyDataSetChanged()
        }

    abstract class GalleryViewHolder(binding: ViewDataBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            viewType,
            parent,
            false
        )

        return when (viewType) {
            R.layout.gallery_image_cell -> ImageViewHolder(binding as GalleryImageCellBinding)
            else -> {
                val error = IllegalArgumentException("Unrecognized view type in gallery adapter")
                FirebaseCrashlytics.getInstance().recordException(error)
                throw error
            }
        }
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        when (holder) {
            is ImageViewHolder -> holder.bind(viewModels[position] as ImageItemViewModel)
            else -> Timber.e("Error in Gallery onBindViewHolder")
        }
    }

    override fun getItemCount() = viewModels.size

    class ImageViewHolder(private val binding: GalleryImageCellBinding): GalleryViewHolder(binding) {
        fun bind(vm: ImageItemViewModel) {
            with(binding) {
                viewModel = vm
                executePendingBindings()
            }
        }
    }
}