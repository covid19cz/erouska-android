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

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import java.util.*

/**
 * An exposure element for display in the exposures UI.
 *
 *
 * Partners should implement a daily TTL/expiry, for on-device storage of this data, and must
 * ensure compliance with all applicable laws and requirements with respect to encryption, storage,
 * and retention polices for end user data.
 */
@Entity
class ExposureEntity internal constructor(
    /**
     * The dateMillisSinceEpoch provided by the ExposureInformation in the Exposure Notifications
     * API.
     *
     *
     * Represents a date of an exposure in millis since epoch rounded to the day.
     */
    @field:ColumnInfo(name = "date_millis_since_epoch") var dateMillisSinceEpoch: Long,
    /**
     * The timestamp in millis since epoch for when the exposure notification status update was
     * received.
     */
    @field:ColumnInfo(name = "received_timestamp_ms") var receivedTimestampMs: Long
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val that = o as ExposureEntity
        return id == that.id && dateMillisSinceEpoch == that.dateMillisSinceEpoch && receivedTimestampMs == that.receivedTimestampMs
    }

    override fun hashCode(): Int {
        return Objects.hash(id, dateMillisSinceEpoch, receivedTimestampMs)
    }

    override fun toString(): String {
        return "ExposureEntity{" +
                "id=" + id +
                ", dateMillisSinceEpoch=" + dateMillisSinceEpoch +
                "(" + Instant.ofEpochMilli(dateMillisSinceEpoch) + ")" +
                ", receivedTimestampMs=" + receivedTimestampMs +
                "(" + Instant.ofEpochMilli(receivedTimestampMs) + ")" +
                '}'
    }

    companion object {
        /**
         * Creates an ExposureEntity.
         *
         * @param dateMillisSinceEpoch the date of an exposure in millis since epoch rounded to the day of
         * the detected exposure
         * @param receivedTimestampMs  the timestamp in milliseconds since epoch for when the exposure was
         * received by the app
         */
        fun create(dateMillisSinceEpoch: Long, receivedTimestampMs: Long): ExposureEntity {
            return ExposureEntity(dateMillisSinceEpoch, receivedTimestampMs)
        }
    }

}