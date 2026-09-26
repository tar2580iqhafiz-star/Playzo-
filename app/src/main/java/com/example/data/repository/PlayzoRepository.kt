package com.example.data.repository

import com.example.data.database.InitialDataSeeder
import com.example.data.database.PlayzoDatabase
import com.example.data.model.AccountEntity
import com.example.data.model.ActiveSessionEntity
import com.example.data.model.CommentEntity
import com.example.data.model.FollowEntity
import com.example.data.model.PrivateAccountInfo
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity
import com.example.data.security.SecurityHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

sealed class AuthResult {
    data class Success(val user: UserEntity, val message: String? = null) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

sealed class PasswordResetRequestResult {
    data class Success(val email: String, val resetCode: String) : PasswordResetRequestResult()
    data class Error(val message: String) : PasswordResetRequestResult()
}

class PlayzoRepository(private val database: PlayzoDatabase) {

    private val userDao = database.userDao()
    private val videoDao = database.videoDao()
    private val commentDao = database.commentDao()
    private val followDao = database.followDao()
    private val accountDao = database.accountDao()
    private val sessionDao = database.sessionDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedDatabaseIfNeeded()
        }
    }

    private suspend fun seedDatabaseIfNeeded() {
        val currentUser = userDao.getCurrentUser()
        if (currentUser == null) {
            userDao.insertUsers(InitialDataSeeder.initialUsers)
            videoDao.insertVideos(InitialDataSeeder.initialVideos)
            for (comment in InitialDataSeeder.initialComments) {
                commentDao.insertComment(comment)
            }
            for (follow in InitialDataSeeder.initialFollows) {
                followDao.follow(follow)
            }
        }

        // Seed demo account securely if not present
        if (accountDao.getAccountsCount() == 0) {
            val demoSalt = SecurityHelper.generateSalt()
            val demoHash = SecurityHelper.hashPassword(InitialDataSeeder.DEMO_PASSWORD, demoSalt)
            val demoAccount = AccountEntity(
                userId = InitialDataSeeder.CURRENT_USER_ID,
                email = InitialDataSeeder.DEMO_EMAIL,
                passwordHash = demoHash,
                passwordSalt = demoSalt,
                createdAtTimestamp = System.currentTimeMillis()
            )
            accountDao.insertAccount(demoAccount)
            sessionDao.setActiveSession(ActiveSessionEntity(userId = InitialDataSeeder.CURRENT_USER_ID))
            userDao.setActiveCurrentUser(InitialDataSeeder.CURRENT_USER_ID)
            userDao.syncFollowStatusesForUser(InitialDataSeeder.CURRENT_USER_ID)
        }
    }

    // === AUTHENTICATION & SECURITY ===

    fun getActiveSessionFlow(): Flow<ActiveSessionEntity?> = sessionDao.getActiveSessionFlow()

    /**
     * Retrieves private account information ONLY for the authenticated user.
     * This is strictly prohibited from being queried for other creators.
     */
    fun getPrivateAccountInfoFlow(userId: String): Flow<PrivateAccountInfo?> =
        accountDao.getPrivateAccountInfoFlow(userId)

    suspend fun login(emailInput: String, passwordInput: String): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = SecurityHelper.normalizeEmail(emailInput)
        if (cleanEmail.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email address")
        }
        if (!SecurityHelper.isValidEmail(cleanEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address (e.g. name@example.com)")
        }
        if (passwordInput.isBlank()) {
            return@withContext AuthResult.Error("Please enter your password")
        }

        val account = accountDao.getAccountByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("No Playzo account found with this email.")

        val isPasswordCorrect = SecurityHelper.verifyPassword(
            password = passwordInput,
            saltHex = account.passwordSalt,
            expectedHashHex = account.passwordHash
        )

        if (!isPasswordCorrect) {
            return@withContext AuthResult.Error("Incorrect password. Please verify and try again.")
        }

        // Update active session and set user as current
        sessionDao.setActiveSession(ActiveSessionEntity(userId = account.userId))
        userDao.setActiveCurrentUser(account.userId)
        userDao.syncFollowStatusesForUser(account.userId)

        val user = userDao.getUserById(account.userId)
            ?: return@withContext AuthResult.Error("User profile could not be loaded.")

        AuthResult.Success(user, "Welcome back, ${user.name}!")
    }

    suspend fun signUp(
        nameInput: String,
        handleInput: String,
        emailInput: String,
        passwordInput: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val trimmedName = nameInput.trim()
        if (trimmedName.isBlank()) {
            return@withContext AuthResult.Error("Please enter your display name")
        }

        val cleanHandle = SecurityHelper.normalizeHandle(handleInput)
        if (cleanHandle.length < 3) {
            return@withContext AuthResult.Error("Public username must be at least 3 characters")
        }

        if (userDao.isHandleTaken(cleanHandle, "")) {
            return@withContext AuthResult.Error("The username $cleanHandle is already taken. Please choose another.")
        }

        val cleanEmail = SecurityHelper.normalizeEmail(emailInput)
        if (cleanEmail.isBlank()) {
            return@withContext AuthResult.Error("Please enter your email address")
        }
        if (!SecurityHelper.isValidEmail(cleanEmail)) {
            return@withContext AuthResult.Error("Please enter a valid email address")
        }

        if (accountDao.isEmailRegistered(cleanEmail)) {
            return@withContext AuthResult.Error("A Playzo account with this email already exists. Try logging in.")
        }

        if (!SecurityHelper.isValidPassword(passwordInput)) {
            return@withContext AuthResult.Error("Password must be at least 6 characters long")
        }

        val newUserId = "user_" + UUID.randomUUID().toString().take(8)

        // 1. Create Public User Profile (Strictly NO email or password)
        val newUser = UserEntity(
            id = newUserId,
            name = trimmedName,
            handle = cleanHandle,
            avatarDrawableName = "playzo_icon_1790327564914",
            customAvatarUri = null,
            bio = "New creator on Playzo! 🎬",
            followersCount = 0,
            followingCount = 0,
            isFollowedByMe = false,
            isCurrentUser = true,
            joinedDate = "Member since recently"
        )
        userDao.insertUser(newUser)

        // 2. Create Isolated Private Account Record (Cryptographically hashed password)
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(passwordInput, salt)
        val account = AccountEntity(
            userId = newUserId,
            email = cleanEmail,
            passwordHash = hash,
            passwordSalt = salt,
            createdAtTimestamp = System.currentTimeMillis()
        )
        accountDao.insertAccount(account)

        // 3. Set active local session
        sessionDao.setActiveSession(ActiveSessionEntity(userId = newUserId))
        userDao.setActiveCurrentUser(newUserId)
        userDao.syncFollowStatusesForUser(newUserId)

        AuthResult.Success(newUser, "Account created successfully! Welcome to Playzo.")
    }

    suspend fun logout(): Boolean = withContext(Dispatchers.IO) {
        sessionDao.clearActiveSession()
        userDao.clearActiveCurrentUser()
        userDao.clearAllFollowStatuses()
        true
    }

    suspend fun requestPasswordReset(emailInput: String): PasswordResetRequestResult = withContext(Dispatchers.IO) {
        val cleanEmail = SecurityHelper.normalizeEmail(emailInput)
        if (!SecurityHelper.isValidEmail(cleanEmail)) {
            return@withContext PasswordResetRequestResult.Error("Please enter a valid email address.")
        }

        val account = accountDao.getAccountByEmail(cleanEmail)
            ?: return@withContext PasswordResetRequestResult.Error("No Playzo account associated with this email.")

        val resetCode = SecurityHelper.generateResetCode()
        val expiry = System.currentTimeMillis() + (15 * 60 * 1000) // 15 minutes
        accountDao.setPasswordResetCode(cleanEmail, resetCode, expiry)

        PasswordResetRequestResult.Success(cleanEmail, resetCode)
    }

    suspend fun resetPassword(
        emailInput: String,
        codeInput: String,
        newPasswordInput: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val cleanEmail = SecurityHelper.normalizeEmail(emailInput)
        val account = accountDao.getAccountByEmail(cleanEmail)
            ?: return@withContext AuthResult.Error("No account found for this email.")

        if (account.resetCode == null || account.resetCode != codeInput.trim()) {
            return@withContext AuthResult.Error("Invalid 6-digit verification code. Please check and try again.")
        }

        if (account.resetCodeExpiryTimestamp != null && System.currentTimeMillis() > account.resetCodeExpiryTimestamp) {
            return@withContext AuthResult.Error("The verification code has expired. Please request a new one.")
        }

        if (!SecurityHelper.isValidPassword(newPasswordInput)) {
            return@withContext AuthResult.Error("New password must be at least 6 characters long.")
        }

        val newSalt = SecurityHelper.generateSalt()
        val newHash = SecurityHelper.hashPassword(newPasswordInput, newSalt)
        accountDao.updatePasswordByEmail(cleanEmail, newHash, newSalt)

        val user = userDao.getUserById(account.userId)
            ?: return@withContext AuthResult.Error("Account updated.")

        AuthResult.Success(user, "Password reset successfully! You can now log in.")
    }

    suspend fun changePassword(
        userId: String,
        currentPasswordInput: String,
        newPasswordInput: String
    ): AuthResult = withContext(Dispatchers.IO) {
        val account = accountDao.getAccountByUserId(userId)
            ?: return@withContext AuthResult.Error("Account not found.")

        val isCurrentValid = SecurityHelper.verifyPassword(
            password = currentPasswordInput,
            saltHex = account.passwordSalt,
            expectedHashHex = account.passwordHash
        )

        if (!isCurrentValid) {
            return@withContext AuthResult.Error("Current password is incorrect.")
        }

        if (!SecurityHelper.isValidPassword(newPasswordInput)) {
            return@withContext AuthResult.Error("New password must be at least 6 characters.")
        }

        val newSalt = SecurityHelper.generateSalt()
        val newHash = SecurityHelper.hashPassword(newPasswordInput, newSalt)
        accountDao.updatePasswordByUserId(userId, newHash, newSalt)

        val user = userDao.getUserById(userId)
            ?: return@withContext AuthResult.Error("Password changed.")

        AuthResult.Success(user, "Password changed securely.")
    }

    // === LONG VIDEOS FEED ===
    fun getAllLongVideosFlow(): Flow<List<VideoEntity>> = videoDao.getLongVideosFlow()

    fun getLongVideosByCategoryFlow(category: String): Flow<List<VideoEntity>> {
        return if (category == "All") {
            videoDao.getLongVideosFlow()
        } else if (category == "Following") {
            val session = sessionDao.getActiveSessionFlow()
            session.flatMapLatest { s ->
                val uid = s?.userId ?: InitialDataSeeder.CURRENT_USER_ID
                videoDao.getFollowingLongFeedFlow(uid)
            }
        } else {
            videoDao.getLongVideosByCategoryFlow(category)
        }
    }

    // === SHORTS FEED ===
    fun getShortVideosFlow(): Flow<List<VideoEntity>> = videoDao.getShortVideosFlow()

    // Common Video Queries
    fun getAllVideosFlow(): Flow<List<VideoEntity>> = videoDao.getAllVideosFlow()

    fun getVideoByIdFlow(videoId: String): Flow<VideoEntity?> = videoDao.getVideoByIdFlow(videoId)

    fun searchVideosFlow(query: String): Flow<List<VideoEntity>> = videoDao.searchVideosFlow(query)

    fun getVideosByCreatorFlow(creatorId: String): Flow<List<VideoEntity>> =
        videoDao.getVideosByCreatorFlow(creatorId)

    fun getVideosByCreatorAndTypeFlow(creatorId: String, isShort: Boolean): Flow<List<VideoEntity>> =
        videoDao.getVideosByCreatorAndTypeFlow(creatorId, isShort)

    fun getLikedVideosFlow(): Flow<List<VideoEntity>> = videoDao.getLikedVideosFlow()

    suspend fun incrementViewCount(videoId: String) = withContext(Dispatchers.IO) {
        videoDao.incrementViewCount(videoId)
    }

    suspend fun toggleLikeVideo(videoId: String) = withContext(Dispatchers.IO) {
        val video = videoDao.getVideoById(videoId) ?: return@withContext
        val newIsLiked = !video.isLikedByMe
        val newCount = if (newIsLiked) video.likesCount + 1 else (video.likesCount - 1).coerceAtLeast(0)
        videoDao.updateLikeStatus(videoId, newCount, newIsLiked)
    }

    // Comments
    fun getCommentsForVideoFlow(videoId: String): Flow<List<CommentEntity>> =
        commentDao.getCommentsForVideoFlow(videoId)

    suspend fun addComment(videoId: String, text: String): Boolean = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext false
        val currentUser = userDao.getCurrentUser() ?: InitialDataSeeder.initialUsers.first()
        val newComment = CommentEntity(
            id = "comm_" + UUID.randomUUID().toString().take(8),
            videoId = videoId,
            userId = currentUser.id,
            userName = currentUser.name,
            userHandle = currentUser.handle,
            userAvatarDrawableName = currentUser.avatarDrawableName,
            text = text.trim(),
            likesCount = 0,
            isLikedByMe = false,
            createdAtTimestamp = System.currentTimeMillis(),
            timeAgo = "Just now"
        )
        commentDao.insertComment(newComment)
        videoDao.incrementCommentCount(videoId)
        true
    }

    suspend fun toggleLikeComment(commentId: String, currentLikes: Int, isLiked: Boolean) =
        withContext(Dispatchers.IO) {
            val newIsLiked = !isLiked
            val newCount = if (newIsLiked) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)
            commentDao.updateCommentLike(commentId, newCount, newIsLiked)
        }

    // Users & Followers / Following
    fun getCurrentUserFlow(): Flow<UserEntity?> = userDao.getCurrentUserFlow()

    fun getUserByIdFlow(userId: String): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    fun searchUsersFlow(query: String): Flow<List<UserEntity>> = userDao.searchUsersFlow(query)

    fun getAllUsersFlow(): Flow<List<UserEntity>> = userDao.getAllUsersFlow()

    fun getFollowersFlow(userId: String): Flow<List<UserEntity>> = userDao.getFollowersFlow(userId)

    fun getFollowingFlow(userId: String): Flow<List<UserEntity>> = userDao.getFollowingFlow(userId)

    suspend fun toggleFollowUser(targetUserId: String) = withContext(Dispatchers.IO) {
        val currentUserId = userDao.getCurrentUser()?.id ?: InitialDataSeeder.CURRENT_USER_ID
        if (targetUserId == currentUserId) return@withContext // Can't follow oneself

        val targetUser = userDao.getUserById(targetUserId) ?: return@withContext
        val isFollowingNow = followDao.isFollowing(currentUserId, targetUserId)

        if (isFollowingNow) {
            // Unfollow
            followDao.unfollow(currentUserId, targetUserId)
            val newFollowersCount = (targetUser.followersCount - 1).coerceAtLeast(0)
            userDao.updateFollowStatus(targetUserId, newFollowersCount, false)
        } else {
            // Follow
            followDao.follow(FollowEntity(currentUserId, targetUserId))
            val newFollowersCount = targetUser.followersCount + 1
            userDao.updateFollowStatus(targetUserId, newFollowersCount, true)
        }

        // Update current user's following count
        val currentFollowingCount = followDao.countFollowing(currentUserId)
        userDao.updateFollowingCount(currentUserId, currentFollowingCount)
    }

    suspend fun isHandleTaken(handle: String): Boolean = withContext(Dispatchers.IO) {
        val currentUserId = userDao.getCurrentUser()?.id ?: ""
        val cleanHandle = SecurityHelper.normalizeHandle(handle)
        userDao.isHandleTaken(cleanHandle, currentUserId)
    }

    suspend fun updateCurrentUserProfile(
        name: String,
        handle: String,
        bio: String,
        avatarDrawableName: String?,
        customAvatarUri: String?
    ): Boolean = withContext(Dispatchers.IO) {
        val current = userDao.getCurrentUser() ?: return@withContext false
        val cleanHandle = SecurityHelper.normalizeHandle(handle)

        if (userDao.isHandleTaken(cleanHandle, current.id)) {
            return@withContext false
        }

        val updated = current.copy(
            name = name.trim(),
            handle = cleanHandle,
            bio = bio.trim(),
            avatarDrawableName = avatarDrawableName ?: current.avatarDrawableName,
            customAvatarUri = customAvatarUri ?: current.customAvatarUri
        )
        userDao.updateUser(updated)
        // Also update creator information on their videos
        videoDao.updateCreatorInfo(current.id, updated.name, updated.handle, updated.customAvatarUri)
        true
    }

    // Video Upload
    suspend fun uploadVideo(
        title: String,
        description: String,
        category: String,
        tags: String,
        duration: String,
        thumbnailDrawableName: String?,
        customThumbnailUri: String?,
        videoUri: String? = null,
        isShort: Boolean
    ): String = withContext(Dispatchers.IO) {
        val currentUser = userDao.getCurrentUser() ?: InitialDataSeeder.initialUsers.first()
        val videoId = (if (isShort) "short_" else "vid_") + UUID.randomUUID().toString().take(8)

        val newVideo = VideoEntity(
            id = videoId,
            title = title.trim(),
            description = description.trim(),
            thumbnailDrawableName = thumbnailDrawableName,
            customThumbnailUri = customThumbnailUri ?: videoUri,
            videoUri = videoUri,
            creatorId = currentUser.id,
            creatorName = currentUser.name,
            creatorHandle = currentUser.handle,
            creatorAvatarDrawableName = currentUser.avatarDrawableName,
            creatorCustomAvatarUri = currentUser.customAvatarUri,
            duration = if (duration.isBlank()) (if (isShort) "00:30" else "01:00") else duration.trim(),
            viewsCount = 1L,
            likesCount = 0L,
            commentsCount = 0L,
            category = if (category.isBlank()) "Gaming" else category,
            tags = tags.trim(),
            createdAtTimestamp = System.currentTimeMillis(),
            timeAgo = "Just now",
            isLikedByMe = false,
            isShort = isShort
        )
        videoDao.insertVideo(newVideo)
        videoId
    }
}
