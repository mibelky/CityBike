package com.belki.citybike.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas

import android.graphics.drawable.Drawable
import androidx.appcompat.content.res.AppCompatResources


fun getBitmap(context: Context, drawableRes: Int): Bitmap {
    val drawable: Drawable = AppCompatResources.getDrawable(context, drawableRes)!!
    val canvas = Canvas()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    canvas.setBitmap(bitmap)
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.draw(canvas)
    return bitmap
}