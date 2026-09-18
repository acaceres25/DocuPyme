package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.theme.*

@Composable
fun ProfileScreen(
    user: UserEntity?,
    company: CompanyEntity?,
    onBack: () -> Unit,
    onSaveProfile: (name: String, cargo: String) -> Unit
) {
    var name by remember(user) { mutableStateOf(user?.name ?: "") }
    var cargo by remember(user) { mutableStateOf(user?.cargo ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DocuBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = DocuNavy)
                }
                Text("Editar perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DocuNavy)
            }
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    Box(
                        modifier = Modifier.size(90.dp).clip(CircleShape).background(DocuNavy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(name.take(2).uppercase(), color = DocuGold, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(DocuGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Cambiar", tint = DocuNavy, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = user?.email ?: "",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Correo electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = company?.name ?: "",
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Empresa") },
                        leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = cargo,
                        onValueChange = { cargo = it },
                        label = { Text("Cargo en la empresa") },
                        leadingIcon = { Icon(Icons.Default.Work, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Button(
                onClick = { onSaveProfile(name, cargo) },
                colors = ButtonDefaults.buttonColors(containerColor = DocuGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Guardar cambios", color = DocuNavy, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
