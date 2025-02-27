package com.example.gaenari.activity.runandwalk.walk

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.gaenari.R

class WCircleProgress @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var progress = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private val trackColor: Int = ContextCompat.getColor(context, R.color.walk1)
    private val progressColor: Int = ContextCompat.getColor(context, R.color.walk2)

    init {
        paint.strokeWidth = 17f // 도넛 모양의 두께 설정
        paint.style = Paint.Style.STROKE // 스타일을 스트로크로 설정

        // 기타 초기화 코드를 여기에 추가할 수 있습니다.
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val size = Math.min(width, height)
        val extra = paint.strokeWidth / 2
        rect.set(extra, extra, size - extra, size - extra)

        paint.color = trackColor // 트랙 색상 사용
        canvas.drawOval(rect, paint)

        paint.color = progressColor // 프로그레스 색상 사용
        canvas.drawArc(rect, 270f, 360 * progress / 100, false, paint)
    }

    fun setProgress(progress: Float) {
        this.progress = progress
        invalidate()
    }
}