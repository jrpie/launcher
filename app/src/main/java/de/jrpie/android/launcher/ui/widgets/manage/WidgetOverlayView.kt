package de.jrpie.android.launcher.ui.widgets.manage

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.SeekBar
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toRectF
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.widgets.GRID_SIZE
import de.jrpie.android.launcher.widgets.MIN_WIDGET_SIZE
import de.jrpie.android.launcher.widgets.Widget
import de.jrpie.android.launcher.widgets.WidgetPosition
import de.jrpie.android.launcher.widgets.updateWidget
import kotlin.apply


private const val HANDLE_SIZE = 100
private const val MIN_HANDLE_LENGTH = 50
private const val HANDLE_EDGE_SIZE = (1.2 * HANDLE_SIZE).toInt()
private const val SETTINGS_ICON_SIZE = (0.9 * HANDLE_SIZE).toInt()
private const val SETTINGS_ICON_OFFSET = (HANDLE_EDGE_SIZE - SETTINGS_ICON_SIZE) / 2

/**
 * An overlay to show configuration options for a widget in [WidgetManagerView]
 */
class WidgetOverlayView : ViewGroup {

    private val paint = Paint()
    private val handlePaint = Paint()
    private val selectedHandlePaint = Paint()
    private val interactionDisabledOverlayPaint = Paint()
    private val interactionDisabledOverlayTextPaint = Paint()
    private val interactionDisabledString = context.getString(R.string.widget_overlay_interaction_disabled)
    private val settingsDrawable = ResourcesCompat.getDrawable(resources, R.drawable.baseline_settings_24, context.theme)

    private val popupAnchor = View(context)
    val settingsButton = ImageView(context).also {
        it.setImageDrawable(settingsDrawable)
        it.setOnClickListener { _ -> showPopupMenu() }
        it.contentDescription = context.getString(R.string.content_description_widget_settings)
    }

    var mode: WidgetManagerView.EditMode? = null

    class Handle(val mode: WidgetManagerView.EditMode, val position: Rect)

    init {
        addView(popupAnchor)
        addView(settingsButton)
        setWillNotDraw(false)
        handlePaint.style = Paint.Style.STROKE
        handlePaint.color = Color.WHITE
        handlePaint.strokeWidth = 2f
        handlePaint.setShadowLayer(10f, 0f, 0f, Color.BLACK)

        selectedHandlePaint.style = Paint.Style.FILL_AND_STROKE
        selectedHandlePaint.setARGB(100, 255, 255, 255)
        selectedHandlePaint.setShadowLayer(10f, 0f, 0f, Color.BLACK)

        interactionDisabledOverlayPaint.style = Paint.Style.FILL
        interactionDisabledOverlayPaint.setARGB(100, 0, 0, 0)


        interactionDisabledOverlayTextPaint.style = Paint.Style.FILL
        interactionDisabledOverlayTextPaint.setARGB(150, 255, 255, 255)
        interactionDisabledOverlayTextPaint.textAlign = Align.CENTER
        interactionDisabledOverlayTextPaint.textSize = 60f
        interactionDisabledOverlayTextPaint.isAntiAlias = true
        interactionDisabledOverlayTextPaint.typeface =
            LauncherPreferences.theme().font().getTypeface(context)

        paint.style = Paint.Style.STROKE
        paint.color = Color.WHITE
        paint.setShadowLayer(10f, 0f, 0f, Color.BLACK)
    }

    private var preview: Drawable? = null
    private var interactionDisabled = true
    var widgetId: Int = -1
        set(newId) {
            field = newId
            Widget.byId(widgetId)?.let {
                preview = it.getPreview(context)
                interactionDisabled = !it.allowInteraction
            }
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

        if (interactionDisabled) {
            canvas.drawRoundRect(
                this.getBounds().toRectF(),
                5f,
                5f,
                interactionDisabledOverlayPaint
            )
            val x = (width / 2).toFloat()
            val y =
                (height - interactionDisabledOverlayTextPaint.descent() - interactionDisabledOverlayTextPaint.ascent()) / 2
            canvas.drawText(interactionDisabledString, x, y, interactionDisabledOverlayTextPaint)
        }

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
        popupAnchor.layout(0, 0, 0, 0)
        settingsButton.layout(SETTINGS_ICON_OFFSET, SETTINGS_ICON_OFFSET, SETTINGS_ICON_OFFSET + SETTINGS_ICON_SIZE, SETTINGS_ICON_OFFSET + SETTINGS_ICON_SIZE)
    }

    fun showPopupMenu() {
        val widget = Widget.byId(widgetId) ?: return
        val menu = PopupMenu(context, popupAnchor)
        menu.menu.let { m ->
            m.add(
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
            if (widget.isReconfigurable(context) && context is Activity) {
                m.add(
                    context.getString(R.string.widget_menu_reconfigure)
                ).setOnMenuItemClickListener { _ ->
                    // TODO: use real request code
                    widget.configure(context as Activity, 100)
                    return@setOnMenuItemClickListener true

                }
            }
            m.add(
                context.getString(R.string.widget_menu_raise)
            ).setOnMenuItemClickListener { _ ->
                widget.position.zIndex = ((widget.position.zIndex?:0) + 1).toShort()
                updateWidget(widget)
                return@setOnMenuItemClickListener true
            }
            m.add(
                context.getString(R.string.widget_menu_lower)
            ).setOnMenuItemClickListener { _ ->
                widget.position.zIndex = ((widget.position.zIndex?:0) - 1).toShort()
                updateWidget(widget)
                return@setOnMenuItemClickListener true

            }
            m.add(
                context.getString(R.string.widget_menu_size_position)
            ).setOnMenuItemClickListener { _ ->
                AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
                    setNegativeButton(R.string.dialog_cancel) { _, _ -> }
                    setPositiveButton(R.string.dialog_ok) { dialogInterface, _ ->
                        (dialogInterface as? AlertDialog)?.let { dialog ->
                            val barX = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_x)!!
                            val barY = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_y)!!
                            val barW = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_width)!!
                            val barH = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_height)!!

                            widget.position = WidgetPosition.coerce(
                                barX.progress,
                                barY.progress,
                                barW.progress + MIN_WIDGET_SIZE - 1,
                                barH.progress + MIN_WIDGET_SIZE - 1
                            ).also { it.zIndex = widget.position.zIndex }
                            updateWidget(widget)
                        }
                    }
                    setView(R.layout.dialog_widget_position)
                }.create().also { it.show() }.let { dialog ->
                    val barX = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_x)!!
                    val barY = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_y)!!
                    val barW = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_width)!!
                    val barH = dialog.findViewById<SeekBar>(R.id.dialog_widget_position_height)!!
                    widget.position.let {
                        barX.progress = it.x.toInt()
                        barY.progress = it.y.toInt()
                        barW.progress = it.width.toInt() - MIN_WIDGET_SIZE + 1
                        barH.progress = it.height.toInt() - MIN_WIDGET_SIZE + 1
                    }
                    fun updateBounds() {
                        barX.max = GRID_SIZE - MIN_WIDGET_SIZE
                        barY.max = GRID_SIZE - MIN_WIDGET_SIZE
                        barW.max = GRID_SIZE - barX.progress - MIN_WIDGET_SIZE + 1
                        barH.max = GRID_SIZE - barY.progress - MIN_WIDGET_SIZE + 1
                    }
                    updateBounds()
                    listOf(barX, barY, barW, barH).forEach { bar ->
                        bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                                updateBounds()
                            }
                            override fun onStartTrackingTouch(p0: SeekBar?) {}
                            override fun onStopTrackingTouch(p0: SeekBar?) {}
                        })
                    }
                }
                return@setOnMenuItemClickListener true
            }
            m.add(
                context.getString(R.string.widget_menu_remove)
            ).setOnMenuItemClickListener { _ ->
                Widget.byId(widgetId)?.delete(context)
                return@setOnMenuItemClickListener true
            }
        }
        menu.show()
    }

    fun getHandles(): List<Handle> {
        return listOfNotNull(
            Rect(HANDLE_EDGE_SIZE, 0, width - HANDLE_EDGE_SIZE, HANDLE_SIZE)
                .let {
                    if (it.width() >= MIN_HANDLE_LENGTH) Handle(
                        WidgetManagerView.EditMode.TOP, it
                    ) else null
                },
            Rect(HANDLE_EDGE_SIZE, height - HANDLE_SIZE, width - HANDLE_EDGE_SIZE, height)
                .let {
                    if (it.width() >= MIN_HANDLE_LENGTH) Handle(
                        WidgetManagerView.EditMode.BOTTOM, it
                    ) else null
                },
            Rect(0, HANDLE_EDGE_SIZE, HANDLE_SIZE, height - HANDLE_EDGE_SIZE).let {
                if (it.height() >= MIN_HANDLE_LENGTH) Handle(
                    WidgetManagerView.EditMode.LEFT,
                    it
                ) else null
            },
            Rect(width - HANDLE_SIZE, HANDLE_EDGE_SIZE, width, height - HANDLE_EDGE_SIZE).let {
                if (it.height() >= MIN_HANDLE_LENGTH) Handle(
                    WidgetManagerView.EditMode.RIGHT,
                    it
                ) else null
            }
        )
    }

    private fun getBounds(): Rect {
        return Rect(0, 0, width, height)
    }
}