package com.example.velocity_recorder.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.velocity_recorder.db.AppDatabase
import com.example.velocity_recorder.utils.ClockUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter

class ExportDataActivity : AppCompatActivity() {

    private var rideId: Long = -1L
    private var startText: String = ""
    private var endText: String = ""

    private val dataDao by lazy { AppDatabase.getDatabase(this).dataDao() }

    private val createDocumentActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                RESULT_OK -> if (it.data?.data != null) {
                    writeInFile(it.data?.data!!)
                }

                RESULT_CANCELED -> {}
            }
            finish()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        rideId = intent.getLongExtra("ride_id", -1L)
        startText = fixTimeForFilename(intent.getStringExtra("start_text"), "start")
        endText = fixTimeForFilename(intent.getStringExtra("end_text"), "end")

        if (rideId != -1L) {
            createFile()
        } else {
            finish()
        }
    }

    private fun fixTimeForFilename(timeText: String?, defaultText: String): String {
        if (timeText == null) {
            return defaultText
        }
        return timeText
            .replace("/", "-")
            .replace(":", "-")
            .replace(" ", "T")
    }

    // create text file
    private fun createFile() {
        // when you create document, you need to add Intent.ACTION_CREATE_DOCUMENT
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)

        // filter to only show openable items.
        intent.addCategory(Intent.CATEGORY_OPENABLE)

        // Create a file with the requested Mime type
        intent.type = "text/csv"
        intent.putExtra(Intent.EXTRA_TITLE, "ride #$rideId - $startText to $endText.csv")

        createDocumentActivityResultLauncher.launch(intent)
    }

    private fun writeInFile(uri: Uri) {
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            val bw = BufferedWriter(OutputStreamWriter(outputStream))

            bw.write("timestamp,latitude degree,longitude degree,velocity in m/s")
            CoroutineScope(Dispatchers.IO).launch {
                dataDao.getVelocities(rideId).forEach {
                    bw.newLine()
                    val timestampStr = ClockUtils.convertLongToString(it.timestamp)
                    bw.write("$timestampStr,${it.latitude},${it.longitude},${it.velocity}")
                }
                withContext(Dispatchers.IO) {
                    bw.close()
                    Log.d("AppLog", "finish export ride data for ride $rideId")
                }
            }
        } catch (e: IOException) {
            Log.d("AppLog", "failed to export ride data, IOException: ${e.stackTrace}")
        }
    }

    companion object {
        fun open(
            context: Context,
            rideId: Long,
            startText: String,
            endText: String
        ) {
            val intent = Intent(context, ExportDataActivity::class.java).also {
                it.putExtra("ride_id", rideId)
                it.putExtra("start_text", startText)
                it.putExtra("end_text", endText)
            }
            context.startActivity(intent)
        }
    }
}