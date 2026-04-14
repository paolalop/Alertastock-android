package com.alertastock.ui.product.screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alertastock.data.model.Producto
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.theme.*

enum class FiltroProducto { TODOS, CRITICO, BAJO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onAtras: () -> Unit,
    onAgregarProducto: () -> Unit = {},
    onEditarProducto: (Producto) -> Unit = {},
    viewModel: ProductoViewModel = viewModel()
) {
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())
    val productosCriticos by viewModel.productosCriticos.observeAsState(emptyList())

    var busqueda by remember { mutableStateOf("") }
    var filtroActivo by remember { mutableStateOf(FiltroProducto.TODOS) }
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    // Derived filter lists
    val productosBajos = remember(todosLosProductos) {
        todosLosProductos.filter { it.stockActual > it.stockMinimo && it.stockActual <= it.stockMinimo * 2 }
    }

    val listaFiltrada = remember(busqueda, filtroActivo, todosLosProductos, productosCriticos, productosBajos) {
        val base = when (filtroActivo) {
            FiltroProducto.TODOS -> todosLosProductos
            FiltroProducto.CRITICO -> productosCriticos
            FiltroProducto.BAJO -> productosBajos
        }
        if (busqueda.isBlank()) base
        else base.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.codigoBarras.contains(busqueda, ignoreCase = true) ||
                    it.categoria.contains(busqueda, ignoreCase = true)
        }
    }

    // Confirm delete dialog
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = BgCard,
            title = {
                Text("Eliminar producto", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Seguro que deseas eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminar(producto)
                        productoAEliminar = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) {
                    Text("Eliminar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        containerColor = BgScreen,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarProducto,
                containerColor = Blue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar producto", modifier = Modifier.size(28.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
        ) {
            // HEADER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onAtras) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Atrás",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "Inventario",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    // Filter icon
                    IconButton(
                        onClick = { /* future: sort options */ }
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            // SEARCH BAR
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Buscar producto...", color = TextHint)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextHint)
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpiar", tint = TextHint)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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
            }

            // FILTER TABS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroChip(
                    texto = "Todos (${todosLosProductos.size})",
                    activo = filtroActivo == FiltroProducto.TODOS,
                    color = Blue,
                    onClick = { filtroActivo = FiltroProducto.TODOS }
                )
                FiltroChip(
                    texto = "Crítico (${productosCriticos.size})",
                    activo = filtroActivo == FiltroProducto.CRITICO,
                    color = Red,
                    onClick = { filtroActivo = FiltroProducto.CRITICO }
                )
                FiltroChip(
                    texto = "Bajo (${productosBajos.size})",
                    activo = filtroActivo == FiltroProducto.BAJO,
                    color = Yellow,
                    onClick = { filtroActivo = FiltroProducto.BAJO }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // PRODUCT LIST
            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (busqueda.isNotEmpty()) "Sin resultados para \"$busqueda\""
                            else "No hay productos aún",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        if (busqueda.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Toca + para agregar tu primer producto",
                                color = TextHint,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(listaFiltrada, key = { it.id }) { producto ->
                        ProductoCard(
                            producto = producto,
                            onEditar = { onEditarProducto(producto) },
                            onEliminar = { productoAEliminar = producto }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
fun FiltroChip(
    texto: String,
    activo: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (activo) color.copy(alpha = 0.2f) else BgCard,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (activo) color else BorderMedium
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            fontSize = 12.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = if (activo) color else TextSecondary
        )
    }
}

@Composable
fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    // Stock status color
    val stockColor = when {
        producto.stockActual <= producto.stockMinimo -> Red
        producto.stockActual <= producto.stockMinimo * 2 -> Yellow
        else -> Green
    }

    val stockProgress = if (producto.stockMinimo > 0) {
        (producto.stockActual.toFloat() / (producto.stockMinimo * 3).toFloat()).coerceIn(0f, 1f)
    } else 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(stockColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = producto.emoji, fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Product info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = producto.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Stock: ${producto.stockActual} unid.  ·  Mín: ${producto.stockMinimo}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(BorderMedium)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stockProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(2.dp))
                            .background(stockColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Stock dot indicator
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(stockColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Action buttons
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Edit button
                Button(
                    onClick = onEditar,
                    modifier = Modifier
                        .width(72.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Delete button
                Button(
                    onClick = onEliminar,
                    modifier = Modifier
                        .width(72.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Borrar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}