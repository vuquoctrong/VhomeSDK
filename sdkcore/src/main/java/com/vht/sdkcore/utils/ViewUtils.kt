package com.vht.sdkcore.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.*
import android.text.Editable
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.annotation.ColorRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.vht.sdkcore.R
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

object ViewUtils {
    //check double click
    @kotlin.jvm.JvmStatic
    fun runLayoutAnimation(recyclerView: RecyclerView, @AnimRes resId: Int) {
        val context = recyclerView.context
        val controller =
            AnimationUtils.loadLayoutAnimation(context, resId)
        recyclerView.layoutAnimation = controller
        recyclerView.scheduleLayoutAnimation()
    }
}

fun String.toast(context: Context) {
    Toast.makeText(context, this, Toast.LENGTH_SHORT).show()
}

fun TextView.disableCopyPaste() {
    isLongClickable = false
    setTextIsSelectable(false)
    customSelectionActionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {}
    }
}

fun ImageView.enableView(isEnable: Boolean) {
    isEnabled = if (isEnable) {
        setColorFilter(context.getColorCompat(R.color.color_button_common_blue))
        true
    } else {
        setColorFilter(context.getColorCompat(R.color.background_color_gray))
        false
    }
}

fun ImageView.tint(@ColorRes colorId: Int) {
    setColorFilter(context.getColorCompat(colorId))
}

fun EditText.onTextChange(content: (Editable?) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            //do nothing
        }

        override fun afterTextChanged(s: Editable?) {
            content(s)
        }
    })
}

fun EditText.onCountTextLength(content: (Int) -> Unit) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            //do nothing
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            content.invoke(count)
        }

        override fun afterTextChanged(s: Editable?) {
        }
    })
}

fun EditText.showKeyBoard() {
    if (this.requestFocus()) {
        val inputMethodManager: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun EditText.hideKeyBoard() {
    if (this.requestFocus()) {
        val inputMethodManager: InputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun EditText.autoUpperCase() {
    this.inputType =
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    this.filters = arrayOf(InputFilter.AllCaps())
}

fun EditText.setMaxLength(maxLength: Int) {
    val fArray = arrayOfNulls<InputFilter>(1)
    fArray[0] = LengthFilter(maxLength)
    this.filters = fArray
}

fun EditText.setMaxLengthWithUpperCase(maxLength: Int) {
    this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    val fArray = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength), InputFilter.AllCaps())
    this.filters = fArray
}

fun EditText.setMaxLengthWithShowOrHiddenPassword(maxLength: Int, isHidden: Boolean) {
    if (isHidden) {
        this.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        val fArray =
            arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        this.filters = fArray
    } else {
        this.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        val fArray =
            arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
        this.filters = fArray
    }
}

fun TextView.autoUpperCase() {
    this.inputType =
        InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS
    this.filters = arrayOf(InputFilter.AllCaps())
}

fun ViewPager.onPageSelected(params: (Int) -> Unit) {
    addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            //do nothing
        }

        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
            //do nothing
        }

        override fun onPageSelected(position: Int) {
            params(position)
        }

    })
}

fun View.setOnClickAction(listener: View.OnClickListener) {

}

/*fun TextView.setTextAsync(data: String) {
    TextViewCompat.setPrecomputedText(
        this,
        PrecomputedTextCompat.create(data, TextViewCompat.getTextMetricsParams(this))
    )
}*/

fun Activity.toastMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.toastMessage(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

fun Fragment.vibrate() {
    val vibrator = context?.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (Build.VERSION.SDK_INT >= 26) {
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vibrator.vibrate(200)
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

@SuppressLint("SimpleDateFormat")
fun File.convertLastModifyTimeToString(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    return formatter.format(this.lastModified())
}

@SuppressLint("SimpleDateFormat")
fun Long.convertLongToStringWithFormatter(): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
    return formatter.format(this)
}

@SuppressLint("SimpleDateFormat")
fun Long.convertLongToStringWithActionFormat(): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ")
    dateFormat.timeZone = TimeZone.getTimeZone("GMT+0700")
    val date = Date(this)
    return dateFormat.format(date)
}


inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Serializable> Bundle.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializable(key: String): T? = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(
        key,
        T::class.java
    )

    else -> @Suppress("DEPRECATION") getSerializableExtra(key) as? T
}

fun TextView.setTextDifferentSize(stringSource: Int, dimenSource: Int, start: Int, end: Int) {
    val textSize = resources.getDimensionPixelSize(dimenSource)
    val ss = SpannableString(this.context.getString(stringSource))
    ss.setSpan(AbsoluteSizeSpan(textSize), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
    text = ss
}

fun View.setMargins(start: Int, top: Int, end: Int, bottom: Int) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        (this.layoutParams as ViewGroup.MarginLayoutParams).setMargins(start, top, end, bottom)
        this.requestLayout()
    }
}

fun Context.convertSourceToPixel(dimenSource: Int): Int {
    return this.resources.getDimension(dimenSource).toInt()
}
fun Context.getVersionApp(): String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        this.packageManager.getPackageInfo(
            this.packageName,
            PackageManager.PackageInfoFlags.of(0L)
        ).versionName
    } else {
        this.packageManager.getPackageInfo(
            this.packageName,
            0
        ).versionName
    }
}
