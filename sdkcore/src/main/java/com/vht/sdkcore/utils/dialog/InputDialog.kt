package com.vht.sdkcore.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.InputFilter
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.view.isVisible
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.visible


class InputDialog constructor(context: Context) : Dialog(context) {

    lateinit var tvTitle: TextView
    lateinit var tvInputLabel: TextView
    lateinit var tvInputLength: TextView
    lateinit var edtInput: EditText
    lateinit var btnCancel: TextView
    lateinit var btnConfirm: TextView
    lateinit var dialogContainer: View
    private var maxLength: Int = 30

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_input_dialog)
        setCanceledOnTouchOutside(true)

        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)
        tvTitle = findViewById(R.id.tvTitle)
        tvInputLabel = findViewById(R.id.tvInputLabel)
        tvInputLength = findViewById(R.id.tvInputLength)
        edtInput = findViewById(R.id.edtInput)
        dialogContainer = findViewById(R.id.dialogContainer)

        tvInputLength.text = String.format(context.getString(R.string.inputLength), 0, maxLength)

        edtInput.imeOptions = EditorInfo.IME_ACTION_GO

        edtInput.onTextChange {
            val length = it?.trim()?.length ?: 0
            tvInputLength.text = String.format(context.getString(R.string.inputLength), length, maxLength)
            btnConfirm.isEnabled = length > 0
        }

        dialogContainer.setOnClickListener {
            hideKeyboard()
        }

        setOnNegativePressed {
            dismiss()
        }

        setOnPositivePressed { _, _ ->
            dismiss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window?.attributes)
        val orientation: Int = context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            lp.width = WindowManager.LayoutParams.MATCH_PARENT
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        } else {
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT
            lp.height = WindowManager.LayoutParams.MATCH_PARENT
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(15, 20, 15, 0)
            btnCancel.layoutParams = params
        }

        window?.apply {
            attributes = lp
            setGravity(Gravity.CENTER_VERTICAL)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    fun setOnPositivePressed(onPositivePressed: (InputDialog, String) -> Unit): InputDialog {

        edtInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                dismiss()
                hideKeyboard()
                onPositivePressed(this, edtInput.text.toString().normalizeString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        btnConfirm.setOnClickListener {
            dismiss()
            onPositivePressed(this, edtInput.text.toString().normalizeString())
        }
        return this
    }

    fun String.normalizeString(): String {
        val trim = this.trim()
        return trim.replace("\\s+".toRegex(), " ") // Remove redundant spaces
    }

    fun setOnNegativePressed(onNegativePressed: (InputDialog) -> Unit): InputDialog {
        btnCancel.setOnClickListener {
            dismiss()
            onNegativePressed(this)
        }
        return this
    }

    fun setTextNegativeButton(@StringRes textId: Int): InputDialog {
        btnCancel.text = context.resources.getString(textId)
        btnCancel.visibility = View.VISIBLE
        return this
    }

    fun setTextPositiveButton(@StringRes textId: Int): InputDialog {
        btnConfirm.text = context.resources.getString(textId)
        return this
    }

    fun showPositiveButton(): InputDialog {
        btnConfirm.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun setDialogTitle(res: Int): InputDialog {
        tvTitle.text = context.resources.getString(res)
        return this
    }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(edtInput.windowToken, 0)
    }

    fun setDialogTitleWithString(title: String? = null): InputDialog {
        if (title.isNullOrEmpty()) {
            tvTitle.gone()
        } else {
            tvTitle.visible()
        }
        tvTitle.text = title
        return this
    }

    fun setInputLabel(res: Int): InputDialog {
        tvInputLabel.text = context.resources.getString(res)
        return this
    }

    fun setInputLabelWithString(label: String? = null): InputDialog {
        tvInputLabel.isVisible = !label.isNullOrEmpty()
        tvInputLabel.text = label
        return this
    }

    fun setContent(content: String?): InputDialog {
        if (!content.isNullOrEmpty()) {
            edtInput.setText(content)
        }
        return this
    }

    fun setMaxInputLength(maxLength: Int): InputDialog {
        tvInputLength.isVisible = maxLength > 0
        if (maxLength > 0) {
            this.maxLength = maxLength
            tvInputLength.text =
                String.format(context.getString(R.string.inputLength), 0, maxLength)
            edtInput.filters = arrayOf(InputFilter.LengthFilter(maxLength))
        }
        return this
    }

    fun showInputLength(isVisible: Boolean): InputDialog {
        tvInputLength.isVisible = isVisible
        return this
    }

    override fun setTitle(res: Int) {
        tvTitle.text = context.resources.getString(res)
    }

    fun showDialog(): InputDialog {
        if (!isShowing)
            super.show()
        return this
    }

    companion object {
        var instance: InputDialog? = null
        fun getInstanceInputDialog(context: Context): InputDialog {
            if (instance != null && instance?.isShowing == true) {
                instance?.dismiss()
            } else {
                instance = InputDialog(context)
            }
            return instance!!
        }
    }
}
