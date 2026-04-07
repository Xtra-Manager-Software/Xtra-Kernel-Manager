package id.xms.xtrakernelmanager.ui.screens.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import compose.icons.SimpleIcons
import compose.icons.simpleicons.Github
import compose.icons.simpleicons.Telegram
import id.xms.xtrakernelmanager.BuildConfig
import id.xms.xtrakernelmanager.R
import id.xms.xtrakernelmanager.ui.theme.ClassicColors
import id.xms.xtrakernelmanager.ui.theme.ExpressiveShapes

private val teamMembers =
    listOf(
        TeamMember(
            R.drawable.logo_a,
            "Gustyx-Power",
            "Founder & Dev",
            githubUrl = "https://github.com/Gustyx-Power",
            telegramUrl = "https://t.me/GustyxPower",
            githubUsername = "Gustyx-Power",
            shapeIndex = 19,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Pavelc4",
            "Founder & UI/UX",
            githubUrl = "https://github.com/Pavelc4",
            telegramUrl = "https://t.me/Pavellc",
            githubUsername = "Pavelc4",
            shapeIndex = 30,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Ziyu",
            "Contributor",
            githubUrl = "https://github.com/ziyu4",
            telegramUrl = "https://t.me/ziyu4",
            githubUsername = "ziyu4",
            shapeIndex = 19,
        ),
        TeamMember(
            R.drawable.team_contributor_rio,
            "Rio",
            "Contributor",
            telegramUrl = "https://t.me/hy6nies",
            shapeIndex = 16,
        ),
        TeamMember(
            R.drawable.team_contributor_shimoku,
            "Shimoku",
            "Contributor",
            githubUrl = "https://github.com/shimokuu",
            telegramUrl = "https://t.me/xdshimokuu",
            shapeIndex = 3,
        ),
        TeamMember(
            R.drawable.logo_a,
            "Wil",
            "Tester",
            githubUrl = "https://github.com/Steambot12",
            telegramUrl = "https://t.me/Steambot12",
            githubUsername = "Steambot12",
            shapeIndex = 20,
        ),
        TeamMember(
            R.drawable.logo_a,
            "ᴶᵁᴻᴵ༄",
            "Tester",
            githubUrl = "https://github.com/juns37",
            telegramUrl = "https://t.me/juns37",
            githubUsername = "juns37",
            shapeIndex = 20,
        ),
        TeamMember(
            R.drawable.team_tester_achmad,
            "Achmadh",
            "Tester",
            shapeIndex = 5,
        ),
        TeamMember(
            R.drawable.team_tester_hasan,
            "Hasan",
            "Tester",
            shapeIndex = 16,
        ),
        TeamMember(
            R.drawable.team_tester_reffan,
            "Reffan Lintang M",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_tester_sleep,
            "Adi Suki",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_tester_azhar,
            "Azhar",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.logo_a,
            "NTT Rules",
            "Tester",
            shapeIndex = 13,
        ),
        TeamMember(
            R.drawable.team_sm_tester,
            "Muttahir",
            "Tester",
            shapeIndex = 10,
        ),
    )

@Composable
fun ClassicInfoScreen(
    onNavigateToWebView: () -> Unit = {},
    onNavigateToLicense: () -> Unit = {},
    onNavigateToSystemInfo: () -> Unit = {}
) {
    val uriHandler = LocalUriHandler.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClassicColors.Background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with About title
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = ClassicColors.SurfaceContainerHigh
            ) {
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            }

            androidx.compose.foundation.lazy.LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 100.dp)
            ) {
                // Hero Card
                item {
                    ClassicHeroCard(onClick = onNavigateToSystemInfo)
                    Spacer(modifier = Modifier.height(16.dp))
                }

            // Device Info Grid (2 columns)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClassicInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.PhoneAndroid,
                        label = "App Name",
                        value = "XKM"
                    )
                    ClassicInfoCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Storage,
                        label = "Version",
                        value = BuildConfig.VERSION_NAME
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Specifications List
            item {
                ClassicSpecItem(
                    label = "Build Type",
                    value = BuildConfig.BUILD_TYPE.uppercase()
                )
            }

            item {
                ClassicSpecItem(
                    label = "License",
                    value = "MIT License",
                    onClick = { onNavigateToLicense() }
                )
            }

            item {
                ClassicSpecItem(
                    label = "Community",
                    value = "Telegram Channel",
                    onClick = { uriHandler.openUri("https://t.me/CH_XtraManagerSoftware") }
                )
            }

            item {
                ClassicSpecItem(
                    label = "Website",
                    value = "xtramanagersoftwares.tech",
                    onClick = { onNavigateToWebView() }
                )
            }

            item {
                ClassicSpecItem(
                    label = "Copyright",
                    value = "© 2025 XMS"
                )
            }

            // Team Section
            item {
                Spacer(modifier = Modifier.height(24.dp))
                ClassicSectionHeader("Team")
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Team Grid
            items(teamMembers.chunked(2)) { rowMembers ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowMembers.forEach { member ->
                        Box(modifier = Modifier.weight(1f)) {
                            ClassicTeamMemberCard(member, uriHandler)
                        }
                    }
                    // Fill empty space if odd number
                    if (rowMembers.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
    }
}

@Composable
private fun ClassicHeroCard(onClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceContainerHigh
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ClassicColors.Primary.copy(alpha = 0.2f),
                            ClassicColors.Secondary.copy(alpha = 0.15f),
                            ClassicColors.SurfaceContainerHigh
                        )
                    )
                )
        ) {
            // Content - Centered
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Logo
                Icon(
                    painter = painterResource(R.drawable.logo_a),
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Unspecified
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // App name
                Text(
                    text = "XKM",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    ),
                    color = ClassicColors.OnSurface,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Xtra Kernel Manager",
                    style = MaterialTheme.typography.titleLarge,
                    color = ClassicColors.OnSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "${BuildConfig.VERSION_NAME} | ${BuildConfig.BUILD_TYPE.uppercase()}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom: Status
                Text(
                    text = "Version up to date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun ClassicInfoCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ClassicColors.OnSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = ClassicColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ClassicSpecItem(
    label: String,
    value: String,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = ClassicColors.OnSurface,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClassicColors.OnSurfaceVariant,
                    textAlign = TextAlign.End
                )
                
                if (onClick != null) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = ClassicColors.OnSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun ClassicSectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ClassicColors.OnSurface,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 12.dp),
    )
}

@Composable
private fun ClassicTeamMemberCard(
    member: TeamMember,
    uriHandler: androidx.compose.ui.platform.UriHandler,
) {
    val memberShape = remember(member.shapeIndex) { ExpressiveShapes.getShape(member.shapeIndex) }
    val hasSocial = member.githubUrl != null || member.telegramUrl != null

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(16.dp),
        color = ClassicColors.SurfaceContainerHigh
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(memberShape)
                    .background(ClassicColors.SurfaceContainer)
            ) {
                if (member.githubUsername != null) {
                    AsyncImage(
                        model = "https://github.com/${member.githubUsername}.png",
                        contentDescription = "${member.name} avatar",
                        placeholder = painterResource(member.imageRes),
                        error = painterResource(member.imageRes),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                } else {
                    Image(
                        painter = painterResource(member.imageRes),
                        contentDescription = "${member.name} avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClassicColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = member.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClassicColors.OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (hasSocial) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        member.githubUrl?.let { url ->
                            IconButton(
                                onClick = { uriHandler.openUri(url) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = SimpleIcons.Github,
                                    contentDescription = "GitHub",
                                    tint = ClassicColors.OnSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }

                        member.telegramUrl?.let { url ->
                            IconButton(
                                onClick = { uriHandler.openUri(url) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = SimpleIcons.Telegram,
                                    contentDescription = "Telegram",
                                    tint = ClassicColors.OnSurfaceVariant,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
