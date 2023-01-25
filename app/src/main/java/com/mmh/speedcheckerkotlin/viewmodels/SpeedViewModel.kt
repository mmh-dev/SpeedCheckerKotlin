package com.mmh.speedcheckerkotlin.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mmh.speedcheckerkotlin.utils.toMbs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

const val DOWNLOAD_URL = "https://uk.loadingtest.com/image31.jpg"

class SpeedViewModel : ViewModel() {

    private val _currentSpeed = MutableStateFlow("")
    val currentSpeed: StateFlow<String> = _currentSpeed
    private val _isJobCancelled = MutableStateFlow(false)
    val isJobCancelled: StateFlow<Boolean> = _isJobCancelled
    private var resetDownloadSize = false
    private var consolidatedSpeedList = ArrayList<Int>()
    private var downloadedSizes = arrayListOf(0,0,0)

    fun getSpeeds() {
        consolidatedSpeedList.clear()
        _isJobCancelled.value = false
        downloadedSizes = arrayListOf(0,0,0)

        val timer = object : CountDownTimer(10000, 1000) {
            override fun onTick(l: Long) {
                val totalDownloadedSize = downloadedSizes[0] + downloadedSizes[1] + downloadedSizes[2]
                consolidatedSpeedList.add(totalDownloadedSize)
                _currentSpeed.value = totalDownloadedSize.toMbs()
                resetDownloadSize = true
            }

            override fun onFinish() {
                _isJobCancelled.value = true
                _currentSpeed.value = consolidatedSpeedList.max().toMbs()
                viewModelScope.cancel()
            }
        }
        timer.start()

        startCoroutine(0)
        startCoroutine(1)
        startCoroutine(2)
    }

    private fun startCoroutine(index: Int) {
        var input: InputStream? = null
        viewModelScope.launch {
            launch(Dispatchers.IO) {
                try {
                    val url = URL(DOWNLOAD_URL)
                    val conn: HttpURLConnection = url.openConnection() as HttpURLConnection
                    conn.apply {
                        requestMethod = "GET"
                        connectTimeout = 1000
                        readTimeout = 1000
                    }
                    if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                        conn.connect()
                        val buffer = ByteArray(1024)
                        var bufferLength = 0
                        input = conn.inputStream

                        while (input!!.read(buffer) > 0) {
                            bufferLength = input!!.read(buffer)
                            if (resetDownloadSize) {
                                downloadedSizes[index] = 0
                                resetDownloadSize = false
                            }
                            downloadedSizes[index] += bufferLength
                        }
                    } else {
                        conn.disconnect()
                        Log.e("server no response: ", conn.responseCode.toString())
                    }

                } catch (e: Exception) {
                    Log.e("error: ", e.message.toString())
                } finally {
                    input?.close()
                }
            }
        }
    }
}