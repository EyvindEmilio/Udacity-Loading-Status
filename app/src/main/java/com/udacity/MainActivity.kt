package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityMainBinding
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private var isDownloading = false

    private lateinit var bind: ActivityMainBinding

    enum class FileDownload { GLIDE, LOADAPP, RETROFIT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_main)
        bind.lifecycleOwner = this
        bind.executePendingBindings()
        Timber.plant(Timber.DebugTree())

        setSupportActionBar(bind.toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        bind.vContentMain.btnLoading.setOnClickListener {
            if (!isDownloading) {
                when (bind.vContentMain.rgOptions.checkedRadioButtonId) {
                    R.id.rbGlide -> {
                        bind.vContentMain.btnLoading.startProgressAnimation()
                        download(FileDownload.GLIDE)
                    }
                    R.id.rbLoadApp -> {
                        bind.vContentMain.btnLoading.startProgressAnimation()
                        download(FileDownload.LOADAPP)
                    }
                    R.id.rbRetrofit -> {
                        bind.vContentMain.btnLoading.startProgressAnimation()
                        download(FileDownload.RETROFIT)
                    }
                    else -> {
                        Toast.makeText(
                            this, R.string.select_an_option_to_download, Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    this, R.string.wait_to_finish_download, Toast.LENGTH_SHORT
                ).show()
            }
        }

        bind.vContentMain.btnLoading.loadingListener = object : LoadingButton.LoadingListener {
            override fun onStart() {
                bind.vContentMain.btnLoading.setText(getString(R.string.button_loading))
            }

            override fun onProgress(progress: Float) {
                bind.vContentMain.btnLoading.setText(getString(R.string.button_loading))
            }

            override fun onCompleted() {
                bind.vContentMain.btnLoading.setText(getString(R.string.button_name))
            }
        }

        createChannel()
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Timber.d("onReceive() intent=$intent")

            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
            if (intent?.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                bind.vContentMain.btnLoading.finishProgressAnimation()
                val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
                val cursor = downloadManager.query(DownloadManager.Query().setFilterById(id))

                if (cursor != null && cursor.moveToNext()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val title =
                        cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE))

                    cursor.close()

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        launchNotification(true, title)
                        isDownloading = false
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        launchNotification(false, title)
                        isDownloading = false
                    }
                }
            }
        }
    }

    private fun download(fileDownload: FileDownload) {
        Timber.d("download()")
        val request = DownloadManager.Request(Uri.parse(URL))
            .setTitle(fileDownload.name)
            .setDescription(getString(R.string.app_description))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "")

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        isDownloading = true
    }

    private fun launchNotification(isCompleted: Boolean, title: String) {
        Timber.d("launchNotification() isCompleted=$isCompleted, title=$title")
        val notificationManager = ContextCompat.getSystemService(
            this,
            NotificationManager::class.java
        ) as NotificationManager

        val contentIntent = Intent(applicationContext, DetailActivity::class.java).apply {
            putExtra(ARG_DOWNLOAD_COMPLETED, isCompleted)
            putExtra(ARG_DOWNLOAD_FILE, title)
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0,
            contentIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val action = NotificationCompat.Action.Builder(
            null,
            getString(R.string.notification_button),
            pendingIntent
        )

        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_assistant_black_24dp)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_description))
            .setAutoCancel(true)
            .addAction(action.build())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.BLUE
            notificationChannel.enableVibration(true)
            notificationChannel.description = "Download File"
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    companion object {
        private const val URL =
            "https://github.com/udacity/nd940-c3-advanced-android-programming-project-starter/archive/master.zip"
        private const val CHANNEL_ID = "udacity_course"
        private const val CHANNEL_NAME = "Udacity Course"
        private const val NOTIFICATION_ID = 0
        const val ARG_DOWNLOAD_COMPLETED = "ARG_DOWNLOAD_COMPLETED"
        const val ARG_DOWNLOAD_FILE = "ARG_DOWNLOAD_FILE"
    }

}
