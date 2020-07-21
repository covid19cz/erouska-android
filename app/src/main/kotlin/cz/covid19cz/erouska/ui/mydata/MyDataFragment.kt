package cz.covid19cz.erouska.ui.mydata

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import arch.adapter.RecyclerLayoutStrategy
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.R
import cz.covid19cz.erouska.databinding.FragmentMyDataBinding
import cz.covid19cz.erouska.ui.base.BaseFragment
import cz.covid19cz.erouska.ui.help.InfoType.DATA_COLLECTION
import kotlinx.android.synthetic.main.fragment_my_data.*

class MyDataFragment :
    BaseFragment<FragmentMyDataBinding, MyDataVM>(R.layout.fragment_my_data, MyDataVM::class) {

}
