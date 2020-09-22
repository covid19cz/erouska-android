package cz.covid19cz.erouska

import android.content.Context
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.DiagnosisKeysDataMapping
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.Infectiousness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExposureNotificationClient(@ApplicationContext context: Context): ExposureNotificationClient {
        return Nearby.getExposureNotificationClient(context).apply {

            val daysList = AppConfig.daysSinceOnsetToInfectiousness
            val daysToInfectiousness = mutableMapOf<Int, Int>()
            for (i in -14..14) {
                daysToInfectiousness[i] = daysList[i + 14]
            }

            val mapping = DiagnosisKeysDataMapping.DiagnosisKeysDataMappingBuilder()
                .setDaysSinceOnsetToInfectiousness(daysToInfectiousness)
                .setInfectiousnessWhenDaysSinceOnsetMissing(Infectiousness.NONE)
                .setReportTypeWhenMissing(AppConfig.reportTypeWhenMissing)
                .build()
            setDiagnosisKeysDataMapping(mapping)
        }
    }
}





