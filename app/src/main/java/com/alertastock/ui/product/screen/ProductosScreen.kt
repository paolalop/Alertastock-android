package com.alertastock.ui.product.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Home
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.alertastock.ui.theme.BgCard
import com.alertastock.ui.theme.Blue
import com.alertastock.ui.theme.TextHint
import com.alertastock.ui.theme.TextPrimary


enum class FiltroEstado { TODOS, CRITICO, BAJO }
enum class OrdenarPor { NOMBRE, STOCK_ASC, STOCK_DESC, VENCIMIENTO }

enum class BottomDestination {
    INICIO,
    PRODUCTOS
}

@Composable
fun AlertaStockBottomBar(
    selected: BottomDestination,
    onInicioClick: () -> Unit,
    onProductosClick: () -> Unit
) {
    NavigationBar(
        containerColor = BgCard,
        tonalElevation = 0.dp,
        modifier = Modifier.height(72.dp)
    ) {
        NavigationBarItem(
            selected = selected == BottomDestination.INICIO,
            onClick = onInicioClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Inicio"
                )
            },
            label = { Text("Inicio") }
        )

        NavigationBarItem(
            selected = selected == BottomDestination.PRODUCTOS,
            onClick = onProductosClick,
            icon = {
                Icon(
                    imageVector = Icons.Default.Inventory,
                    contentDescription = "Productos"
                )
            },
            label = { Text("Productos") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    onAtras: () -> Unit,
    onAgregarProducto: () -> Unit = {},
    onEditarProducto: (Producto) -> Unit = {},
    onIrInicio: () -> Unit = {},
    onIrProductos: () -> Unit = {},
    viewModel: ProductoViewModel = viewModel(),
    filtroInicial: String = "TODOS"

) {
    val todosLosProductos by viewModel.todosLosProductos.observeAsState(emptyList())
    val productosCriticos by viewModel.productosCriticos.observeAsState(emptyList())

    var busqueda by remember { mutableStateOf("") }
    var mostrarFiltros by remember { mutableStateOf(false) }

    var filtroEstadoAplicado by remember {
        mutableStateOf(
            when (filtroInicial) {
                "CRITICO" -> FiltroEstado.CRITICO
                "BAJO" -> FiltroEstado.BAJO
                else -> FiltroEstado.TODOS
            }
        )
    }
    var categoriaAplicada by remember { mutableStateOf("") }
    var ordenAplicado by remember { mutableStateOf(OrdenarPor.NOMBRE) }

    var filtroEstadoTemp by remember { mutableStateOf(filtroEstadoAplicado) }
    var categoriaTemp by remember { mutableStateOf(categoriaAplicada) }
    var ordenTemp by remember { mutableStateOf(ordenAplicado) }

    val productosBajos = remember(todosLosProductos) {
        todosLosProductos.filter {
            it.stockActual > it.stockMinimo && it.stockActual <= it.stockMinimo * 2
        }
    }

    val categorias = remember(todosLosProductos) {
        todosLosProductos
            .map { it.categoria }
            .filter { it.isNotBlank() }
            .distinct()
            .sorted()
    }

    val listaFiltrada = remember(
        busqueda,
        filtroEstadoAplicado,
        categoriaAplicada,
        ordenAplicado,
        todosLosProductos,
        productosCriticos,
        productosBajos
    ) {
        val base = when (filtroEstadoAplicado) {
            FiltroEstado.TODOS -> todosLosProductos
            FiltroEstado.CRITICO -> productosCriticos
            FiltroEstado.BAJO -> productosBajos
        }

        val porCategoria = if (categoriaAplicada.isBlank()) {
            base
        } else {
            base.filter { it.categoria == categoriaAplicada }
        }

        val porBusqueda = if (busqueda.isBlank()) {
            porCategoria
        } else {
            porCategoria.filter {
                it.nombre.contains(busqueda, ignoreCase = true) ||
                        it.codigoBarras.contains(busqueda, ignoreCase = true) ||
                        it.categoria.contains(busqueda, ignoreCase = true)
            }
        }

        when (ordenAplicado) {
            OrdenarPor.NOMBRE -> porBusqueda.sortedBy { it.nombre.lowercase() }
            OrdenarPor.STOCK_ASC -> porBusqueda.sortedBy { it.stockActual }
            OrdenarPor.STOCK_DESC -> porBusqueda.sortedByDescending { it.stockActual }
            OrdenarPor.VENCIMIENTO -> porBusqueda.sortedWith(
                compareBy({ it.fechaVencimiento.isBlank() }, { it.fechaVencimiento })
            )
        }
    }

    val hayFiltrosSheet = remember(filtroEstadoAplicado, categoriaAplicada, ordenAplicado) {
        filtroEstadoAplicado != FiltroEstado.TODOS ||
                categoriaAplicada.isNotBlank() ||
                ordenAplicado != OrdenarPor.NOMBRE
    }

    var productoAEliminar by remember { mutableStateOf<Producto?>(null) }

    productoAEliminar?.let { producto ->
        AlertDialog(
            onDismissRequest = { productoAEliminar = null },
            containerColor = BgCard,
            title = {
                Text(
                    text = "Eliminar producto",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "¿Seguro que deseas eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.",
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

    if (mostrarFiltros) {
        ProductosFilterSheet(
            categorias = categorias,
            filtroEstadoTemp = filtroEstadoTemp,
            categoriaTemp = categoriaTemp,
            ordenTemp = ordenTemp,
            onChangeEstado = { filtroEstadoTemp = it },
            onChangeCategoria = { categoriaTemp = it },
            onChangeOrden = { ordenTemp = it },
            onApply = {
                filtroEstadoAplicado = filtroEstadoTemp
                categoriaAplicada = categoriaTemp
                ordenAplicado = ordenTemp
                mostrarFiltros = false
            },
            onClear = {
                filtroEstadoTemp = FiltroEstado.TODOS
                categoriaTemp = ""
                ordenTemp = OrdenarPor.NOMBRE
                filtroEstadoAplicado = FiltroEstado.TODOS
                categoriaAplicada = ""
                ordenAplicado = OrdenarPor.NOMBRE
                mostrarFiltros = false
            },
            onDismiss = { mostrarFiltros = false }
        )
    }

    Scaffold(
        containerColor = BgScreen,
        bottomBar = {
            AlertaStockBottomBar(
                selected = BottomDestination.PRODUCTOS,
                onInicioClick = onIrInicio,
                onProductosClick = onIrProductos
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAgregarProducto,
                containerColor = Blue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(58.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Agregar producto",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgScreen)
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(BgCard)
                    .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onAtras) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
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
                                imageVector = Icons.Default.FilterList,
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
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextHint
                        )
                    },
                    trailingIcon = {
                        if (busqueda.isNotEmpty()) {
                            IconButton(onClick = { busqueda = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = TextHint
                                )
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

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FiltroChipRapido(
                        texto = "Todos (${todosLosProductos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.TODOS,
                        color = Blue,
                        onClick = {
                            filtroEstadoAplicado = FiltroEstado.TODOS
                            filtroEstadoTemp = FiltroEstado.TODOS
                        }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Crítico (${productosCriticos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.CRITICO,
                        color = Red,
                        onClick = {
                            filtroEstadoAplicado = FiltroEstado.CRITICO
                            filtroEstadoTemp = FiltroEstado.CRITICO
                        }
                    )
                }
                item {
                    FiltroChipRapido(
                        texto = "Bajo (${productosBajos.size})",
                        activo = filtroEstadoAplicado == FiltroEstado.BAJO,
                        color = Yellow,
                        onClick = {
                            filtroEstadoAplicado = FiltroEstado.BAJO
                            filtroEstadoTemp = FiltroEstado.BAJO
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = buildString {
                    append("${listaFiltrada.size} productos visibles")
                    if (hayFiltrosSheet) append(" · filtros activos")
                },
                fontSize = 12.sp,
                color = TextHint,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (listaFiltrada.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📦", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (busqueda.isNotEmpty()) {
                                "Sin resultados para \"$busqueda\""
                            } else {
                                "No hay productos en este filtro"
                            },
                            color = TextSecondary,
                            fontSize = 15.sp
                        )

                        if (
                            busqueda.isEmpty() &&
                            filtroEstadoAplicado == FiltroEstado.TODOS &&
                            !hayFiltrosSheet
                        ) {
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
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductosFilterSheet(
    categorias: List<String>,
    filtroEstadoTemp: FiltroEstado,
    categoriaTemp: String,
    ordenTemp: OrdenarPor,
    onChangeEstado: (FiltroEstado) -> Unit,
    onChangeCategoria: (String) -> Unit,
    onChangeOrden: (OrdenarPor) -> Unit,
    onApply: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BgCard,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(42.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(BorderMedium)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = "Filtrar y ordenar",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Ajusta la vista del inventario según estado, categoría o stock.",
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            FilterSection(title = "Estado") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableFilterChip(
                        text = "Todos",
                        selected = filtroEstadoTemp == FiltroEstado.TODOS,
                        onClick = { onChangeEstado(FiltroEstado.TODOS) }
                    )
                    SelectableFilterChip(
                        text = "Crítico",
                        selected = filtroEstadoTemp == FiltroEstado.CRITICO,
                        onClick = { onChangeEstado(FiltroEstado.CRITICO) }
                    )
                    SelectableFilterChip(
                        text = "Bajo",
                        selected = filtroEstadoTemp == FiltroEstado.BAJO,
                        onClick = { onChangeEstado(FiltroEstado.BAJO) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FilterSection(title = "Categoría") {
                if (categorias.isEmpty()) {
                    Surface(
                        color = BgInput,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = "No hay categorías disponibles todavía",
                            color = TextHint,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categorias.forEach { categoria ->
                            SelectableFilterChip(
                                text = categoria,
                                selected = categoriaTemp == categoria,
                                onClick = {
                                    onChangeCategoria(
                                        if (categoriaTemp == categoria) "" else categoria
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            FilterSection(title = "Ordenar por") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SelectableFilterChip(
                        text = "Nombre",
                        selected = ordenTemp == OrdenarPor.NOMBRE,
                        onClick = { onChangeOrden(OrdenarPor.NOMBRE) }
                    )
                    SelectableFilterChip(
                        text = "Stock ↑",
                        selected = ordenTemp == OrdenarPor.STOCK_ASC,
                        onClick = { onChangeOrden(OrdenarPor.STOCK_ASC) }
                    )
                    SelectableFilterChip(
                        text = "Stock ↓",
                        selected = ordenTemp == OrdenarPor.STOCK_DESC,
                        onClick = { onChangeOrden(OrdenarPor.STOCK_DESC) }
                    )
                    SelectableFilterChip(
                        text = "Vencimiento",
                        selected = ordenTemp == OrdenarPor.VENCIMIENTO,
                        onClick = { onChangeOrden(OrdenarPor.VENCIMIENTO) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            FiltersResume(
                estado = filtroEstadoTemp,
                categoria = categoriaTemp,
                orden = ordenTemp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                Text(
                    text = "Aplicar filtros",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderMedium),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Text(
                    text = "Restablecer",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = BgInput.copy(alpha = 0.55f),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = title.uppercase(),
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            content()
        }
    }
}

@Composable
fun SelectableFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) Blue.copy(alpha = 0.18f) else BgCard,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) Blue else BorderMedium
        ),
        tonalElevation = 0.dp
    ) {
        Text(
            text = text,
            color = if (selected) Blue else TextSecondary,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
fun FiltersResume(
    estado: FiltroEstado,
    categoria: String,
    orden: OrdenarPor
) {
    val estadoTexto = when (estado) {
        FiltroEstado.TODOS -> "Todos"
        FiltroEstado.CRITICO -> "Crítico"
        FiltroEstado.BAJO -> "Bajo"
    }

    val ordenTexto = when (orden) {
        OrdenarPor.NOMBRE -> "Nombre"
        OrdenarPor.STOCK_ASC -> "Stock ↑"
        OrdenarPor.STOCK_DESC -> "Stock ↓"
        OrdenarPor.VENCIMIENTO -> "Vencimiento"
    }

    Surface(
        color = Blue.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Blue.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = "Vista actual",
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = buildString {
                    append("Estado: $estadoTexto")
                    if (categoria.isNotBlank()) append("  ·  Categoría: $categoria")
                    append("  ·  Orden: $ordenTexto")
                },
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

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
        color = if (activo) color.copy(alpha = 0.18f) else BgCard,
        border = BorderStroke(
            width = 1.2.dp,
            color = if (activo) color else BorderMedium
        )
    ) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = if (activo) FontWeight.Bold else FontWeight.Medium,
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
    val stockColor = when {
        producto.stockActual <= producto.stockMinimo -> Red
        producto.stockActual <= producto.stockMinimo * 2 -> Yellow
        else -> Green
    }

    val stockProgress = if (producto.stockMinimo > 0) {
        (producto.stockActual.toFloat() / (producto.stockMinimo * 3).toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, BorderMedium.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, top = 14.dp, end = 10.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(stockColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = producto.emoji,
                    fontSize = 24.sp
                )
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

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(stockColor)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "Stock: ${producto.stockActual} unid. · Mín: ${producto.stockMinimo}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                if (producto.fechaVencimiento.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Vence: ${producto.fechaVencimiento}",
                        fontSize = 11.sp,
                        color = if (producto.estaVenciendo) Color(0xFFFB8C00) else TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(BorderMedium)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(stockProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(stockColor)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onEditar,
                    modifier = Modifier
                        .width(76.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Editar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onEliminar,
                    modifier = Modifier
                        .width(76.dp)
                        .height(34.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Red),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Borrar",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}