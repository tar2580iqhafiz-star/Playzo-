package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.security.SecurityHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Playzo", appName)
    }

    @Test
    fun `password security and cryptographic verification`() {
        val plainPassword = "SecretPassword123!"
        val salt = SecurityHelper.generateSalt()
        val hash = SecurityHelper.hashPassword(plainPassword, salt)

        // Ensure password is never stored plain text in hash
        assertNotEquals(plainPassword, hash)
        assertFalse(hash.contains(plainPassword))

        // Correct password verifies successfully
        val isValid = SecurityHelper.verifyPassword(plainPassword, salt, hash)
        assertTrue(isValid)

        // Wrong password fails verification
        val isInvalid = SecurityHelper.verifyPassword("WrongPassword!", salt, hash)
        assertFalse(isInvalid)
    }

    @Test
    fun `email normalization and validation`() {
        val rawEmail = "  User.Alex@Playzo.APP  "
        val normalized = SecurityHelper.normalizeEmail(rawEmail)
        assertEquals("user.alex@playzo.app", normalized)

        assertTrue(SecurityHelper.isValidEmail(normalized))
        assertTrue(SecurityHelper.isValidEmail("alex.rivera@playzo.app"))
        assertFalse(SecurityHelper.isValidEmail("not-an-email"))
        assertFalse(SecurityHelper.isValidEmail(""))
    }

    @Test
    fun `reset code generation`() {
        val code = SecurityHelper.generateResetCode()
        assertEquals(6, code.length)
        assertTrue(code.all { it.isDigit() })
    }

    @Test
    fun `profile total likes calculation and formatting`() {
        // Example from prompt: 2 videos with 10,000 likes each -> 20,000 total likes
        val video1 = com.example.data.model.VideoEntity(
            id = "v1",
            title = "Video 1",
            description = "Desc",
            creatorId = "creator_1",
            creatorName = "Creator",
            creatorHandle = "@creator",
            duration = "10:00",
            likesCount = 10000L,
            category = "Gaming",
            tags = "tags",
            createdAtTimestamp = System.currentTimeMillis(),
            timeAgo = "1 hour ago"
        )
        val video2 = video1.copy(id = "v2", title = "Video 2", likesCount = 10000L)

        val twoVideos = listOf(video1, video2)
        val totalLikesTwo = twoVideos.sumOf { it.likesCount }
        assertEquals(20000L, totalLikesTwo)
        assertEquals("20,000", com.example.ui.util.ResourceHelper.formatExactCount(totalLikesTwo))

        // Example from prompt: 3 videos with 10,000, 5,000, and 15,000 likes -> 30,000 total likes
        val video3 = video1.copy(id = "v3", title = "Video 3", likesCount = 5000L)
        val video4 = video1.copy(id = "v4", title = "Video 4", likesCount = 15000L)
        val threeVideos = listOf(video1, video3, video4)
        val totalLikesThree = threeVideos.sumOf { it.likesCount }
        assertEquals(30000L, totalLikesThree)
        assertEquals("30,000", com.example.ui.util.ResourceHelper.formatExactCount(totalLikesThree))

        // Dynamically incrementing a like (e.g. video1 receives a like)
        val updatedVideo1 = video1.copy(likesCount = video1.likesCount + 1L)
        val dynamicVideos = listOf(updatedVideo1, video2)
        val updatedTotal = dynamicVideos.sumOf { it.likesCount }
        assertEquals(20001L, updatedTotal)
        assertEquals("20,001", com.example.ui.util.ResourceHelper.formatExactCount(updatedTotal))

        // Dynamically losing a like (e.g. video1 loses a like)
        val unlikedVideo1 = updatedVideo1.copy(likesCount = (updatedVideo1.likesCount - 1L).coerceAtLeast(0L))
        val afterUnlikeVideos = listOf(unlikedVideo1, video2)
        val afterUnlikeTotal = afterUnlikeVideos.sumOf { it.likesCount }
        assertEquals(20000L, afterUnlikeTotal)
        assertEquals("20,000", com.example.ui.util.ResourceHelper.formatExactCount(afterUnlikeTotal))
    }

    @Test
    fun `navigation tabs and top header search flow`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.viewmodel.PlayzoViewModel(app)

        // Default tab is HOME and search is closed
        assertEquals(com.example.ui.viewmodel.MainTab.HOME, viewModel.currentTab.value)
        assertFalse(viewModel.isSearchOpen.value)

        // Opening search from top header sets isSearchOpen = true
        viewModel.openSearch()
        assertTrue(viewModel.isSearchOpen.value)

        // Closing search returns to current tab
        viewModel.closeSearch()
        assertFalse(viewModel.isSearchOpen.value)
        assertEquals(com.example.ui.viewmodel.MainTab.HOME, viewModel.currentTab.value)

        // Navigating to FEED, SHORTS, UPLOAD, PROFILE via bottom navigation
        viewModel.selectTab(com.example.ui.viewmodel.MainTab.SHORTS)
        assertEquals(com.example.ui.viewmodel.MainTab.SHORTS, viewModel.currentTab.value)

        viewModel.selectTab(com.example.ui.viewmodel.MainTab.FEED)
        assertEquals(com.example.ui.viewmodel.MainTab.FEED, viewModel.currentTab.value)

        viewModel.selectTab(com.example.ui.viewmodel.MainTab.UPLOAD)
        assertEquals(com.example.ui.viewmodel.MainTab.UPLOAD, viewModel.currentTab.value)

        viewModel.selectTab(com.example.ui.viewmodel.MainTab.PROFILE)
        assertEquals(com.example.ui.viewmodel.MainTab.PROFILE, viewModel.currentTab.value)

        // Opening a short video navigates directly to SHORTS tab with selectedShortId
        viewModel.openShortVideo("short_neon_dance")
        assertEquals(com.example.ui.viewmodel.MainTab.SHORTS, viewModel.currentTab.value)
        assertEquals("short_neon_dance", viewModel.selectedShortId.value)
        assertFalse(viewModel.isSearchOpen.value)
    }

    @Test
    fun `public user entity does not expose email or password`() {
        val fields = com.example.data.model.UserEntity::class.java.declaredFields.map { it.name.lowercase() }
        assertFalse("Public UserEntity must not contain email", fields.any { it.contains("email") })
        assertFalse("Public UserEntity must not contain password", fields.any { it.contains("password") })
    }

    @Test
    fun `upload screen starts empty without demo content and uses selected device video`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()
        val viewModel = com.example.ui.viewmodel.PlayzoViewModel(app)

        // Before selecting a video, uploadVideoUri, uploadSelectedThumbnail, and uploadCustomThumbnailUri are null
        assertEquals(null, viewModel.uploadVideoUri.value)
        assertEquals(null, viewModel.uploadSelectedThumbnail.value)
        assertEquals(null, viewModel.uploadCustomThumbnailUri.value)
        assertEquals("", viewModel.uploadDuration.value)

        // Selecting a video from device updates uploadVideoUri to that exact video
        val pickedUri = "file:///data/user/0/com.example/files/my_device_video.mp4"
        val extractedThumb = "file:///data/user/0/com.example/files/my_device_thumb.jpg"
        viewModel.onVideoPickedFromDevice(
            videoUri = pickedUri,
            extractedThumbnailUri = extractedThumb,
            extractedDuration = "01:18"
        )

        assertEquals(pickedUri, viewModel.uploadVideoUri.value)
        assertEquals(extractedThumb, viewModel.uploadCustomThumbnailUri.value)
        assertEquals(null, viewModel.uploadSelectedThumbnail.value)
        assertEquals("01:18", viewModel.uploadDuration.value)

        // Clearing selected video returns to empty state
        viewModel.clearSelectedVideo()
        assertEquals(null, viewModel.uploadVideoUri.value)
        assertEquals(null, viewModel.uploadCustomThumbnailUri.value)
        assertEquals(null, viewModel.uploadSelectedThumbnail.value)
        assertEquals("", viewModel.uploadDuration.value)
    }
}
