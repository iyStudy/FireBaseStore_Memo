package com.example.firebasememo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasememo.databinding.ItemMemoBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ktx.toObject

open class MemoAdapter(
    private val snapshotList: List<DocumentSnapshot>,
) : RecyclerView.Adapter<MemoAdapter.MemoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        val binding = ItemMemoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MemoViewHolder(binding)
    }

    override fun getItemCount(): Int = snapshotList.size

    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        val snapshot = snapshotList[position]
        holder.bind(snapshot)
    }

    inner class MemoViewHolder(private val binding:ItemMemoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(snapshot: DocumentSnapshot) {
            val memo = snapshot.toObject<Memo>() ?: return
            with(binding) {
                tvMemo.text = memo.text
            }
        }
    }
}
