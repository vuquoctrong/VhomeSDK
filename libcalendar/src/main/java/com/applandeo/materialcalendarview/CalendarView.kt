package com.applandeo.materialcalendarview

import android.content.Context
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import com.applandeo.materialcalendarview.adapters.CalendarPageAdapter
import com.applandeo.materialcalendarview.databinding.CalendarViewBinding
import com.applandeo.materialcalendarview.exceptions.ErrorsMessages
import com.applandeo.materialcalendarview.exceptions.OutOfDateRangeException
import com.applandeo.materialcalendarview.listeners.OnCalendarPageChangeListener
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.applandeo.materialcalendarview.listeners.OnDayLongClickListener
import com.applandeo.materialcalendarview.utils.*
import java.util.*


/**
 * This class represents a view, displays to user as calendar. It allows to work in date picker
 * mode or like a normal calendar. In a normal calendar mode it can displays an image under the day
 * number. In both modes it marks today day. It also provides click on day events using
 * OnDayClickListener which returns an EventDay object.
 *
 * @see EventDay
 *
 * @see OnDayClickListener
 *
 * XML attributes:
 * - Set calendar type: type="classic or one_day_picker or many_days_picker or range_picker"
 * - Set calendar header color: headerColor="@color/[color]"
 * - Set calendar header label color: headerLabelColor="@color/[color]"
 * - Set previous button resource: previousButtonSrc="@drawable/[drawable]"
 * - Ser forward button resource: forwardButtonSrc="@drawable/[drawable]"
 * - Set today label color: todayLabelColor="@color/[color]"
 * - Set selection color: selectionColor="@color/[color]"
 *
 *
 * Created by Applandeo Team.
 */

class CalendarView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var calendarPageAdapter: CalendarPageAdapter
    private lateinit var calendarProperties: CalendarProperties
    private lateinit var binding: CalendarViewBinding

    private var currentPage: Int = 0

    init {
        initControl(CalendarProperties(context)) {
            setAttributes(attrs)
        }
    }

    //internal constructor to create CalendarView for the dialog date picker
    internal constructor(context: Context,
                         attrs: AttributeSet? = null,
                         defStyleAttr: Int = 0,
                         properties: CalendarProperties
    ) : this(context, attrs, defStyleAttr) {
        initControl(properties, ::initAttributes)
    }

    private fun initControl(calendarProperties: CalendarProperties, onUiCreate: () -> Unit) {
        this.calendarProperties = calendarProperties
        binding = CalendarViewBinding.inflate(LayoutInflater.from(context), this, false)
        initUiElements()
        onUiCreate()
        initCalendar()
    }

    /**
     * This method set xml values for calendar elements
     *
     * @param attrs A set of xml attributes
     */
    private fun setAttributes(attrs: AttributeSet?) {
        context.obtainStyledAttributes(attrs, R.styleable.CalendarView).run {
            initCalendarProperties(this)
            initAttributes()
            recycle()
        }
    }

    private fun initCalendarProperties(typedArray: TypedArray) = with(calendarProperties) {
        headerColor = typedArray.getColor(R.styleable.CalendarView_headerColor, 0)
        headerLabelColor = typedArray.getColor(R.styleable.CalendarView_headerLabelColor, 0)
        abbreviationsBarColor = typedArray.getColor(R.styleable.CalendarView_abbreviationsBarColor, 0)
        abbreviationsLabelsColor = typedArray.getColor(R.styleable.CalendarView_abbreviationsLabelsColor, 0)
        abbreviationsLabelsSize = typedArray.getDimension(R.styleable.CalendarView_abbreviationsLabelsSize, 0F)
        pagesColor = typedArray.getColor(R.styleable.CalendarView_pagesColor, 0)
        daysLabelsColor = typedArray.getColor(R.styleable.CalendarView_daysLabelsColor, 0)
        anotherMonthsDaysLabelsColor = typedArray.getColor(R.styleable.CalendarView_anotherMonthsDaysLabelsColor, 0)
        todayLabelColor = typedArray.getColor(R.styleable.CalendarView_todayLabelColor, 0)
        selectionColor = typedArray.getColor(R.styleable.CalendarView_selectionColor, 0)
        selectionLabelColor = typedArray.getColor(R.styleable.CalendarView_selectionLabelColor, 0)
        disabledDaysLabelsColor = typedArray.getColor(R.styleable.CalendarView_disabledDaysLabelsColor, 0)
        highlightedDaysLabelsColor = typedArray.getColor(R.styleable.CalendarView_highlightedDaysLabelsColor, 0)
        calendarType = typedArray.getInt(R.styleable.CalendarView_typeDataCalendar, CLASSIC)
        maximumDaysRange = typedArray.getInt(R.styleable.CalendarView_maximumDaysRange, 0)

        if (typedArray.hasValue(R.styleable.CalendarView_firstDayOfWeek)) {
            firstDayOfWeek = typedArray.getInt(R.styleable.CalendarView_firstDayOfWeek, Calendar.MONDAY)
        }

        eventsEnabled = typedArray.getBoolean(R.styleable.CalendarView_eventsEnabled, calendarType == CLASSIC)
        swipeEnabled = typedArray.getBoolean(R.styleable.CalendarView_swipeEnabled, true)
        selectionDisabled = typedArray.getBoolean(R.styleable.CalendarView_selectionDisabled, false)
        selectionBetweenMonthsEnabled = typedArray.getBoolean(R.styleable.CalendarView_selectionBetweenMonthsEnabled, false)
        previousButtonSrc = typedArray.getDrawable(R.styleable.CalendarView_previousButtonSrc)
        forwardButtonSrc = typedArray.getDrawable(R.styleable.CalendarView_forwardButtonSrc)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            typeface = typedArray.getFont(R.styleable.CalendarView_typefaceCalendar)
            todayTypeface = typedArray.getFont(R.styleable.CalendarView_todayTypeface)
        }
    }

    private fun initAttributes() {
        with(calendarProperties) {
            binding.setHeaderColor(headerColor)
            binding.setHeaderTypeface(typeface)
            binding.setHeaderVisibility(headerVisibility)
            binding.abbreviationsBar.setAbbreviationsBarVisibility(abbreviationsBarVisibility)
            binding.setNavigationVisibility(navigationVisibility)
            binding.setHeaderLabelColor(headerLabelColor)
            binding.setAbbreviationsBarColor(abbreviationsBarColor)
            binding.setAbbreviationsLabels(abbreviationsLabelsColor, firstDayOfWeek)
            binding.setAbbreviationsLabelsSize(abbreviationsLabelsSize)
            binding.setPagesColor(pagesColor)
            binding.setTypeface(typeface)
            binding.setPreviousButtonImage(previousButtonSrc)
            binding.setForwardButtonImage(forwardButtonSrc)
            binding.calendarViewPager.swipeEnabled = swipeEnabled
        }

        setCalendarRowLayout()
    }

    /**
     * This method set a first day of week, default is monday or sunday depending on user location
     */
    fun setFirstDayOfWeek(weekDay: CalendarWeekDay) = with(calendarProperties) {
        firstDayOfWeek = weekDay.value
        binding.setAbbreviationsLabels(abbreviationsLabelsColor, firstDayOfWeek)
    }

    fun setHeaderColor(@ColorRes color: Int) = with(calendarProperties) {
        headerColor = color
        binding.setHeaderColor(headerColor)
    }

    fun setHeaderVisibility(visibility: Int) = with(calendarProperties) {
        headerVisibility = visibility
        binding.setHeaderVisibility(headerVisibility)
    }

    fun setAbbreviationsBarVisibility(visibility: Int) = with(calendarProperties) {
        abbreviationsBarVisibility = visibility
        binding.abbreviationsBar.setAbbreviationsBarVisibility(abbreviationsBarVisibility)
    }

    fun setHeaderLabelColor(@ColorRes color: Int) = with(calendarProperties) {
        headerLabelColor = color
        binding.setHeaderLabelColor(headerLabelColor)
    }

    fun setPreviousButtonImage(drawable: Drawable) = with(calendarProperties) {
        previousButtonSrc = drawable
        binding.setPreviousButtonImage(previousButtonSrc)
    }

    fun setForwardButtonImage(drawable: Drawable) = with(calendarProperties) {
        forwardButtonSrc = drawable
        binding.setForwardButtonImage(forwardButtonSrc)
    }

    fun setCalendarDayLayout(@LayoutRes layout: Int) {
        calendarProperties.itemLayoutResource = layout
    }

    fun setSelectionBackground(@DrawableRes drawable: Int) {
        calendarProperties.selectionBackground = drawable
    }

    private fun setCalendarRowLayout() {
        if (calendarProperties.itemLayoutResource != R.layout.calendar_view_day) return

        with(calendarProperties) {
            itemLayoutResource = if (eventsEnabled) {
                R.layout.calendar_view_day
            } else {
                R.layout.calendar_view_picker_day
            }
        }
    }

    private fun initUiElements() {
        binding.previousButton.setOnClickListener {
            binding.calendarViewPager.currentItem = binding.calendarViewPager.currentItem - 1
        }

        binding.forwardButton.setOnClickListener {
            binding.calendarViewPager.currentItem = binding.calendarViewPager.currentItem + 1
        }
    }

    private fun initCalendar() {
        calendarPageAdapter = CalendarPageAdapter(context, calendarProperties)

        binding.calendarViewPager.adapter = calendarPageAdapter
        binding.calendarViewPager.onCalendarPageChangedListener(::renderHeader)

        setUpCalendarPosition(Calendar.getInstance())
    }

    /**
     * This method set calendar header label
     *
     * @param position Current calendar page number
     */
    private fun renderHeader(position: Int) {
        val calendar = calendarProperties.firstPageCalendarDate.clone() as Calendar
        calendar.add(Calendar.MONTH, position)

        if (!isScrollingLimited(calendar, position)) {
            setHeaderName(calendar, position)
        }
    }

    private fun setUpCalendarPosition(calendar: Calendar) {
        calendar.setMidnight()

        if (calendarProperties.calendarType == ONE_DAY_PICKER) {
            calendarProperties.setSelectedDay(calendar)
        }

        with(calendarProperties.firstPageCalendarDate) {
            time = calendar.time
            this.add(Calendar.MONTH, -CalendarProperties.FIRST_VISIBLE_PAGE)
        }

        binding.calendarViewPager.currentItem = CalendarProperties.FIRST_VISIBLE_PAGE
    }

    fun setOnPreviousPageChangeListener(listener: OnCalendarPageChangeListener) {
        calendarProperties.onPreviousPageChangeListener = listener
    }

    fun setOnForwardPageChangeListener(listener: OnCalendarPageChangeListener) {
        calendarProperties.onForwardPageChangeListener = listener
    }

    private fun isScrollingLimited(calendar: Calendar, position: Int): Boolean {
        fun scrollTo(position: Int): Boolean {
            binding.calendarViewPager.currentItem = position
            return true
        }

        return when {
            calendarProperties.minimumDate.isMonthBefore(calendar) -> scrollTo(position + 1)
            calendarProperties.maximumDate.isMonthAfter(calendar) -> scrollTo(position - 1)
            else -> false
        }
    }

    private fun setHeaderName(calendar: Calendar, position: Int) {
        binding.currentDateLabel.text = calendar.getMonthAndYearDate(context)
        callOnPageChangeListeners(position)
    }

    // This method calls page change listeners after swipe calendar or click arrow buttons
    private fun callOnPageChangeListeners(position: Int) {
        when {
            position > currentPage -> calendarProperties.onForwardPageChangeListener?.onChange()
            position < currentPage -> calendarProperties.onPreviousPageChangeListener?.onChange()
        }
        currentPage = position
    }

    /**
     * @param onDayClickListener OnDayClickListener interface responsible for handle clicks on calendar cells
     * @see OnDayClickListener
     */
    fun setOnDayClickListener(onDayClickListener: OnDayClickListener) {
        calendarProperties.onDayClickListener = onDayClickListener
    }

    /**
     * @param onDayLongClickListener OnDayClickListener interface responsible for handle long clicks on calendar cells
     * @see OnDayLongClickListener
     */
    fun setOnDayLongClickListener(onDayLongClickListener: OnDayLongClickListener) {
        calendarProperties.onDayLongClickListener = onDayLongClickListener
    }

    /**
     * This method set a current date of the calendar using Calendar object.
     *
     * @param date A Calendar object representing a date to which the calendar will be set
     *
     * Throws exception when set date is not between minimum and maximum date
     */
    @Throws(OutOfDateRangeException::class)
    fun setDate(date: Calendar) {
        if (calendarProperties.minimumDate != null && date.before(calendarProperties.minimumDate)) {
            throw OutOfDateRangeException(ErrorsMessages.OUT_OF_RANGE_MIN)
        }
        if (calendarProperties.maximumDate != null && date.after(calendarProperties.maximumDate)) {
            throw OutOfDateRangeException(ErrorsMessages.OUT_OF_RANGE_MAX)
        }
        setUpCalendarPosition(date)
        binding.currentDateLabel.text = date.getMonthAndYearDate(context)
        calendarPageAdapter.notifyDataSetChanged()
    }

    /**
     * This method set a current and selected date of the calendar using Date object.
     *
     * @param currentDate A date to which the calendar will be set
     */
    fun setDate(currentDate: Date) {
        val calendar = Calendar.getInstance().apply { time = currentDate }
        setDate(calendar)
    }

    /**
     * This method is used to set a list of events displayed in calendar cells,
     * visible as images under the day number.
     *
     * @param eventDays List of EventDay objects
     * @see EventDay
     */
    fun setEvents(eventDays: List<EventDay>) {
        if (calendarProperties.eventsEnabled) {
            calendarProperties.eventDays = eventDays
            calendarPageAdapter.notifyDataSetChanged()
        }
    }

    fun setCalendarDays(calendarDayProperties: List<CalendarDay>) {
        calendarProperties.calendarDayProperties = calendarDayProperties.toMutableList()
        calendarPageAdapter.notifyDataSetChanged()
    }

    /**
     * List of Calendar objects representing a selected dates
     */
    var selectedDates: List<Calendar>
        get() = calendarPageAdapter.selectedDays
                .map { it.calendar }
                .sorted().toList()
        set(selectedDates) {
            calendarProperties.setSelectDays(selectedDates)
            calendarPageAdapter.notifyDataSetChanged()
        }

    /**
     * @return Calendar object representing a selected date
     */
    @Deprecated("Use getFirstSelectedDate()", ReplaceWith("firstSelectedDate"))
    val selectedDate: Calendar
        get() = firstSelectedDate

    /**
     * @return Calendar object representing a selected date
     */
    val firstSelectedDate: Calendar
        get() = calendarPageAdapter.selectedDays.map { it.calendar }.first()


    /**
     * @return Calendar object representing a date of current calendar page
     */
    val currentPageDate: Calendar
        get() = (calendarProperties.firstPageCalendarDate.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, binding.calendarViewPager.currentItem)
        }

    /**
     * This method set a minimum available date in calendar
     *
     * @param calendar Calendar object representing a minimum date
     */
    fun setMinimumDate(calendar: Calendar) {
        calendarProperties.minimumDate = calendar
        calendarPageAdapter.notifyDataSetChanged()
    }

    /**
     * This method set a maximum available date in calendar
     *
     * @param calendar Calendar object representing a maximum date
     */
    fun setMaximumDate(calendar: Calendar) {
        calendarProperties.maximumDate = calendar
        calendarPageAdapter.notifyDataSetChanged()
    }

    /**
     * This method is used to return to current month page
     */
    fun showCurrentMonthPage() {
        val page = binding.calendarViewPager.currentItem - midnightCalendar.getMonthsToDate(currentPageDate)
        binding.calendarViewPager.setCurrentItem(page, true)
    }

    /**
     * This method removes all selected days from calendar
     */
    fun clearSelectedDays() {
        calendarProperties.selectedDays.clear()
        calendarPageAdapter.notifyDataSetChanged()
    }

    fun setDisabledDays(disabledDays: List<Calendar>) {
        calendarProperties.disabledDays = disabledDays
        calendarPageAdapter.notifyDataSetChanged()
    }

    @Deprecated("Use setCalendarDays(List<CalendarDay>) with specific labelColor")
    fun setHighlightedDays(highlightedDays: List<Calendar>) {
        calendarProperties.highlightedDays = highlightedDays
        calendarPageAdapter.notifyDataSetChanged()
    }

    fun setSwipeEnabled(swipeEnabled: Boolean) {
        calendarProperties.swipeEnabled = swipeEnabled
        binding.calendarViewPager.swipeEnabled = calendarProperties.swipeEnabled
    }

    fun setSelectionBetweenMonthsEnabled(enabled: Boolean) {
        calendarProperties.selectionBetweenMonthsEnabled = enabled
    }

    fun setOnPagePrepareListener(listener: OnPagePrepareListener) {
        calendarProperties.onPagePrepareListener = listener
    }

    companion object {
        const val CLASSIC = 0
        const val ONE_DAY_PICKER = 1
        const val MANY_DAYS_PICKER = 2
        const val RANGE_PICKER = 3
    }
}