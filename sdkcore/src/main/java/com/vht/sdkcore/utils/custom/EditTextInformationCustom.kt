package com.vht.sdkcore.utils.custom

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.text.InputFilter
import android.text.InputType
import android.text.method.DigitsKeyListener
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.vht.sdkcore.R
import com.vht.sdkcore.databinding.LayoutEdittextInformationCustomBinding
import com.vht.sdkcore.utils.convertSourceToPixel
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.onTextChange
import com.vht.sdkcore.utils.setMaxLength
import com.vht.sdkcore.utils.visible


class EditTextInformationCustom : ConstraintLayout {

    private lateinit var binding: LayoutEdittextInformationCustomBinding

    private var onChangeText: ((String) -> Unit)? = null
    private var onActionDone: ((String) -> Unit)? = null
    private var onFocus: ((String) -> Unit)? = null
    private var isFocus = false
    private var totalLengthUI = 0

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(context, attrs)
    }


    @SuppressLint("SetTextI18n")
    private fun init(context: Context, attrs: AttributeSet? = null) {
        binding = DataBindingUtil.inflate(
            LayoutInflater.from(context),
            R.layout.layout_edittext_information_custom,
            this,
            true
        )

        val typeArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomEdittext, 0, 0)
        val stringTitle = typeArray.getResourceId(R.styleable.CustomEdittext_title_edittext, -1)
        if (stringTitle != -1) {
            binding.tvTitle.setText(stringTitle)
        }

        val srcImageTitle = typeArray.getResourceId(R.styleable.CustomEdittext_src_image_title, -1)
        if (srcImageTitle != -1) {
            binding.imvTitle.setImageResource(srcImageTitle)
            binding.edtInformation.setPadding(context.convertSourceToPixel(R.dimen.dimen_40),0,context.convertSourceToPixel(R.dimen.dimen_40),0)
        }else{
            binding.imvTitle.gone()
            binding.edtInformation.setPadding(context.convertSourceToPixel(R.dimen.dimen_20),0,context.convertSourceToPixel(R.dimen.dimen_40),0)
        }

        val isShowTitle = typeArray.getBoolean(R.styleable.CustomEdittext_is_show_title, true)
        setShowTitle(isShowTitle)

        val hintEdittext =
            typeArray.getResourceId(R.styleable.CustomEdittext_text_hint_edittext, -1)
        if (hintEdittext != -1) {
            binding.edtInformation.setHint(hintEdittext)
        }
        binding.edtInformation.onFocusChangeListener = View.OnFocusChangeListener { view, b ->
            isFocus = b
            onFocus?.invoke(binding.edtInformation.text.toString().trim())
            if(isFocus){
                binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information)
            }else{
                if (binding.tvError.isVisible){
                    binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information_error)
                }
            }
        }

        val inputTypeEdittext = typeArray.getResourceId(
            R.styleable.CustomEdittext_type_edittext,
            InputType.TYPE_CLASS_TEXT
        )
        binding.edtInformation.inputType = inputTypeEdittext

        binding.imvClearText.setOnClickListener {
            binding.edtInformation.setText("")
            binding.imvClearText.gone()
        }

        binding.edtInformation.onTextChange {
            onChangeText?.invoke(it.toString())
            if (it.toString().trim().isEmpty()) {
                binding.imvClearText.gone()
            } else {
                binding.imvClearText.visible()
            }

            binding.tvTotalLength.text = "${it?.toString()?.trim()?.length?:0}/$totalLengthUI"
        }

        binding.edtInformation.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                (event != null && event.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)
            ) {
                // Xử lý sự kiện khi người dùng nhấn "Done" hoặc "Enter" trên bàn phím
                onActionDone?.invoke(binding.edtInformation.text.toString().trim())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

    }


    fun setTextError(error: String) {
        binding.tvError.text = error
        binding.tvError.visible()
        if(!isFocus){
            binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information_error)
        }
    }

    fun goneError() {
        binding.tvError.text = ""
        binding.tvError.gone()
        binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information)
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
    fun getTextEditText(): String {
        return binding.edtInformation.text.toString().trim()
    }
    fun setTextEditText(text: String) {
         binding.edtInformation.setText(text)
    }
    fun getIsFocus():Boolean{
      return isFocus
    }


    fun setInputTypeEdittext(type: Int) {
        binding.edtInformation.inputType = type
    }

    fun setMaxLength(maxLength: Int){
        binding.edtInformation.setMaxLength(maxLength)
    }

    fun setAutoFillOtp(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.edtInformation.setAutofillHints("smsOTPCode")
            binding.edtInformation.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES

        }
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
        binding.edtInformation.keyListener = keyListener
    }

    fun setNotSpaceEdittext(){
        val inputFilter = InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                if (Character.isWhitespace(source[i])) {
                    return@InputFilter ""
                }
            }
            null
        }
        binding.edtInformation.filters = arrayOf(inputFilter)
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
        binding.edtInformation.filters = arrayOf(inputFilter,lengthFilter)
    }

    fun goneImageViewTitle(){
        binding.imvTitle.gone()
    }

    fun setShowTotalLength(total: Int){
        totalLengthUI = total
        binding.tvTotalLength.visible()
    }

    fun setImeOption(action: Int = EditorInfo.IME_ACTION_DONE){
        binding.edtInformation.imeOptions = action
        binding.edtInformation.isSingleLine = true
    }

    fun setBackgroundErrorGoneText() {
        binding.tvError.gone()
        if(!isFocus){
            binding.edtInformation.setBackgroundResource(R.drawable.bg_edittext_information_error)
        }
    }



}