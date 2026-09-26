package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PlayzoVideoPlayer
import com.example.ui.theme.PlayzoCyan
import com.example.ui.theme.PlayzoViolet
import com.example.ui.util.ResourceHelper
import com.example.ui.viewmodel.PlayzoViewModel

@Composable
fun UploadScreen(
    viewModel: PlayzoViewModel,
    onUploadSuccess: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isShort by viewModel.uploadIsShort.collectAsState()
    val videoUri by viewModel.uploadVideoUri.collectAsState()
    val title by viewModel.uploadTitle.collectAsState()
    val description by viewModel.uploadDescription.collectAsState()
    val category by viewModel.uploadCategory.collectAsState()
    val tags by viewModel.uploadTags.collectAsState()
    val duration by viewModel.uploadDuration.collectAsState()
    val customThumbnailUri by viewModel.uploadCustomThumbnailUri.collectAsState()
    val isUploading by viewModel.isUploading.collectAsState()

    var isPreviewPlaying by remember { mutableStateOf(true) }

    val handleSelectedVideoUri: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            val metadata = ResourceHelper.processSelectedVideo(context, uri)
            viewModel.onVideoPickedFromDevice(
                videoUri = metadata.persistedVideoUri,
                extractedThumbnailUri = metadata.extractedThumbnailUri,
                extractedDuration = metadata.formattedDuration
            )
            isPreviewPlaying = true
        }
    }

    // Android zero-permission Video Picker for selecting video from device
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = handleSelectedVideoUri
    )

    // Optional custom cover image picker from device
    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.uploadCustomThumbnailUri.value = uri.toString()
        }
    }

    val launchVideoPicker = {
        videoPickerLauncher.launch(
            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
        )
    }

    val availableCategories = listOf(
        "Gaming", "Tech", "Music", "Adventure", "Creative", "Vlogs", "Comedy"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 80.dp)
            .testTag("upload_screen")
    ) {
        // Screen Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(PlayzoViolet, PlayzoCyan)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.VideoCall,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Upload Video",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Publish to Playzo Feed or Playzo Shorts",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // FORMAT SELECTOR: Short Video vs Long Video
        Text(
            text = "Video Format",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Long Video Option
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.uploadIsShort.value = false
                    }
                    .testTag("format_long_video"),
                colors = CardDefaults.cardColors(
                    containerColor = if (!isShort) PlayzoViolet.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (!isShort) BorderStroke(2.dp, PlayzoViolet) else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Tv,
                        contentDescription = "Long Video",
                        tint = if (!isShort) PlayzoViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Long Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (!isShort) PlayzoViolet else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "16:9 Landscape",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Short Video Option
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        viewModel.uploadIsShort.value = true
                    }
                    .testTag("format_short_video"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isShort) PlayzoViolet.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = if (isShort) BorderStroke(2.dp, PlayzoViolet) else null
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Short Video",
                        tint = if (isShort) PlayzoViolet else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Short Video",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isShort) PlayzoViolet else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "9:16 Fullscreen",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Video Preview / Empty Upload Area
        Text(
            text = if (isShort) "Vertical Short Preview (9:16)" else "Video Preview (16:9)",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val selectedUri = videoUri
            if (selectedUri.isNullOrBlank()) {
                // EMPTY UPLOAD AREA BEFORE USER SELECTS A VIDEO (No sample/demo content)
                Card(
                    modifier = Modifier
                        .width(if (isShort) 220.dp else 340.dp)
                        .aspectRatio(if (isShort) 9f / 16f else 16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { launchVideoPicker() }
                        .testTag("empty_upload_preview_area"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(
                        width = 1.5.dp,
                        color = PlayzoViolet.copy(alpha = 0.55f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(PlayzoViolet.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoFile,
                                contentDescription = "Choose Video",
                                tint = PlayzoViolet,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "No video selected",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isShort) "Select a 9:16 vertical video from your device" else "Select a video from your device",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { launchVideoPicker() },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayzoViolet),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("choose_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Choose Video",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // EXACT SELECTED VIDEO PREVIEW AFTER USER PICKS A VIDEO FROM DEVICE
                Card(
                    modifier = Modifier
                        .width(if (isShort) 200.dp else 340.dp)
                        .aspectRatio(if (isShort) 9f / 16f else 16f / 9f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { isPreviewPlaying = !isPreviewPlaying }
                        .testTag("selected_video_preview_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Black)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        PlayzoVideoPlayer(
                            videoUri = selectedUri,
                            thumbnailUri = customThumbnailUri,
                            isPlaying = isPreviewPlaying,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Play / Pause control overlay
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .align(Alignment.Center)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.55f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isPreviewPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPreviewPlaying) "Pause preview" else "Play preview",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        // Format badge top-left
                        Surface(
                            color = PlayzoViolet.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .padding(8.dp)
                                .align(Alignment.TopStart)
                        ) {
                            Text(
                                text = if (isShort) "PLAYZO SHORT" else "LONG VIDEO",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Remove selected video button top-right
                        IconButton(
                            onClick = { viewModel.clearSelectedVideo() },
                            modifier = Modifier
                                .padding(6.dp)
                                .size(28.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.65f))
                                .testTag("clear_selected_video_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove selected video",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Duration badge bottom-right
                        if (duration.isNotBlank()) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.8f),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.BottomEnd)
                            ) {
                                Text(
                                    text = duration,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Device Video Picker & Optional Cover Picker Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = { launchVideoPicker() },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("upload_pick_from_device_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.VideoFile,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = PlayzoViolet
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (videoUri.isNullOrBlank()) "Pick from Device" else "Change Video",
                    fontSize = 12.sp,
                    color = PlayzoViolet,
                    fontWeight = FontWeight.SemiBold
                )
            }

            if (!videoUri.isNullOrBlank()) {
                OutlinedButton(
                    onClick = {
                        coverPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("upload_custom_cover_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = PlayzoViolet
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Custom Cover", fontSize = 12.sp, color = PlayzoViolet)
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Title Input
        OutlinedTextField(
            value = title,
            onValueChange = { viewModel.uploadTitle.value = it },
            label = { Text("Video Title *") },
            placeholder = { Text(if (isShort) "e.g. Highlight Clip ⚡" else "e.g. My New Video") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlayzoViolet,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = false,
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Description Input
        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.uploadDescription.value = it },
            label = { Text(if (isShort) "Caption / Sound Details" else "Description") },
            placeholder = { Text("Tell viewers what this video is about...") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("upload_description_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PlayzoViolet,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            minLines = 3,
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Category Selection
        Text(
            text = "Category",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            availableCategories.forEach { cat ->
                val isSelected = category == cat
                Surface(
                    color = if (isSelected) PlayzoViolet else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.uploadCategory.value = cat }
                        .testTag("upload_category_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Duration & Tags
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = duration,
                onValueChange = { viewModel.uploadDuration.value = it },
                label = { Text("Duration") },
                placeholder = { Text(if (isShort) "00:30" else "01:00") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("upload_duration_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PlayzoViolet,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )

            OutlinedTextField(
                value = tags,
                onValueChange = { viewModel.uploadTags.value = it },
                label = { Text("Tags") },
                placeholder = { Text(if (isShort) "#Shorts, #Playzo" else "#Gaming, #Playzo") },
                modifier = Modifier
                    .weight(2f)
                    .testTag("upload_tags_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PlayzoViolet,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        Button(
            onClick = {
                viewModel.uploadVideo { newVideoId, uploadedIsShort ->
                    onUploadSuccess(newVideoId, uploadedIsShort)
                }
            },
            enabled = !isUploading && !videoUri.isNullOrBlank() && title.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("publish_video_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = PlayzoViolet
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isUploading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Publishing to Playzo...", fontWeight = FontWeight.Bold, color = Color.White)
            } else {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isShort) "Publish Short Video" else "Publish Long Video",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
