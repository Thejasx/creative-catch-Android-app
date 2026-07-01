package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CreativeRepository
import com.example.data.PortfolioItem
import com.example.data.User
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreativeViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = CreativeRepository(database)

    // Current Logged-in User
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Filter/Search State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow("All")
    val locationFilter = MutableStateFlow("")
    val sortByPrice = MutableStateFlow("None") // "None", "LowToHigh", "HighToLow"

    // Error and Success Feedback States
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    // Dynamic list of providers matching search, category, location, and sorting criteria
    val filteredProviders: StateFlow<List<User>> = combine(
        repository.allProviders,
        searchQuery,
        selectedCategory,
        locationFilter,
        sortByPrice
    ) { providers, query, category, location, sort ->
        var list = providers

        // Category filter
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Search query (matches name or bio)
        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.bio.contains(query, ignoreCase = true)
            }
        }

        // Location filter
        if (location.isNotBlank()) {
            list = list.filter { it.location.contains(location, ignoreCase = true) }
        }

        // Sorting by price
        list = when (sort) {
            "LowToHigh" -> list.sortedBy { it.pricePerHr }
            "HighToLow" -> list.sortedByDescending { it.pricePerHr }
            else -> list
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Provider Profile State
    private val _selectedProvider = MutableStateFlow<User?>(null)
    val selectedProvider: StateFlow<User?> = _selectedProvider.asStateFlow()

    // Portfolio of the selected provider
    val selectedProviderPortfolio: StateFlow<List<PortfolioItem>> = _selectedProvider
        .flatMapLatest { provider ->
            if (provider != null) {
                repository.getPortfolio(provider.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State (messages between currentUser and active Chat partner)
    private val _chatPartner = MutableStateFlow<User?>(null)
    val chatPartner: StateFlow<User?> = _chatPartner.asStateFlow()

    val chatMessages: StateFlow<List<com.example.data.ChatMessage>> = combine(
        _currentUser,
        _chatPartner
    ) { current, partner ->
        if (current != null && partner != null) {
            repository.getMessages(current.id, partner.id)
        } else {
            flowOf(emptyList())
        }
    }.flatMapLatest { it }
     .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // List of users the current logged-in user has had conversations with
    val chattedUsers: StateFlow<List<User>> = _currentUser
        .flatMapLatest { current ->
            if (current != null) {
                repository.getChattedUsers(current.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun selectProvider(provider: User?) {
        _selectedProvider.value = provider
    }

    fun selectChatPartner(partner: User?) {
        _chatPartner.value = partner
    }

    fun clearFeedback() {
        _errorMessage.value = null
        _successMessage.value = null
    }

    fun login(usernameInput: String, passwordInput: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (usernameInput.isBlank() || passwordInput.isBlank()) {
                _errorMessage.value = "Username and password cannot be empty"
                return@launch
            }
            val user = repository.loginUser(usernameInput, passwordInput)
            if (user != null) {
                _currentUser.value = user
                _errorMessage.value = null
                onSuccess()
            } else {
                _errorMessage.value = "Invalid username or password"
            }
        }
    }

    fun register(
        usernameInput: String,
        passwordInput: String,
        nameInput: String,
        typeInput: String, // "CLIENT" or "PROVIDER"
        categoryInput: String, // e.g. "Photographer"
        locationInput: String,
        priceInput: Double,
        bioInput: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            if (usernameInput.isBlank() || passwordInput.isBlank() || nameInput.isBlank()) {
                _errorMessage.value = "Please fill out all required fields"
                return@launch
            }
            val newUser = User(
                username = usernameInput,
                password = passwordInput,
                userType = typeInput,
                name = nameInput,
                category = if (typeInput == "PROVIDER") categoryInput else "",
                location = locationInput.ifBlank { "Anywhere" },
                pricePerHr = if (typeInput == "PROVIDER") priceInput else 0.0,
                bio = bioInput.ifBlank { "Excited to connect with awesome creatives!" },
                avatarRes = "avatar_" + ((2..6).random()),
                rating = if (typeInput == "PROVIDER") 4.8 else 5.0,
                jobsCount = 0
            )

            val id = repository.registerUser(newUser)
            if (id > 0) {
                _currentUser.value = newUser.copy(id = id)
                _errorMessage.value = null
                onSuccess()
            } else {
                _errorMessage.value = "Username is already taken"
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _chatPartner.value = null
        _selectedProvider.value = null
        clearFeedback()
    }

    // Providers: update pricing, location, bio
    fun updateProfile(
        name: String,
        category: String,
        location: String,
        price: Double,
        bio: String
    ) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            val updated = current.copy(
                name = name,
                category = category,
                location = location,
                pricePerHr = price,
                bio = bio
            )
            repository.updateUserProfile(updated)
            _currentUser.value = updated
            _successMessage.value = "Profile updated successfully!"
        }
    }

    // Providers: add customized portfolio items
    fun addPortfolioWork(title: String, imageUrl: String, description: String) {
        val current = _currentUser.value ?: return
        viewModelScope.launch {
            if (title.isBlank()) {
                _errorMessage.value = "Please provide a title for your work"
                return@launch
            }
            // Use a clean placeholder fallback if imageUrl is empty
            val finalImage = imageUrl.ifBlank { "img_creative_hero" }
            repository.addPortfolioItem(
                userId = current.id,
                title = title,
                imageUrl = finalImage,
                description = description
            )
            _successMessage.value = "New portfolio work added successfully!"
        }
    }

    // Providers: delete a work
    fun removePortfolioWork(item: PortfolioItem) {
        viewModelScope.launch {
            repository.deletePortfolioItem(item)
            _successMessage.value = "Work removed successfully"
        }
    }

    // Send chat message in real-time
    fun sendChatMessage(text: String) {
        val current = _currentUser.value ?: return
        val partner = _chatPartner.value ?: return
        if (text.isBlank()) return

        viewModelScope.launch {
            repository.sendMessage(
                senderId = current.id,
                receiverId = partner.id,
                text = text
            )
        }
    }
}
