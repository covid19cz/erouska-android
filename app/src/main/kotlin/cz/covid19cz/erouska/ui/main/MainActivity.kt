package cz.covid19cz.erouska.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.ActivityRagnarokBinding
import cz.covid19cz.erouska.ui.base.BaseActivity
import cz.covid19cz.erouska.ui.base.UrlEvent
import cz.covid19cz.erouska.ui.ragnarok.RagnarokVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_ragnarok.*

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityRagnarokBinding, RagnarokVM>(R.layout.activity_ragnarok, RagnarokVM::class) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(UrlEvent::class){
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it.url)))
        }
        buttonMoreInfo.setOnLongClickListener {
            Toast.makeText(this,"eRagnarök", Toast.LENGTH_LONG).show()
            true
        }
    }
}