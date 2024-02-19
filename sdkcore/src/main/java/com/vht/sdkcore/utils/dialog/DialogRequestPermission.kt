package com.vht.sdkcore.utils.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.vht.sdkcore.R
import com.vht.sdkcore.utils.gone
import com.vht.sdkcore.utils.visible


class DialogRequestPermission constructor(context: Context) : Dialog(context) {

    lateinit var btnAllow: TextView
    lateinit var btnRefuse: TextView
    lateinit var btnNotShowingAgain: View
    lateinit var ivRadio: ImageView
    lateinit var tvTitle: TextView
    lateinit var tvDescription: TextView
    private var isChecked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_ovelay_permission)

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(window?.attributes)
        btnAllow = findViewById(R.id.btnAllow)
        btnRefuse = findViewById(R.id.btnRefuse)
        btnNotShowingAgain = findViewById(R.id.btnNotShowingAgain)
        ivRadio = findViewById(R.id.ivRadio)
        tvTitle = findViewById(R.id.tvTitle)
        tvDescription = findViewById(R.id.tvDescription)
        setCanceledOnTouchOutside(false)

        btnNotShowingAgain.setOnClickListener {
            dismiss()
        }

        setRadioImage(isChecked)

        btnNotShowingAgain.setOnClickListener {
            isChecked = !isChecked
            setRadioImage(isChecked)
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
        }

        window?.apply {
            attributes = lp
            setGravity(Gravity.CENTER_VERTICAL)
            setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun setRadioImage(isChecked: Boolean) {
        ivRadio.setImageResource(
            if (isChecked) {
                R.drawable.ic_radio_selected
            } else {
                R.drawable.ic_radio_unselected
            }
        )
    }

    fun setTitle(title: String): DialogRequestPermission {
        if (title.isNotBlank()) {
            tvTitle.visible()
            tvTitle.text = title
        } else {
            tvTitle.gone()
        }
        return this
    }

    fun setDescription(description: String): DialogRequestPermission {
        tvDescription.text = description
        return this
    }

    fun showDialog(): DialogRequestPermission {
        if (!isShowing && context != null) {
            super.show()
        } else {
            Log.e("DialogRequestPermission", "Error show dialog")
        }
        return this
    }

    fun setOnPositivePressed(onPositivePressed: (Boolean) -> Unit): DialogRequestPermission {
        btnAllow.setOnClickListener {
            onPositivePressed(isChecked)
            dismiss()
        }
        return this
    }

    fun setOnNegativePressed(onNegativePressed: (Boolean) -> Unit): DialogRequestPermission {
        btnRefuse.setOnClickListener {
            onNegativePressed(isChecked)
            dismiss()
        }
        return this
    }

    companion object {
        var instance: DialogRequestPermission? = null
        fun getInstance(context: Context, ): DialogRequestPermission {
            if (instance != null && instance?.isShowing == true) {
                return instance!!
            } else {
                instance = DialogRequestPermission(context)
            }
            return instance!!
        }
    }

    override fun dismiss() {
        super.dismiss()
        isChecked = false
    }

}

