package com.example.firebasememo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.firebasememo.databinding.DialogPiorityBinding


class PiorityEditDialogFragment : DialogFragment() {

    // Properties
    private var _binding: DialogPiorityBinding? = null
    private val binding get() = _binding!!
    private var ratingListener: PiorityListener? = null

    // Constants
    companion object {
        const val TAG = "EditMemoDialog"
    }

    // Fragment lifecycle methods
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = DialogPiorityBinding.inflate(inflater, container, false)

        initializeUIElements()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setupRatingListener()
    }

    override fun onResume() {
        super.onResume()
        adjustDialogSize()
    }

    // UI setup and event handlers
    private fun initializeUIElements() {
        val selectedMemo = arguments?.getSerializable("selectedMemo") as? Memo
        binding.memoFormText.setText(selectedMemo?.text) // Set existing text
        binding.memoFromPiority.rating = selectedMemo?.piority!!.toFloat()      // 優先度追加

        binding.memoFormButton.setOnClickListener { onUpdateClicked() }
        binding.memoFormCancel.setOnClickListener { onCancelClicked() }
    }

    private fun adjustDialogSize() {
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun setupRatingListener() {
        if (parentFragment is PiorityListener) {
            ratingListener = parentFragment as PiorityListener
        } else {
            Log.e(TAG, "Parent fragment does not implement PiorityListener!")
        }
    }

    private fun onUpdateClicked() {
        val selectedMemo = arguments?.getSerializable("selectedMemo") as? Memo
            ?: run {
                Log.e(TAG, "Memo not available for update!")
                dismiss()
                return
            }

        selectedMemo.text = binding.memoFormText.text.toString()
        selectedMemo.piority =  binding.memoFromPiority.rating.toDouble()   // 優先度追加
        ratingListener?.onUpdateMemo(selectedMemo)
        dismiss()
    }

    private fun onCancelClicked() {
        dismiss()
    }
}
