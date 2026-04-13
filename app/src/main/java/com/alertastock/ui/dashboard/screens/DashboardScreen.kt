package com.alertastock.ui.dashboard.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alertastock.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    onCerrarSesion: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val usuario = auth.currentUser
    val nombre = usuario?.displayName?.split(" ")?.firstOrNull()
        ?: usuario?.email?.substringBefore("@")
        ?: "Usuario"

    // Saludo según hora
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val saludo = when {
        hora < 12 -> "Buenos días,"
        hora < 18 -> "Buenas tardes,"
        else -> "Buenas noches,"
    }

    // Fecha actual
    val formato = SimpleDateFormat("EEEE, d 'de' MMMM yyyy", Locale("es", "CO"))
    val fecha = formato.format(Date()).replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgScreen)
            .verticalScroll(rememberScrollState())
    ) {
        // HEADER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(BlueDark, Blue)
                    )
                )
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 48.dp,
                    bottom = 20.dp
                )
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "$saludo $nombre",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = fecha,
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    // Avatar + cerrar sesión
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Cerrar sesión
                        IconButton(
                            onClick = {
                                auth.signOut()
                                onCerrarSesion()
                            }
                        ) {
                            Icon(
                                Icons.Default.ExitToApp,
                                contentDescription = "Cerrar sesion",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Avatar
                        Card(
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White.copy(alpha = 0.25f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = nombre.first().uppercase(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // RESUMEN GENERAL
        Text(
            text = "RESUMEN GENERAL",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 20.dp,
                bottom = 10.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaEstadistica(
                emoji = "📦",
                valor = "0",
                etiqueta = "Productos totales",
                color = Blue,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                emoji = "⚠️",
                valor = "0",
                etiqueta = "Por agotarse",
                color = Red,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaEstadistica(
                emoji = "📅",
                valor = "0",
                etiqueta = "Por vencer",
                color = Yellow,
                modifier = Modifier.weight(1f)
            )
            TarjetaEstadistica(
                emoji = "✅",
                valor = "0",
                etiqueta = "En buen estado",
                color = Green,
                modifier = Modifier.weight(1f)
            )
        }

        // ACCESO RÁPIDO
        Text(
            text = "ACCESO RÁPIDO",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextSecondary,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(
                start = 20.dp,
                top = 20.dp,
                bottom = 10.dp
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccesoRapido(
                emoji = "📦",
                label = "Productos",
                color = Blue,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            AccesoRapido(
                emoji = "📷",
                label = "Escanear",
                color = Green,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            AccesoRapido(
                emoji = "🔔",
                label = "Alertas",
                color = Red,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
            AccesoRapido(
                emoji = "⚙️",
                label = "Configurar",
                color = Yellow,
                modifier = Modifier.weight(1f),
                onClick = {}
            )
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TarjetaEstadistica(
    emoji: String,
    valor: String,
    etiqueta: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Card(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = valor,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = etiqueta,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AccesoRapido(
    emoji: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}