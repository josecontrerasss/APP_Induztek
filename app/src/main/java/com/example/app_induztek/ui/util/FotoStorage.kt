package com.example.app_induztek.ui.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File

object FotoStorage {

    fun guardarJpeg(context: Context, bitmap: Bitmap): String? {
        return try {
            val file = File(context.filesDir, "foto_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                out.flush()
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
}
