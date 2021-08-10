package com.udacity

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.content.res.ResourcesCompat
import timber.log.Timber
import kotlin.properties.Delegates

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface LoadingListener {
        fun onStart()
        fun onProgress(progress: Float)
        fun onCompleted()
    }

    companion object {
        private const val ANIMATION_TIME = 10000L
        private const val ANIMATION_FINISH_TIME = 700L
    }

    var loadingListener: LoadingListener? = null

    private var widthSize = 0
    private var heightSize = 0

    private val valueAnimator = ValueAnimator.ofFloat(0f, 100f).apply {
        this.addUpdateListener {
            setProgress(it.animatedValue as Float)
        }
        this.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator?) {
                buttonState = ButtonState.Loading
            }

            override fun onAnimationEnd(animation: Animator?) {
                buttonState = ButtonState.Completed
            }

            override fun onAnimationCancel(animation: Animator?) {}
            override fun onAnimationRepeat(animation: Animator?) {}
        })
        this.duration = ANIMATION_TIME
    }

    private var currentProgress = 90f
    private var currentText = ""
    private var textSize = 24f
    private var circularProgressSize = 100f

    private lateinit var horizontalProgressRect: RectF
    private lateinit var circularProgressRect: RectF
    private var textPosX = 0f
    private var textPosY = 0f
    private var circularProgressColor = Color.YELLOW
    private var horizontalProgressColor = Color.BLUE
    private var textColor = Color.BLACK

    private val horizontalProgressPaint = Paint().apply {
        color = horizontalProgressColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val circularProgressPaint = Paint().apply {
        color = circularProgressColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }

    private val textPaint = Paint().apply {
        color = ResourcesCompat.getColor(resources, R.color.colorAccent, null)
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        textSize = 58f
        style = Paint.Style.FILL
    }

    private var buttonState: ButtonState by Delegates.observable<ButtonState>(ButtonState.Initial) { p, old, new ->
        Timber.d("P=$p, old=$old, new=$new")
        when (new) {
            ButtonState.Clicked -> {
                loadingListener?.onStart()
                setProgress(0f)
            }
            ButtonState.Loading -> {
                loadingListener?.onProgress(currentProgress)
            }
            ButtonState.Completed -> {
                loadingListener?.onCompleted()
                setProgress(0f)
            }
        }
    }

    init {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.LoadingButton)
            currentText = typedArray.getText(R.styleable.LoadingButton_text).toString()
            currentProgress = typedArray.getFloat(R.styleable.LoadingButton_progress, 0f)
            textSize = typedArray.getDimension(R.styleable.LoadingButton_textSize, 24f)
            circularProgressColor =
                typedArray.getColor(R.styleable.LoadingButton_circularProgressColor, Color.YELLOW)
            horizontalProgressColor =
                typedArray.getColor(R.styleable.LoadingButton_horizontalProgressColor, Color.BLUE)
            textColor =
                typedArray.getColor(R.styleable.LoadingButton_textColor, Color.BLACK)
            circularProgressSize =
                typedArray.getDimension(R.styleable.LoadingButton_circularProgressSize, 100f)

            typedArray.recycle()
        }
        circularProgressPaint.color = circularProgressColor
        horizontalProgressPaint.color = horizontalProgressColor
        textPaint.color = textColor
        textPaint.textSize = textSize

        measurePosition()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthSize = w
        heightSize = h
        measurePosition()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val relativeCircularProgress = 360 * currentProgress / 100f
        val relativeProgress = widthSize * currentProgress / 100f
        horizontalProgressRect.right = relativeProgress
        canvas?.drawRect(horizontalProgressRect, horizontalProgressPaint)
        canvas?.drawText(currentText, textPosX, textPosY, textPaint)
        canvas?.drawArc(
            circularProgressRect, -90f,
            relativeCircularProgress, true, circularProgressPaint
        )
    }

    fun setProgress(@FloatRange(from = 0.0, to = 100.0) progress: Float) {
        currentProgress = progress
        measurePosition()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minw: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val w: Int = resolveSizeAndState(minw, widthMeasureSpec, 1)
        val h: Int = resolveSizeAndState(
            MeasureSpec.getSize(w),
            heightMeasureSpec,
            0
        )
        widthSize = w
        heightSize = h
        setMeasuredDimension(w, h)
    }

    private fun measurePosition() {
        // Measure text Position to the center
        val boundsRect = Rect()
        textPaint.getTextBounds(currentText, 0, currentText.length, boundsRect)
        textPosX = widthSize / 2f
        textPosY = heightSize / 2f + boundsRect.height() / 2

        // Measure horizontal progress
        horizontalProgressRect = RectF(0f, 0f, 0f, heightSize.toFloat())

        // Measure circular progress
        val textLen = boundsRect.left + boundsRect.width()
        val left = widthSize / 2f + textLen / 2f
        val top = heightSize / 2f - circularProgressSize / 2f
        circularProgressRect =
            RectF(left, top, left + circularProgressSize, top + circularProgressSize)
    }

    fun setText(text: String) {
        this.currentText = text
        measurePosition()
        invalidate()
    }

    fun startProgressAnimation() {
        buttonState = ButtonState.Clicked
        valueAnimator.duration = ANIMATION_TIME
        valueAnimator.start()
    }

    fun finishProgressAnimation() {
        val animatedValue = (valueAnimator.animatedValue as Float)
        if (buttonState == ButtonState.Loading) {
            valueAnimator.cancel()
            ValueAnimator.ofFloat(animatedValue, 100f).apply {
                this.addUpdateListener { setProgress(it.animatedValue as Float) }
                this.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {}
                    override fun onAnimationCancel(animation: Animator?) {}
                    override fun onAnimationRepeat(animation: Animator?) {}
                    override fun onAnimationEnd(animation: Animator?) {
                        buttonState = ButtonState.Completed
                    }
                })
                this.duration = ANIMATION_FINISH_TIME // TIME TO FINISH
            }.start()
        }
    }
}