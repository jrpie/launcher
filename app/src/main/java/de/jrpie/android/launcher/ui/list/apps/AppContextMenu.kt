package de.jrpie.android.launcher.ui.list.apps

import android.app.Service
import android.content.pm.LauncherApps
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isNotEmpty
import androidx.core.view.isVisible
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.actions.ShortcutAction
import de.jrpie.android.launcher.apps.AbstractDetailedAppInfo
import de.jrpie.android.launcher.apps.AppInfo
import de.jrpie.android.launcher.apps.DetailedAppInfo
import de.jrpie.android.launcher.apps.PinnedShortcutInfo
import de.jrpie.android.launcher.databinding.AppContextMenuBinding
import de.jrpie.android.launcher.databinding.AppContextMenuShortcutBinding
import de.jrpie.android.launcher.getAppShortcuts
import de.jrpie.android.launcher.preferences.LauncherPreferences
import de.jrpie.android.launcher.preferences.list.ListLayout
import de.jrpie.android.launcher.ui.setTooltipAndContentDescription
import de.jrpie.android.launcher.ui.transformMonochrome
import de.jrpie.android.launcher.ui.settings.DialogBindToGesture

fun AppContextMenuBinding.bindApp(
    appInfo: AbstractDetailedAppInfo,
    activity: AppCompatActivity,
    dismiss: () -> Unit
) {
    bindActions(appInfo, activity, dismiss)
    bindToggles(appInfo, dismiss)
    bindShortcuts(appInfo, activity, dismiss)
    appMenuDivider.isVisible = appMenuShortcuts.isNotEmpty()
}

private fun AppContextMenuBinding.bindActions(
    appInfo: AbstractDetailedAppInfo,
    activity: AppCompatActivity,
    dismiss: () -> Unit
) {
    appMenuTopInfo.apply {
        setTooltipAndContentDescription(R.string.list_app_info)
        isVisible = appInfo is DetailedAppInfo
        setOnClickListener { _ ->
            (appInfo.getRawInfo() as? AppInfo)?.openSettings(activity)
            dismiss()
        }
    }

    appMenuTopDelete.apply {
        setTooltipAndContentDescription(R.string.list_app_delete)
        isVisible = appInfo.isRemovable()
        setOnClickListener { _ ->
            appInfo.getRawInfo().uninstall(activity)
            dismiss()
        }
    }

    appMenuTopRename.apply {
        setTooltipAndContentDescription(R.string.list_app_rename)
        setOnClickListener { _ ->
            appInfo.showRenameDialog(activity)
        }
    }
}

private fun AppContextMenuBinding.bindToggles(
    appInfo: AbstractDetailedAppInfo,
    dismiss: () -> Unit
) {
    appMenuTopFavorites.apply {
        if (LauncherPreferences.apps().favorites()?.contains(appInfo.getRawInfo()) == true) {
            setTooltipAndContentDescription(R.string.list_app_favorite_remove)
            appMenuTopFavoritesIcon.setImageResource(R.drawable.baseline_favorite_24)
        } else {
            setTooltipAndContentDescription(R.string.list_app_favorite_add)
            appMenuTopFavoritesIcon.setImageResource(R.drawable.baseline_favorite_border_24)
        }

        setOnClickListener { _ ->
            appInfo.getRawInfo().toggleFavorite()
            dismiss()
        }
    }

    appMenuTopHide.apply {
        if (LauncherPreferences.apps().hidden()?.contains(appInfo.getRawInfo()) == true) {
            setTooltipAndContentDescription(R.string.list_app_hidden_remove)
            appMenuTopHideIcon.setImageResource(R.drawable.baseline_visibility_24)
        } else {
            setTooltipAndContentDescription(R.string.list_app_hidden_add)
            appMenuTopHideIcon.setImageResource(R.drawable.baseline_visibility_off_24)
        }

        setOnClickListener { _ ->
            appInfo.getRawInfo().toggleHidden(this@bindToggles.root)
            dismiss()
        }
    }
}

private fun AppContextMenuBinding.bindShortcuts(
    appInfo: AbstractDetailedAppInfo,
    activity: AppCompatActivity,
    dismiss: () -> Unit
) {
    appMenuShortcuts.removeAllViews()
    if (appInfo !is DetailedAppInfo || Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return

    val colorTheme = LauncherPreferences.theme().colorTheme()
    val grayscale = colorTheme.monochromeIcons()
    val showIcons = LauncherPreferences.list().layout() != ListLayout.TEXT
    val launcherApps = activity.getSystemService(Service.LAUNCHER_APPS_SERVICE) as LauncherApps

    getAppShortcuts(appInfo.getRawInfo(), activity).forEach { shortcutInfo ->
        val shortcut = AppContextMenuShortcutBinding.inflate(
            activity.layoutInflater,
            appMenuShortcuts,
            true
        )
        shortcut.appMenuShortcutLabel.text = shortcutInfo.shortLabel
        if (showIcons) {
            shortcut.appMenuShortcutIcon.setImageDrawable(
                launcherApps.getShortcutBadgedIconDrawable(shortcutInfo, 0)?.apply {
                    if (isDarkAndColorless()) {
                        setTint(shortcut.appMenuShortcutLabel.currentTextColor)
                    } else {
                        transformMonochrome(grayscale, colorTheme)
                    }
                }
            )
        } else {
            shortcut.appMenuShortcutIcon.isVisible = false
        }

        shortcut.root.setOnClickListener { _ ->
            launcherApps.startShortcut(shortcutInfo, null, null)
            dismiss()
        }
        shortcut.root.setOnLongClickListener { _ ->
            DialogBindToGesture(activity) { gesture ->
                LauncherPreferences.getSharedPreferences().edit {
                    ShortcutAction(PinnedShortcutInfo(shortcutInfo))
                        .bindToGesture(this, gesture.id)
                }
            }
            true
        }
    }
}

private fun Drawable.isDarkAndColorless(): Boolean {
    val pixels = IntArray(ICON_SAMPLE_SIZE * ICON_SAMPLE_SIZE)
    toBitmap(ICON_SAMPLE_SIZE, ICON_SAMPLE_SIZE, Bitmap.Config.ARGB_8888)
        .getPixels(pixels, 0, ICON_SAMPLE_SIZE, 0, 0, ICON_SAMPLE_SIZE, ICON_SAMPLE_SIZE)
    var opaque = 0
    var brightness = 0
    for (pixel in pixels) {
        if (Color.alpha(pixel) < OPAQUE_ALPHA) continue
        val r = Color.red(pixel)
        val g = Color.green(pixel)
        val b = Color.blue(pixel)
        if (maxOf(r, g, b) - minOf(r, g, b) > MAX_CHROMA) return false
        brightness += (r + g + b) / 3
        opaque++
    }
    return opaque > 0 && brightness / opaque < DARK_BRIGHTNESS
}

private const val ICON_SAMPLE_SIZE = 24
private const val OPAQUE_ALPHA = 128
private const val MAX_CHROMA = 32
private const val DARK_BRIGHTNESS = 128
