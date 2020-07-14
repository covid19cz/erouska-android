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
import org.threeten.bp.ZonedDateTime
import java.util.*

/**
 * A positive diagnosis inputted by the user.
 *
 *
 * Partners should implement a daily TTL/expiry, for on-device storage of this data, and must
 * ensure compliance with all applicable laws and requirements with respect to encryption, storage,
 * and retention polices for end user data.
 */
@Entity
class PositiveDiagnosisEntity(testTimestamp: ZonedDateTime, shared: Boolean) {
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    @ColumnInfo(name = "created_timestamp_ms")
    var createdTimestampMs: Long = System.currentTimeMillis()

    @ColumnInfo(name = "test_timestamp")
    var testTimestamp: ZonedDateTime = testTimestamp

    @ColumnInfo(name = "shared")
    var isShared: Boolean = shared

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val entity = o as PositiveDiagnosisEntity
        return id == entity.id && createdTimestampMs == entity.createdTimestampMs && isShared == entity.isShared && testTimestamp == entity.testTimestamp
    }

    override fun hashCode(): Int {
        return Objects.hash(id, createdTimestampMs, testTimestamp, isShared)
    }
}