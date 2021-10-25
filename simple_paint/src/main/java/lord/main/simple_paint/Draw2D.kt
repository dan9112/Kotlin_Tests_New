package lord.main.simple_paint

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.view.View

class Draw2D(context: Context) : View(context) {
    private val paint = Paint()

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.apply {
            drawPaint(paint.apply {
                style = Paint.Style.FILL // стиль "Заливка"
                color = Color.WHITE // закрашиваем холст белым цветом
            })
            drawCircle(950f, 30f, 25f, paint.apply {
                isAntiAlias = true
                color = Color.YELLOW
            })
            drawRect(20f, 650f, 950f, 680f, paint.apply {
                color = Color.GREEN
            })
            drawText("Лужайка только для котов", 30f, 648f, paint.apply {
                color = Color.BLUE
                style = Paint.Style.FILL
                isAntiAlias = true
                textSize = 32f
            })

            val x = 810f
            val y = 190f

            val rect = Rect()
            save()
            rotate(-45f, x + rect.exactCenterX(), y + rect.exactCenterY())
            drawText("Лучик солнца!", x, y, paint.apply {
                color = Color.GRAY
                style = Paint.Style.FILL
                textSize = 27f
            })

            restore()
        }
    }
}
