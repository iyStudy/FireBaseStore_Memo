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
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// メインのフラグメントクラスです。メモを表示、追加、更新する役割を持っています。
class MainFragment : Fragment(), PiorityListener {

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

    // ライフサイクルメソッド
    // Viewが作成されるときの処理
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        // オプションメニューを持つことを設定
        setHasOptionsMenu(true)
        // ViewBindingを用いてViewを生成
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    // Viewが作成された後の処理
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerMemos.layoutManager = LinearLayoutManager(context)
        // FirestoreとRecyclerViewの初期設定
        initFirestore()

        query = firestore.collection("memos")

        (query as CollectionReference).get().addOnSuccessListener { querySnapshot ->
            val documents = querySnapshot.documents
            adapter = MemoAdapter(documents)
            binding.recyclerMemos.adapter = adapter
        }.addOnFailureListener { exception ->
            // Handle the error here
        }

        // FAB（浮き出るアクションボタン）がクリックされたら優先度ダイアログを表示
        binding.fabAddMemo.setOnClickListener { showPriorityDialog() }
    }

    // Firestore関連のメソッド
    private fun initFirestore() {
        // Firestoreのインスタンスを取得
        firestore = Firebase.firestore
        // Firestoreのクエリを更新
        updateFirestoreQuery()
    }

    private fun updateFirestoreQuery() {
        // "memos"コレクションのクエリを取得
        query = firestore.collection("memos")
        registration = (query as CollectionReference).addSnapshotListener { querySnapshot, e ->
            if (e != null) {
                // エラー処理
                showErrorSnackbar(e.message ?: "データ取得エラー")
                return@addSnapshotListener
            }

            // 新しいデータセットでアダプターを更新
            val documents = querySnapshot?.documents
            if (documents != null) {
                adapter = MemoAdapter(documents)
                binding.recyclerMemos.adapter = adapter
            }
        }
    }

    // 新しいメモをFirestoreに追加するメソッド
    private fun addMemo(memo: Memo): Task<Void> = firestore.collection("memos").document().set(memo)

    // UI関連のヘルパーメソッド
    // エラー時にSnackbarでメッセージを表示するメソッド
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, "エラー: $message", Snackbar.LENGTH_LONG).show()
    }

    // 優先度ダイアログを表示するメソッド
    private fun showPriorityDialog() {
        val priorityDialog = PiorityDialogFragment()
        priorityDialog.show(childFragmentManager, PiorityDialogFragment.TAG)
    }

    // キーボードを隠すメソッド
    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            (requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    // リスナーメソッド（コールバック）
    // 優先度が選択されたときの処理
    override fun onPiority(memo: Memo) {
        addMemo(memo).addOnSuccessListener {
            hideKeyboard()
            binding.recyclerMemos.smoothScrollToPosition(0)
            Snackbar.make(binding.root, "メモを追加しました", Snackbar.LENGTH_SHORT).show()
        }.addOnFailureListener { _ ->
            hideKeyboard()
            Snackbar.make(binding.root, "メモの追加に失敗しました", Snackbar.LENGTH_SHORT).show()
        }
    }

    // メモが更新されたときの処理
    override fun onUpdateMemo(memo: Memo) {
        memo.documentId?.let {
            firestore.collection("memos").document(it).update("text", memo.text, "piority",memo.piority)
                .addOnSuccessListener {
                    Log.d(TAG, "Memo successfully updated!")
                }.addOnFailureListener { e ->
                    Log.w(TAG, "Error updating memo", e)
                }
        } ?: run {
            Log.e(TAG, "Document ID not available for update!")
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        registration?.remove() // Remove the listener
    }
}
