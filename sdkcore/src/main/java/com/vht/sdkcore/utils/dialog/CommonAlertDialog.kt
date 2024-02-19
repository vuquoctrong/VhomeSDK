package com.vht.sdkcore.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.isVisible
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.visible


class CommonAlertDialog constructor(context: Context) : Dialog(context) {

    lateinit var btnConfirm: TextView
    lateinit var tvTitle: TextView
    lateinit var tvSubTitle: TextView
    lateinit var btnCancel: TextView
    lateinit var btnAgree: TextView
    lateinit var llOption: LinearLayout
    lateinit var ivCancel: AppCompatImageView
    lateinit var ivDescription: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.layout_alert_error)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window?.attributes)
        btnConfirm = findViewById(R.id.btn_confirm)
        btnAgree = findViewById(R.id.btn_agree)
        btnCancel = findViewById(R.id.btn_cancel)
        tvTitle = findViewById(R.id.tv_title)
        tvSubTitle = findViewById(R.id.tv_sub_title)
        llOption = findViewById(R.id.llOption)
        ivCancel = findViewById(R.id.ivCancel)
        ivDescription = findViewById(R.id.ivDescription)
        setCanceledOnTouchOutside(false)
        setOnPositivePressed {
            this.dismiss()
        }

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
            btnAgree.layoutParams = params
        }
        window?.apply {
            attributes = lp
            setGravity(Gravity.CENTER_VERTICAL)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    fun setOnPositivePressed(onPositivePressed: (CommonAlertDialog) -> Unit): CommonAlertDialog {
        btnConfirm.setOnClickListener {
            onPositivePressed(this)
        }
        btnAgree.setOnClickListener {
            onPositivePressed(this)
        }
        return this
    }

    fun setOnNegativePressed(onNegativePressed: (CommonAlertDialog) -> Unit): CommonAlertDialog {
        btnCancel.setOnClickListener {
            onNegativePressed(this)
        }
        ivCancel.setOnClickListener {
            onNegativePressed(this)
        }
        return this
    }

    fun setTextNegativeButton(@StringRes textId: Int): CommonAlertDialog {
        btnCancel.text = context.resources.getString(textId)
        btnCancel.visibility = View.VISIBLE
        return this
    }

    fun setTextPositiveButton(@StringRes textId: Int): CommonAlertDialog {
        btnConfirm.text = context.resources.getString(textId)
        btnAgree.text = context.resources.getString(textId)
        return this
    }

    fun showPositiveButton(): CommonAlertDialog {
        btnConfirm.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun showNegativeAndPositiveButton(): CommonAlertDialog {
        btnConfirm.visibility = View.GONE
        llOption.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun hideOptionButton(): CommonAlertDialog {
        btnConfirm.visibility = View.GONE
        llOption.visibility = View.GONE
        setCanceledOnTouchOutside(true)
        return this
    }

    fun showCancelButton(): CommonAlertDialog {
        ivCancel.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun showImageDescription(res: Int, isCancelTouchOutSide: Boolean = false): CommonAlertDialog {
        ivDescription.visibility = View.VISIBLE
        ivDescription.setImageResource(res)
        setCanceledOnTouchOutside(isCancelTouchOutSide)
        return this
    }

    fun setDialogTitle(res: Int): CommonAlertDialog {
        tvTitle.text = context.resources.getString(res)
        return this
    }

    fun setDialogTitleWithString(title: String? = null): CommonAlertDialog {
        if (title.isNullOrEmpty()) {
            tvTitle.gone()
        } else {
            tvTitle.visible()
        }
        tvTitle.text = title
        return this
    }

    fun setCancelBack(boolean: Boolean): CommonAlertDialog {
        setCancelable(boolean)
        return this
    }

    override fun setTitle(res: Int) {
        tvTitle.text = context.resources.getString(res)
    }

    fun setContent(vararg res: Int): CommonAlertDialog {
        var content = ""
        for (i in res.indices) {
            content += context.resources.getString(res[i])
            if (i < res.size - 1) content += "\n"
        }
        tvSubTitle.text = content
        tvSubTitle.visibility = View.VISIBLE
        return this
    }

    fun setContent(vararg contents: String): CommonAlertDialog {
        var content = ""
        for (i in contents.indices) {
            content += contents[i]
            if (i < contents.size - 1) content += "\n"
        }
        tvSubTitle.text = content
        tvSubTitle.visibility = View.VISIBLE
        return this
    }

    fun showDialog(): CommonAlertDialog {
        if (!isShowing)
            super.show()
        return this
    }

    fun showTitleDialog(isVisible: Boolean): CommonAlertDialog {
        tvTitle.isVisible = isVisible
        return this
    }

    var typeDialog = Define.TYPE_DIALOG.DEFAULT

    companion object {
        var instance: CommonAlertDialog? = null
        fun getInstanceCommonAlertdialog(
            context: Context,
            type: Int = Define.TYPE_DIALOG.DEFAULT
        ): CommonAlertDialog {
            if (instance != null && instance?.isShowing == true) {
                if (((instance?.typeDialog ?: Define.TYPE_DIALOG.DEFAULT == Define.TYPE_DIALOG.DEFAULT) || (type != instance?.typeDialog ?: Define.TYPE_DIALOG.DEFAULT))) {
                    instance?.dismiss()
                } else {
                    return instance!!
                }
            } else {
                instance = CommonAlertDialog(context)
            }
            return instance!!
        }
    }

    override fun dismiss() {
        super.dismiss()
        typeDialog = Define.TYPE_DIALOG.DEFAULT
    }
}
