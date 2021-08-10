package com.udacity

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.udacity.databinding.ActivityDetailBinding
import timber.log.Timber

class DetailActivity : AppCompatActivity() {

    private lateinit var bind: ActivityDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bind = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        bind.lifecycleOwner = this
        bind.executePendingBindings()
        setSupportActionBar(bind.toolbar)

        if (intent.extras?.containsKey(MainActivity.ARG_DOWNLOAD_COMPLETED) == true &&
            intent.extras?.containsKey(MainActivity.ARG_DOWNLOAD_FILE) == true
        ) {
            val isCompleted =
                intent.extras?.getBoolean(MainActivity.ARG_DOWNLOAD_COMPLETED) ?: false
            val fileSelected = intent.extras?.getString(MainActivity.ARG_DOWNLOAD_FILE, "") ?: ""

            when (fileSelected) {
                MainActivity.FileDownload.GLIDE.name -> bind.vContentDetail.tvFileName.setText(R.string.glide_option)
                MainActivity.FileDownload.LOADAPP.name -> bind.vContentDetail.tvFileName.setText(R.string.loadapp_option)
                MainActivity.FileDownload.RETROFIT.name -> bind.vContentDetail.tvFileName.setText(R.string.retrofit_option)
            }

            if (isCompleted) {
                bind.vContentDetail.tvStatus.setText(R.string.success)
                bind.vContentDetail.tvStatus.setTextColor(Color.GREEN)
            } else {
                bind.vContentDetail.tvStatus.setText(R.string.fail)
                bind.vContentDetail.tvStatus.setTextColor(Color.RED)
            }
        } else {
            Timber.e("Missing argument ARG_DOWNLOAD_COMPLETED or ARG_DOWNLOAD_FILE")
        }
        bind.vContentDetail.btnDone.setOnClickListener { finish() }
    }

}
