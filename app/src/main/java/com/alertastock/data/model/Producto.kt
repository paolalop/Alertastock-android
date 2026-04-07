package com.alertastock.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val categoria: String,
    val stockActual: Int,
    val stockMinimo: Int,
    val precioCompra: Double,
    val precioVenta: Double,
    val codigoBarras: String = "",
    val fechaVencimiento: String = "",
    val emoji: String = "📦"
)