package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.PlayzoAvatarImage
import com.example.ui.components.VideoCard
import com.example.ui.theme.PlayzoCyan
import com.example.ui.theme.PlayzoViolet
import com.example.ui.util.ResourceHelper

enum class FeedFilter {
    ALL,
    VIDEOS,
    SHORTS
}

@Composable
fun FeedScreen(
    feedVideos: List<VideoEntity>,
    followedCreators: List<UserEntity>,
    recommendedCreators: List<UserEntity>,
    onVideoClick: (String) -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (VideoEntity) -> Unit,
    onFollowClick: (String) -> Unit,
    onExploreClick: () -> Unit,
    onShortClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(FeedFilter.ALL) }

    val filteredVideos = when (selectedFilter) {
        FeedFilter.ALL -> feedVideos
        FeedFilter.VIDEOS -> feedVideos.filter { !it.isShort }
        FeedFilter.SHORTS -> feedVideos.filter { it.isShort }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("feed_screen"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Feed Header & Status
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DynamicFeed,
                        contentDescription = "Feed",
                        tint = PlayzoViolet,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Subscriptions & Activity",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (followedCreators.isNotEmpty())
                        "Latest releases and updates from ${followedCreators.size} creators you follow"
                    else
                        "Follow creators to personalize your daily release stream",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Followed Creators Carousel / Story Rings
        item {
            if (followedCreators.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(followedCreators, key = { "followed_${it.id}" }) { creator ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onCreatorClick(creator.id) }
                                .testTag("feed_creator_story_${creator.id}")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.sweepGradient(
                                            listOf(PlayzoViolet, PlayzoCyan, PlayzoViolet)
                                        ),
                                        shape = CircleShape
                                    )
                                    .padding(3.dp)
                                    .clip(CircleShape)
                            ) {
                                PlayzoAvatarImage(
                                    drawableName = creator.avatarDrawableName,
                                    customUri = creator.customAvatarUri,
                                    contentDescription = creator.name,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = creator.name.take(9),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                // If user is not following anyone yet, show Recommended Creators cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Suggested Creators to Follow",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(recommendedCreators.filter { !it.isCurrentUser }.take(6), key = { "rec_${it.id}" }) { creator ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .width(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onCreatorClick(creator.id) }
                                    .testTag("recommended_creator_${creator.id}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(10.dp)
                                ) {
                                    PlayzoAvatarImage(
                                        drawableName = creator.avatarDrawableName,
                                        customUri = creator.customAvatarUri,
                                        contentDescription = creator.name,
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = creator.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${ResourceHelper.formatCount(creator.followersCount)} fans",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = { onFollowClick(creator.id) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (creator.isFollowedByMe)
                                                MaterialTheme.colorScheme.surfaceVariant
                                            else PlayzoViolet
                                        ),
                                        shape = RoundedCornerShape(14.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                        modifier = Modifier.height(26.dp)
                                    ) {
                                        Text(
                                            text = if (creator.isFollowedByMe) "Following" else "Follow",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (creator.isFollowedByMe)
                                                MaterialTheme.colorScheme.onSurface
                                            else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    FeedFilter.ALL to "All Updates",
                    FeedFilter.VIDEOS to "Long Videos",
                    FeedFilter.SHORTS to "Shorts"
                ).forEach { (filter, label) ->
                    val isSelected = selectedFilter == filter
                    Surface(
                        color = if (isSelected) PlayzoViolet else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedFilter = filter }
                            .testTag("feed_filter_${filter.name.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        // Videos in Feed
        if (filteredVideos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.DynamicFeed,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent updates in this section",
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onExploreClick,
                            colors = ButtonDefaults.buttonColors(containerColor = PlayzoViolet),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Discover Creators", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredVideos, key = { "feed_vid_${it.id}" }) { video ->
                val creator = recommendedCreators.find { it.id == video.creatorId }
                val isCreatorFollowed = followedCreators.any { it.id == video.creatorId } || (creator?.isFollowedByMe == true)
                val isMe = creator?.isCurrentUser ?: false
                VideoCard(
                    video = video,
                    isFollowed = isCreatorFollowed,
                    isMe = isMe,
                    onVideoClick = {
                        if (video.isShort && onShortClick != null) {
                            onShortClick(video.id)
                        } else {
                            onVideoClick(video.id)
                        }
                    },
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
