package de.jrpie.android.launcher.widgets

import android.graphics.Rect
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.max

const val GRID_SIZE: Short = 12

@Serializable
data class WidgetPosition(var x: Short, var y: Short, var width: Short, var height: Short) {

    fun getAbsoluteRect(screenWidth: Int, screenHeight: Int): Rect {
        val gridWidth = screenWidth / GRID_SIZE.toFloat()
        val gridHeight= screenHeight / GRID_SIZE.toFloat()

        return Rect(
            (x * gridWidth).toInt(),
            (y * gridHeight).toInt(),
            ((x + width) * gridWidth).toInt(),
            ((y + height) * gridHeight).toInt()
        )
    }

    companion object {
        fun fromAbsoluteRect(absolute: Rect, screenWidth: Int, screenHeight: Int): WidgetPosition {
            val gridWidth = screenWidth / GRID_SIZE.toFloat()
            val gridHeight= screenHeight / GRID_SIZE.toFloat()

            val x = (absolute.left / gridWidth).roundToInt().toShort().coerceIn(0, (GRID_SIZE-1).toShort())
            val y = (absolute.top / gridHeight).roundToInt().toShort().coerceIn(0, (GRID_SIZE-1).toShort())


            val w = max(2, ((absolute.right - absolute.left) / gridWidth).roundToInt()).toShort()
            val h = max(2, ((absolute.bottom - absolute.top) / gridHeight).roundToInt()).toShort()

            return WidgetPosition(x,y,w,h)

        }

        fun center(minWidth: Int, minHeight: Int, screenWidth: Int, screenHeight: Int): WidgetPosition {
            val gridWidth = screenWidth / GRID_SIZE.toFloat()
            val gridHeight= screenHeight / GRID_SIZE.toFloat()

            val cellsWidth = ceil(minWidth / gridWidth).toInt().toShort()
            val cellsHeight = ceil(minHeight / gridHeight).toInt().toShort()

            return WidgetPosition(
                ((GRID_SIZE - cellsWidth) / 2).toShort(),
                ((GRID_SIZE - cellsHeight) / 2).toShort(),
                cellsWidth,
                cellsHeight
            )

        }
    }
}