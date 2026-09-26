package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CommentBottomSheet
import com.example.ui.components.PlayzoBottomBar
import com.example.ui.components.PlayzoTopBar
import com.example.ui.components.ShareDialog
import com.example.ui.screens.AuthDialog
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.FollowListSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.ShortsScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PlayzoViolet
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.PlayzoViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PlayzoApp()
            }
        }
    }
}

@Composable
fun PlayzoApp(viewModel: PlayzoViewModel = viewModel()) {
    val context = LocalContext.current
    val currentTab by viewModel.currentTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val longVideos by viewModel.longVideos.collectAsState()
    val shortVideos by viewModel.shortVideos.collectAsState()
    val feedVideos by viewModel.feedVideos.collectAsState()
    val followedCreators by viewModel.followedCreators.collectAsState()
    val isSearchOpen by viewModel.isSearchOpen.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val inspectedUser by viewModel.inspectedUser.collectAsState()
    val inspectedUserId by viewModel.inspectedUserId.collectAsState()
    val privateAccountInfo by viewModel.privateAccountInfo.collectAsState()

    val showAuthDialog by viewModel.showAuthDialog.collectAsState()

    val activeVideoId by viewModel.activeVideoId.collectAsState()
    val activeVideo by viewModel.activeVideo.collectAsState()

    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchFilter by viewModel.searchFilter.collectAsState()
    val allVideos by viewModel.allVideos.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val selectedShortId by viewModel.selectedShortId.collectAsState()
    val searchVideos by viewModel.searchVideos.collectAsState()
    val searchUsers by viewModel.searchUsers.collectAsState()

    val shareVideo by viewModel.shareVideo.collectAsState()
    val commentSheetVideoId by viewModel.commentSheetVideoId.collectAsState()
    val currentVideoComments by viewModel.currentVideoComments.collectAsState()

    val followListUserId by viewModel.followListUserId.collectAsState()
    val followListType by viewModel.followListType.collectAsState()
    val followListUsers by viewModel.followListUsers.collectAsState()

    val toastMessage by viewModel.toastMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToastMessage()
        }
    }

    // Handle back button when viewing creator profile or active video
    if (inspectedUserId != null && activeVideoId == null) {
        BackHandler {
            viewModel.closeUserProfileToCurrentUser()
        }
    }

    if (activeVideoId != null) {
        BackHandler {
            viewModel.closeVideoDetail()
        }
    }

    if (isSearchOpen || currentTab == MainTab.SEARCH) {
        BackHandler {
            viewModel.closeSearch()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (activeVideoId == null && !isSearchOpen && currentTab != MainTab.SHORTS && currentTab != MainTab.SEARCH) {
                PlayzoTopBar(
                    currentUser = currentUser,
                    onSearchClick = { viewModel.openSearch() },
                    onProfileClick = {
                        viewModel.closeUserProfileToCurrentUser()
                        viewModel.selectTab(MainTab.PROFILE)
                    },
                    onAuthClick = {
                        viewModel.openAuthDialog()
                    }
                )
            }
        },
        bottomBar = {
            if (activeVideoId == null) {
                PlayzoBottomBar(
                    currentTab = currentTab,
                    onTabSelected = { tab ->
                        if (tab == MainTab.UPLOAD && currentUser == null) {
                            viewModel.openAuthDialog()
                        } else {
                            if (tab == MainTab.PROFILE) {
                                viewModel.closeUserProfileToCurrentUser()
                            }
                            viewModel.selectTab(tab)
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (activeVideoId != null && activeVideo != null) {
                // Video Player & Detail Screen for Long Videos
                val currentCreator = allUsers.find { it.id == activeVideo!!.creatorId }
                VideoDetailScreen(
                    video = activeVideo!!,
                    creator = currentCreator,
                    comments = currentVideoComments,
                    relatedVideos = longVideos,
                    onBack = { viewModel.closeVideoDetail() },
                    onCreatorClick = { creatorId ->
                        viewModel.closeVideoDetail()
                        viewModel.openUserProfile(creatorId)
                    },
                    onLikeClick = { viewModel.toggleLikeVideo(activeVideo!!.id) },
                    onCommentClick = { viewModel.openCommentSheet(activeVideo!!.id) },
                    onShareClick = { v -> viewModel.openShareDialog(v) },
                    onFollowClick = { targetId -> viewModel.toggleFollowUser(targetId) },
                    onRelatedVideoClick = { vidId -> viewModel.openVideoDetail(vidId) },
                    allUsers = allUsers,
                    onRelatedLikeClick = { vidId -> viewModel.toggleLikeVideo(vidId) },
                    onRelatedCommentClick = { vidId -> viewModel.openCommentSheet(vidId) }
                )
            } else if (isSearchOpen || currentTab == MainTab.SEARCH) {
                SearchScreen(
                    searchQuery = searchQuery,
                    searchFilter = searchFilter,
                    videos = searchVideos,
                    users = searchUsers,
                    onQueryChange = { q -> viewModel.setSearchQuery(q) },
                    onFilterChange = { f -> viewModel.setSearchFilter(f) },
                    onVideoClick = { videoId ->
                        viewModel.openVideoDetail(videoId)
                    },
                    onShortClick = { shortId ->
                        viewModel.openShortVideo(shortId)
                    },
                    onCreatorClick = { creatorId ->
                        viewModel.closeSearch()
                        viewModel.openUserProfile(creatorId)
                    },
                    onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                    onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                    onShareClick = { v -> viewModel.openShareDialog(v) },
                    onFollowClick = { creatorId -> viewModel.toggleFollowUser(creatorId) },
                    onBack = { viewModel.closeSearch() },
                    allUsers = allUsers
                )
            } else {
                when (currentTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            videos = longVideos,
                            allUsers = allUsers,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { cat -> viewModel.setCategory(cat) },
                            onVideoClick = { videoId -> viewModel.openVideoDetail(videoId) },
                            onCreatorClick = { creatorId -> viewModel.openUserProfile(creatorId) },
                            onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                            onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                            onShareClick = { v -> viewModel.openShareDialog(v) },
                            onFollowClick = { creatorId -> viewModel.toggleFollowUser(creatorId) }
                        )
                    }

                    MainTab.SHORTS -> {
                        ShortsScreen(
                            shortVideos = shortVideos,
                            allUsers = allUsers,
                            onCreatorClick = { creatorId -> viewModel.openUserProfile(creatorId) },
                            onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                            onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                            onShareClick = { v -> viewModel.openShareDialog(v) },
                            onFollowClick = { creatorId -> viewModel.toggleFollowUser(creatorId) },
                            onUploadClick = {
                                if (currentUser == null) {
                                    viewModel.openAuthDialog()
                                } else {
                                    viewModel.uploadIsShort.value = true
                                    viewModel.selectTab(MainTab.UPLOAD)
                                }
                            },
                            onSearchClick = { viewModel.openSearch() },
                            initialShortId = selectedShortId
                        )
                    }

                    MainTab.FEED -> {
                        FeedScreen(
                            feedVideos = feedVideos,
                            followedCreators = followedCreators,
                            recommendedCreators = allUsers,
                            onVideoClick = { videoId -> viewModel.openVideoDetail(videoId) },
                            onCreatorClick = { creatorId -> viewModel.openUserProfile(creatorId) },
                            onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                            onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                            onShareClick = { v -> viewModel.openShareDialog(v) },
                            onFollowClick = { creatorId -> viewModel.toggleFollowUser(creatorId) },
                            onExploreClick = { viewModel.openSearch() },
                            onShortClick = { shortId -> viewModel.openShortVideo(shortId) }
                        )
                    }

                    MainTab.SEARCH -> {
                        SearchScreen(
                            searchQuery = searchQuery,
                            searchFilter = searchFilter,
                            videos = searchVideos,
                            users = searchUsers,
                            onQueryChange = { q -> viewModel.setSearchQuery(q) },
                            onFilterChange = { f -> viewModel.setSearchFilter(f) },
                            onVideoClick = { videoId -> viewModel.openVideoDetail(videoId) },
                            onShortClick = { shortId ->
                                viewModel.openShortVideo(shortId)
                            },
                            onCreatorClick = { creatorId ->
                                viewModel.closeSearch()
                                viewModel.openUserProfile(creatorId)
                            },
                            onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                            onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                            onShareClick = { v -> viewModel.openShareDialog(v) },
                            onFollowClick = { creatorId -> viewModel.toggleFollowUser(creatorId) },
                            onBack = { viewModel.closeSearch() },
                            allUsers = allUsers
                        )
                    }

                    MainTab.UPLOAD -> {
                        if (currentUser == null) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text(
                                        text = "Log in to upload videos",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Join Playzo to share shorts and long-form videos with creators worldwide.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(20.dp))
                                    Button(
                                        onClick = { viewModel.openAuthDialog() },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PlayzoViolet
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Log In or Sign Up", color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        } else {
                            UploadScreen(
                                viewModel = viewModel,
                                onUploadSuccess = { newId, isShort ->
                                    if (isShort) {
                                        viewModel.openShortVideo(newId)
                                    } else {
                                        viewModel.selectTab(MainTab.HOME)
                                        viewModel.openVideoDetail(newId)
                                    }
                                }
                            )
                        }
                    }

                    MainTab.PROFILE -> {
                        val targetUser = inspectedUser ?: currentUser
                        val userVideos = if (targetUser != null) allVideos.filter { it.creatorId == targetUser.id } else emptyList()
                        val likedVideos = allVideos.filter { it.isLikedByMe }

                        ProfileScreen(
                            user = targetUser,
                            userVideos = userVideos,
                            likedVideos = likedVideos,
                            privateAccountInfo = if (targetUser?.id == currentUser?.id) privateAccountInfo else null,
                            onOpenFollowers = {
                                targetUser?.let {
                                    viewModel.openFollowList(it.id, com.example.ui.viewmodel.FollowListType.FOLLOWERS)
                                }
                            },
                            onOpenFollowing = {
                                targetUser?.let {
                                    viewModel.openFollowList(it.id, com.example.ui.viewmodel.FollowListType.FOLLOWING)
                                }
                            },
                            onFollowClick = {
                                targetUser?.let { viewModel.toggleFollowUser(it.id) }
                            },
                            onUpdateProfile = { name, handle, bio, avatarDrawable, customUri, onDone ->
                                viewModel.updateProfile(name, handle, bio, avatarDrawable, customUri, onDone)
                            },
                            onChangePassword = { currentPass, newPass, onDone ->
                                viewModel.changePassword(currentPass, newPass, onDone)
                            },
                            onLogout = {
                                viewModel.logout()
                            },
                            onOpenAuth = {
                                viewModel.openAuthDialog()
                            },
                            onVideoClick = { videoId -> viewModel.openVideoDetail(videoId) },
                            onShortClick = { shortId ->
                                viewModel.openShortVideo(shortId)
                            },
                            onCreatorClick = { creatorId -> viewModel.openUserProfile(creatorId) },
                            onLikeClick = { videoId -> viewModel.toggleLikeVideo(videoId) },
                            onCommentClick = { videoId -> viewModel.openCommentSheet(videoId) },
                            onShareClick = { v -> viewModel.openShareDialog(v) },
                            allUsers = allUsers,
                            onFollowCreatorClick = { creatorId -> viewModel.toggleFollowUser(creatorId) }
                        )
                    }
                }
            }

            // Authentication Modal Dialog
            if (showAuthDialog) {
                AuthDialog(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeAuthDialog() }
                )
            }

            // Share Dialog Modal
            shareVideo?.let { v ->
                ShareDialog(
                    video = v,
                    onDismiss = { viewModel.closeShareDialog() }
                )
            }

            // Comment Bottom Sheet
            if (commentSheetVideoId != null) {
                CommentBottomSheet(
                    comments = currentVideoComments,
                    currentUser = currentUser,
                    onDismiss = { viewModel.closeCommentSheet() },
                    onAddComment = { text ->
                        if (currentUser == null) {
                            viewModel.openAuthDialog()
                        } else {
                            viewModel.addComment(commentSheetVideoId!!, text)
                        }
                    },
                    onLikeComment = { comment ->
                        if (currentUser == null) {
                            viewModel.openAuthDialog()
                        } else {
                            viewModel.toggleLikeComment(comment)
                        }
                    }
                )
            }

            // Followers / Following List Sheet
            if (followListUserId != null) {
                FollowListSheet(
                    users = followListUsers,
                    type = followListType,
                    onDismiss = { viewModel.closeFollowList() },
                    onUserClick = { userId ->
                        viewModel.closeFollowList()
                        viewModel.openUserProfile(userId)
                    },
                    onFollowClick = { userId ->
                        viewModel.toggleFollowUser(userId)
                    }
                )
            }
        }
    }
}
