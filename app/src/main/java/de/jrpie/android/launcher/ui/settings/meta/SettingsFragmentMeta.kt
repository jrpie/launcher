package de.jrpie.android.launcher.ui.settings.meta

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import de.jrpie.android.launcher.BuildConfig
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.copyToClipboard
import de.jrpie.android.launcher.databinding.SettingsMetaBinding
import de.jrpie.android.launcher.getDeviceInfo
import de.jrpie.android.launcher.openInBrowser
import de.jrpie.android.launcher.openTutorial
import de.jrpie.android.launcher.preferences.ImportResult
import de.jrpie.android.launcher.preferences.exportConfig
import de.jrpie.android.launcher.preferences.importConfig
import de.jrpie.android.launcher.preferences.resetPreferences
import de.jrpie.android.launcher.ui.LegalInfoActivity
import de.jrpie.android.launcher.ui.UIObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * The [SettingsFragmentMeta] is a used as a tab in the SettingsActivity.
 *
 * It is used to change settings and access resources about Launcher,
 * that are not directly related to the behaviour of the app itself.
 *
 * (greek `meta` = above, next level)
 */
class SettingsFragmentMeta : Fragment(), UIObject {

    private lateinit var binding: SettingsMetaBinding

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { outputStream ->
                exportConfig(requireContext(), outputStream)
            }
            Toast.makeText(
                requireContext(),
                R.string.settings_meta_export_success,
                Toast.LENGTH_SHORT
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                R.string.settings_meta_export_error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private val importLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@registerForActivityResult
        try {
            requireContext().contentResolver.openInputStream(uri)?.use { inputStream ->
                val result = importConfig(requireContext(), inputStream)
                when (result) {
                    is ImportResult.Success -> {
                        Toast.makeText(
                            requireContext(),
                            R.string.settings_meta_import_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        requireActivity().recreate()
                    }
                    is ImportResult.Error -> {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.settings_meta_import_error, result.message),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                getString(R.string.settings_meta_import_error, e.message),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SettingsMetaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super<Fragment>.onStart()
        super<UIObject>.onStart()
    }

    override fun setOnClicks() {

        fun bindURL(view: View, urlRes: Int) {
            view.setOnClickListener {
                openInBrowser(
                    getString(urlRes),
                    requireContext()
                )
            }
        }

        binding.settingsMetaButtonViewTutorial.setOnClickListener {
            openTutorial(requireContext())
        }

        // export settings
        binding.settingsMetaButtonExportSettings.setOnClickListener {
            val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            exportLauncher.launch("ulauncher-config-$dateStr.json")
        }

        // import settings
        binding.settingsMetaButtonImportSettings.setOnClickListener {
            AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                .setTitle(getString(R.string.settings_meta_import))
                .setMessage(getString(R.string.settings_meta_import_confirm))
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    importLauncher.launch(arrayOf("application/json"))
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }

        // prompting for settings-reset confirmation
        binding.settingsMetaButtonResetSettings.setOnClickListener {
            AlertDialog.Builder(this.requireContext(), R.style.AlertDialogCustom)
                .setTitle(getString(R.string.settings_meta_reset))
                .setMessage(getString(R.string.settings_meta_reset_confirm))
                .setPositiveButton(
                    android.R.string.ok
                ) { _, _ ->
                    resetPreferences(this.requireContext())
                    requireActivity().finish()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }


        // view code
        bindURL(binding.settingsMetaButtonViewCode, R.string.settings_meta_link_github)

        // view documentation
        bindURL(binding.settingsMetaButtonViewDocs, R.string.settings_meta_link_docs)

        // report a bug
        binding.settingsMetaButtonReportBug.setOnClickListener {
            val deviceInfo = getDeviceInfo()
            AlertDialog.Builder(context, R.style.AlertDialogCustom).apply {
                setView(R.layout.dialog_report_bug)
                setTitle(R.string.dialog_report_bug_title)
                setPositiveButton(R.string.dialog_report_bug_create_report) { _, _ ->
                    openInBrowser(
                        getString(R.string.settings_meta_report_bug_link),
                        requireContext()
                    )
                }
                setNegativeButton(R.string.dialog_cancel) { _, _ -> }
            }.create().also { it.show() }.apply {
                val info = findViewById<TextView>(R.id.dialog_report_bug_device_info)
                val buttonClipboard = findViewById<Button>(R.id.dialog_report_bug_button_clipboard)
                val buttonSecurity = findViewById<Button>(R.id.dialog_report_bug_button_security)
                info.text = deviceInfo
                buttonClipboard.setOnClickListener {
                    copyToClipboard(requireContext(), deviceInfo)
                }
                info.setOnClickListener {
                    copyToClipboard(requireContext(), deviceInfo)
                }
                buttonSecurity.setOnClickListener {
                    openInBrowser(
                        getString(R.string.settings_meta_report_vulnerability_link),
                        requireContext()
                    )
                }
            }
        }

        // join chat
        bindURL(binding.settingsMetaButtonJoinChat, R.string.settings_meta_chat_url)

        // contact developer
        // bindURL(binding.settingsMetaButtonContact, R.string.settings_meta_contact_url)

        // contact fork developer
        bindURL(binding.settingsMetaButtonForkContact, R.string.settings_meta_fork_contact_url)

        // donate
        bindURL(binding.settingsMetaButtonDonate, R.string.settings_meta_donate_url)

        // privacy policy
        bindURL(binding.settingsMetaButtonPrivacy, R.string.settings_meta_privacy_url)

        // legal info
        binding.settingsMetaButtonLicenses.setOnClickListener {
            startActivity(Intent(this.context, LegalInfoActivity::class.java))
        }

        // version
        binding.settingsMetaTextVersion.text = BuildConfig.VERSION_NAME
        binding.settingsMetaTextVersion.setOnClickListener {
            val deviceInfo = getDeviceInfo()
            copyToClipboard(requireContext(), deviceInfo)
        }

    }
}
