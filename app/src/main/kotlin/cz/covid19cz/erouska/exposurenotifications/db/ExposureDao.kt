/*
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package cz.covid19cz.erouska.exposurenotifications.db

import androidx.room.*
import com.google.android.gms.nearby.exposurenotification.ExposureWindow
import kotlinx.coroutines.flow.Flow

/**
 * Dao for the bucket [ExposureEntity] in the exposure notification database.
 */
@Dao
abstract class ExposureDao {
    @Query("SELECT * FROM ExposureEntity ORDER BY date_millis_since_epoch DESC")
    abstract suspend fun getAll() : MutableList<ExposureEntity>

    @get:Query("SELECT * FROM ExposureEntity ORDER BY date_millis_since_epoch DESC")
    abstract val getAllLive: Flow<List<ExposureEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(entities: List<ExposureEntity?>?)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: ExposureEntity?)

    @Query("DELETE FROM ExposureEntity")
    abstract suspend fun deleteAll()

    /**
     * Adds missing exposures based on the current windows state.
     *
     * @param exposureWindows the [ExposureWindow]s
     * @return if any exposure was added
     */
    @Transaction
    open suspend fun refreshWithExposureWindows(exposureWindows: List<ExposureWindow>): Boolean {
        // Keep track of the exposures already handled and remove them when we find matching windows.
        val exposureEntities: MutableList<ExposureEntity> = getAll()
        var somethingAdded = false
        for (exposureWindow in exposureWindows) {
            var found = false
            for (i in exposureEntities.indices) {
                if (exposureEntities[i].dateMillisSinceEpoch == exposureWindow.dateMillisSinceEpoch
                ) {
                    exposureEntities.removeAt(i)
                    found = true
                    break
                }
            }
            if (!found) {
                // No existing ExposureEntity with the given date, must add an entity for this window.
                somethingAdded = true
                upsert(
                    ExposureEntity
                        .create(exposureWindow.dateMillisSinceEpoch, System.currentTimeMillis())
                )
            }
        }
        return somethingAdded
    }
}