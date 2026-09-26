package com.example.data.database

import com.example.data.model.CommentEntity
import com.example.data.model.FollowEntity
import com.example.data.model.UserEntity
import com.example.data.model.VideoEntity

object InitialDataSeeder {
    const val CURRENT_USER_ID = "user_me"
    const val DEMO_EMAIL = "alex.rivera@playzo.app"
    const val DEMO_PASSWORD = "Password123!"

    val initialUsers = listOf(
        UserEntity(
            id = CURRENT_USER_ID,
            name = "Alex Rivera",
            handle = "@alex_playz",
            avatarDrawableName = "playzo_icon_1790327564914",
            customAvatarUri = null,
            bio = "Creator on Playzo 🎮 Exploring cutting-edge tech, futuristic gaming & synth soundscapes. Welcome to my channel!",
            followersCount = 1420,
            followingCount = 2,
            isFollowedByMe = false,
            isCurrentUser = true,
            joinedDate = "Member since September 2024"
        ),
        UserEntity(
            id = "user_neon_valk",
            name = "Neon Valkyrie",
            handle = "@neon_valk",
            avatarDrawableName = "thumb_cyber_game_1790327621065",
            customAvatarUri = null,
            bio = "Cyberpunk streamer & pro esports player. Sharing weekly walkthroughs & high-octane highlights on Playzo! ⚡",
            followersCount = 89400,
            followingCount = 135,
            isFollowedByMe = true,
            isCurrentUser = false,
            joinedDate = "Member since March 2024"
        ),
        UserEntity(
            id = "user_aura_beats",
            name = "Aura Beats",
            handle = "@aura_beats",
            avatarDrawableName = "thumb_synth_music_1790327636545",
            customAvatarUri = null,
            bio = "Lo-Fi producer and sound designer crafting cozy synthwave and ambient chill beats for coding & study 🎧",
            followersCount = 54200,
            followingCount = 98,
            isFollowedByMe = true,
            isCurrentUser = false,
            joinedDate = "Member since January 2024"
        ),
        UserEntity(
            id = "user_terra_vista",
            name = "Terra Vista",
            handle = "@terravista",
            avatarDrawableName = "thumb_drone_nature_1790327650626",
            customAvatarUri = null,
            bio = "Cinematic drone filmmaker & wilderness adventurer. Discovering Earth's most breathtaking untouched landscapes 🏔️",
            followersCount = 112500,
            followingCount = 210,
            isFollowedByMe = false,
            isCurrentUser = false,
            joinedDate = "Member since May 2023"
        ),
        UserEntity(
            id = "user_nova_tech",
            name = "Nova Tech",
            handle = "@novatech",
            avatarDrawableName = "thumb_tech_future_1790327664295",
            customAvatarUri = null,
            bio = "Next-gen hardware analyst exploring breakthrough concept devices, transparent displays & mobile engineering 🚀",
            followersCount = 230800,
            followingCount = 76,
            isFollowedByMe = false,
            isCurrentUser = false,
            joinedDate = "Member since November 2023"
        ),
        UserEntity(
            id = "user_pixel_studio",
            name = "Pixel Studio",
            handle = "@pixelstudio",
            avatarDrawableName = "playzo_icon_1790327564914",
            customAvatarUri = null,
            bio = "3D animator & visual storytelling artist. Behind the scenes breakdowns, shader tutorials & creative experiments.",
            followersCount = 38400,
            followingCount = 180,
            isFollowedByMe = false,
            isCurrentUser = false,
            joinedDate = "Member since July 2024"
        )
    )

    val initialVideos = listOf(
        // === LONG VIDEOS (Feed 1) ===
        VideoEntity(
            id = "vid_cyber_game",
            title = "NEO-TOKYO 2099: Full Cyberpunk Raytracing Gameplay & Combat Breakdown",
            description = "Welcome back to Playzo! Today we are diving deep into the neon-lit alleys of Neo-Tokyo 2099. Check out this combat sequence running with full path tracing and unreal engine 5 nanite graphics. Let me know in the comments your favorite loadout!",
            thumbnailDrawableName = "thumb_cyber_game_1790327621065",
            creatorId = "user_neon_valk",
            creatorName = "Neon Valkyrie",
            creatorHandle = "@neon_valk",
            creatorAvatarDrawableName = "thumb_cyber_game_1790327621065",
            duration = "14:28",
            viewsCount = 48200L,
            likesCount = 3840L,
            commentsCount = 142L,
            category = "Gaming",
            tags = "Gaming,Cyberpunk,NeoTokyo,Gameplay,Action",
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 4,
            timeAgo = "4 hours ago",
            isLikedByMe = true,
            isShort = false
        ),
        VideoEntity(
            id = "vid_synth_music",
            title = "Midnight Chill Studio: Lo-Fi Synthwave & Ambient Rain Beats for Deep Focus",
            description = "A peaceful 20-minute audio-visual journey made directly with modular synthesizers and field-recorded gentle rainfall. Perfect for late night creative coding sessions and unwinding after a long day. Streamed exclusively on Playzo.",
            thumbnailDrawableName = "thumb_synth_music_1790327636545",
            creatorId = "user_aura_beats",
            creatorName = "Aura Beats",
            creatorHandle = "@aura_beats",
            creatorAvatarDrawableName = "thumb_synth_music_1790327636545",
            duration = "22:15",
            viewsCount = 31900L,
            likesCount = 2950L,
            commentsCount = 89L,
            category = "Music",
            tags = "Music,LoFi,Synthwave,Focus,Ambient",
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 12,
            timeAgo = "12 hours ago",
            isLikedByMe = false,
            isShort = false
        ),
        VideoEntity(
            id = "vid_drone_nature",
            title = "Alpine Serenity: 4K Cinematic Drone Flight Across Crystal Glacial Valleys",
            description = "Shot with custom FPV cinema drones over the Swiss Alps at sunrise. Witness pristine glaciers, misty evergreen ridges, and turquoise alpine tarns untouched by civilization. Turn on highest resolution and sound for the best experience!",
            thumbnailDrawableName = "thumb_drone_nature_1790327650626",
            creatorId = "user_terra_vista",
            creatorName = "Terra Vista",
            creatorHandle = "@terravista",
            creatorAvatarDrawableName = "thumb_drone_nature_1790327650626",
            duration = "08:42",
            viewsCount = 76400L,
            likesCount = 6120L,
            commentsCount = 310L,
            category = "Adventure",
            tags = "Adventure,Drone,4K,Nature,Mountains",
            createdAtTimestamp = System.currentTimeMillis() - 86400000L,
            timeAgo = "1 day ago",
            isLikedByMe = true,
            isShort = false
        ),
        VideoEntity(
            id = "vid_tech_future",
            title = "The Transparent Glass Smartphone is REAL! Exclusive Hands-On Review",
            description = "We got early access to the world's first fully transparent OLED prototype phone! In this Playzo exclusive, we test battery density, see-through camera sensors, outdoor visibility, and UI privacy layers. Is this the future of mobile design?",
            thumbnailDrawableName = "thumb_tech_future_1790327664295",
            creatorId = "user_nova_tech",
            creatorName = "Nova Tech",
            creatorHandle = "@novatech",
            creatorAvatarDrawableName = "thumb_tech_future_1790327664295",
            duration = "16:05",
            viewsCount = 145000L,
            likesCount = 12400L,
            commentsCount = 890L,
            category = "Tech",
            tags = "Tech,Hardware,Smartphones,Innovation,Review",
            createdAtTimestamp = System.currentTimeMillis() - 86400000L * 2,
            timeAgo = "2 days ago",
            isLikedByMe = false,
            isShort = false
        ),
        VideoEntity(
            id = "vid_alex_my_intro",
            title = "My Setup Tour 2026: Ultra-Minimal Creator Station & Audio Gear",
            description = "Hey everyone! First official video on my Playzo channel. Here is a full breakdown of my desk ergonomics, audio interface, mechanical keyboard, and camera setup. Follow along for more tech builds!",
            thumbnailDrawableName = "thumb_tech_future_1790327664295",
            creatorId = CURRENT_USER_ID,
            creatorName = "Alex Rivera",
            creatorHandle = "@alex_playz",
            creatorAvatarDrawableName = "playzo_icon_1790327564914",
            duration = "09:30",
            viewsCount = 3200L,
            likesCount = 412L,
            commentsCount = 28L,
            category = "Creative",
            tags = "Creative,DeskTour,Setup,Creator,Playzo",
            createdAtTimestamp = System.currentTimeMillis() - 86400000L * 3,
            timeAgo = "3 days ago",
            isLikedByMe = true,
            isShort = false
        ),

        // === SHORT VIDEOS (Feed 2 - Shorts) ===
        VideoEntity(
            id = "short_dance_1",
            title = "Neon Street Battle! Neo-Tokyo Hologram Freestyle ⚡👟 #Shorts #Dance",
            description = "Wait for the beat drop at 0:15! Freestyle session under the rainy holographic district. Who wants a tutorial on this combo? Drop a follow!",
            thumbnailDrawableName = "short_neon_dance_1790328720182",
            creatorId = "user_neon_valk",
            creatorName = "Neon Valkyrie",
            creatorHandle = "@neon_valk",
            creatorAvatarDrawableName = "thumb_cyber_game_1790327621065",
            duration = "00:32",
            viewsCount = 214000L,
            likesCount = 18450L,
            commentsCount = 492L,
            category = "Gaming",
            tags = "Shorts,Dance,Neon,Cyberpunk,Playzo",
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 2,
            timeAgo = "2 hours ago",
            isLikedByMe = true,
            isShort = true
        ),
        VideoEntity(
            id = "short_skater_2",
            title = "Fastest Speed Roller Run at Sunset Blvd! 🛼🔥 60 MPH #Shorts",
            description = "Pushing top speeds along the coastal highway at golden hour! Pure adrenaline rush. Helmet and protection always on! ⚡",
            thumbnailDrawableName = "short_speed_skater_1790328737446",
            creatorId = "user_terra_vista",
            creatorName = "Terra Vista",
            creatorHandle = "@terravista",
            creatorAvatarDrawableName = "thumb_drone_nature_1790327650626",
            duration = "00:45",
            viewsCount = 156000L,
            likesCount = 14200L,
            commentsCount = 289L,
            category = "Adventure",
            tags = "Shorts,Skate,Speed,Action,Adventure",
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 5,
            timeAgo = "5 hours ago",
            isLikedByMe = false,
            isShort = true
        ),
        VideoEntity(
            id = "short_tech_3",
            title = "Transparent Phone Haptics Test in 10s! 📱✨ Is this pure glass magic?",
            description = "Testing the vibration motor and touch responsiveness through a 100% transparent glass chassis! Would you switch to this phone?",
            thumbnailDrawableName = "thumb_tech_future_1790327664295",
            creatorId = "user_nova_tech",
            creatorName = "Nova Tech",
            creatorHandle = "@novatech",
            creatorAvatarDrawableName = "thumb_tech_future_1790327664295",
            duration = "00:18",
            viewsCount = 389000L,
            likesCount = 32100L,
            commentsCount = 980L,
            category = "Tech",
            tags = "Shorts,Tech,TransparentPhone,Gadgets,Hardware",
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 18,
            timeAgo = "18 hours ago",
            isLikedByMe = true,
            isShort = true
        ),
        VideoEntity(
            id = "short_beat_4",
            title = "Cooking a cozy Lo-Fi melody in 25 seconds! 🎹🌧️ #Shorts #Beatmaker",
            description = "Rhodes electric piano + vinyl crackle + tape flutter = instant chill. Save this sound for your late night study vibes!",
            thumbnailDrawableName = "thumb_synth_music_1790327636545",
            creatorId = "user_aura_beats",
            creatorName = "Aura Beats",
            creatorHandle = "@aura_beats",
            creatorAvatarDrawableName = "thumb_synth_music_1790327636545",
            duration = "00:25",
            viewsCount = 94000L,
            likesCount = 8900L,
            commentsCount = 145L,
            category = "Music",
            tags = "Shorts,LoFi,MusicProduction,Synth,Beats",
            createdAtTimestamp = System.currentTimeMillis() - 86400000L,
            timeAgo = "1 day ago",
            isLikedByMe = false,
            isShort = true
        ),
        VideoEntity(
            id = "short_alex_5",
            title = "Ultimate Cable Management Hack in 30 Seconds! 🖥️⚡ #Shorts",
            description = "Tired of messy wires hanging below your desk? Here's how I hide every single power cord using magnetic raceways. Follow for more desk setups!",
            thumbnailDrawableName = "short_neon_dance_1790328720182",
            creatorId = CURRENT_USER_ID,
            creatorName = "Alex Rivera",
            creatorHandle = "@alex_playz",
            creatorAvatarDrawableName = "playzo_icon_1790327564914",
            duration = "00:30",
            viewsCount = 12500L,
            likesCount = 1340L,
            commentsCount = 68L,
            category = "Creative",
            tags = "Shorts,Setup,LifeHack,TechDesk,Playzo",
            createdAtTimestamp = System.currentTimeMillis() - 86400000L * 2,
            timeAgo = "2 days ago",
            isLikedByMe = true,
            isShort = true
        )
    )

    val initialComments = listOf(
        CommentEntity(
            id = "comm_1",
            videoId = "vid_cyber_game",
            userId = "user_nova_tech",
            userName = "Nova Tech",
            userHandle = "@novatech",
            userAvatarDrawableName = "thumb_tech_future_1790327664295",
            text = "The lighting reflections on wet pavement at 07:15 are insane! Great combat timing 🔥",
            likesCount = 42,
            isLikedByMe = false,
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 3,
            timeAgo = "3 hours ago"
        ),
        CommentEntity(
            id = "comm_2",
            videoId = "vid_cyber_game",
            userId = "user_aura_beats",
            userName = "Aura Beats",
            userHandle = "@aura_beats",
            userAvatarDrawableName = "thumb_synth_music_1790327636545",
            text = "That sound track sync during the boss arena was so satisfying!",
            likesCount = 18,
            isLikedByMe = true,
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 2,
            timeAgo = "2 hours ago"
        ),
        CommentEntity(
            id = "comm_short_1",
            videoId = "short_dance_1",
            userId = "user_aura_beats",
            userName = "Aura Beats",
            userHandle = "@aura_beats",
            userAvatarDrawableName = "thumb_synth_music_1790327636545",
            text = "That sync with the bass drop was unreal! 🔥⚡ Need this track ID ASAP",
            likesCount = 94,
            isLikedByMe = true,
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 1,
            timeAgo = "1 hour ago"
        ),
        CommentEntity(
            id = "comm_short_2",
            videoId = "short_skater_2",
            userId = "user_neon_valk",
            userName = "Neon Valkyrie",
            userHandle = "@neon_valk",
            userAvatarDrawableName = "thumb_cyber_game_1790327621065",
            text = "The camera tracking on this speed run is next level! What gimbal did you use?",
            likesCount = 57,
            isLikedByMe = false,
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 3,
            timeAgo = "3 hours ago"
        ),
        CommentEntity(
            id = "comm_short_3",
            videoId = "short_tech_3",
            userId = "user_alex_playz",
            userName = "Alex Rivera",
            userHandle = "@alex_playz",
            userAvatarDrawableName = "playzo_icon_1790327564914",
            text = "The transparent UI layer looks so futuristic! Wonder how good outdoor battery life is.",
            likesCount = 31,
            isLikedByMe = true,
            createdAtTimestamp = System.currentTimeMillis() - 3600000L * 10,
            timeAgo = "10 hours ago"
        )
    )

    val initialFollows = listOf(
        // User follows Neon Valkyrie and Aura Beats
        FollowEntity(followerUserId = CURRENT_USER_ID, targetUserId = "user_neon_valk"),
        FollowEntity(followerUserId = CURRENT_USER_ID, targetUserId = "user_aura_beats"),
        // Neon Valkyrie, Terra Vista, and Pixel Studio follow User
        FollowEntity(followerUserId = "user_neon_valk", targetUserId = CURRENT_USER_ID),
        FollowEntity(followerUserId = "user_terra_vista", targetUserId = CURRENT_USER_ID),
        FollowEntity(followerUserId = "user_pixel_studio", targetUserId = CURRENT_USER_ID)
    )
}
