package cz.covid19cz.erouska.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cz.covid19cz.erouska.ui.main.ShortcutsManager

class ShortcutActivity : AppCompatActivity() {

    private val shortcutsManager = ShortcutsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            shortcutsManager.handleShortcut(it)
        }

        finish() // hide this activity ASAP
    }
}