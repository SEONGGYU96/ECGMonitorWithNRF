package com.seoultech.ecgmonitor.bpm

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import com.seoultech.ecgmonitor.bpm.data.HeartBeatSample
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class SampleStorageManagerImpl(private val context: Context) : SampleStorageManager {

    companion object {
        private const val TAG = "SampleStorageManager"
        private const val TIME_STAMP_PERIOD = 60000L
        private const val DISCONNECT_STAMP = -1
        private const val EOP = "e"
        private const val FILE_NAME_DATE_FORMAT = "yy-MM-dd"
        private const val STOP_SIGNAL = -1
    }
    private val dateFormat = SimpleDateFormat(FILE_NAME_DATE_FORMAT, Locale.KOREA)

    private var backUpFileStream: FileOutputStream? = null
    private var currentFileName = getFileName()

    private var timeStamp = 0L

    private var saveSampleHandlerThread: HandlerThread? = null
    private var saveSampleHandler: Handler? = null

    override fun startSave() {
        backUpFileStream = getFileStream()
        saveSampleHandlerThread = HandlerThread("saveSample").apply { start() }
        saveSampleHandler = Handler(saveSampleHandlerThread!!.looper) {
            if (it.arg1 == STOP_SIGNAL) {
                saveSampleHandlerThread?.quit()
                stopSave()
            } else {
                dispatchSampleData(it.obj as HeartBeatSample)
            }
            return@Handler true
        }
    }

    override fun saveSample(sample: Float, time: Long) {
        val sampleData = HeartBeatSample.obtain().apply {
            this.value = sample
            this.time = time
        }

        saveSampleHandler?.sendMessage(Message.obtain().apply { obj = sampleData }) ?: run {
            errorLog()
        }
    }

    override fun safeStopSave() {
        saveSampleHandler?.sendMessage(Message.obtain().apply { arg1 = STOP_SIGNAL }) ?: kotlin.run {
            errorLog()
        }
    }

    private fun stopSave() {
        backUpFileStream?.use {
            it.write("$DISCONNECT_STAMP ".toByteArray())
            it.close()
        } ?: run {
            errorLog()
        }
        context.openFileInput(getFileName()).bufferedReader().useLines { lines ->
            Log.d(TAG, lines.fold("file_read_test") { some, text ->
                "$some\n$text"
            })
        }
    }

    private fun getFileName(): String {
        return dateFormat.format(Date())
    }

    private fun isSameFile(): Boolean {
        return currentFileName == getFileName()
    }

    private fun refreshCurrentFileName() {
        currentFileName = getFileName()
    }

    private fun refreshFileStream() {
        backUpFileStream?.use {
            it.write("$EOP ".toByteArray())
            it.close()
        } ?: run {
            errorLog()
        }
        backUpFileStream = getFileStream()
    }

    private fun getFileStream(): FileOutputStream {
        return context.openFileOutput(getFileName(), Context.MODE_APPEND)
    }

    private fun errorLog() {
        Log.e(TAG, "backUpFileStream is null")
    }

    private fun dispatchSampleData(sampleData: HeartBeatSample) {
        if (!isSameFile()) {
            refreshFileStream()
            refreshCurrentFileName()
        }
        if (timeStamp == 0L || abs(timeStamp - sampleData.time) >= TIME_STAMP_PERIOD) {
            timeStamp = sampleData.time
            backUpFileStream?.write("${sampleData.time} ".toByteArray()) ?: run { errorLog() }
        }
        backUpFileStream?.write("${sampleData.value} ".toByteArray()) ?: run { errorLog() }
        sampleData.recycle()
    }
}