package com.alertastock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alertastock.ui.auth.screens.CuentaCreadaScreen
import com.alertastock.ui.auth.screens.LoginScreen
import com.alertastock.ui.auth.screens.OlvideContrasenaScreen
import com.alertastock.ui.auth.screens.RegistroScreen
import com.alertastock.ui.auth.screens.VerificarCorreoScreen
import com.alertastock.ui.dashboard.screens.DashboardScreen
import com.alertastock.ui.product.ProductoViewModel
import com.alertastock.ui.product.screen.AgregarEditarProductoScreen
import com.alertastock.ui.product.screen.ProductosScreen
import com.google.firebase.auth.FirebaseAuth

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val OLVIDE_CONTRASENA = "olvide_contrasena"
    const val VERIFICAR_CORREO = "verificar_correo"
    const val CUENTA_CREADA = "cuenta_creada"
    const val DASHBOARD = "dashboard"
    const val PRODUCTOS = "productos"
    const val AGREGAR_PRODUCTO = "agregar_producto"
    const val SCANNER = "scanner"
    const val ALERTAS = "alertas"
}

@Composable
fun AlertaStockNavigation() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()

    val productoViewModel: ProductoViewModel = viewModel()

    val startDestination = if (auth.currentUser != null) Rutas.DASHBOARD else Rutas.LOGIN

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Rutas.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onRegistro = { navController.navigate(Rutas.REGISTRO) },
                onOlvideContrasena = { navController.navigate(Rutas.OLVIDE_CONTRASENA) }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroScreen(
                onRegistroExitoso = { navController.navigate(Rutas.VERIFICAR_CORREO) },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.VERIFICAR_CORREO) {
            VerificarCorreoScreen(
                onVerificado = { navController.navigate(Rutas.CUENTA_CREADA) },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.CUENTA_CREADA) {
            CuentaCreadaScreen(
                onIrDashboard = {
                    navController.navigate(Rutas.DASHBOARD) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Rutas.OLVIDE_CONTRASENA) {
            OlvideContrasenaScreen(
                onCorreoEnviado = { navController.popBackStack() },
                onAtras = { navController.popBackStack() }
            )
        }

        composable(Rutas.DASHBOARD) {
            DashboardScreen(
                viewModel = productoViewModel,
                onIrInicio = {
                    navController.navigate(Rutas.DASHBOARD) {
                        launchSingleTop = true
                        popUpTo(Rutas.DASHBOARD) { inclusive = false }
                    }
                },
                onProductos = {
                    navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") {
                        launchSingleTop = true
                    }
                },
                onProductosCriticos = {
                    navController.navigate("${Rutas.PRODUCTOS}?filtro=CRITICO")
                },
                onProductosBajos = {
                    navController.navigate("${Rutas.PRODUCTOS}?filtro=BAJO")
                },
                onCerrarSesion = {
                    navController.navigate(Rutas.LOGIN) {
                        popUpTo(Rutas.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "${Rutas.PRODUCTOS}?filtro={filtro}",
            arguments = listOf(
                navArgument("filtro") {
                    type = NavType.StringType
                    defaultValue = "TODOS"
                }
            )
        ) { backStackEntry ->
            val filtroInicial = backStackEntry.arguments?.getString("filtro") ?: "TODOS"

            ProductosScreen(
                viewModel = productoViewModel,
                filtroInicial = filtroInicial,
                onAtras = { navController.popBackStack() },
                onAgregarProducto = { navController.navigate(Rutas.AGREGAR_PRODUCTO) },
                onEditarProducto = { producto ->
                    productoViewModel.seleccionarProducto(producto)
                    navController.navigate(Rutas.AGREGAR_PRODUCTO)
                },
                onIrInicio = {
                    navController.navigate(Rutas.DASHBOARD) {
                        launchSingleTop = true
                        popUpTo(Rutas.DASHBOARD) { inclusive = false }
                    }
                },
                onIrProductos = {
                    navController.navigate("${Rutas.PRODUCTOS}?filtro=TODOS") {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Rutas.AGREGAR_PRODUCTO) {
            val productoEditar = productoViewModel.productoSeleccionado
            AgregarEditarProductoScreen(
                productoExistente = productoEditar,
                onGuardado = {
                    productoViewModel.limpiarSeleccion()
                    navController.popBackStack()
                },
                onAtras = {
                    productoViewModel.limpiarSeleccion()
                    navController.popBackStack()
                },
                viewModel = productoViewModel
            )
        }
    }
}