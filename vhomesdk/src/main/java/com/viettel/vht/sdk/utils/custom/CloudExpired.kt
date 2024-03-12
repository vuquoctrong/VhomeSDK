package com.viettel.vht.sdk.utils.custom

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TextAppearanceSpan
import android.text.style.UnderlineSpan
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.viettel.vht.sdk.R
import com.viettel.vht.sdk.databinding.LayoutCloudExpiesBinding


class CloudExpired(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var binding: LayoutCloudExpiesBinding

    private var onClickRenew: (() -> Unit)? = null

    init {
        binding = LayoutCloudExpiesBinding.inflate(LayoutInflater.from(context), this, true)
        val renewClickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                onClickRenew?.invoke()
            }
        }
        val renewSpannable = SpannableString(context.getString(com.vht.sdkcore.R.string.renew))
        renewSpannable.setSpan(
            renewClickableSpan,
            0,
            renewSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        renewSpannable.setSpan(
            TextAppearanceSpan(context, com.vht.sdkcore.R.style.text_bold_12),
            0,
            renewSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        renewSpannable.setSpan(
            UnderlineSpan(),
            0,
            renewSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        renewSpannable.setSpan(
            ForegroundColorSpan(context.getColor(R.color.color_ef1e4a)),
            0,
            renewSpannable.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        binding.tvContent.text = SpannableStringBuilder()
            .append(context.getString(com.vht.sdkcore.R.string.the_hosting_package_has_expired))
            .append(" ")
            .append(renewSpannable)
            .append(" ")
            .append(context.getString(com.vht.sdkcore.R.string.to_continue_using))
        binding.tvContent.movementMethod = LinkMovementMethod.getInstance()
    }

    fun setOnClickRenew(onClickRenew: (() -> Unit)?) {
        this.onClickRenew = onClickRenew
    }
}
