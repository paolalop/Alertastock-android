package com.alertastock.data.repository

import android.util.Log
import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.model.Producto
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductoRepository(private val productoDao: ProductoDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // 🔥 UID seguro (SIN "unknown")
    private fun getUid(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("No hay usuario autenticado")
    }

    // 🔥 Ruta correcta en Firebase
    private val coleccionProductos
        get() = firestore
            .collection("usuarios")
            .document(getUid())
            .collection("productos")

    // LiveData locales
    val todosLosProductos = productoDao.obtenerTodos()
    val productosCriticos = productoDao.obtenerCriticos()

    fun buscar(texto: String) = productoDao.buscar(texto)

    // ===============================
    // INSERTAR
    // ===============================
    suspend fun insertar(producto: Producto) {
        productoDao.insertar(producto)

        try {
            val mapa = producto.toMap()
            val docId = producto.codigoBarras.ifEmpty { producto.nombre }

            coleccionProductos
                .document(docId)
                .set(mapa)
                .await()

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al guardar en Firestore", e)
        }
    }

    // ===============================
    // ACTUALIZAR
    // ===============================
    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)

        try {
            val docId = producto.codigoBarras.ifEmpty { producto.nombre }

            coleccionProductos
                .document(docId)
                .set(producto.toMap())
                .await()

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al actualizar en Firestore", e)
        }
    }

    // ===============================
    // ELIMINAR
    // ===============================
    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)

        try {
            val docId = producto.codigoBarras.ifEmpty { producto.nombre }

            coleccionProductos
                .document(docId)
                .delete()
                .await()

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al eliminar en Firestore", e)
        }
    }

    // ===============================
    // BUSCAR POR CÓDIGO
    // ===============================
    suspend fun buscarPorCodigo(codigo: String): Producto? {
        return productoDao.buscarPorCodigo(codigo)
    }

    // ===============================
    // DESCONTAR STOCK
    // ===============================
    suspend fun descontarStock(id: Int, cantidad: Int) {
        productoDao.descontarStock(id, cantidad)
    }

    // ===============================
    // 🔥 NUEVO: actualizar stock mínimo (para alertas)
    // ===============================
    suspend fun actualizarStockMinimo(producto: Producto, nuevoStockMinimo: Int) {
        val productoActualizado = producto.copy(stockMinimo = nuevoStockMinimo)
        actualizar(productoActualizado)
    }

    // ===============================
    // SINCRONIZAR DESDE FIRESTORE
    // ===============================
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

        } catch (e: Exception) {
            Log.e("ProductoRepository", "Error al sincronizar", e)
        }
    }
}

// ===============================
// EXTENSIÓN PARA FIRESTORE
// ===============================
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