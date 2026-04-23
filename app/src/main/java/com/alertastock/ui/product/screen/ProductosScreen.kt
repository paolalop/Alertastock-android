package com.alertastock.ui.product.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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

enum class FiltroEstado { TODOS, CRITICO, BAJO, POR_VENCER, BUEN_ESTADO }
enum class OrdenarPor { NOMBRE, STOCK_ASC, STOCK_DESC, VENCIMIENTO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onAtras: () -> Unit,
    onAgregarProducto: () -> Unit = {},
    onEditarProducto: (Producto) -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    filtroInicial: String = "TODOS"
) {
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())
    val productosCriticos by viewModel.productosCriticos.observeAsState(emptyList())

    var busqueda by remember { mutableStateOf("") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    // Filtros APLICADOS
    var filtroEstadoAplicado by remember {
        mutableStateOf(
            when (filtroInicial) {
                "CRITICO"     -> FiltroEstado.CRITICO
                "BAJO"        -> FiltroEstado.BAJO
                "POR_VENCER"  -> FiltroEstado.POR_VENCER
                "BUEN_ESTADO" -> FiltroEstado.BUEN_ESTADO
                else          -> FiltroEstado.TODOS
            }
        )
    }
    var categoriaAplicada by remember { mutableStateOf("") }
    var ordenAplicado by remember { mutableStateOf(OrdenarPor.NOMBRE) }

    // Filtros TEMPORALES
    var filtroEstadoTemp by remember { mutableStateOf(filtroEstadoAplicado) }
    var categoriaTemp by remember { mutableStateOf("") }
    var ordenTemp by remember { mutableStateOf(OrdenarPor.NOMBRE) }

    // Listas derivadas
    val productosBajos = remember(todosLosProductos) {
        todosLosProductos.filter {
            it.stockActual > it.stockMinimo && it.stockActual <= it.stockMinimo * 2
        }
    }

    val productosPorVencer = remember(todosLosProductos) {
        todosLosProductos.filter { it.estaVenciendo }
    }

    val productosEnBuenEstado = remember(todosLosProductos) {
        todosLosProductos.filter {
            it.stockActual > it.stockMinimo * 2 && !it.estaVenciendo
        }
    }

    val categorias = remember(todosLosProductos) {
        todosLosProductos
            .map { it.categoria }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    // Lista final filtrada y ordenada
    val listaFiltrada = remember(
        busqueda, filtroEstadoAplicado, categoriaAplicada, ordenAplicado,
        todosLosProductos, productosCriticos, productosBajos, productosPorVencer, productosEnBuenEstado
    ) {
        val base = when (filtroEstadoAplicado) {
            FiltroEstado.TODOS       -> todosLosProductos
            FiltroEstado.CRITICO     -> productosCriticos
            FiltroEstado.BAJO        -> productosBajos
            FiltroEstado.POR_VENCER  -> productosPorVencer
            FiltroEstado.BUEN_ESTADO -> productosEnBuenEstado
        }
        val porCategoria = if (categoriaAplicada.isBlank()) base
        else base.filter { it.categoria == categoriaAplicada }

        val porBusqueda = if (busqueda.isBlank()) porCategoria
        else porCategoria.filter {
            it.nombre.contains(busqueda, ignoreCase = true) ||
                    it.codigoBarras.contains(busqueda, ignoreCase = true) ||
                    it.categoria.contains(busqueda, ignoreCase = true)
        }
        when (ordenAplicado) {
            OrdenarPor.NOMBRE      -> porBusqueda.sortedBy { it.nombre.lowercase() }
            OrdenarPor.STOCK_ASC   -> porBusqueda.sortedBy { it.stockActual }
            OrdenarPor.STOCK_DESC  -> porBusqueda.sortedByDescending { it.stockActual }
            OrdenarPor.VENCIMIENTO -> porBusqueda.sortedWith(
                compareBy({ it.fechaVencimiento.isBlank() }, { it.fechaVencimiento })
            )
        }
    }

    val hayFiltrosSheet = categoriaAplicada.isNotBlank() || ordenAplicado != OrdenarPor.NOMBRE

    // Diálogo confirmar eliminar
    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }
    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = BgCard,
            title = { Text("Eliminar producto", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Seguro que deseas eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.eliminar(producto); productoAEliminar = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Red)
                ) { Text("Eliminar", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { productoAEliminar = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }

    // Bottom Sheet
    if (mostrarFiltros) {
        ModalBottomSheet(
            onDismissRequest = { mostrarFiltros = false },
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
                    text = "Filtrar y ordenar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Estado
                Text("ESTADO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { BottomSheetChip("Todos",       filtroEstadoTemp == FiltroEstado.TODOS)       { filtroEstadoTemp = FiltroEstado.TODOS } }
                    item { BottomSheetChip("Crítico",     filtroEstadoTemp == FiltroEstado.CRITICO)     { filtroEstadoTemp = FiltroEstado.CRITICO } }
                    item { BottomSheetChip("Bajo",        filtroEstadoTemp == FiltroEstado.BAJO)        { filtroEstadoTemp = FiltroEstado.BAJO } }
                    item { BottomSheetChip("Por vencer",  filtroEstadoTemp == FiltroEstado.POR_VENCER)  { filtroEstadoTemp = FiltroEstado.POR_VENCER } }
                    item { BottomSheetChip("Buen estado", filtroEstadoTemp == FiltroEstado.BUEN_ESTADO) { filtroEstadoTemp = FiltroEstado.BUEN_ESTADO } }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Categoría
                Text("CATEGORÍA", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                if (categorias.isEmpty()) {
                    Text(text = "No hay categorías aún", fontSize = 13.sp, color = TextHint)
                } else {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(categorias) { categoria ->
                            BottomSheetChip(
                                texto = categoria,
                                activo = categoriaTemp == categoria,
                                onClick = { categoriaTemp = if (categoriaTemp == categoria) "" else categoria }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Ordenar por
                Text("ORDENAR POR", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 0.8.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BottomSheetChip("Nombre",  ordenTemp == OrdenarPor.NOMBRE)      { ordenTemp = OrdenarPor.NOMBRE }
                    BottomSheetChip("Stock ↑", ordenTemp == OrdenarPor.STOCK_ASC)   { ordenTemp = OrdenarPor.STOCK_ASC }
                    BottomSheetChip("Stock ↓", ordenTemp == OrdenarPor.STOCK_DESC)  { ordenTemp = OrdenarPor.STOCK_DESC }
                    BottomSheetChip("Venc.",   ordenTemp == OrdenarPor.VENCIMIENTO) { ordenTemp = OrdenarPor.VENCIMIENTO }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = {
                        filtroEstadoAplicado = filtroEstadoTemp
                        categoriaAplicada = categoriaTemp
                        ordenAplicado = ordenTemp
                        mostrarFiltros = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue)
                ) {
                    Text("Aplicar filtros", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        filtroEstadoTemp = FiltroEstado.TODOS
                        categoriaTemp = ""
                        ordenTemp = OrdenarPor.NOMBRE
                        filtroEstadoAplicado = FiltroEstado.TODOS
                        categoriaAplicada = ""
                        ordenAplicado = OrdenarPor.NOMBRE
                        mostrarFiltros = false
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                ) {
                    Text("Limpiar filtros", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Pantalla principal
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
                        text = "Inventario",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Box {
                        IconButton(
                            onClick = {
                                filtroEstadoTemp = filtroEstadoAplicado
                                categoriaTemp = categoriaAplicada
                                ordenTemp = ordenAplicado
                                mostrarFiltros = true
                            }
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtrar",
                                tint = if (hayFiltrosSheet) Blue else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        if (hayFiltrosSheet) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Blue)
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-6).dp, y = 6.dp)
                            )
                        }
                    }
                }
            }

            // Barra de búsqueda
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                OutlinedTextField(
                    value = busqueda,
                    onValueChange = { busqueda = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Buscar producto...", color = TextHint) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextHint) },
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

            // Chips de acceso rápido
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FiltroChipRapido(
                        texto = "Todos (${todosLosProductos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.TODOS,
                        color = Blue,
                        onClick = { filtroEstadoAplicado = FiltroEstado.TODOS }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Crítico (${productosCriticos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.CRITICO,
                        color = Red,
                        onClick = { filtroEstadoAplicado = FiltroEstado.CRITICO }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Bajo (${productosBajos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.BAJO,
                        color = Yellow,
                        onClick = { filtroEstadoAplicado = FiltroEstado.BAJO }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Por vencer (${productosPorVencer.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.POR_VENCER,
                        color = Color(0xFFFB8C00),
                        onClick = { filtroEstadoAplicado = FiltroEstado.POR_VENCER }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Buen estado (${productosEnBuenEstado.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.BUEN_ESTADO,
                        color = Green,
                        onClick = { filtroEstadoAplicado = FiltroEstado.BUEN_ESTADO }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${listaFiltrada.size} productos",
                fontSize = 12.sp,
                color = TextHint,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Lista de productos
            if (listaFiltrada.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (busqueda.isNotEmpty()) "Sin resultados para \"$busqueda\""
                            else "No hay productos en este filtro",
                            color = TextSecondary,
                            fontSize = 15.sp
                        )
                        if (busqueda.isEmpty() && filtroEstadoAplicado == FiltroEstado.TODOS && !hayFiltrosSheet) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Toca + para agregar tu primer producto", color = TextHint, fontSize = 13.sp)
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

// Chip de acceso rápido
@Composable
fun FiltroChipRapido(
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

// Chip del bottom sheet
@Composable
fun BottomSheetChip(
    texto: String,
    activo: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (activo) Blue.copy(alpha = 0.15f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.5.dp,
            color = if (activo) Blue else BorderMedium
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 13.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
            color = if (activo) Blue else TextSecondary
        )
    }
}

// Tarjeta de producto
@Composable
fun ProductoCard(
    producto: Producto,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
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
                if (producto.fechaVencimiento.isNotBlank()) {
                    Text(
                        text = "Vence: ${producto.fechaVencimiento}",
                        fontSize = 11.sp,
                        color = if (producto.estaVenciendo) Color(0xFFFB8C00) else TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
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

            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(stockColor)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier.width(72.dp).height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onEliminar,
                    modifier = Modifier.width(72.dp).height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Borrar", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Borrar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}