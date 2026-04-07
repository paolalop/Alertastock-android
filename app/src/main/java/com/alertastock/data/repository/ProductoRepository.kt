package com.alertastock.data.repository

import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.model.Producto

class ProductoRepository(private val productoDao: ProductoDao) {

    // El ViewModel observa estas propiedades
    val todosLosProductos = productoDao.obtenerTodos()
    val productosCriticos = productoDao.obtenerCriticos()

    fun buscar(texto: String) = productoDao.buscar(texto)

    suspend fun insertar(producto: Producto) {
        productoDao.insertar(producto)
    }

    suspend fun actualizar(producto: Producto) {
        productoDao.actualizar(producto)
    }

    suspend fun eliminar(producto: Producto) {
        productoDao.eliminar(producto)
    }

    suspend fun buscarPorCodigo(codigo: String): Producto? {
        return productoDao.buscarPorCodigo(codigo)
    }

    suspend fun descontarStock(id: Int, cantidad: Int) {
        productoDao.descontarStock(id, cantidad)
    }
}