package com.alertastock.data.local.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.alertastock.data.model.Producto

@Dao
interface ProductoDao {

    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun obtenerTodos(): LiveData<List<Producto>>

    @Query("""
        SELECT * FROM productos 
        WHERE nombre LIKE '%' || :busqueda || '%' 
        OR codigoBarras LIKE '%' || :busqueda || '%'
    """)
    fun buscar(busqueda: String): LiveData<List<Producto>>

    @Query("SELECT * FROM productos WHERE stockActual <= stockMinimo")
    fun obtenerCriticos(): LiveData<List<Producto>>

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigo LIMIT 1")
    suspend fun buscarPorCodigo(codigo: String): Producto?

    // ✅ NUEVO — busca un producto por su id
    @Query("SELECT * FROM productos WHERE id = :id LIMIT 1")
    suspend fun obtenerPorId(id: Int): Producto?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: Producto)

    @Update
    suspend fun actualizar(producto: Producto)

    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("UPDATE productos SET stockActual = stockActual - :cantidad WHERE id = :id")
    suspend fun descontarStock(id: Int, cantidad: Int)
}