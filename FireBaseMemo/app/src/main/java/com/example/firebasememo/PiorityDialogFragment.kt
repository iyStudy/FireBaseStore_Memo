package com.example.firebasememo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.firebasememo.databinding.DialogPiorityBinding

// 優先度関連のインターフェース。優先度の選択やメモの更新をハンドルします。
interface PiorityListener {
    fun onPiority(memo: Memo)   // 優先度が選択されたときの処理
    fun onUpdateMemo(memo: Memo)  // メモが更新されたときの処理
}

// 優先度の詳細を入力するためのダイアログフラグメント
class PiorityDialogFragment : DialogFragment() {

    // ViewBindingのプロパティ
    private var _binding: DialogPiorityBinding? = null  // 実際のバインディング変数
    private val binding get() = _binding!!  // nullでないことを保証するバインディング変数

    // 優先度のイベントをハンドルするリスナー
    private var ratingListener: PiorityListener? = null

    // 定数
    companion object {
        const val TAG = "PiorityDialog"  // ログに表示するタグ
    }

    // ライフサイクルメソッド
    // Viewが作成されるときの処理
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBindingを用いてViewを生成
        _binding = DialogPiorityBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    // Viewが破棄されるときの処理
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // メモリリークを避けるためにnullを設定
    }

    // Fragmentがアタッチされるときの処理
    override fun onAttach(context: Context) {
        super.onAttach(context)
        // 親のフラグメントがPiorityListenerを実装しているか確認
        if (parentFragment is PiorityListener) {
            ratingListener = parentFragment as PiorityListener
        }
    }

    // Fragmentが表示されるときの処理
    override fun onResume() {
        super.onResume()
        adjustDialogSize()  // ダイアログのサイズを調整
    }

    // UI関連のヘルパーメソッドとイベントハンドラー
    private fun setupClickListeners() {
        // 送信ボタンがクリックされたときの処理を設定
        binding.memoFormButton.setOnClickListener { onSubmitClicked() }
        // キャンセルボタンがクリックされたときの処理を設定
        binding.memoFormCancel.setOnClickListener { onCancelClicked() }
    }

    // ダイアログのサイズを調整するメソッド
    private fun adjustDialogSize() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    // 送信ボタンがクリックされたときの処理
    private fun onSubmitClicked() {
        // メモと優先度を取得して、新しいMemoオブジェクトを作成
        val memo = Memo(binding.memoFormText.text.toString(), binding.memoFromPiority.rating.toDouble())
        ratingListener?.onPiority(memo)  // リスナーを通じてメモの優先度を通知
        dismiss()  // ダイアログを閉じる
    }

    // キャンセルボタンがクリックされたときの処理
    private fun onCancelClicked() {
        dismiss()  // ダイアログを閉じる
    }
}
