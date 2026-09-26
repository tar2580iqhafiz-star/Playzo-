package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.annotation.DrawableRes
import com.example.R
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.util.Locale

data class SelectedVideoMetadata(
    val persistedVideoUri: String,
    val extractedThumbnailUri: String?,
    val formattedDuration: String?
)

object ResourceHelper {
    @DrawableRes
    fun getDrawableResId(context: Context, resName: String?): Int {
        if (resName.isNullOrBlank()) {
            return R.drawable.playzo_icon_1790327564914
        }
        val id = context.resources.getIdentifier(resName, "drawable", context.packageName)
        return if (id != 0) id else R.drawable.playzo_icon_1790327564914
    }

    /**
     * Persists a device-selected video URI locally and extracts its real frame thumbnail and duration.
     */
    fun processSelectedVideo(context: Context, sourceUri: Uri): SelectedVideoMetadata {
        // 1. Try taking persistable read permission if supported
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                sourceUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }

        val timestamp = System.currentTimeMillis()
        var finalVideoUriString = sourceUri.toString()

        // 2. Copy video stream to internal storage so it remains permanently accessible
        runCatching {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                val destFile = File(context.filesDir, "playzo_video_$timestamp.mp4")
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
                if (destFile.exists() && destFile.length() > 0L) {
                    finalVideoUriString = Uri.fromFile(destFile).toString()
                }
            }
        }

        // 3. Extract real video frame & duration via MediaMetadataRetriever
        var extractedThumbUri: String? = null
        var formattedDuration: String? = null

        val retriever = MediaMetadataRetriever()
        try {
            val targetUri = Uri.parse(finalVideoUriString)
            if (targetUri.scheme == "file" && targetUri.path != null) {
                retriever.setDataSource(targetUri.path)
            } else {
                retriever.setDataSource(context, sourceUri)
            }

            val durationMsStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationMsStr?.toLongOrNull()
            if (durationMs != null && durationMs > 0L) {
                val totalSeconds = (durationMs / 1000L).coerceAtLeast(1L)
                val minutes = totalSeconds / 60L
                val seconds = totalSeconds % 60L
                formattedDuration = String.format(Locale.US, "%02d:%02d", minutes, seconds)
            }

            val frameBitmap = retriever.getFrameAtTime(
                500_000L,
                MediaMetadataRetriever.OPTION_CLOSEST_SYNC
            ) ?: retriever.getFrameAtTime(0L)

            if (frameBitmap != null) {
                val thumbFile = File(context.filesDir, "playzo_thumb_$timestamp.jpg")
                FileOutputStream(thumbFile).use { out ->
                    frameBitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
                }
                if (thumbFile.exists() && thumbFile.length() > 0L) {
                    extractedThumbUri = Uri.fromFile(thumbFile).toString()
                }
            }
        } catch (_: Exception) {
            // Ignore metadata extraction errors on synthetic test URIs
        } finally {
            runCatching { retriever.release() }
        }

        return SelectedVideoMetadata(
            persistedVideoUri = finalVideoUriString,
            extractedThumbnailUri = extractedThumbUri,
            formattedDuration = formattedDuration
        )
    }

    fun extractVideoFrameBitmap(context: Context, videoUriString: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            val uri = Uri.parse(videoUriString)
            if (uri.scheme == "file" && uri.path != null) {
                retriever.setDataSource(uri.path)
            } else {
                retriever.setDataSource(context, uri)
            }
            retriever.getFrameAtTime(500_000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                ?: retriever.getFrameAtTime(0L)
        } catch (_: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    /**
     * Formats large numbers compactly with K/M suffix (e.g. 1.2M, 45.6K, 950).
     */
    fun formatCount(count: Long): String {
        return when {
            count >= 1_000_000 -> String.format(Locale.US, "%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format(Locale.US, "%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    fun formatCount(count: Int): String = formatCount(count.toLong())

    /**
     * Formats count with thousands comma separators (e.g. 20,000 or 30,000).
     */
    fun formatExactCount(count: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(count)
    }

    fun formatExactCount(count: Int): String = formatExactCount(count.toLong())
}
