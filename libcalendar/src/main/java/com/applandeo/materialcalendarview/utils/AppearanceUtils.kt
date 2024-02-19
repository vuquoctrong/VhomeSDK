package com.applandeo.materialcalendarview.utils

import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.view.View
import com.applandeo.materialcalendarview.R
import com.applandeo.materialcalendarview.databinding.CalendarViewBinding


/**
 * Created by Applandeo Team.
 */

internal fun CalendarViewBinding.setAbbreviationsLabels(color: Int, firstDayOfWeek: Int) {
    val labels = getAbbreviationsTextViews()

    val abbreviations = this.root.context.resources.getStringArray(com.vht.sdkcore.R.array.material_calendar_day_abbreviations_array)

    labels.forEachIndexed { index, label ->
        label.text = abbreviations[(index + firstDayOfWeek - 1) % 7]

        if (color != 0) {
            label.setTextColor(color)
        }
    }
}

internal fun CalendarViewBinding.setAbbreviationsLabelsSize(size: Float) {
    val labels = getAbbreviationsTextViews()
    val maxTextSize = this.root.resources.getDimensionPixelSize(R.dimen.abbreviation_text_size_max)
    labels.forEachIndexed { _, label ->
        if (size > 0.0 && size <= maxTextSize) {
            label.textSize = size
        }
    }
}

private fun CalendarViewBinding.getAbbreviationsTextViews() = listOf(
        mondayLabel,
        tuesdayLabel,
        wednesdayLabel,
        thursdayLabel,
        fridayLabel,
        saturdayLabel,
        sundayLabel)

internal fun CalendarViewBinding.setTypeface(typeface: Typeface?) {
    if (typeface == null) return
    getAbbreviationsTextViews().forEach { label ->
        label.typeface = typeface
    }
}

internal fun CalendarViewBinding.setHeaderColor(color: Int) {
    if (color == 0) return
    this.calendarHeader.setBackgroundColor(color)
}

internal fun CalendarViewBinding.setHeaderLabelColor(color: Int) {
    if (color == 0) return
    this.currentDateLabel.setTextColor(color)
}

internal fun CalendarViewBinding.setHeaderTypeface(typeface: Typeface?) {
    if (typeface == null) return
    this.currentDateLabel.typeface = typeface
}

internal fun CalendarViewBinding.setAbbreviationsBarColor(color: Int) {
    if (color == 0) return
    this.abbreviationsBar.setBackgroundColor(color)
}

internal fun CalendarViewBinding.setPagesColor(color: Int) {
    if (color == 0) return
    this.calendarViewPager.setBackgroundColor(color)
}

internal fun CalendarViewBinding.setPreviousButtonImage(drawable: Drawable?) {
    if (drawable == null) return
    this.previousButton.setImageDrawable(drawable)
}

internal fun CalendarViewBinding.setForwardButtonImage(drawable: Drawable?) {
    if (drawable == null) return
    this.forwardButton.setImageDrawable(drawable)
}

internal fun CalendarViewBinding.setHeaderVisibility(visibility: Int) {
    this.calendarHeader.visibility = visibility
}

internal fun CalendarViewBinding.setNavigationVisibility(visibility: Int) {
    this.previousButton.visibility = visibility
    this.forwardButton.visibility = visibility
}

internal fun View.setAbbreviationsBarVisibility(visibility: Int) {
    this.visibility = visibility
}
