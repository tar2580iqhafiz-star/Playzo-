package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.InitialDataSeeder
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity
import com.example.ui.components.PlayzoAvatarImage
import com.example.ui.components.PlayzoThumbnailImage
import com.example.ui.components.PlayzoVideoPlayer
import com.example.ui.theme.PlayzoCyan
import com.example.ui.theme.PlayzoViolet
import com.example.ui.util.ResourceHelper
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShortsScreen(
    shortVideos: List<VideoEntity>,
    allUsers: List<UserEntity>,
    onCreatorClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (VideoEntity) -> Unit,
    onFollowClick: (String) -> Unit,
    onUploadClick: () -> Unit,
    onSearchClick: (() -> Unit)? = null,
    initialShortId: String? = null,
    modifier: Modifier = Modifier
) {
    if (shortVideos.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.VideoLibrary,
                    contentDescription = null,
                    tint = PlayzoViolet,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No Playzo Shorts yet",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Be the first creator to share a 9:16 vertical short on Playzo!",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onUploadClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PlayzoViolet),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Upload a Short", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    val initialPageIndex = remember(initialShortId, shortVideos) {
        if (initialShortId != null) {
            shortVideos.indexOfFirst { it.id == initialShortId }.coerceAtLeast(0)
        } else 0
    }
    val pagerState = rememberPagerState(
        initialPage = initialPageIndex,
        pageCount = { shortVideos.size }
    )

    LaunchedEffect(initialShortId, shortVideos.size) {
        if (initialShortId != null) {
            val targetIdx = shortVideos.indexOfFirst { it.id == initialShortId }
            if (targetIdx >= 0 && targetIdx != pagerState.currentPage) {
                pagerState.scrollToPage(targetIdx)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("shorts_feed_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> shortVideos.getOrNull(index)?.id ?: index }
        ) { pageIndex ->
            val shortVideo = shortVideos[pageIndex]
            val creator = allUsers.find { it.id == shortVideo.creatorId }
            val isFollowed = creator?.isFollowedByMe ?: false
            val isMe = creator?.isCurrentUser ?: (shortVideo.creatorId == InitialDataSeeder.CURRENT_USER_ID)

            ShortVideoPageItem(
                video = shortVideo,
                creator = creator,
                isFollowed = isFollowed,
                isMe = isMe,
                isActivePage = pagerState.currentPage == pageIndex,
                onCreatorClick = { onCreatorClick(shortVideo.creatorId) },
                onLikeClick = { onLikeClick(shortVideo.id) },
                onCommentClick = { onCommentClick(shortVideo.id) },
                onShareClick = { onShareClick(shortVideo) },
                onFollowClick = { onFollowClick(shortVideo.creatorId) }
            )
        }

        // Top Shorts Badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Playzo Shorts",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = PlayzoViolet.copy(alpha = 0.8f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "BETA",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            if (onSearchClick != null) {
                androidx.compose.material3.IconButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("shorts_top_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShortVideoPageItem(
    video: VideoEntity,
    creator: UserEntity?,
    isFollowed: Boolean,
    isMe: Boolean,
    isActivePage: Boolean,
    onCreatorClick: () -> Unit,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onFollowClick: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var playProgress by remember { mutableFloatStateOf(0f) }
    var showPlayPauseIndicator by remember { mutableStateOf(false) }
    var isCaptionExpanded by remember { mutableStateOf(false) }

    // Simulate video playing progress when active
    LaunchedEffect(isActivePage, isPlaying) {
        if (!isActivePage) {
            isPlaying = true
            playProgress = 0f
            return@LaunchedEffect
        }
        while (isActivePage && isPlaying) {
            delay(100)
            if (playProgress < 1f) {
                playProgress += 0.007f
            } else {
                playProgress = 0f
            }
        }
    }

    // Rotating vinyl record animation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_anim")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotation"
    )

    val likeScale by animateFloatAsState(
        targetValue = if (video.isLikedByMe) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "like_scale"
    )

    val likeColor by animateColorAsState(
        targetValue = if (video.isLikedByMe) Color(0xFFF43F5E) else Color.White,
        label = "like_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPlaying = !isPlaying
                showPlayPauseIndicator = true
            }
            .testTag("short_item_${video.id}")
    ) {
        // Fullscreen 9:16 Vertical Video Frame
        if (!video.videoUri.isNullOrBlank()) {
            PlayzoVideoPlayer(
                videoUri = video.videoUri,
                thumbnailUri = video.customThumbnailUri,
                isPlaying = isActivePage && isPlaying,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            PlayzoThumbnailImage(
                drawableName = video.thumbnailDrawableName,
                customUri = video.customThumbnailUri,
                videoUri = video.videoUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Gradient overlays: Top scrim & Bottom scrim for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.5f),
                            Color.Transparent,
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Center Play / Pause indicator pop-in
        LaunchedEffect(showPlayPauseIndicator) {
            if (showPlayPauseIndicator) {
                delay(800)
                showPlayPauseIndicator = false
            }
        }

        AnimatedVisibility(
            visible = showPlayPauseIndicator,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        // Right Action Bar Column (Creator Avatar, Like, Comment, Share, Sound Disc)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Creator Avatar with quick Follow (+) badge
            Box(
                modifier = Modifier
                    .size(52.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .border(2.dp, PlayzoViolet, CircleShape)
                        .clickable(onClick = onCreatorClick)
                        .testTag("short_creator_avatar_${video.id}")
                ) {
                    PlayzoAvatarImage(
                        drawableName = creator?.avatarDrawableName ?: video.creatorAvatarDrawableName,
                        customUri = creator?.customAvatarUri ?: video.creatorCustomAvatarUri,
                        contentDescription = video.creatorName,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Follow (+) pill badge if not followed and not current user
                if (!isMe && !isFollowed) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(PlayzoViolet)
                            .clickable(onClick = onFollowClick)
                            .testTag("short_follow_btn_${video.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Follow",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            // Like Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onLikeClick)
                    .testTag("short_like_btn_${video.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (video.isLikedByMe) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = likeColor,
                        modifier = Modifier
                            .size(28.dp)
                            .scale(likeScale)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ResourceHelper.formatCount(video.likesCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comment Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onCommentClick)
                    .testTag("short_comment_btn_${video.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comment",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ResourceHelper.formatCount(video.commentsCount),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Share Action
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clickable(onClick = onShareClick)
                    .testTag("short_share_btn_${video.id}")
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Share",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Rotating Vinyl Sound Disc
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.8f))
                    .border(2.dp, Color(0xFF333333), CircleShape)
                    .rotate(if (isPlaying) discRotation else 0f),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(PlayzoViolet, PlayzoCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        // Bottom Left Information Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.80f)
                .padding(start = 16.dp, bottom = 24.dp)
        ) {
            // Creator Handle & Follow Status
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = video.creatorName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.clickable(onClick = onCreatorClick)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = video.creatorHandle,
                    color = PlayzoCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (!isMe) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (isFollowed) Color.White.copy(alpha = 0.25f) else PlayzoViolet,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onFollowClick)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            if (isFollowed) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Following",
                                    tint = Color.White,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Following", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            } else {
                                Text("Follow", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Video Title & Caption
            Text(
                text = video.title,
                color = Color.White,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                maxLines = if (isCaptionExpanded) 5 else 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.clickable { isCaptionExpanded = !isCaptionExpanded }
            )

            if (video.description.isNotBlank() && isCaptionExpanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = video.description,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sound tag pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = null,
                    tint = PlayzoCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Original Audio • ${video.creatorName} on Playzo",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Live Scrub Progress Bar along the very bottom of the short
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.25f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(playProgress)
                    .height(3.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PlayzoViolet, PlayzoCyan)
                        )
                    )
            )
        }
    }
}
