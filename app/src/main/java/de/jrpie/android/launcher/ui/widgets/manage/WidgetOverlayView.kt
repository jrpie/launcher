package de.jrpie.android.launcher.ui.widgets.manage

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.graphics.toRectF
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.updateWidget


private const val HANDLE_SIZE = 100
private const val HANDLE_EDGE_SIZE = (1.2 * HANDLE_SIZE).toInt()

/**
 * An overlay to show configuration options for a widget in [WidgetManagerView]
 */
class WidgetOverlayView : ViewGroup {

    private val paint = Paint()
    private val handlePaint = Paint()
    private val selectedHandlePaint = Paint()

    private val popupAnchor = View(context)

    var mode: WidgetManagerView.EditMode? = null

    class Handle(val mode: WidgetManagerView.EditMode, val position: Rect)
    init {
        addView(popupAnchor)
        setWillNotDraw(false)
        handlePaint.style = Paint.Style.STROKE
        handlePaint.color = Color.WHITE
        handlePaint.strokeWidth = 2f
        handlePaint.setShadowLayer(10f,0f,0f, Color.BLACK)

        selectedHandlePaint.style = Paint.Style.FILL_AND_STROKE
        selectedHandlePaint.setARGB(100, 255, 255, 255)
        handlePaint.setShadowLayer(10f,0f,0f, Color.BLACK)

        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.setShadowLayer(10f,0f,0f, Color.BLACK)
    }

    private var preview: Drawable? = null
    var widgetId: Int = -1
        set(newId) {
            field = newId
            preview = Widget.byId(widgetId)?.getPreview(context)
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    )

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

        canvas.drawRoundRect(bounds.toRectF(), 5f, 5f, paint)

        if (mode == null) {
            return
        }
        //preview?.bounds = bounds
        //preview?.draw(canvas)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        popupAnchor.layout(0,0,0,0)
    }

    fun showPopupMenu() {
        val widget = Widget.byId(widgetId)?: return
        val menu = PopupMenu(context, popupAnchor)
        menu.menu.let {
            it.add(
                context.getString(R.string.widget_menu_remove)
            ).setOnMenuItemClickListener { _ ->
                Widget.byId(widgetId)?.delete(context)
                return@setOnMenuItemClickListener true
            }
            it.add(
                if (widget.allowInteraction) {
                    context.getString(R.string.widget_menu_disable_interaction)
                } else {
                    context.getString(R.string.widget_menu_enable_interaction)
                }
            ).setOnMenuItemClickListener { _ ->
                    widget.allowInteraction = !widget.allowInteraction
                    updateWidget(widget)
                    return@setOnMenuItemClickListener true
            }
        }
        menu.show()
    }

    fun getHandles(): List<Handle> {
        return listOf(
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