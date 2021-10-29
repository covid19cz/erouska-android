package cz.covid19cz.erouska

import android.content.Context
import androidx.room.Room
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import cz.covid19cz.erouska.db.DailySummariesDb
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideExposureNotificationClient(@ApplicationContext context: Context): ExposureNotificationClient {
        return Nearby.getExposureNotificationClient(context)
    }

    @Provides
    @Singleton
    fun provideDailySummariesDb(@ApplicationContext context: Context): DailySummariesDb {
        return Room.databaseBuilder(
            context.applicationContext,
            DailySummariesDb::class.java, "daily_summaries"
        ).build()
    }


}





