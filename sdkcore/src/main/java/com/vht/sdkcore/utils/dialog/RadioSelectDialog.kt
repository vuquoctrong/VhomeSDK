package com.vht.sdkcore.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.visible
import com.vht.sdkcore.utils.widget.RadioAdapter

abstract class ItemRadio {

    open var isSelected: Boolean = false

    abstract fun getItemId(): String

    abstract fun getText(): String

}

@Suppress("UNCHECKED_CAST")
class RadioSelectDialog<T : ItemRadio> constructor(context: Context) : Dialog(context) {

    lateinit var tvTitle: TextView
    lateinit var btnCancel: TextView
    lateinit var btnConfirm: TextView
    lateinit var rvRadios: RecyclerView
    private val adapter: RadioAdapter<T> by lazy { RadioAdapter(::onRadioClick) }
    private var onNegativePressed: ((RadioSelectDialog<T>) -> Unit)? = null
    private var onPositivePressed: ((RadioSelectDialog<T>, T?) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_radio_dialog)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window?.attributes)
        btnConfirm = findViewById(R.id.btnConfirm)
        btnCancel = findViewById(R.id.btnCancel)
        tvTitle = findViewById(R.id.tvTitle)
        rvRadios = findViewById(R.id.rvRadios)
        rvRadios.adapter = adapter
        setCanceledOnTouchOutside(false)
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

        btnCancel.setOnClickListener {
            dismiss()
            onNegativePressed?.invoke(this)
        }

        btnConfirm.setOnClickListener {
            dismiss()
            onPositivePressed?.invoke(this, adapter.getSelectedItem())
        }
    }

    private fun onRadioClick(data: T?) {
        btnConfirm.isEnabled = data != null
    }

    fun setOnPositivePressed(onPositivePressed: (RadioSelectDialog<T>, T?) -> Unit): RadioSelectDialog<T> {
        this.onPositivePressed = onPositivePressed
        return this
    }

    fun setOnNegativePressed(onNegativePressed: (RadioSelectDialog<T>) -> Unit): RadioSelectDialog<T> {
        this.onNegativePressed = onNegativePressed
        return this
    }

    fun setTextNegativeButton(@StringRes textId: Int): RadioSelectDialog<T> {
        btnCancel.text = context.resources.getString(textId)
        btnCancel.visibility = View.VISIBLE
        return this
    }

    fun setTextPositiveButton(@StringRes textId: Int): RadioSelectDialog<T> {
        btnConfirm.text = context.resources.getString(textId)
        return this
    }

    fun showPositiveButton(): RadioSelectDialog<T> {
        btnConfirm.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun setDialogTitle(res: Int): RadioSelectDialog<T> {
        tvTitle.text = context.resources.getString(res)
        return this
    }

    fun setDialogTitleWithString(title: String? = null): RadioSelectDialog<T> {
        if (title.isNullOrEmpty()) {
            tvTitle.gone()
        } else {
            tvTitle.visible()
        }
        tvTitle.text = title
        return this
    }

    fun setIconRadioSelected(@DrawableRes res: Int?): RadioSelectDialog<T> {
        res?.let { adapter.changeIconRadioSelected(res) }
        return this
    }

    fun setIconRadioUnselected(@DrawableRes res: Int?): RadioSelectDialog<T> {
        res?.let { adapter.changeIconRadioUnselected(res) }
        return this
    }

    fun setData(data: MutableList<T>? = null): RadioSelectDialog<T> {
        if (!data.isNullOrEmpty()) {
            adapter.assignAll(data)
        }
        return this
    }

    fun setSelectedItem(selectedItem: T? = null): RadioSelectDialog<T> {
        adapter.setSelectedItem(selectedItem)
        return this
    }

    override fun setTitle(res: Int) {
        tvTitle.text = context.resources.getString(res)
    }

    fun showDialog(): RadioSelectDialog<T> {
        if (!isShowing)
            super.show()
        return this
    }

    override fun dismiss() {
        super.dismiss()
        adapter.clearData()
    }

    companion object {
        var instance: Dialog? = null

        fun <T : ItemRadio> getInstanceInputDialog(context: Context): RadioSelectDialog<T> {
            if (instance != null && instance?.isShowing == true) {
                instance?.dismiss()
            } else {
                instance = RadioSelectDialog<T>(context)
            }
            return (instance as RadioSelectDialog<T>)
        }
    }
}
