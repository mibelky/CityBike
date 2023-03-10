package com.belki.citybike.utils

import android.graphics.*
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.belki.citybike.R
import com.belki.citybike.domain.Station
import com.belki.citybike.nearest.StationListAdapter
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import kotlin.math.min
import kotlin.math.roundToInt


@BindingAdapter("customVisibility")
fun setVisibility(view : View, visible : Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("stationPercentageImage")
fun bindStationPercentageImage(imageView: ImageView, station: Station) {
    val percent = station.freeBikes.toDouble() / (station.freeBikes + station.emptySlots)
    when {
        percent >= 1 -> imageView.setImageResource(R.drawable.percentage_100)
        percent >= 0.7 -> imageView.setImageResource(R.drawable.percentage_75)
        percent >= 0.35 -> imageView.setImageResource(R.drawable.percentage_50)
        percent >= 0.05 -> imageView.setImageResource(R.drawable.percentage_25)
        else -> imageView.setImageResource(R.drawable.percentage_0)
    }
}

@BindingAdapter("listData")
fun bindRecyclerView(recyclerView: RecyclerView, data: List<Station>?) {
    val adapter = recyclerView.adapter as StationListAdapter
    adapter.submitList(data)
}

@BindingAdapter("distanceInMeters")
fun bindDistanceToText(textView: TextView, distanceInMeters: Int) {
    val distanceInKm = (distanceInMeters / 10) / 100.0
    textView.text = distanceInKm.toString()
}

@BindingAdapter("distanceToTime")
fun bindTimeToText(textView: TextView, distanceInMeters: Int) {
    val minutes = (distanceInMeters * 0.012).roundToInt() // 0.012 min / meter
    textView.text = String.format("%d", minutes)
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {
    imgUrl?.let {
        Picasso.with(imgView.context)
            .load(it)
            .placeholder(R.drawable.placeholder_image)
            .error(R.drawable.placeholder_image)
            .fit()
            .transform(PicassoCircleTransformation())
            .into(imgView)
    }
}

class PicassoCircleTransformation : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        val size = min(source.width, source.height)
        val x = (source.width - size) / 2
        val y = (source.height - size) / 2
        val squaredBitmap = Bitmap.createBitmap(source, x, y, size, size)

        if (squaredBitmap != source) {
            source.recycle()
        }

        val bitmap = Bitmap.createBitmap(size, size, source.config)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        val shader = BitmapShader(
            squaredBitmap,
            Shader.TileMode.CLAMP, Shader.TileMode.CLAMP
        )
        paint.shader = shader
        paint.isAntiAlias = true
        val r = size / 2f
        canvas.drawCircle(r, r, r, paint)
        squaredBitmap.recycle()
        return bitmap
    }

    override fun key(): String {
        return "circle"
    }
}