package com.karyarina.labs.util

import android.graphics.Bitmap
import kotlin.math.roundToInt

/**
 * Уменьшает картинку до размера схемы и сокращает палитру методом median cut,
 * чтобы каждая клетка соответствовала одному цвету нити.
 */
object ImageToGridConverter {

    fun convert(source: Bitmap, targetWidth: Int, colorCount: Int): Bitmap {
        val targetHeight = (targetWidth.toFloat() * source.height / source.width)
            .roundToInt()
            .coerceAtLeast(1)
        val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)

        val pixelCount = targetWidth * targetHeight
        val pixels = IntArray(pixelCount)
        scaled.getPixels(pixels, 0, targetWidth, 0, 0, targetWidth, targetHeight)

        val palette = buildPalette(pixels, colorCount)
        val result = IntArray(pixelCount) { i -> nearestColor(pixels[i], palette) }

        val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        output.setPixels(result, 0, targetWidth, 0, 0, targetWidth, targetHeight)
        return output
    }

    private fun buildPalette(pixels: IntArray, colorCount: Int): List<Int> {
        var buckets = listOf(pixels.indices.toMutableList())

        while (buckets.size < colorCount) {
            val splitIndex = buckets.indices.maxByOrNull { bucketRange(buckets[it], pixels) } ?: break
            val bucket = buckets[splitIndex]
            if (bucket.size < 2 || bucketRange(bucket, pixels) == 0) break

            val channel = widestChannel(bucket, pixels)
            val sorted = bucket.sortedBy { channelValue(pixels[it], channel) }
            val mid = sorted.size / 2

            buckets = buckets.toMutableList().also {
                it[splitIndex] = sorted.subList(0, mid).toMutableList()
                it.add(sorted.subList(mid, sorted.size).toMutableList())
            }
        }

        return buckets.map { bucket -> averageColor(bucket, pixels) }
    }

    private fun channelValue(color: Int, channel: Int): Int = when (channel) {
        0 -> (color shr 16) and 0xFF
        1 -> (color shr 8) and 0xFF
        else -> color and 0xFF
    }

    private fun widestChannel(bucket: List<Int>, pixels: IntArray): Int {
        val ranges = IntArray(3)
        for (channel in 0..2) {
            var min = 255
            var max = 0
            for (index in bucket) {
                val value = channelValue(pixels[index], channel)
                if (value < min) min = value
                if (value > max) max = value
            }
            ranges[channel] = max - min
        }
        return ranges.indices.maxByOrNull { ranges[it] } ?: 0
    }

    private fun bucketRange(bucket: List<Int>, pixels: IntArray): Int {
        val channel = widestChannel(bucket, pixels)
        var min = 255
        var max = 0
        for (index in bucket) {
            val value = channelValue(pixels[index], channel)
            if (value < min) min = value
            if (value > max) max = value
        }
        return max - min
    }

    private fun averageColor(bucket: List<Int>, pixels: IntArray): Int {
        var r = 0L
        var g = 0L
        var b = 0L
        for (index in bucket) {
            val color = pixels[index]
            r += (color shr 16) and 0xFF
            g += (color shr 8) and 0xFF
            b += color and 0xFF
        }
        val size = bucket.size.coerceAtLeast(1)
        return (0xFF shl 24) or
            (((r / size).toInt() and 0xFF) shl 16) or
            (((g / size).toInt() and 0xFF) shl 8) or
            ((b / size).toInt() and 0xFF)
    }

    private fun nearestColor(color: Int, palette: List<Int>): Int {
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF

        var best = palette[0]
        var bestDistance = Int.MAX_VALUE
        for (candidate in palette) {
            val cr = (candidate shr 16) and 0xFF
            val cg = (candidate shr 8) and 0xFF
            val cb = candidate and 0xFF
            val distance = (r - cr) * (r - cr) + (g - cg) * (g - cg) + (b - cb) * (b - cb)
            if (distance < bestDistance) {
                bestDistance = distance
                best = candidate
            }
        }
        return best
    }
}
