package id.ac.ukdw.deto.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.ukdw.deto.R

data class ImageItem(val imageUrl: String)

class MyRecyclerViewAdapter(private var imageList: MutableList<ImageItem>) :
    RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.imageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageItem = imageList[position]
        Glide.with(holder.itemView.context)
            .load(imageItem.imageUrl)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun updateImages(newImages: List<ImageItem>) {
        imageList.clear()
        imageList.addAll(newImages)
        notifyDataSetChanged()
    }

    fun addImage(imageItem: ImageItem) {
        imageList.add(imageItem)
        notifyItemInserted(imageList.size - 1)
    }
}
