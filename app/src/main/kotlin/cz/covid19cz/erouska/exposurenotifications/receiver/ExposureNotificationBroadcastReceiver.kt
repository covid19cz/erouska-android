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
package cz.covid19cz.erouska.exposurenotifications.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import cz.covid19cz.erouska.exposurenotifications.ExposureNotificationsRepository
import cz.covid19cz.erouska.net.ExposureServerRepository
import cz.covid19cz.erouska.utils.L
import org.koin.core.KoinComponent
import org.koin.core.inject

/**
 * Broadcast receiver for callbacks from exposure notification API.
 */
class ExposureNotificationBroadcastReceiver : BroadcastReceiver(), KoinComponent {

    val exposureNotificationsRepository : ExposureNotificationsRepository by inject()
    val exposureServerRepository : ExposureServerRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED) {
            val token = intent.getStringExtra(ExposureNotificationClient.EXTRA_TOKEN)

            L.d("Exposure state updated, token: $token")
        } else if (intent.action == ExposureNotificationClient.ACTION_EXPOSURE_NOT_FOUND){
            L.d("Exposure not found")
        }
    }
}