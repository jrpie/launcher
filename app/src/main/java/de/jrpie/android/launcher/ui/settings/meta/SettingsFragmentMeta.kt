package de.jrpie.android.launcher.ui.settings.meta

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.copyToClipboard
import de.jrpie.android.launcher.getDeviceInfo
import de.jrpie.android.launcher.openInBrowser
import de.jrpie.android.launcher.openTutorial
import de.jrpie.android.launcher.preferences.resetPreferences
import de.jrpie.android.launcher.ui.LegalInfoActivity
import de.jrpie.android.launcher.ui.UIObject

/**
 * The [SettingsFragmentMeta] is a used as a tab in the SettingsActivity.
 *
 * It is used to change settings and access resources about Launcher,
 * that are not directly related to the behaviour of the app itself.
 *
 * (greek `meta` = above, next level)
 */
class SettingsFragmentMeta : Fragment(), UIObject {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    SettingsMetaScreen(
                        context = requireContext(),
                        onResetConfirmed = { requireActivity().finish() }
                    )
                }
            }
        }
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun setOnClicks() {
        // No longer needed as click handlers are defined in Compose
    }
}

// Data class to represent a settings action
private data class SettingsAction(
    val textResId: Int,
    val onClick: (Context) -> Unit
)

// Composable for the settings meta screen
@Composable
fun SettingsMetaScreen(
    context: Context,
    onResetConfirmed: () -> Unit
) {
    val openAlertDialog = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SettingsButtonList(context, openAlertDialog)
        if (openAlertDialog.value) {
            AlertDialogResetSettings(
                onDismissRequest = { openAlertDialog.value = false },
                onConfirmation = {
                    openAlertDialog.value = false
                    resetPreferences(context)
                    onResetConfirmed()
                },
                dialogTitle = stringResource(R.string.settings_meta_reset),
                dialogText = stringResource(R.string.settings_meta_reset_confirm),
                icon = Icons.Default.Warning
            )
        }
    }
}

// Composable for the scrollable button list and version number
@Composable
private fun SettingsButtonList(
    context: Context,
    openAlertDialog: MutableState<Boolean>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val actions = listOf(
            SettingsAction(R.string.settings_meta_show_tutorial) { openTutorial(it) },
            SettingsAction(R.string.settings_meta_reset) { openAlertDialog.value = true },
            SettingsAction(R.string.settings_meta_view_code) {
                openInBrowser(it.getString(R.string.settings_meta_link_github), it)
            },
            SettingsAction(R.string.settings_meta_report_bug) {
                openInBrowser(it.getString(R.string.settings_meta_report_bug_link), it)
            },
            SettingsAction(R.string.settings_meta_join_chat) {
                openInBrowser(it.getString(R.string.settings_meta_chat_url), it)
            },
            SettingsAction(R.string.settings_meta_fork_contact) {
                openInBrowser(it.getString(R.string.settings_meta_fork_contact_url), it)
            },
            SettingsAction(R.string.settings_meta_donate) {
                openInBrowser(it.getString(R.string.settings_meta_donate_url), it)
            },
            SettingsAction(R.string.settings_meta_privacy) {
                openInBrowser(it.getString(R.string.settings_meta_privacy_url), it)
            },
            SettingsAction(R.string.settings_meta_licenses) {
                it.startActivity(Intent(it, LegalInfoActivity::class.java))
            }
        )

        actions.forEachIndexed { index, action ->
            SettingsButton(
                text = stringResource(action.textResId),
                onClick = { action.onClick(context) }
            )
            if (index == 1 || index == 3 || index == 6) {
                SettingsButtonSpacer()
            }
        }
        // Version number at the bottom of buttons
        Text(
            text = BuildConfig.VERSION_NAME,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            color = colorResource(R.color.finnmglasTheme_text_color),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, end = 8.dp)
                .clickable {
                    val deviceInfo = getDeviceInfo()
                    copyToClipboard(context, deviceInfo)
                }
        )
        Spacer(modifier = Modifier.height(48.dp))
    }
}

// Composable for a settings button
@Composable
fun SettingsButton(
    text: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(8.dp),
        shape = RoundedCornerShape(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorResource(R.color.finnmglasTheme_accent_color),
            contentColor = colorResource(R.color.finnmglasTheme_text_color)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = colorResource(R.color.finnmglasTheme_text_color)
        )
    }
}

// Composable for spacing between buttons
@Composable
fun SettingsButtonSpacer() {
    Spacer(modifier = Modifier.height(16.dp))
}

// Composable for the reset settings alert dialog
@Composable
fun AlertDialogResetSettings(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(android.R.string.dialog_alert_title),
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(android.R.string.cancel))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    )
}