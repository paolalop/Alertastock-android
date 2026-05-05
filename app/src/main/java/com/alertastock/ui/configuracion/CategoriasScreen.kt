package com.alertastock.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.repository.ProductoRepository
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.theme.*

// Colores disponibles para categorías
val COLORES_CATEGORIA = listOf(
    Color(0xFF2E67F8), // Azul
    Color(0xFF43A047), // Verde
    Color(0xFFFB8C00), // Naranja
    Color(0xFFE53935), // Rojo
    Color(0xFF9C27B0), // Morado
    Color(0xFFFFB300)  // Amarillo
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    onAtras: () -> Unit,
    viewModel: ProductoViewModel = viewModel()
) {
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())

    // Categorías únicas con conteo de productos
    val categorias = remember(todosLosProductos) {
        todosLosProductos
            .groupBy { it.categoria }
            .filter { it.key.isNotBlank() }
            .map { (nombre, productos) -> Pair(nombre, productos.size) }
            .sortedBy { it.first }
    }

    var mostrarFormulario by remember { mutableStateOf(false) }
    var categoriaEnEdicion by remember { mutableStateOf<String?>(null) }
    var nombreNuevaCategoria by remember { mutableStateOf("") }
    var colorSeleccionado by remember { mutableStateOf(COLORES_CATEGORIA[0]) }
    var mensajeExito by remember { mutableStateOf<String?>(null) }
    var mostrarEliminar by remember { mutableStateOf<String?>(null) }

    // Diálogo confirmar eliminar
    mostrarEliminar?.let { categoria ->
        AlertDialog(
            onDismissRequest = { mostrarEliminar = null },
            containerColor = BgCard,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Red.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Red, modifier = Modifier.size(28.dp))
                }
            },
            title = { Text("¿Eliminar Categoría?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Estás seguro que deseas eliminar esta categoría? Si la eliminas, ya no aparecerá en el listado de categorías.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { mostrarEliminar = null },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Sí, eliminar", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarEliminar = null }, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Bottom Sheet — Nueva / Editar categoría
    if (mostrarFormulario) {
        ModalBottomSheet(
            onDismissRequest = {
                mostrarFormulario = false
                categoriaEnEdicion = null
                nombreNuevaCategoria = ""
                colorSeleccionado = COLORES_CATEGORIA[0]
            },
            containerColor = BgCard,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (categoriaEnEdicion == null) "Nueva Categoría" else "Editar Categoría",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = if (categoriaEnEdicion == null)
                        "Crea una categoría para organizar tus productos"
                    else "Los cambios afectarán los productos de esta categoría",
                    fontSize = 13.sp,
                    color = if (categoriaEnEdicion == null) TextSecondary else Color(0xFFFB8C00),
                    modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                )

                // Nombre
                Text("NOMBRE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombreNuevaCategoria,
                    onValueChange = { nombreNuevaCategoria = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ej: Carnes y embutidos", color = TextHint) },
                    leadingIcon = {
                        Icon(Icons.Default.Category, contentDescription = null, tint = TextHint, modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgInput,
                        unfocusedContainerColor = BgInput,
                        focusedBorderColor = Blue,
                        unfocusedBorderColor = BorderMedium,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = Blue
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Selector de color
                Text("COLOR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    COLORES_CATEGORIA.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(if (colorSeleccionado == color) 36.dp else 30.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { colorSeleccionado = color },
                            contentAlignment = Alignment.Center
                        ) {
                            if (colorSeleccionado == color) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (categoriaEnEdicion != null) {
                    // Edición — botones Guardar y Eliminar
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                if (nombreNuevaCategoria.isNotBlank()) {
                                    mensajeExito = "Guardado correctamente"
                                    mostrarFormulario = false
                                    categoriaEnEdicion = null
                                    nombreNuevaCategoria = ""
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Blue)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Guardar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Button(
                            onClick = {
                                mostrarEliminar = categoriaEnEdicion
                                mostrarFormulario = false
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Red)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Eliminar", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = {
                            mostrarFormulario = false
                            categoriaEnEdicion = null
                            nombreNuevaCategoria = ""
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar", color = TextSecondary) }
                } else {
                    // Nueva — botón Crear
                    Button(
                        onClick = {
                            if (nombreNuevaCategoria.isNotBlank()) {
                                mensajeExito = "Categoría agregada correctamente"
                                mostrarFormulario = false
                                nombreNuevaCategoria = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Green),
                        enabled = nombreNuevaCategoria.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Crear categoría +", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(
                        onClick = { mostrarFormulario = false; nombreNuevaCategoria = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Cancelar", color = TextSecondary) }
                }
            }
        }
    }

    Scaffold(
        containerColor = BgScreen,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoriaEnEdicion = null
                    nombreNuevaCategoria = ""
                    colorSeleccionado = COLORES_CATEGORIA[0]
                    mostrarFormulario = true
                },
                containerColor = Blue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAtras) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Atrás", tint = TextPrimary)
                    }
                    Text(
                        "Categorías",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Banner éxito
            mensajeExito?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    mensajeExito = null
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Green.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Green, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(msg, color = Green, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (categorias.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗂️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No hay categorías aún", color = TextSecondary, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Toca + para crear tu primera categoría", color = TextHint, fontSize = 13.sp)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categorias) { (nombre, conteo) ->
                        val colorIndex = categorias.indexOf(Pair(nombre, conteo)) % COLORES_CATEGORIA.size
                        val color = COLORES_CATEGORIA[colorIndex]

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    categoriaEnEdicion = nombre
                                    nombreNuevaCategoria = nombre
                                    colorSeleccionado = color
                                    mostrarFormulario = true
                                },
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Category,
                                        contentDescription = null,
                                        tint = color,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(nombre, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("$conteo productos", fontSize = 12.sp, color = TextSecondary)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}