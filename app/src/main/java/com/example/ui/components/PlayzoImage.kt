package com.example.ui.components

import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.ui.util.ResourceHelper

@Composable
fun PlayzoAvatarImage(
    drawableName: String? = null,
    customUri: String? = null,
    contentDescription: String? = null,
    avatarDrawableName: String? = null,
    customAvatarUri: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val actualDrawable = drawableName ?: avatarDrawableName
    val actualUri = customUri ?: customAvatarUri

    if (!actualUri.isNullOrBlank()) {
        AsyncImage(
            model = Uri.parse(actualUri),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = ResourceHelper.getDrawableResId(context, actualDrawable))
        )
    } else {
        val resId = ResourceHelper.getDrawableResId(context, actualDrawable)
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

@Composable
fun PlayzoThumbnailImage(
    drawableName: String? = null,
    customUri: String? = null,
    contentDescription: String? = null,
    thumbnailDrawableName: String? = null,
    customThumbnailUri: String? = null,
    videoUri: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val actualDrawable = drawableName ?: thumbnailDrawableName
    val actualUri = customUri ?: customThumbnailUri

    if (!actualUri.isNullOrBlank()) {
        AsyncImage(
            model = Uri.parse(actualUri),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            error = painterResource(id = ResourceHelper.getDrawableResId(context, actualDrawable))
        )
    } else if (!videoUri.isNullOrBlank()) {
        val extractedBitmap = remember(videoUri) {
            ResourceHelper.extractVideoFrameBitmap(context, videoUri)
        }
        if (extractedBitmap != null) {
            Image(
                bitmap = extractedBitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        } else {
            AsyncImage(
                model = Uri.parse(videoUri),
                contentDescription = contentDescription,
                modifier = modifier.background(Color(0xFF12131A)),
                contentScale = contentScale
            )
        }
    } else {
        val resId = ResourceHelper.getDrawableResId(context, actualDrawable)
        Image(
            painter = painterResource(id = resId),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}

/**
 * Renders and plays a user-selected device video URI directly using Android's VideoView,
 * backed by the video's real extracted frame so there is never any demo/sample fallback.
 */
@Composable
fun PlayzoVideoPlayer(
    videoUri: String,
    thumbnailUri: String? = null,
    isPlaying: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.background(Color.Black)) {
        // Real frame backdrop while surface prepares
        PlayzoThumbnailImage(
            drawableName = null,
            customUri = thumbnailUri,
            videoUri = videoUri,
            contentDescription = "Selected video preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        var videoViewRef = remember { arrayOfNulls<VideoView>(1) }

        DisposableEffect(videoUri) {
            onDispose {
                runCatching {
                    videoViewRef[0]?.stopPlayback()
                }
            }
        }

        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                FrameLayout(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val vv = VideoView(ctx).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            Gravity.CENTER
                        )
                        setOnErrorListener { _, _, _ -> true }
                        setOnPreparedListener { mp ->
                            runCatching {
                                mp.isLooping = true
                                if (isPlaying) {
                                    start()
                                } else {
                                    seekTo(100)
                                }
                            }
                        }
                        runCatching {
                            setVideoURI(Uri.parse(videoUri))
                        }
                    }
                    videoViewRef[0] = vv
                    addView(vv)
                }
            },
            update = { frameLayout ->
                val vv = frameLayout.getChildAt(0) as? VideoView
                if (vv != null) {
                    if (vv.tag != videoUri) {
                        vv.tag = videoUri
                        runCatching {
                            vv.setVideoURI(Uri.parse(videoUri))
                        }
                    }
                    runCatching {
                        if (isPlaying && !vv.isPlaying) {
                            vv.start()
                        } else if (!isPlaying && vv.isPlaying) {
                            vv.pause()
                        }
                    }
                }
            }
        )
    }
}

