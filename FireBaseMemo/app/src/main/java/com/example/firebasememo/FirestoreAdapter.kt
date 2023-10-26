package com.example.firebasememo

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.*

/**
 * FirestoreAdapterはFirebase Firestoreのクエリ結果をRecyclerViewに表示するためのアダプタです。
 * VHはViewHolderの型を示しています。
 */
abstract class FirestoreAdapter<VH : RecyclerView.ViewHolder>(private var query: Query) :
    RecyclerView.Adapter<VH>(), EventListener<QuerySnapshot> {

    // Firestoreのクエリの変更を検知するためのリスナーを管理する変数
    private var registration: ListenerRegistration? = null

    // Firestoreから取得したドキュメントのデータを保持するリスト
    private val snapshots = ArrayList<DocumentSnapshot>()

    // クエリの結果の監視を開始する
    fun startListening() {
        if (registration == null) {
            registration = query.addSnapshotListener(this)
        }
    }

    // クエリの結果の監視を終了する
    fun stopListening() {
        registration?.remove()
        registration = null

        // 保持しているデータをクリアする
        snapshots.clear()
        notifyDataSetChanged()
    }

    // 監視するクエリを更新する
    fun setQuery(query: Query) {
        // 現在の監視を終了する
        stopListening()

        // 保持しているデータをクリアする
        snapshots.clear()
        notifyDataSetChanged()

        // 新しいクエリで監視を再開する
        this.query = query
        startListening()
    }

    // エラー発生時の処理
    open fun onError(e: FirebaseFirestoreException) {
        Log.w(TAG, "onError", e)
    }

    // データ変更時のコールバック（サブクラスでオーバーライド可能）
    open fun onDataChanged() {}

    // RecyclerViewのアイテム数を返す
    override fun getItemCount(): Int {
        return snapshots.size
    }

    // 指定された位置のドキュメントデータを返す
    protected fun getSnapshot(index: Int): DocumentSnapshot {
        return snapshots[index]
    }

    companion object {
        private const val TAG = "FirestoreAdapter"
    }

    // Firestoreからデータの変更通知を受け取った時の処理
    override fun onEvent(documentSnapshots: QuerySnapshot?, e: FirebaseFirestoreException?) {
        // エラーが発生した場合の処理
        if (e != null) {
            Log.w(TAG, "onEvent:error", e)
            onError(e)
            return
        }

        // データの変更を各タイプごとに処理する
        if (documentSnapshots != null) {
            for (change in documentSnapshots.documentChanges) {
                when (change.type) {
                    DocumentChange.Type.ADDED -> onDocumentAdded(change)
                    DocumentChange.Type.MODIFIED -> onDocumentModified(change)
                    DocumentChange.Type.REMOVED -> onDocumentRemoved(change)
                }
            }
        }
        // データ変更時のコールバックを呼び出す
        onDataChanged()
    }

    // 新規ドキュメントが追加された時の処理
    private fun onDocumentAdded(change: DocumentChange) {
        snapshots.add(change.newIndex, change.document)
        notifyItemInserted(change.newIndex)
    }

    // ドキュメントが変更された時の処理
    private fun onDocumentModified(change: DocumentChange) {
        if (change.oldIndex == change.newIndex) {
            // 同じ位置での変更の場合
            snapshots[change.oldIndex] = change.document
            notifyItemChanged(change.oldIndex)
        } else {
            // 位置が変わって変更された場合
            snapshots.removeAt(change.oldIndex)
            snapshots.add(change.newIndex, change.document)
            notifyItemMoved(change.oldIndex, change.newIndex)
        }
    }

    // ドキュメントが削除された時の処理
    private fun onDocumentRemoved(change: DocumentChange) {
        snapshots.removeAt(change.oldIndex)
        notifyItemRemoved(change.oldIndex)
    }
}
