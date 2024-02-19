package com.vht.sdkcore.utils.custom

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.LayoutEdittextPasswordCustomBinding
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.setMaxLength
import com.vht.sdkcore.utils.visible

class EditTextPasswordCustom: ConstraintLayout {

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context,attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(context,attrs)
    }
    private var isFocus = false

    private lateinit var binding: LayoutEdittextPasswordCustomBinding

    private var onChangeText: ((String) -> Unit)? = null
    private var focusListener: ((Boolean) -> Unit)? = null
    private var onActionDone: ((String) -> Unit)? = null
    private var onFocus: ((String) -> Unit)? = null


    private fun init(context: Context, attrs: AttributeSet? = null) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_edittext_password_custom,
            this,
            true
        )

        val typeArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomEdittext, 0, 0)
        val stringTitle = typeArray.getResourceId(R.styleable.CustomEdittext_title_edittext, -1)
        if(stringTitle != -1){
            binding.tvTitle.setText(stringTitle)
        }

        val hintEdittext = typeArray.getResourceId(R.styleable.CustomEdittext_text_hint_edittext,-1)
        if(hintEdittext != -1){
            binding.edtInformationPw.setHint(hintEdittext)
        }
        
        val isShowTitle = typeArray.getBoolean(R.styleable.CustomEdittext_is_show_title, true)
        setShowTitle(isShowTitle)

        val inputTypeEdittext = typeArray.getResourceId(
            R.styleable.CustomEdittext_type_edittext,
            InputType.TYPE_CLASS_TEXT
        )
        binding.edtInformationPw.inputType = inputTypeEdittext

        binding.imvClearText.setOnClickListener {
            binding.edtInformationPw.setText("")
            binding.imvClearText.gone()
        }

        binding.edtInformationPw.onTextChange {
            onChangeText?.invoke(it.toString().trim())
            if (it.toString().trim().isEmpty()) {
                binding.imvClearText.gone()
            } else {
                binding.imvClearText.visible()
            }
        }

        binding.imvShowPassword.setOnClickListener {
            togglePasswordVisibility()
        }

        binding.edtInformationPw.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            isFocus = b
            focusListener?.invoke(b)
            onFocus?.invoke(binding.edtInformationPw.text.toString().trim())
            if(isFocus){
                binding.edtInformationPw.setBackgroundResource(R.drawable.bg_edittext_information)
            }else{
                if (binding.tvError.isVisible){
                    binding.edtInformationPw.setBackgroundResource(R.drawable.bg_edittext_information_error)
                }
            }
        }

        binding.edtInformationPw.transformationMethod = PasswordTransformationMethod.getInstance()
        binding.imvShowPassword.setImageResource(R.drawable.ic_gone_pass_word)

        binding.edtInformationPw.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                // Xử lý sự kiện khi người dùng nhấn "Done" hoặc "Enter" trên bàn phím
                onActionDone?.invoke(binding.edtInformationPw.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }

    fun setTextError(error: String) {
        binding.tvError.text = error
        binding.tvError.visible()
        if(!isFocus){
            binding.edtInformationPw.setBackgroundResource(R.drawable.bg_edittext_information_error)
        }
    }

    fun handleEdittextOnTextChange(onChangeText: ((String) -> Unit)? = null) {
        this.onChangeText = onChangeText
    }

    fun handleEdittextOnActionDone(onActionDone: ((String) -> Unit)? = null) {
        this.onActionDone = onActionDone
    }

    fun handleEdittextOnFocus(onFocus: ((String) -> Unit)? = null) {
        this.onFocus = onFocus
    }

    private fun togglePasswordVisibility() {
        if (binding.edtInformationPw.transformationMethod == PasswordTransformationMethod.getInstance()) {
            binding.edtInformationPw.transformationMethod =
                HideReturnsTransformationMethod.getInstance()
            binding.imvShowPassword.setImageResource(R.drawable.ic_hide_password)
        } else {
            // Nếu đang hiển thị mật khẩu, ẩn
            binding.edtInformationPw.transformationMethod = PasswordTransformationMethod.getInstance()
            binding.imvShowPassword.setImageResource(R.drawable.ic_gone_pass_word)
        }
        binding.edtInformationPw.setSelection(binding.edtInformationPw.text?.length ?: 0)
    }
    fun getTextEditText(): String{
        return binding.edtInformationPw.text.toString().trim()
    }

    fun setTextEditText(text: String) {
        binding.edtInformationPw.setText(text)
    }


    fun  handleFocusChangeListener(focus: ((Boolean) -> Unit)? = null){
        this.focusListener = focus
    }

    fun getIsFocus():Boolean{
        return isFocus
    }


    fun goneError() {
        binding.tvError.text = ""
        binding.tvError.gone()
        binding.edtInformationPw.setBackgroundResource(R.drawable.bg_edittext_information)
    }
    fun setMaxLength(maxLength: Int){
        binding.edtInformationPw.setMaxLength(maxLength)
    }

    private fun setShowTitle(isShow: Boolean) {
        if (isShow) {
            binding.tvTitle.visible()
        } else {
            binding.tvTitle.gone()
        }
    }

    fun setDigitsEdittext(digits: String){
        val keyListener = DigitsKeyListener.getInstance(digits)
        binding.edtInformationPw.keyListener = keyListener
    }

    fun setNotSpaceAndMaxLengthEdittext(maxLength: Int){
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        val lengthFilter = InputFilter.LengthFilter(maxLength)
        binding.edtInformationPw.filters = arrayOf(inputFilter,lengthFilter)
    }


}