package com.alertastock.data.repository

import com.alertastock.data.local.dao.VentaDao
import com.alertastock.data.model.ItemCanasta
import com.alertastock.data.model.Venta
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class VentaRepository(private val ventaDao: VentaDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Colección compartida de ventas en Firestore
    private val coleccionVentas
        get() = firestore.collection("ventas")

    val todasLasVentas = ventaDao.obtenerTodas()

    fun obtenerPorFecha(fecha: String) = ventaDao.obtenerPorFecha(fecha)

    // GUARDAR VENTA — guarda en Room y en Firestore
    suspend fun guardarVenta(items: List<ItemCanasta>, total: Double) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        // Convertir lista de items a JSON para guardar en Room
        val jsonArray = JSONArray()
        items.forEach { item ->
            val obj = JSONObject()
            obj.put("productoId", item.productoId)
            obj.put("nombre", item.nombre)
            obj.put("emoji", item.emoji)
            obj.put("codigoBarras", item.codigoBarras)
            obj.put("precioVenta", item.precioVenta)
            obj.put("cantidad", item.cantidad)
            jsonArray.put(obj)
        }

        val venta = Venta(
            fecha = fecha,
            productosJson = jsonArray.toString(),
            total = total
        )

        // Guardar en Room
        ventaDao.insertar(venta)

        // Guardar en Firestore
        try {
            val mapa = hashMapOf(
                "fecha" to fecha,
                "total" to total,
                "vendedor" to (auth.currentUser?.email ?: "desconocido"),
                "productos" to items.map { item ->
                    hashMapOf(
                        "nombre" to item.nombre,
                        "cantidad" to item.cantidad,
                        "precioVenta" to item.precioVenta,
                        "subtotal" to item.subtotal
                    )
                }
            )
            coleccionVentas.add(mapa).await()
        } catch (e: Exception) {
            // Si falla Firestore, Room ya lo guardó
        }
    }
}

// Convierte el JSON guardado en Room de vuelta a lista de ItemCanasta
fun parsearItemsDeJson(json: String): List<ItemCanasta> {
    return try {
        val array = JSONArray(json)
        val lista = mutableListOf<ItemCanasta>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            lista.add(
                ItemCanasta(
                    productoId = obj.getInt("productoId"),
                    nombre = obj.getString("nombre"),
                    emoji = obj.getString("emoji"),
                    codigoBarras = obj.getString("codigoBarras"),
                    precioVenta = obj.getDouble("precioVenta"),
                    cantidad = obj.getInt("cantidad")
                )
            )
        }
        lista
    } catch (e: Exception) {
        emptyList()
    }
}