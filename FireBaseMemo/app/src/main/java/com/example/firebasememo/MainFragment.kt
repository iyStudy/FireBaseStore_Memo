package com.example.firebasememo

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firebasememo.databinding.FragmentMainBinding

import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// メインのフラグメントクラスです。メモを表示、追加、更新する役割を持っています。
class MainFragment : Fragment(), MemoListener {

    // プロパティの宣言部分
    // Firestoreのインスタンス
    private lateinit var firestore: FirebaseFirestore
    // Firestoreのクエリ
    private var query: Query? = null
    // ViewBindingのインスタンス
    private lateinit var binding: FragmentMainBinding
    // メモのアダプター
    private var adapter: MemoAdapter? = null

    private var registration: ListenerRegistration? = null


    // ログに表示するタグ
    companion object {
        private const val TAG = "MainFragment"
    }

    // Viewが作成されるときの処理
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // ViewBindingを用いてViewを生成
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Viewが作成された後の処理
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // RecyclerViewのLayoutManagerを設定
        binding.recyclerMemos.layoutManager = LinearLayoutManager(context)

        // FirestoreとRecyclerViewの初期設定を行うメソッドを呼び出し
        initFirestore()

        // "memos"コレクションのFirestoreクエリを作成
        query = firestore.collection("memos")

        // Firestoreからメモを取得し、成功した場合はアダプターを設定
        (query as CollectionReference).get().addOnSuccessListener { querySnapshot ->
            val documents = querySnapshot.documents
            adapter = createAdapter(documents)
            binding.recyclerMemos.adapter = adapter
        }.addOnFailureListener { exception ->
            // Firestoreからのデータ取得に失敗した場合のエラーハンドリング
        }

        // FAB（浮き出るアクションボタン）がクリックされたときに優先度ダイアログを表示するリスナーを設定
        binding.fabAddMemo.setOnClickListener { showMemoDialog() }
    }

    // Firestoreの初期設定を行うメソッド
    private fun initFirestore() {
        // Firestoreのインスタンスを取得
        firestore = Firebase.firestore
        // Firestoreのクエリを更新する
        updateFirestoreQuery()
    }

    // Firestoreのクエリを更新するメソッド
    private fun updateFirestoreQuery() {
        // "memos"コレクションのクエリを取得し、リアルタイム更新
        query = firestore.collection("memos")
        registration = (query as CollectionReference).addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                // Firestoreのデータ取得でエラーが発生した場合のハンドリング
                showErrorSnackbar(e.message ?: "データ取得エラー")
                return@addSnapshotListener
            }

            // データが更新された場合、アダプターを新しいデータセットで更新
            val documents = querySnapshot?.documents
            if (documents != null) {
                adapter = createAdapter(documents)
                binding.recyclerMemos.adapter = adapter
            }
        }
    }

    // 新しいメモをFirestoreに追加するメソッド
    private fun addMemo(memo: Memo): Task<Void> = firestore.collection("memos").document().set(memo)

    // エラー時にSnackbarでメッセージを表示するメソッド
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, "エラー: $message", Snackbar.LENGTH_LONG).show()
    }

    // メモダイアログを表示するメソッド
    private fun showMemoDialog() {
        val memoDialog = MemoDialogFragment()
        memoDialog.show(childFragmentManager, MemoDialogFragment.TAG)
    }

    // キーボードを隠すメソッド
    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // 優先度が選択されたときのコールバックメソッド
    override fun onCreateMemo(memo: Memo) {
        addMemo(memo).addOnSuccessListener {
            // メモの追加に成功したときの処理
            hideKeyboard()
            binding.recyclerMemos.smoothScrollToPosition(0)
            Snackbar.make(binding.root, "メモを追加しました", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener { _ ->
            // メモの追加に失敗したときの処理
            hideKeyboard()
            Snackbar.make(binding.root, "メモの追加に失敗しました", Snackbar.LENGTH_SHORT).show()
        }
    }

    // メモが更新されたときのコールバックメソッド
    override fun onUpdateMemo(memo: Memo) {
        memo.documentId?.let {
            firestore.collection("memos")
                .document(it).update("text", memo.text, )
                .addOnSuccessListener {
                    // メモの更新に成功したときのログ出力
                    Log.d(TAG, "Memo successfully updated!")
                }.addOnFailureListener { e ->
                    // メモの更新に失敗したときのログ出力
                    Log.w(TAG, "Error updating memo", e)
                }
        } ?: run {
            // ドキュメントIDが無い場合のエラーログ
            Log.e(TAG, "Document ID not available for update!")
        }
    }

    // Viewが破棄されるときのリスナーを削除する処理
    override fun onDestroyView() {
        super.onDestroyView()
        registration?.remove() // リスナーの削除
    }

    // Firestoreのドキュメントからアダプターを作成するメソッド
    private  fun createAdapter(documents: List<DocumentSnapshot>): MemoAdapter {
        return MemoAdapter(documents){
                snapshot ->
            // メモの項目がクリックされた時の処理
            val memoData = snapshot.toObject(Memo::class.java)?.copy(documentId = snapshot.id) ?: return@MemoAdapter
            // 選択されたメモのデータをバンドルに設定
            val bundle = Bundle().apply {
                putSerializable("selectedMemo", memoData)
            }

            // 優先度編集ダイアログを表示する
            val priorityEditDialog = MemoEditDialogFragment().apply {
                arguments = bundle
            }
            priorityEditDialog.show(childFragmentManager, MemoDialogFragment.TAG)
        }
    }
}
