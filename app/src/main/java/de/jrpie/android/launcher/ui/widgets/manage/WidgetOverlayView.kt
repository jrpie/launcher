package de.jrpie.android.launcher.ui.widgets.manage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.ContextMenu
import android.view.View
import android.widget.PopupMenu
import androidx.core.graphics.toRectF
import de.jrpie.android.launcher.Application
import de.jrpie.android.launcher.widgets.WidgetInfo
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.deleteAppWidget

/**
 * An overlay to show configuration options for a widget.
 */

private const val HANDLE_SIZE = 100
private const val HANDLE_EDGE_SIZE = (1.2 * HANDLE_SIZE).toInt()
class WidgetOverlayView : View {


    val paint = Paint()
    val handlePaint = Paint()
    val selectedHandlePaint = Paint()
    var mode: WidgetManagerView.EditMode? = null
    class Handle(val mode: WidgetManagerView.EditMode, val position: Rect)
    init {
        handlePaint.style = Paint.Style.STROKE
        handlePaint.setARGB(100, 255, 255, 255)


        selectedHandlePaint.style = Paint.Style.FILL_AND_STROKE
        selectedHandlePaint.setARGB(255, 255, 255, 255)


        paint.style = Paint.Style.STROKE
        paint.setARGB(50, 255, 255, 255)
    }

    private var preview: Drawable? = null
    var widgetId: Int = -1
        get() = field
        set(newId) {
            field = newId
            val appWidgetManager= (context.applicationContext as Application).appWidgetManager

            preview =
                appWidgetManager.getAppWidgetInfo(newId).loadPreviewImage(context, DisplayMetrics.DENSITY_HIGH) ?:
                appWidgetManager.getAppWidgetInfo(newId).loadIcon(context, DisplayMetrics.DENSITY_HIGH)
        }

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init(attrs, defStyle)
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) { }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        getHandles().forEach {
            if (it.mode == mode) {
                canvas.drawRoundRect(it.position.toRectF(), 5f, 5f, selectedHandlePaint)
            } else {
                canvas.drawRoundRect(it.position.toRectF(), 5f, 5f, handlePaint)
            }
        }
        val bounds = getBounds()
        canvas.drawRect(bounds, paint)

        if (mode == null) {
            return
        }

        preview?.bounds = bounds
        preview?.draw(canvas)


    }

    fun showPopupMenu() {
        val menu = PopupMenu(context, this)
        menu.menu.let {
            it.add("Remove").setOnMenuItemClickListener { _ ->
                deleteAppWidget(context, WidgetInfo(widgetId))
                return@setOnMenuItemClickListener true
            }
            it.add("Allow Interaction").setOnMenuItemClickListener { _ ->
                    return@setOnMenuItemClickListener true
                }
            it.add("Add Padding")
        }
        menu.show()
    }

    fun getHandles(): List<Handle> {
        return listOf<Handle>(
            Handle(WidgetManagerView.EditMode.TOP,
                Rect(HANDLE_EDGE_SIZE, 0, width - HANDLE_EDGE_SIZE, HANDLE_SIZE)),
            Handle(WidgetManagerView.EditMode.BOTTOM,
                Rect(HANDLE_EDGE_SIZE, height - HANDLE_SIZE, width - HANDLE_EDGE_SIZE, height)),
            Handle(WidgetManagerView.EditMode.LEFT,
                Rect(0, HANDLE_EDGE_SIZE, HANDLE_SIZE, height - HANDLE_EDGE_SIZE)),
            Handle(WidgetManagerView.EditMode.RIGHT,
                Rect(width - HANDLE_SIZE, HANDLE_EDGE_SIZE, width, height - HANDLE_EDGE_SIZE))
        )

    }

    private fun getBounds(): Rect {
        return Rect(0,0, width, height)
    }

}