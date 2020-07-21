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
@Database(entities = [PositiveDiagnosisEntity::class, ExposureEntity::class], version = 1, exportSchema = false)
@TypeConverters(ZonedDateTimeTypeConverter::class)
internal abstract class ExposureNotificationDatabase : RoomDatabase() {
    abstract fun positiveDiagnosisDao(): PositiveDiagnosisDao?
    abstract fun exposureDao(): ExposureDao?

    companion object {
        private const val DATABASE_NAME = "exposurenotification"

        @Volatile
        // Singleton pattern.
        private var INSTANCE: ExposureNotificationDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ExposureNotificationDatabase? {
            if (INSTANCE == null) {
                INSTANCE = buildDatabase(context)
            }
            return INSTANCE
        }

        private fun buildDatabase(context: Context): ExposureNotificationDatabase {
            return Room.databaseBuilder(
                context.applicationContext, ExposureNotificationDatabase::class.java, DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}