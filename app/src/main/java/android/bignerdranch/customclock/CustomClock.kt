package android.bignerdranch.customclock

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class CustomClock : View {

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0) {
        setupAttributes(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr,
        0
    ) {
        setupAttributes(attrs)
    }

    private var mPaint: Paint = Paint()
    private var mHeight: Int = 0
    private var mWidth: Int = 0
    private var mRadius: Int = 0

    private var isInitialized: Boolean = false

    private var lightBackgroundColor = ContextCompat.getColor(context, R.color.lightGray)
    private var shadowColor = ContextCompat.getColor(context, R.color.gray)
    private var pointerColor = ContextCompat.getColor(context, R.color.darkGray)
    private var borderColor = ContextCompat.getColor(context, R.color.black)

    private var hourHandColor = ContextCompat.getColor(context, R.color.purple_700)
    private var minuteHandColor = ContextCompat.getColor(context, R.color.teal_200)
    private var secondHandColor = ContextCompat.getColor(context, R.color.purple_200)

    private val handSize = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 4f, context.resources.displayMetrics
    )

    private var defaultMargin =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 8f, context.resources.displayMetrics
        )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (!isInitialized) initialize()

        drawClockShape(canvas)
        drawIndicatorMark(canvas)
        drawHands(canvas)

        postInvalidateDelayed(1000)
        invalidate()


    }

    private fun setupAttributes(attrs: AttributeSet?) {

        val typedArray: TypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CustomClock, 0, 0)

        hourHandColor =
            typedArray.getColor(R.styleable.CustomClock_hourHandColor, hourHandColor)
        minuteHandColor =
            typedArray.getColor(R.styleable.CustomClock_minuteHandColor, minuteHandColor)
        secondHandColor =
            typedArray.getColor(R.styleable.CustomClock_secondHandColor, secondHandColor)

        typedArray.recycle()
    }

    private fun calcXYForPosition(pos: Float, rad: Float, skipAngle: Int): ArrayList<Float> {
        val result = ArrayList<Float>(2)
        val startAngle = 270f
        val angle = startAngle + (pos * skipAngle)
        result.add(0, (rad * cos(angle * Math.PI / 180) + width / 2).toFloat())
        result.add(1, (height / 2 + rad * sin(angle * Math.PI / 180)).toFloat())
        return result
    }

    private fun initialize() {
        mHeight = height
        mWidth = width
        val minHeightWidthValue = min(mHeight, mWidth)
        mRadius = (minHeightWidthValue / 2 - defaultMargin).toInt()
        mPaint.isAntiAlias = true
        isInitialized = true
    }

    private fun drawClockShape(canvas: Canvas) {
        mPaint.color = lightBackgroundColor
        mPaint.setShadowLayer(10f, -10f, -10f, shadowColor)
        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            (mRadius + 5).toFloat(),
            mPaint
        )

        mPaint.strokeWidth = 4f
        mPaint.style = Paint.Style.STROKE
        mPaint.color = borderColor

        canvas.drawCircle(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            (mRadius + 5).toFloat(),
            mPaint
        )
        mPaint.reset()
    }

    private fun drawIndicatorMark(canvas: Canvas) {
        mPaint.color = pointerColor

        val numberCircleRadius = (mRadius - 60).toFloat()
        val pointRadius = 20f
        for (i in 0..11) {
            val xyData = calcXYForPosition(i.toFloat(), numberCircleRadius, 30)
            canvas.drawCircle(xyData[0], xyData[1], pointRadius, mPaint)
        }
        mPaint.reset()
    }

    private fun drawHands(canvas: Canvas) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR)

        drawHandLine(canvas, (hour + calendar.get(Calendar.MINUTE) / 60f) * 5f, HandType.HOUR)
        drawHandLine(canvas, calendar.get(Calendar.MINUTE).toFloat(), HandType.MINUTE)
        drawHandLine(canvas, calendar.get(Calendar.SECOND).toFloat(), HandType.SECONDS)

        mPaint.reset()
    }

    private fun drawHandLine(canvas: Canvas, value: Float, handType: HandType) {
        val angle = Math.PI * value / 30 - Math.PI / 2

        val handRadius = when (handType) {
            HandType.HOUR -> mRadius - mRadius / 3
            HandType.MINUTE -> mRadius - mRadius / 6
            HandType.SECONDS -> mRadius - mRadius / 9
        }

        when (handType) {
            HandType.SECONDS -> mPaint.color = secondHandColor
            HandType.MINUTE -> mPaint.color = minuteHandColor
            HandType.HOUR -> mPaint.color = hourHandColor
        }

        mPaint.strokeWidth = if (handType == HandType.SECONDS) handSize else handSize * 2
        mPaint.strokeCap = Paint.Cap.ROUND

        canvas.drawLine(
            (mWidth / 2).toFloat(),
            (mHeight / 2).toFloat(),
            (mWidth / 2 + cos(angle) * handRadius).toFloat(),
            (mHeight / 2 + sin(angle) * handRadius).toFloat(),
            mPaint
        )
    }

    private enum class HandType { HOUR, MINUTE, SECONDS }
}
