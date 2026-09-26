package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.components.VideoCard
import com.example.ui.theme.PlayzoCyan
import com.example.ui.theme.PlayzoViolet
import com.example.ui.util.ResourceHelper
import com.example.ui.viewmodel.SearchFilter

@Composable
fun SearchScreen(
    searchQuery: String,
    searchFilter: SearchFilter,
    videos: List<VideoEntity>,
    users: List<UserEntity>,
    onQueryChange: (String) -> Unit,
    onFilterChange: (SearchFilter) -> Unit,
    onVideoClick: (String) -> Unit,
    onShortClick: (String) -> Unit,
    onCreatorClick: (String) -> Unit,
    onLikeClick: (String) -> Unit,
    onCommentClick: (String) -> Unit,
    onShareClick: (VideoEntity) -> Unit,
    onFollowClick: (String) -> Unit,
    onBack: (() -> Unit)? = null,
    allUsers: List<UserEntity> = users,
    modifier: Modifier = Modifier
) {
    if (onBack != null) {
        BackHandler { onBack() }
    }

    val trendingTags = listOf("Cyberpunk", "Synthwave", "4K Drone", "Future Tech", "Shorts", "Action", "Night Drive")

    val longVideos = videos.filter { !it.isShort }
    val shortVideos = videos.filter { it.isShort }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .testTag("search_screen")
    ) {
        // Search Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("search_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = {
                    Text(
                        "Search videos, shorts, or creators on Playzo...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = PlayzoViolet
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_input_field"),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PlayzoViolet,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Filter Tabs: All, Videos, Shorts, Creators
        TabRow(
            selectedTabIndex = searchFilter.ordinal,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = PlayzoViolet,
            divider = {}
        ) {
            Tab(
                selected = searchFilter == SearchFilter.ALL,
                onClick = { onFilterChange(SearchFilter.ALL) },
                text = { Text("All", fontWeight = if (searchFilter == SearchFilter.ALL) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_search_all")
            )
            Tab(
                selected = searchFilter == SearchFilter.VIDEOS,
                onClick = { onFilterChange(SearchFilter.VIDEOS) },
                text = { Text("Videos", fontWeight = if (searchFilter == SearchFilter.VIDEOS) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_search_videos")
            )
            Tab(
                selected = searchFilter == SearchFilter.SHORTS,
                onClick = { onFilterChange(SearchFilter.SHORTS) },
                text = { Text("Shorts", fontWeight = if (searchFilter == SearchFilter.SHORTS) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_search_shorts")
            )
            Tab(
                selected = searchFilter == SearchFilter.CREATORS,
                onClick = { onFilterChange(SearchFilter.CREATORS) },
                text = { Text("Creators", fontWeight = if (searchFilter == SearchFilter.CREATORS) FontWeight.Bold else FontWeight.Normal) },
                modifier = Modifier.testTag("tab_search_creators")
            )
        }

        // Trending Tags Suggestion Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = null,
                tint = PlayzoCyan,
                modifier = Modifier.size(18.dp)
            )
            trendingTags.forEach { tag ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onQueryChange(tag) }
                ) {
                    Text(
                        text = "#$tag",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // 1. Creators section if filter is ALL or CREATORS
            if (searchFilter == SearchFilter.ALL || searchFilter == SearchFilter.CREATORS) {
                if (users.isNotEmpty()) {
                    item {
                        Text(
                            text = "Creators",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(users, key = { "creator_${it.id}" }) { user ->
                        CreatorSearchResultItem(
                            user = user,
                            onUserClick = { onCreatorClick(user.id) },
                            onFollowClick = { onFollowClick(user.id) }
                        )
                    }
                }
            }

            // 2. Shorts section if filter is ALL or SHORTS
            if (searchFilter == SearchFilter.ALL || searchFilter == SearchFilter.SHORTS) {
                if (shortVideos.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = null,
                                tint = PlayzoViolet,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Playzo Shorts",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    if (searchFilter == SearchFilter.ALL) {
                        // Horizontal strip for Shorts in ALL tab
                        item {
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(shortVideos, key = { "short_strip_${it.id}" }) { shortVideo ->
                                    ShortSearchPreviewCard(
                                        video = shortVideo,
                                        onClick = { onShortClick(shortVideo.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        // Vertical grid-like display in SHORTS tab
                        items(shortVideos, key = { "short_item_${it.id}" }) { shortVideo ->
                            val creator = allUsers.find { it.id == shortVideo.creatorId } ?: users.find { it.id == shortVideo.creatorId }
                            ShortSearchListItem(
                                video = shortVideo,
                                creator = creator,
                                onClick = { onShortClick(shortVideo.id) },
                                onCreatorClick = { onCreatorClick(shortVideo.creatorId) }
                            )
                        }
                    }
                }
            }

            // 3. Long Videos section if filter is ALL or VIDEOS
            if (searchFilter == SearchFilter.ALL || searchFilter == SearchFilter.VIDEOS) {
                if (longVideos.isNotEmpty()) {
                    item {
                        Text(
                            text = "Long Videos",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    items(longVideos, key = { "video_${it.id}" }) { video ->
                        val creator = allUsers.find { it.id == video.creatorId } ?: users.find { it.id == video.creatorId }
                        val isFollowed = creator?.isFollowedByMe ?: false
                        val isMe = creator?.isCurrentUser ?: (video.creatorId == InitialDataSeeder.CURRENT_USER_ID)

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

            // If empty
            if (users.isEmpty() && videos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No results found for \"$searchQuery\"",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Try searching for a different keyword, topic, or creator",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ShortSearchPreviewCard(
    video: VideoEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(230.dp)
            .clickable(onClick = onClick)
            .testTag("short_preview_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            PlayzoThumbnailImage(
                thumbnailDrawableName = video.thumbnailDrawableName,
                customThumbnailUri = video.customThumbnailUri,
                videoUri = video.videoUri,
                contentDescription = video.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Shorts flash icon
            Surface(
                color = PlayzoViolet,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = "Short",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .padding(2.dp)
                )
            }

            // Title and views
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = video.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${ResourceHelper.formatCount(video.viewsCount)} views",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun ShortSearchListItem(
    video: VideoEntity,
    creator: UserEntity?,
    onClick: () -> Unit,
    onCreatorClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("short_search_item_${video.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(70.dp)
                    .height(105.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                PlayzoThumbnailImage(
                    thumbnailDrawableName = video.thumbnailDrawableName,
                    customThumbnailUri = video.customThumbnailUri,
                    videoUri = video.videoUri,
                    contentDescription = video.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Surface(
                    color = PlayzoViolet,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                creator?.let {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(onClick = onCreatorClick)
                    ) {
                        PlayzoAvatarImage(
                            avatarDrawableName = it.avatarDrawableName,
                            customAvatarUri = it.customAvatarUri,
                            contentDescription = it.name,
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = it.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${ResourceHelper.formatCount(video.viewsCount)} views • ${ResourceHelper.formatCount(video.likesCount)} likes",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun CreatorSearchResultItem(
    user: UserEntity,
    onUserClick: () -> Unit,
    onFollowClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMe = user.id == InitialDataSeeder.CURRENT_USER_ID

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .clickable(onClick = onUserClick)
            .testTag("creator_result_${user.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayzoAvatarImage(
                avatarDrawableName = user.avatarDrawableName,
                customAvatarUri = user.customAvatarUri,
                contentDescription = user.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, PlayzoViolet, CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${user.handle} • ${ResourceHelper.formatCount(user.followersCount)} Followers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (user.bio.isNotBlank()) {
                    Text(
                        text = user.bio,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            if (!isMe) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(onClick = onFollowClick)
                        .testTag("creator_follow_btn_${user.id}"),
                    color = if (user.isFollowedByMe) MaterialTheme.colorScheme.surfaceVariant else PlayzoViolet,
                    shape = RoundedCornerShape(20.dp),
                    border = if (user.isFollowedByMe) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline) else null
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user.isFollowedByMe) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Following",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Following",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = "Follow",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Follow",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
