package cz.covid19cz.erouska.ui.update.efgs

import androidx.hilt.lifecycle.ViewModelInject
import arch.viewmodel.BaseArchViewModel
import cz.covid19cz.erouska.AppConfig
import cz.covid19cz.erouska.db.SharedPrefsRepository

class EfgsUpdateVM @ViewModelInject constructor(
     val sharedPrefsRepository: SharedPrefsRepository
) : BaseArchViewModel() {

     fun efgsDays() = AppConfig.efgsDays
     fun efgsSupportedCountries() = AppConfig.efgsSupportedCountries
}