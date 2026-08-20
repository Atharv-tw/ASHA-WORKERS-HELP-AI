package com.asha.worker.ai.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.asha.worker.ai.R
import com.asha.worker.ai.data.AppDatabase
import com.asha.worker.ai.data.AshaRepository
import com.asha.worker.ai.planner.BriefingGenerator
import java.util.concurrent.TimeUnit

/**
 * Builds the day's ranked plan and posts it as a local notification. Runs entirely
 * offline. Scheduled once (daily) via [schedule].
 */
class MorningBriefingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repo = AshaRepository(AppDatabase.get(applicationContext))
        val message = BriefingGenerator.briefing(repo.todaysPlan())
        postNotification(message)
        return Result.success()
    }

    private fun postNotification(message: String) {
        val ctx = applicationContext
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mgr.createNotificationChannel(
                NotificationChannel(CHANNEL_ID, "सुबह की योजना", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("आज की योजना")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()
        try {
            NotificationManagerCompat.from(ctx).notify(NOTIF_ID, notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS not granted on API 33+; nothing else to do offline.
        }
    }

    companion object {
        private const val CHANNEL_ID = "asha_briefing"
        private const val NOTIF_ID = 1001
        const val WORK_NAME = "morning_briefing"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<MorningBriefingWorker>(1, TimeUnit.DAYS).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request
            )
        }
    }
}
