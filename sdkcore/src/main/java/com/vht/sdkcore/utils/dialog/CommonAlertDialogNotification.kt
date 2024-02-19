package com.vht.sdkcore.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.text.SpannableString
import android.util.Log
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
import com.vht.sdkcore.utils.Constants
import com.vht.sdkcore.utils.Define
import com.vht.sdkcore.utils.autoUpperCase
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.visible


class CommonAlertDialogNotification constructor(context: Context) : Dialog(context) {

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
        setContentView(R.layout.layout_alert_notification)

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

        btnConfirm.autoUpperCase()
        btnAgree.autoUpperCase()
        btnCancel.autoUpperCase()

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

    fun setOnPositivePressed(onPositivePressed: (CommonAlertDialogNotification) -> Unit): CommonAlertDialogNotification {
        btnConfirm.setOnClickListener {
            onPositivePressed(this)
        }
        btnAgree.setOnClickListener {
            onPositivePressed(this)
        }
        return this
    }

    fun showCenterImage(dialogType: DialogType): CommonAlertDialogNotification {
        when (dialogType) {
            DialogType.SUCCESS -> {
                btnConfirm.visibility = View.VISIBLE
                btnConfirm.text = context.getString(R.string.ok)
            }

            DialogType.NOTIFICATION -> {
                btnConfirm.visibility = View.VISIBLE
            }

            DialogType.CONFIRM -> {
                llOption.visibility = View.VISIBLE
            }

            DialogType.ERROR -> {
                btnConfirm.visibility = View.VISIBLE
            }

            DialogType.ERROR_CONFIG -> {
                llOption.visibility = View.VISIBLE
            }

            DialogType.SYNC -> {
                btnConfirm.visibility = View.VISIBLE
            }
        }
        ivDescription.setImageResource(
            dialogType.imageData
        )
        ivDescription.visibility = View.VISIBLE
        return this
    }

    fun setOnNegativePressed(onNegativePressed: (CommonAlertDialogNotification) -> Unit): CommonAlertDialogNotification {
        btnCancel.setOnClickListener {
            onNegativePressed(this)
        }
        ivCancel.setOnClickListener {
            onNegativePressed(this)
        }
        return this
    }

    fun setTextNegativeButton(@StringRes textId: Int): CommonAlertDialogNotification {
        btnCancel.visibility = View.GONE
        if (textId == -1) return this
        btnCancel.text = context.resources.getString(textId)
        btnCancel.visibility = View.VISIBLE
        return this
    }

    fun setTextNegativeButtonWithString(text: String): CommonAlertDialogNotification {
        if (text.isEmpty()) return this
        btnCancel.text = text
        btnCancel.visibility = View.VISIBLE
        return this
    }

    fun setTextPositiveButton(@StringRes textId: Int): CommonAlertDialogNotification {
        if (textId == -1) return this
        btnConfirm.text = context.resources.getString(textId)
        btnAgree.text = context.resources.getString(textId)
        return this
    }

    fun setTextPositiveButtonWithString(text: String): CommonAlertDialogNotification {
        if (text.isEmpty()) return this
        btnConfirm.text = text
        btnAgree.text = text
        return this
    }

    fun showPositiveButton(): CommonAlertDialogNotification {
        btnConfirm.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun setOnClickTextContent(isClickContent: (CommonAlertDialogNotification) -> Unit): CommonAlertDialogNotification {
        tvSubTitle.setOnClickListener {
            isClickContent(this)
        }
        return this
    }

    fun showNegativeAndPositiveButton(): CommonAlertDialogNotification {
        btnConfirm.visibility = View.GONE
        llOption.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun hideOptionButton(): CommonAlertDialogNotification {
        btnConfirm.visibility = View.GONE
        llOption.visibility = View.GONE
        setCanceledOnTouchOutside(true)
        return this
    }

    fun showCancelButton(): CommonAlertDialogNotification {
        ivCancel.visibility = View.VISIBLE
        setCanceledOnTouchOutside(false)
        return this
    }

    fun showImageDescription(
        res: Int,
        isCancelTouchOutSide: Boolean = false
    ): CommonAlertDialogNotification {
        ivDescription.visibility = View.VISIBLE
        ivDescription.setImageResource(res)
        setCanceledOnTouchOutside(isCancelTouchOutSide)
        return this
    }

    fun setDialogTitle(res: Int): CommonAlertDialogNotification {
        tvTitle.text = context.resources.getString(res)
        return this
    }

    fun setDialogTitleWithString(title: String? = null): CommonAlertDialogNotification {
        if (title.isNullOrEmpty()) {
            tvTitle.gone()
        } else {
            tvTitle.visible()
        }
        tvTitle.text = title
        return this
    }

    fun setCancelBack(boolean: Boolean): CommonAlertDialogNotification {
        setCancelable(boolean)
        setCanceledOnTouchOutside(boolean)
        return this
    }

    override fun setTitle(res: Int) {
        tvTitle.text = context.resources.getString(res)
    }

    fun setContent(vararg res: Int): CommonAlertDialogNotification {
        if (res.getOrNull(0) == Constants.ERROR_NUMBER) return this
        var content = ""
        for (i in res.indices) {
            content += context.resources.getString(res[i])
            if (i < res.size - 1) content += "\n"
        }
        tvSubTitle.text = content
        tvSubTitle.visibility = View.VISIBLE
        return this
    }

    fun setContentPrivate(contents: SpannableString): CommonAlertDialogNotification {
        tvSubTitle.text = contents
        tvSubTitle.visibility = View.VISIBLE
        return this
    }

    fun setContent(vararg contents: String): CommonAlertDialogNotification {
        var content = ""
        for (i in contents.indices) {
            content += contents[i]
            if (i < contents.size - 1) content += "\n"
        }
        tvSubTitle.text = content
        tvSubTitle.visibility = View.VISIBLE
        return this
    }

    fun showDialog(): CommonAlertDialogNotification {
        if (!isShowing && context != null) {
            super.show()
        } else {
            Log.e("CommonAlertDialogNotification", "Error show dialog")
        }
        return this
    }

    fun showTitleDialog(isVisible: Boolean): CommonAlertDialogNotification {
        tvTitle.isVisible = isVisible
        return this
    }

    var typeDialog = Define.TYPE_DIALOG.DEFAULT

    companion object {
        var instance: CommonAlertDialogNotification? = null
        fun getInstanceCommonAlertdialog(
            context: Context,
            type: Int = Define.TYPE_DIALOG.DEFAULT
        ): CommonAlertDialogNotification {
            if (instance != null && instance?.isShowing == true) {
                if (((instance?.typeDialog ?: Define.TYPE_DIALOG.DEFAULT == Define.TYPE_DIALOG.DEFAULT) || (type != instance?.typeDialog ?: Define.TYPE_DIALOG.DEFAULT))) {
                    instance?.dismiss()
                } else {
                    return instance!!
                }
            } else {
                instance = CommonAlertDialogNotification(context)
            }
            return instance!!
        }
    }

    override fun dismiss() {
        super.dismiss()
        typeDialog = Define.TYPE_DIALOG.DEFAULT
    }

}

enum class DialogType(val imageData: Int) {
    SUCCESS(R.drawable.ic_dialog_success),
    ERROR(R.drawable.ic_dialog_error),
    ERROR_CONFIG(R.drawable.ic_dialog_error),
    CONFIRM(R.drawable.ic_dialog_confirm),
    NOTIFICATION(R.drawable.ic_dialog_notification),
    SYNC(R.drawable.ic_dialog_sync)
}
