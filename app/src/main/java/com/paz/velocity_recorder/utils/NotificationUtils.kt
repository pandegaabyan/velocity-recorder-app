/*
 * Copyright 2022 Prasanna Anbazhagan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paz.velocity_recorder.utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.paz.velocity_recorder.MainActivity
import com.paz.velocity_recorder.R

object NotificationUtils {

    private const val FOREGROUND_CHANNEL_ID = "Velocity Recorder"
    private const val REQ_CODE_OPEN_ACTIVITY = 0
    const val VELOCITY_RECORDER_NOTIFICATION_ID = 1

    private fun checkAndCreateChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            context.getSystemService(NotificationManager::class.java)?.let { notificationManager ->

                if (notificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {

                    val serviceChannel = NotificationChannel(
                        FOREGROUND_CHANNEL_ID,
                        FOREGROUND_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_HIGH
                    )

                    notificationManager.createNotificationChannel(serviceChannel)
                }
            }
        }

    }

    fun getNotification(
        context: Context,
    ): Notification {

        checkAndCreateChannel(context)

        val notificationIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            REQ_CODE_OPEN_ACTIVITY,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder = NotificationCompat.Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notif_title))
            .setContentText(context.getString(R.string.notif_text))
            .setWhen(System.currentTimeMillis())
            .setSmallIcon(R.drawable.ic_speedometer_white_24dp)
            .setContentIntent(pendingIntent)

        return notificationBuilder.build()
    }
}