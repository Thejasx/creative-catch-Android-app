package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreativeRepository(private val db: AppDatabase) {
    val userDao = db.userDao()
    val portfolioDao = db.portfolioDao()
    val chatDao = db.chatDao()

    val allProviders: Flow<List<User>> = userDao.getAllProviders()

    fun getPortfolio(userId: Long): Flow<List<PortfolioItem>> = portfolioDao.getPortfolioForUser(userId)

    fun getUser(userId: Long): Flow<User?> = userDao.getUserById(userId)

    suspend fun getUserSync(userId: Long): User? = userDao.getUserByIdSync(userId)

    fun getMessages(user1Id: Long, user2Id: Long): Flow<List<ChatMessage>> = chatDao.getChatMessages(user1Id, user2Id)

    fun getChattedUsers(userId: Long): Flow<List<User>> = flow {
        chatDao.getChattedUserIds(userId).collect { ids ->
            val users = ids.mapNotNull { id -> userDao.getUserByIdSync(id) }
            emit(users)
        }
    }

    suspend fun sendMessage(senderId: Long, receiverId: Long, text: String) {
        withContext(Dispatchers.IO) {
            chatDao.insertMessage(
                ChatMessage(
                    senderId = senderId,
                    receiverId = receiverId,
                    message = text
                )
            )
        }
    }

    suspend fun registerUser(user: User): Long {
        return withContext(Dispatchers.IO) {
            val existing = userDao.getUserByUsername(user.username)
            if (existing != null) {
                -1L // Username already taken
            } else {
                userDao.insertUser(user)
            }
        }
    }

    suspend fun loginUser(username: String, password: String): User? {
        return withContext(Dispatchers.IO) {
            userDao.login(username, password)
        }
    }

    suspend fun updateUserProfile(user: User) {
        withContext(Dispatchers.IO) {
            userDao.updateUser(user)
        }
    }

    suspend fun addPortfolioItem(userId: Long, title: String, imageUrl: String, description: String = "") {
        withContext(Dispatchers.IO) {
            portfolioDao.insertPortfolioItem(
                PortfolioItem(
                    userId = userId,
                    title = title,
                    imageUrl = imageUrl,
                    description = description
                )
            )
        }
    }

    suspend fun deletePortfolioItem(item: PortfolioItem) {
        withContext(Dispatchers.IO) {
            portfolioDao.deletePortfolioItem(item)
        }
    }

    // Prepopulate database if empty to give user a rich, working workspace
    suspend fun checkAndSeedDatabase() {
        withContext(Dispatchers.IO) {
            val providers = userDao.getAllProviders().first()
            if (providers.isEmpty()) {
                // Seed Providers (Photographers and other categories)
                val elenaId = userDao.insertUser(
                    User(
                        username = "elena",
                        password = "password",
                        userType = "PROVIDER",
                        name = "Elena Vance",
                        category = "Photographer",
                        location = "San Francisco, CA",
                        pricePerHr = 120.0,
                        bio = "Award-winning editorial and portrait photographer. Specialized in high-fashion studio shoots and dreamy natural light outdoor weddings. 8+ years experience making people look like legends.",
                        avatarRes = "avatar_2",
                        rating = 4.9,
                        jobsCount = 54
                    )
                )

                val marcusId = userDao.insertUser(
                    User(
                        username = "marcus",
                        password = "password",
                        userType = "PROVIDER",
                        name = "Marcus Brooks",
                        category = "Videographer",
                        location = "Los Angeles, CA",
                        pricePerHr = 150.0,
                        bio = "Cinematic director and drone pilot. Creating state-of-the-art music videos, commercial brand ads, and high-energy wedding short films with 4K HDR gear.",
                        avatarRes = "avatar_3",
                        rating = 4.8,
                        jobsCount = 42
                    )
                )

                val siennaId = userDao.insertUser(
                    User(
                        username = "sienna",
                        password = "password",
                        userType = "PROVIDER",
                        name = "Sienna Park",
                        category = "Designer",
                        location = "New York, NY",
                        pricePerHr = 95.0,
                        bio = "Modern brand identity designer and Web3 visual consultant. Crafting distinctive logos, vector art illustrations, and premium mobile app UI layouts.",
                        avatarRes = "avatar_4",
                        rating = 5.0,
                        jobsCount = 29
                    )
                )

                val zoeId = userDao.insertUser(
                    User(
                        username = "zoe",
                        password = "password",
                        userType = "PROVIDER",
                        name = "Zoe Miller",
                        category = "Makeup Artist",
                        location = "Miami, FL",
                        pricePerHr = 85.0,
                        bio = "Certified celebrity makeup artist. Specialized in luxury bridal glam, avant-garde runway highlights, and flawless high-definition editorial matte styling.",
                        avatarRes = "avatar_5",
                        rating = 4.7,
                        jobsCount = 61
                    )
                )

                val danielId = userDao.insertUser(
                    User(
                        username = "daniel",
                        password = "password",
                        userType = "PROVIDER",
                        name = "Daniel Kim",
                        category = "Photographer",
                        location = "Chicago, IL",
                        pricePerHr = 80.0,
                        bio = "Action sports and fitness lifestyle photographer. Catching vibrant candid energy on the streets, courts, and fitness studios. Casual and fun shoot style.",
                        avatarRes = "avatar_6",
                        rating = 4.6,
                        jobsCount = 19
                    )
                )

                // Seed some clients for mock chats
                val clientId = userDao.insertUser(
                    User(
                        username = "alice",
                        password = "password",
                        userType = "CLIENT",
                        name = "Alice Jenkins",
                        category = "",
                        location = "San Francisco, CA",
                        pricePerHr = 0.0,
                        bio = "Just looking for a professional photographer for our upcoming beach family portraits!",
                        avatarRes = "avatar_1",
                        rating = 5.0,
                        jobsCount = 0
                    )
                )

                // Seed Portfolio Items
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = elenaId, title = "Golden Hour Vogue", imageUrl = "img_creative_hero", description = "Featured photoshoot in natural daylight"))
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = elenaId, title = "Monochrome Shadows", imageUrl = "portfolio_portrait_2", description = "Fine art black and white studio portrait"))
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = elenaId, title = "Sunset Wedding", imageUrl = "portfolio_wedding_1", description = "Candid emotional laugh during coastal vow ceremony"))

                portfolioDao.insertPortfolioItem(PortfolioItem(userId = marcusId, title = "Cyberpunk Music Video", imageUrl = "portfolio_video_1", description = "Music video shoot with intense neon lighting and fog"))
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = marcusId, title = "Alpine Flight Reel", imageUrl = "portfolio_video_2", description = "Sleek 4K cinematic drone flight over snowy peaks"))

                portfolioDao.insertPortfolioItem(PortfolioItem(userId = siennaId, title = "Amethyst Rebranding Pack", imageUrl = "portfolio_design_1", description = "Full brand package styled with elegant violet color palettes"))
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = siennaId, title = "Aura Web3 Platform UI", imageUrl = "portfolio_design_2", description = "Responsive UI layout for creative digital portfolios"))

                portfolioDao.insertPortfolioItem(PortfolioItem(userId = zoeId, title = "Dewy Bridal Grace", imageUrl = "portfolio_mua_1", description = "Natural glowing makeup finish for summer wedding"))
                portfolioDao.insertPortfolioItem(PortfolioItem(userId = zoeId, title = "Electric Glitter Glow", imageUrl = "portfolio_mua_2", description = "Creative festival look featuring sparkles and intense highlights"))

                portfolioDao.insertPortfolioItem(PortfolioItem(userId = danielId, title = "Midcourt Slam-Dunk", imageUrl = "portfolio_sports_1", description = "High-speed action capture at city court"))

                // Seed some default chat messages
                chatDao.insertMessage(ChatMessage(senderId = clientId, receiverId = elenaId, message = "Hi Elena! I love your portfolio. Do you have availability for next Saturday?", timestamp = System.currentTimeMillis() - 1000 * 60 * 30))
                chatDao.insertMessage(ChatMessage(senderId = elenaId, receiverId = clientId, message = "Hi Alice! Yes, I do! I'd love to catch some beautiful moments for you. What location did you have in mind?", timestamp = System.currentTimeMillis() - 1000 * 60 * 15))
            }
        }
    }
}
