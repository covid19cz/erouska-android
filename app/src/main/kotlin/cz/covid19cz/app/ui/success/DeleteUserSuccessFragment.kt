package cz.covid19cz.app.ui.success

import android.view.MenuItem
import cz.covid19cz.app.R

class DeleteUserSuccessFragment : SuccessFragment() {
    override val title = R.string.delete_user_success_text

    override fun onClose() {
        navigate(R.id.action_nav_delete_user_success_to_nav_welcome_fragment)
    }

    override fun onBackPressed(): Boolean {
        onClose()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onClose()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}