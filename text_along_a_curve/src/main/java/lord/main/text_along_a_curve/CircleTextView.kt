package lord.main.text_along_a_curve

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class CircleTextView(context: Context): View(context) {

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // фон
        setBackgroundResource(R.drawable.background)

        canvas.apply {
            val circlePath = Path(). apply {
                // добавляем окружность
                addCircle(240f, 240f, 120f, Path.Direction.CW)
            }

            drawPath(circlePath, Paint().apply {// настраиваем окружность
                color = Color.BLUE// цвет окружности
                style = Paint.Style.STROKE// стиль контура без заливки
                strokeWidth = 2.0f// толщина контура
                isAntiAlias = true// включить сглаживание
            })
            drawTextOnPath(
                "Коты и кошки всех стран, объединяйтесь! * ",
                circlePath,
                0f,
                -32f,
                Paint().apply {// настраиваем текст
                    textSize = 36f
                    isAntiAlias = true
                    color = Color.WHITE
                })
        }
    }
}
