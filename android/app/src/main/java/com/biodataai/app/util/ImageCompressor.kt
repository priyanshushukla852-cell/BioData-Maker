package com.biodataai.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File

object ImageCompressor {
    private const val MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024 // 5MB
    private const val MAX_DIMENSION_PX = 2048
    private const val INITIAL_QUALITY = 85

    data class CompressionResult(
        val success: Boolean,
        val compressedUri: Uri? = null,
        val fileSizeBytes: Long = 0,
        val error: String? = null
    )

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    suspend fun compressImage(context: Context, imageUri: Uri): CompressionResult {
        return withContext(Dispatchers.Default) {
            try {
                // Pre-check file size
                val cursor = context.contentResolver.query(imageUri, arrayOf(android.provider.MediaStore.MediaColumns.SIZE), null, null, null)
                val fileSize = if (cursor != null) {
                    cursor.use {
                        if (it.moveToFirst()) it.getLong(0) else -1L
                    }
                } else {
                    -1L
                }
                if (fileSize > MAX_FILE_SIZE_BYTES * 10) { // Allow 50MB max before compression
                    return@withContext CompressionResult(false, error = "File is too large. Maximum 5MB allowed after compression.")
                }

                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext CompressionResult(false, error = "Cannot open image")

                // Decode bounds first to avoid OOM
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream.close()

                // Calculate inSampleSize for downsampling during decode
                val inSampleSize = calculateInSampleSize(options, MAX_DIMENSION_PX, MAX_DIMENSION_PX)
                val inputStream2 = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext CompressionResult(false, error = "Cannot open image")
                val decodeOptions = BitmapFactory.Options().apply {
                    this.inSampleSize = inSampleSize
                }
                val originalBitmap = BitmapFactory.decodeStream(inputStream2, null, decodeOptions)
                    ?: return@withContext CompressionResult(false, error = "Cannot decode image")
                inputStream2.close()

                // Scale down if necessary
                val scaledBitmap = if (originalBitmap.width > MAX_DIMENSION_PX || originalBitmap.height > MAX_DIMENSION_PX) {
                    val scale = maxOf(
                        originalBitmap.width.toFloat() / MAX_DIMENSION_PX,
                        originalBitmap.height.toFloat() / MAX_DIMENSION_PX
                    )
                    val newWidth = (originalBitmap.width / scale).toInt().coerceAtLeast(1)
                    val newHeight = (originalBitmap.height / scale).toInt().coerceAtLeast(1)
                    Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
                } else {
                    originalBitmap
                }

                // Compress to JPEG with quality reduction loop (max 20 iterations)
                var quality = INITIAL_QUALITY
                var compressedBytes: ByteArray
                var iterations = 0
                do {
                    val stream = ByteArrayOutputStream()
                    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
                    compressedBytes = stream.toByteArray()
                    stream.close()
                    quality -= 5
                    iterations++
                } while (compressedBytes.size > MAX_FILE_SIZE_BYTES && quality > 10 && iterations < 20)

                if (compressedBytes.size > MAX_FILE_SIZE_BYTES) {
                    return@withContext CompressionResult(
                        false,
                        error = "Image is too large. Please choose a smaller or lower-resolution image."
                    )
                }

                // Write compressed image to cache file
                val cacheDir = context.cacheDir
                val compressedFile = File(cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
                compressedFile.writeBytes(compressedBytes)

                scaledBitmap.recycle()
                if (scaledBitmap !== originalBitmap) {
                    originalBitmap.recycle()
                }

                CompressionResult(
                    success = true,
                    compressedUri = Uri.fromFile(compressedFile),
                    fileSizeBytes = compressedBytes.size.toLong()
                )
            } catch (e: Exception) {
                CompressionResult(false, error = "Unable to process image. Please try another photo.")
            }
        }
    }
}
