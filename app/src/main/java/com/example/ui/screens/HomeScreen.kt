package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.VideoCard
import com.example.ui.theme.PlayzoViolet

@Composable
fun HomeScreen(
    videos: List<VideoEntity>,
    allUsers: List<UserEntity>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onVideoClick: (String) -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (VideoEntity) -> Unit,
    onFollowClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("All", "Following", "Gaming", "Music", "Tech", "Adventure", "Creative")

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen")
    ) {
        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    color = if (isSelected) PlayzoViolet else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCategorySelected(category) }
                        .testTag("category_chip_$category")
                ) {
                    Text(
                        text = category,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // Feed
        if (videos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = PlayzoViolet.copy(alpha = 0.6f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCategory == "Following") "No videos from followed creators" else "No videos in this category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedCategory == "Following")
                            "Follow your favorite creators on Playzo to see their newest releases here!"
                        else
                            "Check back soon or explore other topics.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                    if (selectedCategory == "Following") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { onCategorySelected("All") },
                            colors = ButtonDefaults.buttonColors(containerColor = PlayzoViolet)
                        ) {
                            Text("Explore All Videos")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(videos, key = { it.id }) { video ->
                    val creator = allUsers.find { it.id == video.creatorId }
                    val isFollowed = creator?.isFollowedByMe ?: false
                    val isMe = creator?.isCurrentUser ?: false

                    VideoCard(
                        video = video,
                        isFollowed = isFollowed,
                        isMe = isMe,
                        onVideoClick = { onVideoClick(video.id) },
                        onCreatorClick = { onCreatorClick(video.creatorId) },
                        onLikeClick = { onLikeClick(video.id) },
                        onCommentClick = { onCommentClick(video.id) },
                        onShareClick = { onShareClick(video) },
                        onFollowClick = { onFollowClick(video.creatorId) }
                    )
                }
            }
        }
    }
}
