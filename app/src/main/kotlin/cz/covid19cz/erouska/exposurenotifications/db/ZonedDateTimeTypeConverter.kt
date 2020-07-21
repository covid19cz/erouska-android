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

import androidx.room.TypeConverter
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

/**
 * TypeConverters for converting to and from [ZonedDateTime] instances.
 */
class ZonedDateTimeTypeConverter {
    private val sFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

    @TypeConverter
    fun toOffsetDateTime(timestamp: String?): ZonedDateTime? {
        return if (timestamp != null) {
            sFormatter.parse(timestamp, ZonedDateTime.FROM)
        } else {
            null
        }
    }

    @TypeConverter
    fun fromOffsetDateTime(timestamp: ZonedDateTime?): String? {
        return timestamp?.format(sFormatter)
    }
}