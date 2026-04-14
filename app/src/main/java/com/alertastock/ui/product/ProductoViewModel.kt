package com.alertastock.ui.product

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.alertastock.data.local.database.AlertaStockDatabase
import com.alertastock.data.model.Producto
import com.alertastock.data.repository.ProductoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProductoViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ProductoRepository

    // Expose LiveData from repository directly
    val todosLosProductos by lazy { repository.todosLosProductos }
    val productosCriticos by lazy { repository.productosCriticos }

    // Search query state
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // Search results as LiveData
    val resultadosBusqueda by lazy { repository.buscar("") }

    // Loading state
    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    // Error state
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        val dao = AlertaStockDatabase.getDatabase(application).productoDao()
        repository = ProductoRepository(dao)
    }

    fun insertar(producto: Producto) = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.insertar(producto)
        } catch (e: Exception) {
            _error.value = "Error al guardar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun actualizar(producto: Producto) = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.actualizar(producto)
        } catch (e: Exception) {
            _error.value = "Error al actualizar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun eliminar(producto: Producto) = viewModelScope.launch {
        try {
            repository.eliminar(producto)
        } catch (e: Exception) {
            _error.value = "Error al eliminar: ${e.message}"
        }
    }

    fun setBusqueda(texto: String) {
        _busqueda.value = texto
    }

    fun buscar(texto: String) = repository.buscar(texto)

    fun sincronizar() = viewModelScope.launch {
        try {
            _cargando.value = true
            repository.sincronizarDesdeFirestore()
        } catch (e: Exception) {
            _error.value = "Error al sincronizar: ${e.message}"
        } finally {
            _cargando.value = false
        }
    }

    fun limpiarError() {
        _error.value = null
    }
}