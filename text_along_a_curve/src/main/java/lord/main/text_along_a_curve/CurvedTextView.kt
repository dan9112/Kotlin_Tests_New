package lord.main.text_along_a_curve

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class CurvedTextView(context: Context): View(context) {
    private var mPath = Path()
    private var mPaint = Paint()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        setBackgroundResource(R.drawable.background)

        mPaint.apply {
            style = Paint.Style.STROKE
            color = Color.WHITE
            strokeWidth = 3f
            isAntiAlias = true
        }
        mPath.apply {
            moveTo(20f, 75f)
            cubicTo(500f, 100f, 100f, 500f, 950f, 280f)
            cubicTo(955f, 470f, 440f, 510f, 570f, 555f)
        }
        canvas.apply {
            drawPath(mPath, mPaint)

            mPaint.apply {
                style = Paint.Style.FILL_AND_STROKE
                textSize = 66f
                color = Color.GREEN
            }
            drawTextOnPath("Диаграмма падения чего-то куда-то зачем-то", mPath, 90f, -4f, mPaint)
        }
    }
}
