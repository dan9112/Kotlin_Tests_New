package com.example.face_detector

import android.content.Context
import android.graphics.*
import android.media.FaceDetector
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class MyImageView : AppCompatImageView {
    private var imageWidth = 0
    private var imageHeight = 0
    private val numberOfFace = 10 // 100
    private var myFaceDetect: FaceDetector
    private var myFace: Array<FaceDetector.Face?>
    private var myEyesDistance = 0f
    private var numberOfFaceDetected = 0
    private var myPaint = Paint()
    private var myMidPoint = PointF()
    private var myBitmap: Bitmap

    constructor(context: Context) : super(context)

    constructor(c: Context, attrs: AttributeSet?) : super(c, attrs)

    override fun onDraw(canvas: Canvas) {
        canvas.apply {
            val scaleFactor = if (myBitmap.height - height > myBitmap.width - width) {
                (height.toFloat() / myBitmap.height)
            } else {
                (width.toFloat() / myBitmap.width)
            }
            scale(scaleFactor, scaleFactor)
            drawBitmap(myBitmap, 0f, 0f, null)
        }
        myPaint.apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        myFace.forEach {
            if (it != null) {
                it.getMidPoint(myMidPoint) // середина между глазами
                myEyesDistance = it.eyesDistance()

                canvas.drawRect(
                    (myMidPoint.x - myEyesDistance),
                    (myMidPoint.y - myEyesDistance),
                    (myMidPoint.x + myEyesDistance),
                    (myMidPoint.y + myEyesDistance), myPaint
                )
            }
        }
    }

    init {
        val bitmapFactoryOptionsbfo = BitmapFactory.Options()
        bitmapFactoryOptionsbfo.inPreferredConfig = Bitmap.Config.RGB_565
        // Рожа Шарапова
        myBitmap = BitmapFactory.decodeResource(
            resources,
            R.drawable.img2, bitmapFactoryOptionsbfo
        )
        imageWidth = myBitmap.width
        imageHeight = myBitmap.height
        myFace = arrayOfNulls(numberOfFace)
        myFaceDetect = FaceDetector(imageWidth, imageHeight, numberOfFace)
        numberOfFaceDetected = myFaceDetect.findFaces(myBitmap, myFace)
    }
}