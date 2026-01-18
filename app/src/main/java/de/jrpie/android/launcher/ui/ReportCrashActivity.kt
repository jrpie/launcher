package de.jrpie.android.launcher.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.copyToClipboard
import de.jrpie.android.launcher.databinding.ActivityReportCrashBinding
import de.jrpie.android.launcher.getDeviceInfo
import de.jrpie.android.launcher.openInBrowser
import de.jrpie.android.launcher.writeEmail

const val EXTRA_CRASH_LOG = "crashLog"

class ReportCrashActivity : AppCompatActivity() {
    // We don't know what caused the crash, so this Activity should use as little functionality as possible.
    // In particular it is not a UIObject (and hence looks quite ugly)
    private lateinit var binding: ActivityReportCrashBinding
    private var report: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise layout
        binding = ActivityReportCrashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.report_crash_title)
        setSupportActionBar(binding.reportCrashAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)

        report = intent.getStringExtra(EXTRA_CRASH_LOG)

        binding.reportCrashButtonCopy.setOnClickListener {
            copyToClipboard(
                this,
                "Device Info:\n${getDeviceInfo()}\n\nCrash Log:\n${report}"
            )
        }

        binding.reportCrashButtonMail.setOnClickListener {
            writeEmail(
                this,
                getString(R.string.settings_meta_report_bug_mail),
                "Crash in μLauncher",
                "Hi!\nUnfortunately, μLauncher crashed:\n" +
                        "\nDevice Info\n\n${getDeviceInfo()}\n\n" +
                        "\nCrash Log\n\n${report}\n" +
                        "\nAdditional Information\n\n" +
                        "[Please add additional information: What did you do when the crash happened? Do you know how to trigger it? ... ]"
            )
        }
        binding.reportCrashButtonReport.setOnClickListener {
            openInBrowser(
                getString(R.string.settings_meta_report_bug_link),
                this
            )
        }
    }
}