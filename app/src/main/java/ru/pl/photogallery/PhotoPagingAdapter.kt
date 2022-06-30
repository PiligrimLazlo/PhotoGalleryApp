package ru.pl.photogallery

import android.net.Uri
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import ru.pl.photogallery.api.GalleryItem
import ru.pl.photogallery.databinding.ListItemGalleryBinding

class PhotoPagingAdapter(private val onItemClicked: (Uri) -> Unit) :
    PagingDataAdapter<GalleryItem, PhotoPagingViewHolder>(GalleryItemComparator) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoPagingViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGalleryBinding.inflate(inflater, parent, false)
        return PhotoPagingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoPagingViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null)
            holder.bind(item, onItemClicked)
    }
}

object GalleryItemComparator : DiffUtil.ItemCallback<GalleryItem>() {
    override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
        return oldItem == newItem
    }
}


class PhotoPagingViewHolder(private val binding: ListItemGalleryBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(galleryItem: GalleryItem, onItemClicked: (Uri) -> Unit) {
        /*binding.itemImageView.load(galleryItem.url) {
            placeholder(R.drawable.placeholder_120_120)
        }*/

        Glide.with(binding.root.context)
            .load(galleryItem.url)
            .placeholder(R.drawable.placeholder_120_120)
            .into(binding.itemImageView)

        binding.root.setOnClickListener {
            onItemClicked(galleryItem.photoPageUri)
        }
    }

}