package com.example.firebasememo

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firebasememo.databinding.ItemMemoBinding
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject

/**
 * `MemoAdapter`は、RecyclerViewでメモのリストを表示するためのアダプタクラスです。
 * Firestoreからのクエリ結果に基づいて、メモの一覧を表示します。
 */
open class MemoAdapter(
    query: Query,
    private val listener: OnMemoSelectedListener
) : FirestoreAdapter<MemoAdapter.MemoViewHolder>(query) {

    // メモが選択されたときに呼び出されるリスナー
    interface OnMemoSelectedListener {
        fun onMemoSelected(memo: DocumentSnapshot)
    }

    /**
     * 新しいViewHolderを作成します。これはRecyclerViewが新しいアイテムを表示するときに呼び出されます。
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoViewHolder {
        // `ItemMemoBinding`を使って、メモのアイテムのレイアウトを読み込みます。
        val binding = ItemMemoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemoViewHolder(binding)
    }

    /**
     * 指定された位置のデータをViewHolderに結び付けます。
     */
    override fun onBindViewHolder(holder: MemoViewHolder, position: Int) {
        holder.bind(getSnapshot(position), listener)
    }

    /**
     * `MemoViewHolder`は、メモの各アイテムを表示するためのViewHolderクラスです。
     */
    inner class MemoViewHolder(private val binding: ItemMemoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        /**
         * メモのデータをViewHolderに結び付けます。
         */
        fun bind(snapshot: DocumentSnapshot, listener: OnMemoSelectedListener?) {
            // スナップショットから`Memo`オブジェクトに変換します。
            val memo = snapshot.toObject<Memo>() ?: return

            // ビューバインディングを使って、メモのデータを表示します。
            with(binding) {
                tvMemo.text = memo.text
                root.setOnClickListener { listener?.onMemoSelected(snapshot) }
                tvPiority.text = memo.piority.toString()
//                /* 削除処理
                btDelete.setOnClickListener {
                    snapshot.reference.delete().addOnFailureListener { e ->
                        // エラーのログを出力
                        Log.e("MemoAdapter", "Document deletion failed.", e)

                        // Toastを使用してユーザーにエラーメッセージを表示
                        Toast.makeText(
                            binding.root.context,
                            "メモの削除に失敗しました。しばらくしてから再度お試しください。",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
//                 */
            }
        }
    }
}
