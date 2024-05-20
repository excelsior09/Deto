package id.ac.ukdw.deto.model

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import id.ac.ukdw.deto.databinding.ItemImageBinding

data class ImageItem(val imageUrl: String)

class MyRecyclerViewAdapter(private val items: MutableList<ImageItem>) : RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ItemImageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(imageItem: ImageItem) {
            Glide.with(binding.root.context).load(imageItem.imageUrl).into(binding.imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun addItem(item: ImageItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun setItems(newItems: List<ImageItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
