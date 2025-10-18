package de.jrpie.android.launcher.ui

import android.os.Bundle
import android.view.MenuItem
import de.jrpie.android.launcher.R
import de.jrpie.android.launcher.databinding.LegalInfoBinding
import de.jrpie.android.launcher.ui.UIObjectActivity

class LegalInfoActivity : UIObjectActivity() {
    private lateinit var binding: LegalInfoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialise layout
        binding = LegalInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setTitle(R.string.legal_info_title)
        setSupportActionBar(binding.legalInfoAppbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
}