package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.R
import com.example.data.PortfolioItem
import com.example.data.User
import com.example.ui.theme.CosmicDarkBg
import com.example.ui.theme.CosmicDarkSurface

fun Modifier.glassmorphic(
    backgroundColor: Color = Color(0xFF251E2E).copy(alpha = 0.55f),
    borderColor: Color = Color(0xFFD0BCFF).copy(alpha = 0.18f),
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(16.dp)
): Modifier {
    return this
        .clip(shape)
        .background(backgroundColor)
        .border(1.dp, borderColor, shape)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreativeAppUi(viewModel: CreativeViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val chatPartner by viewModel.chatPartner.collectAsStateWithLifecycle()
    val selectedProvider by viewModel.selectedProvider.collectAsStateWithLifecycle()

    var currentClientTab by remember { mutableStateOf("Explore") } // "Explore", "Messages", "Profile"
    var currentProviderTab by remember { mutableStateOf("Workspace") } // "Workspace", "Messages"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (currentUser != null && chatPartner == null && selectedProvider == null) {
                NavigationBar(
                    containerColor = Color(0xFF1C122C).copy(alpha = 0.88f),
                    tonalElevation = 0.dp,
                    modifier = Modifier.border(
                        BorderStroke(1.dp, Color(0xFFD0BCFF).copy(alpha = 0.15f)),
                        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                ) {
                    if (currentUser?.userType == "CLIENT") {
                        // Client tabs
                        NavigationBarItem(
                            selected = currentClientTab == "Explore",
                            onClick = { currentClientTab = "Explore" },
                            icon = { Icon(Icons.Default.Search, contentDescription = "Explore") },
                            label = { Text("Explore") },
                            modifier = Modifier.testTag("nav_explore")
                        )
                        NavigationBarItem(
                            selected = currentClientTab == "Messages",
                            onClick = { currentClientTab = "Messages" },
                            icon = { Icon(Icons.Default.Email, contentDescription = "Messages") },
                            label = { Text("Chats") },
                            modifier = Modifier.testTag("nav_chats")
                        )
                        NavigationBarItem(
                            selected = currentClientTab == "Profile",
                            onClick = { currentClientTab = "Profile" },
                            icon = { Icon(Icons.Default.Person, contentDescription = "My Profile") },
                            label = { Text("Profile") },
                            modifier = Modifier.testTag("nav_profile")
                        )
                    } else {
                        // Provider tabs
                        NavigationBarItem(
                            selected = currentProviderTab == "Workspace",
                            onClick = { currentProviderTab = "Workspace" },
                            icon = { Icon(Icons.Default.Home, contentDescription = "Workspace") },
                            label = { Text("Workspace") },
                            modifier = Modifier.testTag("nav_workspace")
                        )
                        NavigationBarItem(
                            selected = currentProviderTab == "Messages",
                            onClick = { currentProviderTab = "Messages" },
                            icon = { Icon(Icons.Default.Email, contentDescription = "Chats") },
                            label = { Text("Chats") },
                            modifier = Modifier.testTag("nav_provider_chats")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F0716), // CosmicDarkBg
                            Color(0xFF040207)  // Deep cosmic void
                        )
                    )
                )
        ) {
            when {
                currentUser == null -> {
                    AuthScreen(viewModel = viewModel)
                }
                chatPartner != null -> {
                    ChatScreen(viewModel = viewModel, partner = chatPartner!!)
                }
                selectedProvider != null -> {
                    ProviderDetailScreen(viewModel = viewModel, provider = selectedProvider!!)
                }
                else -> {
                    if (currentUser?.userType == "CLIENT") {
                        when (currentClientTab) {
                            "Explore" -> ClientDashboard(viewModel = viewModel)
                            "Messages" -> ChatsOverview(viewModel = viewModel)
                            "Profile" -> ClientProfileScreen(viewModel = viewModel)
                        }
                    } else {
                        when (currentProviderTab) {
                            "Workspace" -> ProviderWorkspace(viewModel = viewModel)
                            "Messages" -> ChatsOverview(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

// 1. AUTHENTICATION & REGISTRATION SCREEN
@Composable
fun AuthScreen(viewModel: CreativeViewModel) {
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    var isRegisterMode by remember { mutableStateOf(false) }

    // Input states
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("CLIENT") } // "CLIENT" or "PROVIDER"
    var category by remember { mutableStateOf("Photographer") }
    var location by remember { mutableStateOf("") }
    var pricePerHr by remember { mutableStateOf("50.0") }
    var bio by remember { mutableStateOf("") }

    val categories = listOf("Photographer", "Videographer", "Designer", "Makeup Artist", "Illustrator")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Image(
                painter = painterResource(id = R.drawable.img_auth_header),
                contentDescription = "Creative Catch Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )
        }

        item {
            Text(
                text = "Creative Catch",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Connect with elite photographers & creative pros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .glassmorphic(
                        backgroundColor = Color(0xFF251E2E).copy(alpha = 0.55f),
                        borderColor = Color(0xFFD0BCFF).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                TabButton(
                    text = "Login",
                    selected = !isRegisterMode,
                    onClick = {
                        isRegisterMode = false
                        viewModel.clearFeedback()
                    },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Register",
                    selected = isRegisterMode,
                    onClick = {
                        isRegisterMode = true
                        viewModel.clearFeedback()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (errorMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth().testTag("username_input"),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        item {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth().testTag("password_input"),
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                )
            )
        }

        if (isRegisterMode) {
            item {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth().testTag("name_input"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("I want to:", style = MaterialTheme.typography.labelMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterPill(
                            text = "Hire Talent",
                            selected = userType == "CLIENT",
                            onClick = { userType = "CLIENT" },
                            modifier = Modifier.weight(1f)
                        )
                        FilterPill(
                            text = "Offer Services",
                            selected = userType == "PROVIDER",
                            onClick = { userType = "PROVIDER" },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (userType == "PROVIDER") {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("My Professional Category:", style = MaterialTheme.typography.labelMedium)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(categories) { cat ->
                                FilterPill(
                                    text = cat,
                                    selected = category == cat,
                                    onClick = { category = cat }
                                )
                            }
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location (e.g. San Francisco, CA)") },
                        modifier = Modifier.fillMaxWidth().testTag("location_input"),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = pricePerHr,
                        onValueChange = { pricePerHr = it },
                        label = { Text("Hourly Rate ($)") },
                        modifier = Modifier.fillMaxWidth().testTag("price_input"),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        label = { Text("Professional Bio") },
                        modifier = Modifier.fillMaxWidth().height(100.dp).testTag("bio_input"),
                        maxLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(
                            usernameInput = username,
                            passwordInput = password,
                            nameInput = name,
                            typeInput = userType,
                            categoryInput = category,
                            locationInput = location,
                            priceInput = pricePerHr.toDoubleOrNull() ?: 50.0,
                            bioInput = bio,
                            onSuccess = {
                                username = ""
                                password = ""
                                name = ""
                            }
                        )
                    } else {
                        viewModel.login(
                            usernameInput = username,
                            passwordInput = password,
                            onSuccess = {
                                username = ""
                                password = ""
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("submit_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "Register Account" else "Sign In",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 2. CLIENT MAIN DASHBOARD SCREEN (EXPLORE SERVICES)
@Composable
fun ClientDashboard(viewModel: CreativeViewModel) {
    val providers by viewModel.filteredProviders.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()
    val category by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val location by viewModel.locationFilter.collectAsStateWithLifecycle()
    val sort by viewModel.sortByPrice.collectAsStateWithLifecycle()

    var showFilterSheet by remember { mutableStateOf(false) }
    val categories = listOf("All", "Photographer", "Videographer", "Designer", "Makeup Artist", "Illustrator")

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Hero Image Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_creative_hero),
                contentDescription = "Workspace Banner",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, CosmicDarkBg.copy(alpha = 0.95f))
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = "Catch Elite Talent",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Book photographers, videographers & designers instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Search and Filter Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search by name, tags, bio...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_field"),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )

            IconButton(
                onClick = { showFilterSheet = !showFilterSheet },
                modifier = Modifier
                    .background(
                        if (showFilterSheet || location.isNotBlank() || sort != "None")
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .size(50.dp)
                    .testTag("filter_button")
            ) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Filters",
                    tint = if (showFilterSheet || location.isNotBlank() || sort != "None")
                        Color.White
                    else
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Expanded filter options
        AnimatedVisibility(
            visible = showFilterSheet,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Advanced Filters", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    // Location filter input
                    OutlinedTextField(
                        value = location,
                        onValueChange = { viewModel.locationFilter.value = it },
                        label = { Text("Filter by Location (City)") },
                        modifier = Modifier.fillMaxWidth().testTag("filter_location"),
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Place, contentDescription = "location icon") }
                    )

                    // Sorting selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Sort by Price:", style = MaterialTheme.typography.labelMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterPill(
                                text = "Standard",
                                selected = sort == "None",
                                onClick = { viewModel.sortByPrice.value = "None" }
                            )
                            FilterPill(
                                text = "$ Low to High",
                                selected = sort == "LowToHigh",
                                onClick = { viewModel.sortByPrice.value = "LowToHigh" }
                            )
                            FilterPill(
                                text = "$ High to Low",
                                selected = sort == "HighToLow",
                                onClick = { viewModel.sortByPrice.value = "HighToLow" }
                            )
                        }
                    }

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Apply Filters")
                    }
                }
            }
        }

        // Category pills
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(categories) { cat ->
                FilterPill(
                    text = cat,
                    selected = category == cat,
                    onClick = { viewModel.selectedCategory.value = cat }
                )
            }
        }

        // Provider cards list
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No results",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text("No professionals match your search", style = MaterialTheme.typography.bodyLarge)
                    Text("Try adjusting your filters or location", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(providers) { provider ->
                    ProviderCard(
                        provider = provider,
                        onClick = { viewModel.selectProvider(provider) }
                    )
                }
            }
        }
    }
}

// 2b. COMPONENT: PROVIDER CARD IN EXPLORE GRID
@Composable
fun ProviderCard(provider: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(
                backgroundColor = Color(0xFF251E2E).copy(alpha = 0.55f),
                borderColor = Color(0xFFD0BCFF).copy(alpha = 0.18f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .testTag("provider_card_${provider.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ProfileAvatar(name = provider.name, size = 64.dp)

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = provider.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "$${provider.pricePerHr.toInt()}/hr",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = provider.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                                ),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(16.dp))
                        Text(text = provider.rating.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(Icons.Default.Place, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                    Text(
                        text = provider.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1
                    )
                }

                Text(
                    text = provider.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

// 3. PROVIDER DETAIL SCREEN (PORTFOLIO, DETAILS & CHAT TRIGGER)
@Composable
fun ProviderDetailScreen(viewModel: CreativeViewModel, provider: User) {
    val portfolio by viewModel.selectedProviderPortfolio.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Back toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { viewModel.selectProvider(null) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text("Professional Profile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(48.dp)) // balancing space
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Profile Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().glassmorphic(shape = RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ProfileAvatar(name = provider.name, size = 80.dp)

                        Text(text = provider.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = provider.category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                                Text(text = provider.rating.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

                        // Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Hourly Rate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("$${provider.pricePerHr.toInt()}/hr", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Location", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(provider.location, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Completed Jobs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text(provider.jobsCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Bio Section
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Professional Bio", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier.fillMaxWidth().glassmorphic(shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Text(
                            text = provider.bio,
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Portfolio Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Portfolio & Past Works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${portfolio.size} items", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
            }

            if (portfolio.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().glassmorphic(shape = RoundedCornerShape(16.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "Empty portfolio", tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                            Text("No portfolio works shared yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            } else {
                // List of portfolio items (horizontal or beautifully formatted vertical)
                items(portfolio) { work ->
                    Card(
                        modifier = Modifier.fillMaxWidth().glassmorphic(shape = RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column {
                            // Portfolio Visual Header (Using painter resource for hero or canvas/beautiful placeholders)
                            if (work.imageUrl == "img_creative_hero") {
                                Image(
                                    painter = painterResource(id = R.drawable.img_creative_hero),
                                    contentDescription = work.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(180.dp),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                // Dynamic beautiful abstract gradient background for simulated portfolio images
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.Star, contentDescription = "Shot", tint = Color.White, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = "PORTFOLIO SHOT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }

                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = work.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                if (work.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = work.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar (Hire & Message Button)
        Card(
            modifier = Modifier.fillMaxWidth().glassmorphic(
                backgroundColor = Color(0xFF1C122C).copy(alpha = 0.88f),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { viewModel.selectProvider(null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Close")
                }

                Button(
                    onClick = {
                        // Enter Chat Screen
                        viewModel.selectChatPartner(provider)
                    },
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp)
                        .testTag("chat_now_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Chat icon")
                        Text("Instant Chat & Deal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 4. CHAT SCREEN (CONVERSATION LAYER WITH CLIENTS/PROVIDERS)
@Composable
fun ChatScreen(viewModel: CreativeViewModel, partner: User) {
    val messages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    var textMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = { viewModel.selectChatPartner(null) }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back to list")
            }

            ProfileAvatar(name = partner.name, size = 44.dp)

            Column(modifier = Modifier.weight(1f)) {
                Text(text = partner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = if (partner.userType == "PROVIDER") partner.category else "Client partner",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Messages List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.senderId == currentUser?.id
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isMe)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 2.dp,
                            bottomEnd = if (isMe) 2.dp else 16.dp
                        ),
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.message,
                                color = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        // Bottom Text Input Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textMessage,
                    onValueChange = { textMessage = it },
                    placeholder = { Text("Agree rates, scheduling and ideas...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.background,
                        unfocusedContainerColor = MaterialTheme.colorScheme.background
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                IconButton(
                    onClick = {
                        if (textMessage.isNotBlank()) {
                            viewModel.sendChatMessage(textMessage)
                            textMessage = ""
                        }
                    },
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .size(48.dp)
                        .testTag("send_chat_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

// 5. CHATS OVERVIEW / ACTIVE MESSAGES TAB
@Composable
fun ChatsOverview(viewModel: CreativeViewModel) {
    val conversations by viewModel.chattedUsers.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Your Catch Deals & Conversations", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (conversations.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.Email,
                        contentDescription = "No chats",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text("No active conversations yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Browse professionals and send a message to start deal!", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(conversations) { partner ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectChatPartner(partner) }
                            .testTag("chat_row_${partner.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ProfileAvatar(name = partner.name, size = 52.dp)

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = partner.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = if (partner.userType == "PROVIDER") partner.category else "Client partner",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Open Chat")
                        }
                    }
                }
            }
        }
    }
}

// 6. CLIENT PROFILE SCREEN
@Composable
fun ClientProfileScreen(viewModel: CreativeViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        ProfileAvatar(name = currentUser?.name ?: "Client User", size = 96.dp)

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = currentUser?.name ?: "Client User", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = "Registered Client Profile", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            Text(text = "Location: ${currentUser?.location ?: "San Francisco, CA"}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Account Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Profile Type:", style = MaterialTheme.typography.bodyMedium)
                    Text("Client (Hire Talent)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Deal Conversations:", style = MaterialTheme.typography.bodyMedium)
                    Text("Active", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("logout_button")
        ) {
            Text("Sign Out", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

// 7. PROVIDER WORKSPACE (PROFILE SETTINGS, PORTFOLIO UPLOAD, CRUD CREATION)
@Composable
fun ProviderWorkspace(viewModel: CreativeViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val portfolio by viewModel.selectedProviderPortfolio.collectAsStateWithLifecycle()

    // Trigger load of current provider's portfolio initially
    LaunchedEffect(currentUser) {
        viewModel.selectProvider(currentUser)
    }

    // Editable fields for profile settings
    var isEditingSettings by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(currentUser?.name ?: "") }
    var category by remember { mutableStateOf(currentUser?.category ?: "Photographer") }
    var location by remember { mutableStateOf(currentUser?.location ?: "") }
    var pricePerHr by remember { mutableStateOf(currentUser?.pricePerHr?.toString() ?: "50.0") }
    var bio by remember { mutableStateOf(currentUser?.bio ?: "") }

    // Portfolio adding states
    var isAddingWork by remember { mutableStateOf(false) }
    var newWorkTitle by remember { mutableStateOf("") }
    var newWorkDesc by remember { mutableStateOf("") }
    var newWorkImg by remember { mutableStateOf("img_creative_hero") }

    val categories = listOf("Photographer", "Videographer", "Designer", "Makeup Artist", "Illustrator")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "My Workspace",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.background(MaterialTheme.colorScheme.errorContainer, CircleShape)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Log out", tint = MaterialTheme.colorScheme.error)
                }
            }
        }

        // Success dialog/card feedback
        if (successMessage != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = successMessage ?: "", color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { viewModel.clearFeedback() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Dismiss", tint = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }

        // Workspace Profile Stats Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    ProfileAvatar(name = currentUser?.name ?: "Provider", size = 64.dp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = currentUser?.name ?: "Creative Pro", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = currentUser?.category ?: "Photographer",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp))
                                Text(text = currentUser?.rating.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(
                            text = "Rate: $${currentUser?.pricePerHr?.toInt()}/hr | ${currentUser?.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // PROFILE EDIT ACCORDION
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Profile Settings", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        IconButton(
                            onClick = {
                                if (isEditingSettings) {
                                    // Save changes
                                    viewModel.updateProfile(
                                        name = name,
                                        category = category,
                                        location = location,
                                        price = pricePerHr.toDoubleOrNull() ?: 50.0,
                                        bio = bio
                                    )
                                    isEditingSettings = false
                                } else {
                                    // Enter edit mode, pull current values
                                    name = currentUser?.name ?: ""
                                    category = currentUser?.category ?: "Photographer"
                                    location = currentUser?.location ?: ""
                                    pricePerHr = currentUser?.pricePerHr?.toString() ?: "50.0"
                                    bio = currentUser?.bio ?: ""
                                    isEditingSettings = true
                                }
                            },
                            modifier = Modifier.testTag("edit_settings_toggle")
                        ) {
                            Icon(
                                if (isEditingSettings) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isEditingSettings) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Display Name") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_name"),
                                singleLine = true
                            )

                            Column {
                                Text("Professional Category:", style = MaterialTheme.typography.labelSmall)
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    items(categories) { cat ->
                                        FilterPill(
                                            text = cat,
                                            selected = category == cat,
                                            onClick = { category = cat }
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = { Text("Location") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_location"),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = pricePerHr,
                                onValueChange = { pricePerHr = it },
                                label = { Text("Hourly Rate ($)") },
                                modifier = Modifier.fillMaxWidth().testTag("edit_price"),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Professional Biography") },
                                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("edit_bio"),
                                maxLines = 4
                            )
                        }
                    } else {
                        // Display bio summary
                        Text(
                            text = currentUser?.bio ?: "No bio defined. Add one to attract clients!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // PORTFOLIO MANAGER SECTION (ADD & DELETE WORKS)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Manage Portfolio Works", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Button(
                    onClick = { isAddingWork = !isAddingWork },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_work_toggle_button")
                ) {
                    Icon(
                        if (isAddingWork) Icons.Default.Close else Icons.Default.Add,
                        contentDescription = "Add work icon",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isAddingWork) "Cancel" else "Add Work", fontSize = 12.sp)
                }
            }
        }

        if (isAddingWork) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Share New Creation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                        OutlinedTextField(
                            value = newWorkTitle,
                            onValueChange = { newWorkTitle = it },
                            label = { Text("Work/Shot Title") },
                            modifier = Modifier.fillMaxWidth().testTag("new_work_title"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newWorkDesc,
                            onValueChange = { newWorkDesc = it },
                            label = { Text("Description (Optional)") },
                            modifier = Modifier.fillMaxWidth().testTag("new_work_desc"),
                            singleLine = true
                        )

                        // Sample quick layout choice
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Mock Theme:", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterPill(
                                    text = "Main Banner",
                                    selected = newWorkImg == "img_creative_hero",
                                    onClick = { newWorkImg = "img_creative_hero" }
                                )
                                FilterPill(
                                    text = "Abstract",
                                    selected = newWorkImg == "abstract",
                                    onClick = { newWorkImg = "abstract" }
                                )
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.addPortfolioWork(
                                    title = newWorkTitle,
                                    imageUrl = newWorkImg,
                                    description = newWorkDesc
                                )
                                newWorkTitle = ""
                                newWorkDesc = ""
                                isAddingWork = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_work_button")
                        ) {
                            Text("Publish Work & Showcase")
                        }
                    }
                }
            }
        }

        // Showcase listing
        if (portfolio.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Your showcase is empty. Publish your first work above!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        } else {
            items(portfolio) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // thumbnail preview
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (item.imageUrl == "img_creative_hero")
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Work icon", tint = MaterialTheme.colorScheme.primary)
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            if (item.description.isNotBlank()) {
                                Text(text = item.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }

                        IconButton(
                            onClick = { viewModel.removePortfolioWork(item) },
                            modifier = Modifier.testTag("delete_work_${item.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Work", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// SUB-COMPONENTS & UTILS
// ==========================================

@Composable
fun TabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundModifier = if (selected) {
        Modifier.background(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFFD0BCFF),
                    Color(0xFFB6A3E2)
                )
            )
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .then(backgroundModifier)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF381E72) else Color(0xFFE6E1E5).copy(alpha = 0.7f),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun FilterPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (selected) {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFFD0BCFF),
                            Color(0xFFB6A3E2)
                        )
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF251E2E).copy(alpha = 0.5f),
                            Color(0xFF1F1A24).copy(alpha = 0.4f)
                        )
                    )
                }
            )
            .border(
                1.dp,
                if (selected) Color(0xFFD0BCFF).copy(alpha = 0.4f) else Color(0xFFD0BCFF).copy(alpha = 0.15f),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color(0xFF381E72) else Color(0xFFE6E1E5),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
fun ProfileAvatar(name: String, modifier: Modifier = Modifier, size: Dp = 48.dp) {
    // Generate simple high-end initial avatar using vibrant gradient
    val initials = name.split(" ")
        .mapNotNull { it.firstOrNull() }
        .joinToString("")
        .take(2)
        .uppercase()

    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.secondary
                    )
                ),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.35f).sp
        )
    }
}
