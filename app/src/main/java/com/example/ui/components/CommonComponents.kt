package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.CompanyEntity
import com.example.data.local.UserEntity
import com.example.ui.Screen
import com.example.ui.theme.*

@Composable
fun DocuPymeTopBar(
    currentScreen: Screen,
    company: CompanyEntity?,
    user: UserEntity?,
    unreadNotificationsCount: Int,
    onBackClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onChatbotClick: () -> Unit,
    onSwitchCompanyClick: () -> Unit,
    onSwitchUserClick: () -> Unit,
    onAuditClick: () -> Unit
) {
    val showBack = currentScreen != Screen.DASHBOARD &&
            currentScreen != Screen.SEARCH &&
            currentScreen != Screen.DOCUMENTS &&
            currentScreen != Screen.USERS_PERMISSIONS &&
            currentScreen != Screen.CHATBOT

    Surface(
        color = DocuNavy,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (showBack) {
                        IconButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .testTag("top_bar_back_button")
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DocuGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "DocuPyme",
                                tint = DocuNavy,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSwitchCompanyClick() }
                        ) {
                            Text(
                                text = company?.name ?: "DocuPyme",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Cambiar empresa",
                                tint = DocuGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { onSwitchUserClick() }
                        ) {
                            Text(
                                text = "${user?.name ?: "Usuario"} • ",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = when (user?.role) {
                                    "Administrador" -> DocuGold
                                    "Editor" -> Color(0xFF60A5FA)
                                    else -> Color(0xFF9CA3AF)
                                }
                            ) {
                                Text(
                                    text = user?.role ?: "Consulta",
                                    color = DocuNavy,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Chatbot Quick Access Button
                    IconButton(
                        onClick = onChatbotClick,
                        modifier = Modifier
                            .testTag("top_bar_chatbot_button")
                            .size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "DocuBot IA",
                            tint = if (currentScreen == Screen.CHATBOT) DocuGold else Color.White
                        )
                    }

                    // Audit log button
                    IconButton(
                        onClick = onAuditClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Auditoría",
                            tint = Color.White.copy(alpha = 0.9f)
                        )
                    }

                    // Notifications bell with badge
                    Box {
                        IconButton(
                            onClick = onNotificationsClick,
                            modifier = Modifier
                                .testTag("notifications_button")
                                .size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notificaciones",
                                tint = Color.White
                            )
                        }
                        if (unreadNotificationsCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 6.dp, end = 6.dp)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(DocuGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = unreadNotificationsCount.toString(),
                                    color = DocuNavy,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DocuPymeBottomNav(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    Surface(
        color = DocuNavy,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Inicio",
                isSelected = currentScreen == Screen.DASHBOARD,
                testTag = "nav_home",
                onClick = { onNavigate(Screen.DASHBOARD) }
            )
            BottomNavItem(
                icon = Icons.Default.Search,
                label = "Buscar",
                isSelected = currentScreen == Screen.SEARCH,
                testTag = "nav_search",
                onClick = { onNavigate(Screen.SEARCH) }
            )
            BottomNavItem(
                icon = Icons.Default.SmartToy,
                label = "DocuBot",
                isSelected = currentScreen == Screen.CHATBOT,
                testTag = "nav_chatbot",
                onClick = { onNavigate(Screen.CHATBOT) }
            )
            BottomNavItem(
                icon = Icons.Default.Folder,
                label = "Carpetas",
                isSelected = currentScreen == Screen.DOCUMENTS || currentScreen == Screen.FOLDER_CONTENT,
                testTag = "nav_documents",
                onClick = { onNavigate(Screen.DOCUMENTS) }
            )
            BottomNavItem(
                icon = Icons.Default.Settings,
                label = "Ajustes",
                isSelected = currentScreen == Screen.USERS_PERMISSIONS ||
                        currentScreen == Screen.BACKUP_SECURITY ||
                        currentScreen == Screen.EDIT_PROFILE ||
                        currentScreen == Screen.REPORTS,
                testTag = "nav_settings",
                onClick = { onNavigate(Screen.USERS_PERMISSIONS) }
            )
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .testTag(testTag)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DocuGold else Color.White.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) DocuGold else Color.White.copy(alpha = 0.6f)
        )
    }
}
