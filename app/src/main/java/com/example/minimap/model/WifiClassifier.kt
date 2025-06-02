package com.example.minimap.model

import android.content.Context
import android.util.Log
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class WifiClassifier(context: Context) {

    private val interpreter: Interpreter by lazy {
        val options = Interpreter.Options()
        options.setUseXNNPACK(true) // Accélération CPU
        Interpreter(loadModelFile(context), options)
    }


    private fun loadModelFile(context: Context): MappedByteBuffer {
        val assetDescriptor = context.assets.openFd("wifi_classifier.tflite")
        val fileInputStream = FileInputStream(assetDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset = assetDescriptor.startOffset
        val declaredLength = assetDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun predictSecurityLevel(features: FloatArray): WifiSecurityLevel {
        val input = Array(1) { features }
        val output = Array(1) { FloatArray(3) } // 3 classes: SAFE, MEDIUM, DANGEROUS

        try {
            interpreter.run(input, output)
        } catch (e: Exception) {
            Log.e("WifiClassifier", "Erreur d'inférence", e)
            WifiSecurityLevel.MEDIUM // Valeur par défaut
        }

        return when (output[0].indices.maxByOrNull { output[0][it] }) {
            2 -> WifiSecurityLevel.SAFE
            1 -> WifiSecurityLevel.MEDIUM
            else -> WifiSecurityLevel.DANGEROUS
        }
    }
}