package de.jrpie.android.launcher.widgets

import android.graphics.Rect
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.roundToInt

const val GRID_SIZE: Short = 12

@Serializable
data class WidgetPosition(var x: Short, var y: Short, var width: Short, var height: Short) {

    constructor(rect: Rect) : this(
        rect.left.toShort(),
        rect.top.toShort(),
        (rect.right - rect.left).toShort(),
        (rect.bottom - rect.top).toShort()
    )

    fun toRect(): Rect {
        return Rect(x.toInt(), y.toInt(), x + width, y + height)
    }

    fun getAbsoluteRect(screenWidth: Int, screenHeight: Int): Rect {
        val gridWidth = screenWidth / GRID_SIZE.toFloat()
        val gridHeight = screenHeight / GRID_SIZE.toFloat()

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
            val gridHeight = screenHeight / GRID_SIZE.toFloat()

            val x = (absolute.left / gridWidth).roundToInt().toShort()
                .coerceIn(0, (GRID_SIZE - 1).toShort())
            val y = (absolute.top / gridHeight).roundToInt().toShort()
                .coerceIn(0, (GRID_SIZE - 1).toShort())


            val w = max(2, ((absolute.right - absolute.left) / gridWidth).roundToInt()).toShort()
            val h = max(2, ((absolute.bottom - absolute.top) / gridHeight).roundToInt()).toShort()

            return WidgetPosition(x, y, w, h)

        }

        fun center(
            minWidth: Int,
            minHeight: Int,
            screenWidth: Int,
            screenHeight: Int
        ): WidgetPosition {
            val gridWidth = screenWidth / GRID_SIZE.toFloat()
            val gridHeight = screenHeight / GRID_SIZE.toFloat()

            val cellsWidth = ceil(minWidth / gridWidth).toInt().toShort()
            val cellsHeight = ceil(minHeight / gridHeight).toInt().toShort()

            return WidgetPosition(
                ((GRID_SIZE - cellsWidth) / 2).toShort(),
                ((GRID_SIZE - cellsHeight) / 2).toShort(),
                cellsWidth,
                cellsHeight
            )
        }

        fun findFreeSpace(
            widgetPanel: WidgetPanel?,
            minWidth: Int,
            minHeight: Int
        ): WidgetPosition {
            val rect = Rect(0, 0, minWidth, minHeight)
            if (widgetPanel == null) {
                return WidgetPosition(rect)
            }

            val widgets = widgetPanel.getWidgets().map { it.position.toRect() }

            for (x in 0..<GRID_SIZE - minWidth) {
                rect.left = x
                rect.right = x + minWidth
                for (y in 0..<GRID_SIZE - minHeight) {
                    rect.top = y
                    rect.bottom = y + minHeight
                    if (!widgets.any { Rect(it).intersect(rect) }) {
                        return WidgetPosition(rect)
                    }
                }
            }
            return WidgetPosition(0, 0, minWidth.toShort(), minHeight.toShort())
        }
    }
}