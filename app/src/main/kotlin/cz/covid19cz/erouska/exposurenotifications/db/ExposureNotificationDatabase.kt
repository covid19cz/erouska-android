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

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Defines the sqlite database for the room persistence API.
 *
 *
 * Partners should implement a daily TTL/expiry, for on-device storage of this data, and must
 * ensure compliance with all applicable laws and requirements with respect to encryption, storage,
 * and retention polices for end user data.
 */
@Database(entities = [PositiveDiagnosisEntity::class, ExposureEntity::class], version = 9, exportSchema = false)
@TypeConverters(ZonedDateTimeTypeConverter::class)
abstract class ExposureNotificationDatabase : RoomDatabase() {
    abstract fun positiveDiagnosisDao(): PositiveDiagnosisDao
    abstract fun exposureDao(): ExposureDao

    companion object {
        const val DATABASE_NAME = "database"
    }
}