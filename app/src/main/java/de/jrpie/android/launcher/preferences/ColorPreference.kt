package de.jrpie.android.launcher.preferences

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.widget.EditText
import android.widget.SeekBar
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.alpha
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import androidx.core.graphics.toColorInt
import androidx.preference.Preference
import de.jrpie.android.launcher.R

class ColorPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    @Suppress("unused")
    constructor(context: Context) : this(context, null)

    private var selectedColor = Color.WHITE

    init {
        isPersistent = true
        selectedColor = getPersistedInt(selectedColor)
        summary = selectedColor.getHex()
    }

    override fun onClick() {
        showDialog()
    }

    @ColorInt
    override fun onGetDefaultValue(a: TypedArray, index: Int): Int {
        return a.getInt(index, selectedColor)
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        selectedColor = getPersistedInt(selectedColor)
        summary = selectedColor.getHex()
    }

    private fun showDialog() {
        var currentColor = getPersistedInt(selectedColor)

        AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
            setView(R.layout.dialog_choose_color)
            setTitle(R.string.dialog_choose_color_title)
            setPositiveButton(android.R.string.ok) { _, _ ->
                persistInt(currentColor)
                summary = currentColor.getHex()
            }
            setNegativeButton(R.string.dialog_cancel) { _, _ -> }
        }.create().also { it.show() }.apply {
            val preview = findViewById<EditText>(R.id.dialog_select_color_preview)

            val red = findViewById<SeekBar>(R.id.dialog_select_color_seekbar_red)
            val green = findViewById<SeekBar>(R.id.dialog_select_color_seekbar_green)
            val blue = findViewById<SeekBar>(R.id.dialog_select_color_seekbar_blue)
            val alpha = findViewById<SeekBar>(R.id.dialog_select_color_seekbar_alpha)

            val updateColor = { updateText: Boolean ->
                preview?.setTextColor(currentColor.foregroundTextColor())
                preview?.setBackgroundColor(currentColor)
                if (updateText) {
                    preview?.setText(currentColor.getHex(), TextView.BufferType.EDITABLE)
                }
                red?.progress = currentColor.red
                green?.progress = currentColor.green
                blue?.progress = currentColor.blue
                alpha?.progress = currentColor.alpha
            }
            updateColor(true)

            preview?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {}
                override fun afterTextChanged(editable: Editable?) {
                    preview.hasFocus() || return
                    val newText = editable?.toString() ?: return
                    newText.isBlank() && return
                    try {
                        val newColor = newText.toColorInt()
                        currentColor = newColor
                        updateColor(false)
                    } catch (_: IllegalArgumentException) {
                    }
                }
            })
            red?.setOnSeekBarChangeListener(SeekBarChangeListener {
                currentColor = currentColor.updateRed(it)
                updateColor(true)
            })
            green?.setOnSeekBarChangeListener(SeekBarChangeListener {
                currentColor = currentColor.updateGreen(it)
                updateColor(true)
            })
            blue?.setOnSeekBarChangeListener(SeekBarChangeListener {
                currentColor = currentColor.updateBlue(it)
                updateColor(true)
            })
            alpha?.setOnSeekBarChangeListener(SeekBarChangeListener {
                currentColor = currentColor.updateAlpha(it)
                updateColor(true)
            })
        }
    }


    private class SeekBarChangeListener(val update: (Int) -> Unit) :
        SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, v: Int, fromUser: Boolean) {
            fromUser || return
            update(v)
        }

        override fun onStartTrackingTouch(p0: SeekBar?) {}
        override fun onStopTrackingTouch(p0: SeekBar?) {}
    }

    companion object {
        fun @receiver:ColorInt Int.getHex(): String {
            return "#%08X".format(this)
        }

        @ColorInt
        fun @receiver:ColorInt Int.updateRed(red: Int): Int {
            return Color.argb(this.alpha, red, this.green, this.blue)
        }

        @ColorInt
        fun @receiver:ColorInt Int.updateGreen(green: Int): Int {
            return Color.argb(this.alpha, this.red, green, this.blue)
        }

        @ColorInt
        fun @receiver:ColorInt Int.updateBlue(blue: Int): Int {
            return Color.argb(this.alpha, this.red, this.green, blue)
        }

        @ColorInt
        fun @receiver:ColorInt Int.updateAlpha(alpha: Int): Int {
            return Color.argb(alpha, this.red, this.green, this.blue)
        }

        @ColorInt
        fun @receiver:ColorInt Int.foregroundTextColor(): Int {
            // https://stackoverflow.com/a/3943023
            return if (
                this.red * 0.299 + this.green * 0.587 + this.blue * 0.114
                > this.alpha / 256f * 150
            ) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        }
    }
}