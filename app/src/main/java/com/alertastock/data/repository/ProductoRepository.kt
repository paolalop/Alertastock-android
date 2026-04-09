package com.alertastock.data.repository

import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository(private val productoDao: ProductoDao) {

    // Instancias de Firebase
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Colección de productos del usuario actual
    private val coleccionProductos
        get() = firestore
            .collection("usuarios")
            .document(auth.currentUser?.uid ?: "unknown")
            .collection("productos")

    // LiveData locales — la UI los observa
    val todosLosProductos = productoDao.obtenerTodos()
    val productosCriticos = productoDao.obtenerCriticos()

    fun buscar(texto: String) = productoDao.buscar(texto)

    // INSERTAR — guarda en Room y en Firestore
    suspend fun insertar(producto: Producto) {
        // 1. Guardar en Room (local)
        productoDao.insertar(producto)

        // 2. Guardar en Firestore (nube)
        try {
            val mapa = producto.toMap()
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .set(mapa)
                .await()
        } catch (e: Exception) {
            // Si falla Firestore, Room ya lo guardó
        }
    }

    // ACTUALIZAR — actualiza en Room y en Firestore
    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .set(producto.toMap())
                .await()
        } catch (e: Exception) {}
    }

    // ELIMINAR — elimina en Room y en Firestore
    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)
        try {
            coleccionProductos
                .document(producto.codigoBarras.ifEmpty { producto.nombre })
                .delete()
                .await()
        } catch (e: Exception) {}
    }

    suspend fun buscarPorCodigo(codigo: String): Producto? {
        return productoDao.buscarPorCodigo(codigo)
    }

    suspend fun descontarStock(id: Int, cantidad: Int) {
        productoDao.descontarStock(id, cantidad)
    }

    // SINCRONIZAR — trae productos de Firestore a Room
    suspend fun sincronizarDesdeFirestore() {
        try {
            val documentos = coleccionProductos.get().await()
            for (doc in documentos) {
                val producto = Producto(
                    nombre = doc.getString("nombre") ?: "",
                    categoria = doc.getString("categoria") ?: "",
                    stockActual = (doc.getLong("stockActual") ?: 0).toInt(),
                    stockMinimo = (doc.getLong("stockMinimo") ?: 0).toInt(),
                    precioCompra = doc.getDouble("precioCompra") ?: 0.0,
                    precioVenta = doc.getDouble("precioVenta") ?: 0.0,
                    codigoBarras = doc.getString("codigoBarras") ?: "",
                    fechaVencimiento = doc.getString("fechaVencimiento") ?: "",
                    emoji = doc.getString("emoji") ?: "📦"
                )
                productoDao.insertar(producto)
            }
        } catch (e: Exception) {}
    }
}

// Extensión para convertir Producto a Map para Firestore
fun Producto.toMap(): Map<String, Any> = mapOf(
    "nombre" to nombre,
    "categoria" to categoria,
    "stockActual" to stockActual,
    "stockMinimo" to stockMinimo,
    "precioCompra" to precioCompra,
    "precioVenta" to precioVenta,
    "codigoBarras" to codigoBarras,
    "fechaVencimiento" to fechaVencimiento,
    "emoji" to emoji
)