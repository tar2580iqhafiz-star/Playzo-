package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.InitialDataSeeder
import com.example.data.database.PlayzoDatabase
import com.example.data.model.ActiveSessionEntity
import com.example.data.model.CommentEntity
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity
import com.example.data.repository.AuthResult
import com.example.data.repository.PasswordResetRequestResult
import com.example.data.repository.PlayzoRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    HOME,       // Long Video Feed
    SHORTS,     // Dedicated Short Video Feed (Vertical full-screen swipeable)
    FEED,       // Following & Subscriptions Feed
    UPLOAD,     // Upload Video
    PROFILE,    // User Profile
    SEARCH      // Search (accessible via top header icon)
}

enum class SearchFilter {
    ALL,
    VIDEOS,
    SHORTS,
    CREATORS
}

enum class FollowListType {
    FOLLOWERS,
    FOLLOWING
}

class PlayzoViewModel(application: Application) : AndroidViewModel(application) {

    val repository: PlayzoRepository

    init {
        val db = PlayzoDatabase.getDatabase(application)
        repository = PlayzoRepository(db)
    }

    // === AUTHENTICATION & SESSION STATE ===
    val activeSession: StateFlow<ActiveSessionEntity?> = repository.getActiveSessionFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentUser: StateFlow<UserEntity?> = repository.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isLoggedIn: StateFlow<Boolean> = currentUser.flatMapLatest { user ->
        MutableStateFlow(user != null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    /**
     * Private account info (email, join date) is ONLY available to the logged-in user.
     * Never visible to or queryable by other creators.
     */
    val privateAccountInfo: StateFlow<PrivateAccountInfo?> = currentUser.flatMapLatest { user ->
        if (user == null) MutableStateFlow(null)
        else repository.getPrivateAccountInfoFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _resetCodeSent = MutableStateFlow<String?>(null)
    val resetCodeSent: StateFlow<String?> = _resetCodeSent.asStateFlow()

    // Navigation & Screen State
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // 1. Long Videos Feed (Home Screen)
    val longVideos: StateFlow<List<VideoEntity>> = _selectedCategory.flatMapLatest { category ->
        repository.getLongVideosByCategoryFlow(category)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 2. Short Videos Feed (Shorts Screen)
    val shortVideos: StateFlow<List<VideoEntity>> = repository.getShortVideosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Subscriptions / Following Feed (Feed Screen)
    val feedVideos: StateFlow<List<VideoEntity>> = combine(
        currentUser,
        repository.getAllVideosFlow()
    ) { user, allVids ->
        Pair(user, allVids)
    }.flatMapLatest { (user, allVids) ->
        if (user == null) {
            MutableStateFlow(allVids)
        } else {
            repository.getFollowingFlow(user.id).flatMapLatest { followingList ->
                val followedIds = followingList.map { it.id }.toSet()
                val result = if (followedIds.isNotEmpty()) {
                    allVids.filter { it.creatorId in followedIds || it.creatorId == user.id }
                } else {
                    allVids
                }
                MutableStateFlow(result)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Followed creators for the stories/carousel at the top of Feed
    val followedCreators: StateFlow<List<UserEntity>> = currentUser.flatMapLatest { user ->
        if (user == null) MutableStateFlow(emptyList())
        else repository.getFollowingFlow(user.id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Video Player Detail Screen (for Long Videos)
    private val _activeVideoId = MutableStateFlow<String?>(null)
    val activeVideoId: StateFlow<String?> = _activeVideoId.asStateFlow()

    val activeVideo: StateFlow<VideoEntity?> = _activeVideoId.flatMapLatest { id ->
        if (id == null) MutableStateFlow(null)
        else repository.getVideoByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // User Profile Screen (if null, current user's profile is shown)
    private val _inspectedUserId = MutableStateFlow<String?>(null)
    val inspectedUserId: StateFlow<String?> = _inspectedUserId.asStateFlow()

    val inspectedUser: StateFlow<UserEntity?> = _inspectedUserId.flatMapLatest { id ->
        if (id == null) repository.getCurrentUserFlow()
        else repository.getUserByIdFlow(id)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Followers / Following List Sheet/Screen
    private val _followListUserId = MutableStateFlow<String?>(null)
    val followListUserId: StateFlow<String?> = _followListUserId.asStateFlow()

    private val _followListType = MutableStateFlow(FollowListType.FOLLOWERS)
    val followListType: StateFlow<FollowListType> = _followListType.asStateFlow()

    val followListUsers: StateFlow<List<UserEntity>> = combine(
        _followListUserId,
        _followListType
    ) { userId, type ->
        Pair(userId, type)
    }.flatMapLatest { (userId, type) ->
        if (userId == null) MutableStateFlow(emptyList())
        else if (type == FollowListType.FOLLOWERS) repository.getFollowersFlow(userId)
        else repository.getFollowingFlow(userId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    val allVideos: StateFlow<List<VideoEntity>> = repository.getAllVideosFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedShortId = MutableStateFlow<String?>(null)
    val selectedShortId: StateFlow<String?> = _selectedShortId.asStateFlow()

    val searchVideos: StateFlow<List<VideoEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.getAllVideosFlow()
        else repository.searchVideosFlow(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchUsers: StateFlow<List<UserEntity>> = _searchQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.getAllUsersFlow()
        else repository.searchUsersFlow(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Comments for active video, short, or bottom sheet
    private val _commentSheetVideoId = MutableStateFlow<String?>(null)
    val commentSheetVideoId: StateFlow<String?> = _commentSheetVideoId.asStateFlow()

    val currentVideoComments: StateFlow<List<CommentEntity>> = combine(
        _activeVideoId,
        _commentSheetVideoId
    ) { activeId, sheetId ->
        sheetId ?: activeId
    }.flatMapLatest { videoId ->
        if (videoId == null) MutableStateFlow(emptyList())
        else repository.getCommentsForVideoFlow(videoId)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Share dialog
    private val _shareVideo = MutableStateFlow<VideoEntity?>(null)
    val shareVideo: StateFlow<VideoEntity?> = _shareVideo.asStateFlow()

    // Upload Form State (Strictly empty until user selects a video from device)
    var uploadIsShort = MutableStateFlow(false) // Toggle between Short (9:16) and Long (16:9)
    var uploadVideoUri = MutableStateFlow<String?>(null)
    var uploadTitle = MutableStateFlow("")
    var uploadDescription = MutableStateFlow("")
    var uploadCategory = MutableStateFlow("Gaming")
    var uploadTags = MutableStateFlow("#Playzo, #Gaming")
    var uploadDuration = MutableStateFlow("")
    var uploadSelectedThumbnail = MutableStateFlow<String?>(null)
    var uploadCustomThumbnailUri = MutableStateFlow<String?>(null)

    fun onVideoPickedFromDevice(
        videoUri: String,
        extractedThumbnailUri: String?,
        extractedDuration: String?
    ) {
        uploadVideoUri.value = videoUri
        uploadSelectedThumbnail.value = null
        if (!extractedThumbnailUri.isNullOrBlank()) {
            uploadCustomThumbnailUri.value = extractedThumbnailUri
        }
        if (!extractedDuration.isNullOrBlank()) {
            uploadDuration.value = extractedDuration
        } else if (uploadDuration.value.isBlank()) {
            uploadDuration.value = if (uploadIsShort.value) "00:30" else "01:00"
        }
    }

    fun clearSelectedVideo() {
        uploadVideoUri.value = null
        uploadCustomThumbnailUri.value = null
        uploadSelectedThumbnail.value = null
        uploadDuration.value = ""
    }

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Feedback message
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    // Search overlay state (controlled via top-right search icon)
    private val _isSearchOpen = MutableStateFlow(false)
    val isSearchOpen: StateFlow<Boolean> = _isSearchOpen.asStateFlow()

    fun openSearch() {
        _isSearchOpen.value = true
        _activeVideoId.value = null
    }

    fun closeSearch() {
        _isSearchOpen.value = false
        if (_currentTab.value == MainTab.SEARCH) {
            _currentTab.value = MainTab.HOME
        }
    }

    // Navigation methods
    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
        if (tab == MainTab.SEARCH) {
            _isSearchOpen.value = true
        } else {
            _isSearchOpen.value = false
        }
        if (tab != MainTab.HOME) {
            _activeVideoId.value = null
        }
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun openVideoDetail(videoId: String) {
        _activeVideoId.value = videoId
        viewModelScope.launch {
            repository.incrementViewCount(videoId)
        }
    }

    fun openShortVideo(shortId: String) {
        _selectedShortId.value = shortId
        _activeVideoId.value = null
        _isSearchOpen.value = false
        _currentTab.value = MainTab.SHORTS
        viewModelScope.launch {
            repository.incrementViewCount(shortId)
        }
    }

    fun closeVideoDetail() {
        _activeVideoId.value = null
    }

    fun openUserProfile(userId: String) {
        _inspectedUserId.value = userId
        _currentTab.value = MainTab.PROFILE
    }

    fun closeUserProfileToCurrentUser() {
        _inspectedUserId.value = null
    }

    fun openFollowList(userId: String, type: FollowListType) {
        _followListUserId.value = userId
        _followListType.value = type
    }

    fun closeFollowList() {
        _followListUserId.value = null
    }

    fun openShareDialog(video: VideoEntity) {
        _shareVideo.value = video
    }

    fun closeShareDialog() {
        _shareVideo.value = null
    }

    fun openCommentSheet(videoId: String) {
        _commentSheetVideoId.value = videoId
    }

    fun closeCommentSheet() {
        _commentSheetVideoId.value = null
    }

    // === AUTHENTICATION ACTIONS ===

    fun openAuthDialog() {
        _authError.value = null
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _authError.value = null
        _showAuthDialog.value = false
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun login(email: String, pass: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = repository.login(email, pass)) {
                is AuthResult.Success -> {
                    _authLoading.value = false
                    _showAuthDialog.value = false
                    _toastMessage.value = result.message ?: "Welcome back!"
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun signUp(
        name: String,
        handle: String,
        email: String,
        pass: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = repository.signUp(name, handle, email, pass)) {
                is AuthResult.Success -> {
                    _authLoading.value = false
                    _showAuthDialog.value = false
                    _toastMessage.value = result.message ?: "Account created successfully!"
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _toastMessage.value = "You have been logged out of Playzo."
            _inspectedUserId.value = null
            _currentTab.value = MainTab.HOME
            _showAuthDialog.value = true
        }
    }

    fun requestPasswordReset(email: String, onCodeGenerated: (String) -> Unit = {}) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = repository.requestPasswordReset(email)) {
                is PasswordResetRequestResult.Success -> {
                    _authLoading.value = false
                    _resetCodeSent.value = result.resetCode
                    _toastMessage.value = "Verification code generated: ${result.resetCode}"
                    onCodeGenerated(result.resetCode)
                }
                is PasswordResetRequestResult.Error -> {
                    _authLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun resetPassword(
        email: String,
        code: String,
        newPass: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            when (val result = repository.resetPassword(email, code, newPass)) {
                is AuthResult.Success -> {
                    _authLoading.value = false
                    _toastMessage.value = result.message ?: "Password reset successfully!"
                    onSuccess()
                }
                is AuthResult.Error -> {
                    _authLoading.value = false
                    _authError.value = result.message
                }
            }
        }
    }

    fun changePassword(
        currentPass: String,
        newPass: String,
        onDone: (Boolean, String) -> Unit
    ) {
        val user = currentUser.value
        if (user == null) {
            onDone(false, "You must be logged in to change password.")
            return
        }
        viewModelScope.launch {
            when (val result = repository.changePassword(user.id, currentPass, newPass)) {
                is AuthResult.Success -> {
                    _toastMessage.value = result.message
                    onDone(true, result.message ?: "Password changed.")
                }
                is AuthResult.Error -> {
                    onDone(false, result.message)
                }
            }
        }
    }

    // Actions
    fun toggleLikeVideo(videoId: String) {
        viewModelScope.launch {
            repository.toggleLikeVideo(videoId)
        }
    }

    fun toggleFollowUser(targetUserId: String) {
        viewModelScope.launch {
            repository.toggleFollowUser(targetUserId)
        }
    }

    fun addComment(videoId: String, text: String) {
        viewModelScope.launch {
            val success = repository.addComment(videoId, text)
            if (success) {
                _toastMessage.value = "Comment posted on Playzo!"
            }
        }
    }

    fun toggleLikeComment(comment: CommentEntity) {
        viewModelScope.launch {
            repository.toggleLikeComment(comment.id, comment.likesCount, comment.isLikedByMe)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
    }

    fun updateProfile(
        name: String,
        handle: String,
        bio: String,
        avatarDrawableName: String?,
        customAvatarUri: String?,
        onDone: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val success = repository.updateCurrentUserProfile(
                name = name,
                handle = handle,
                bio = bio,
                avatarDrawableName = avatarDrawableName,
                customAvatarUri = customAvatarUri
            )
            if (success) {
                _toastMessage.value = "Profile updated successfully!"
                onDone(true)
            } else {
                _toastMessage.value = "Username is already taken by another Playzo user."
                onDone(false)
            }
        }
    }

    fun uploadVideo(onSuccess: (String, Boolean) -> Unit) {
        val selectedVideoUri = uploadVideoUri.value
        if (selectedVideoUri.isNullOrBlank()) {
            _toastMessage.value = "Please select a video from your device first"
            return
        }
        if (uploadTitle.value.isBlank()) {
            _toastMessage.value = "Please enter a video title"
            return
        }

        viewModelScope.launch {
            _isUploading.value = true
            delay(800)
            val isShort = uploadIsShort.value
            val newId = repository.uploadVideo(
                title = uploadTitle.value,
                description = uploadDescription.value,
                category = uploadCategory.value,
                tags = uploadTags.value,
                duration = if (uploadDuration.value.isBlank()) (if (isShort) "00:30" else "01:00") else uploadDuration.value,
                thumbnailDrawableName = null,
                customThumbnailUri = uploadCustomThumbnailUri.value ?: selectedVideoUri,
                videoUri = selectedVideoUri,
                isShort = isShort
            )
            _isUploading.value = false
            // Reset fields
            uploadVideoUri.value = null
            uploadTitle.value = ""
            uploadDescription.value = ""
            uploadDuration.value = ""
            uploadSelectedThumbnail.value = null
            uploadCustomThumbnailUri.value = null
            _toastMessage.value = if (isShort) "Short video published to Playzo Shorts!" else "Long video published to Playzo!"
            onSuccess(newId, isShort)
        }
    }

    fun clearToastMessage() {
        _toastMessage.value = null
    }
}
