package ru.pl.photogallery

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.bumptech.glide.Glide
import kotlinx.coroutines.withTimeoutOrNull
import ru.pl.photogallery.api.GalleryItem
import ru.pl.photogallery.databinding.ListItemGalleryBinding

class PhotoListAdapter(
    private val galleryItems: List<GalleryItem>,
    private val onItemClicked: (Uri) -> Unit
) :
    RecyclerView.Adapter<PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ListItemGalleryBinding.inflate(inflater, parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val item = galleryItems[position]
        holder.bind(item, onItemClicked)
    }

    override fun getItemCount(): Int {
        return galleryItems.size
    }
}


class PhotoViewHolder(private val binding: ListItemGalleryBinding) :
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