package com.princetronics.softdeck

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity() : AppCompatActivity() {

    //    private final String DEVICE_NAME="MyBTBee";
    private val DEVICE_ADDRESS = "7C:66:9D:9A:B0:26"
    private val PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")//Serial Port Service ID
    private var startButton: Button? = null
    private var sendButton:Button? = null
    private var clearButton:Button? = null
    private var stopButton: Button? = null
    private var textView: TextView? = null
    private var editText: EditText? = null
    private var deviceConnected = false
    private var thread: Thread? = null
    private var buffer: ByteArray? = null
    private var bufferPosition: Int = 0
    private var stopThread: Boolean = false
    private var device: BluetoothDevice? = null
    private var socket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Example of a call to a native method
        //sample_text.text = stringFromJNI()

        startButton = findViewById<Button>(R.id.buttonStart)
        sendButton = findViewById<Button>(R.id.buttonSend)
        clearButton = findViewById<Button>(R.id.buttonClear)
        stopButton = findViewById<Button>(R.id.buttonStop)
        editText = findViewById<EditText>(R.id.editText)
        textView = findViewById<Button>(R.id.textView)
        setUiEnabled(false)
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {

        // Used to load the 'native-lib' library on application startup.
        init {
            System.loadLibrary("native-lib")
        }
    }

    fun setUiEnabled(bool: Boolean) {
        startButton?.setEnabled(!bool)
        sendButton?.setEnabled(bool)
        stopButton?.setEnabled(bool)
        textView?.setEnabled(bool)

    }

    fun BTinit(): Boolean {
        var found = false
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show()
        }
        if (!bluetoothAdapter!!.isEnabled) {
            val enableAdapter = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableAdapter, 0)
            try {
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
        val bondedDevices = bluetoothAdapter.bondedDevices
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show()
        } else {
            for (iterator in bondedDevices) {
                if (iterator.address == DEVICE_ADDRESS) {
                    device = iterator
                    found = true
                    break
                }
            }
        }
        return found
    }

    fun BTconnect(): Boolean {
        var connected = true
        try {
            socket = device?.createRfcommSocketToServiceRecord(PORT_UUID)
            socket?.connect()
        } catch (e: IOException) {
            e.printStackTrace()
            connected = false
        }

        if (connected) {
            try {
                outputStream = socket?.getOutputStream()
            } catch (e: IOException) {
                e.printStackTrace()
            }

            try {
                inputStream = socket?.getInputStream()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }


        return connected
    }

    fun onClickStart(view: View) {
        if (BTinit()) {
            if (BTconnect()) {
                setUiEnabled(true)
                deviceConnected = true
                beginListenForData()
                textView?.append("\nConnection Opened!\n")
            }

        }
    }

    internal fun beginListenForData() {
        val handler = Handler()
        stopThread = false
        buffer = ByteArray(1024)
        val thread = Thread(Runnable {
            while (!Thread.currentThread().isInterrupted && !stopThread) {
                try {
                    val byteCount = inputStream?.available()
                    if (byteCount != null) {
                        if (byteCount > 0) {
                            val rawBytes = ByteArray(byteCount)
                            inputStream?.read(rawBytes)
                            //val string = String(rawBytes, "UTF-8")
                            val string: String? = rawBytes.toString()
                            handler.post { textView?.append(string) }
                        }
                    }
                } catch (ex: IOException) {
                    stopThread = true
                }

            }
        })

        thread.start()
    }

    fun onClickSend(view: View) {
        val string = editText?.getText().toString()
        string + "\n"
        try {
            outputStream?.write(string.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
        }

        textView?.append("\nSent Data:" + string + "\n")

    }

    @Throws(IOException::class)
    fun onClickStop(view: View) {
        stopThread = true
        outputStream?.close()
        inputStream?.close()
        socket?.close()
        setUiEnabled(false)
        deviceConnected = false
        textView?.append("\nConnection Closed!\n")
    }

    fun onClickClear(view: View) {
        textView?.setText("")
    }
}
