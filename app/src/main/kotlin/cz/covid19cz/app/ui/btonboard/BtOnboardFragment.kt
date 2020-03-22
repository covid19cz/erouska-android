package cz.covid19cz.app.ui.btonboard

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.tbruyelle.rxpermissions2.RxPermissions
import cz.covid19cz.app.R
import cz.covid19cz.app.databinding.FragmentBtOnboardBinding
import cz.covid19cz.app.ui.base.BaseFragment
import cz.covid19cz.app.ui.btonboard.event.BtOnboardCommandEvent
import io.reactivex.disposables.CompositeDisposable

class BtOnboardFragment :
    BaseFragment<FragmentBtOnboardBinding, BtOnboardVM>(R.layout.fragment_bt_onboard, BtOnboardVM::class) {

    private lateinit var rxPermissions: RxPermissions
    private val compositeDisposable = CompositeDisposable()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rxPermissions = RxPermissions(this)

        subscribe(BtOnboardCommandEvent::class) {
            when (it.command) {
                BtOnboardCommandEvent.Command.ENABLE_BT -> enableBluetooth()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        enableUpInToolbar(true)
    }

    override fun onDestroy() {
        compositeDisposable.dispose()
        super.onDestroy()
    }

    fun enableBluetooth() {
        requestEnableBt()
    }

    override fun onBluetoothEnabled() {
        compositeDisposable.add(rxPermissions
            .request(Manifest.permission.ACCESS_FINE_LOCATION)
            .subscribe { granted: Boolean ->
                if (granted) {
                    navigate(R.id.action_nav_bt_onboard_to_nav_login)
                } else {
                    //TODO: better dialog and navigate to settings
                    Toast.makeText(context, "Povolte přístup k poloze", Toast.LENGTH_LONG)
                        .show()
                }
            })
    }
}