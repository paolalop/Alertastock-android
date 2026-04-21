package com.alertastock.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.local.dao.VentaDao
import com.alertastock.data.model.Producto
import com.alertastock.data.model.Venta

@Database(
    entities = [Producto::class, Venta::class],  // ✅ agregamos Venta
    version = 2,                                  // ✅ subimos la versión
    exportSchema = false
)
abstract class AlertaStockDatabase : RoomDatabase() {

    abstract fun productoDao(): ProductoDao
    abstract fun ventaDao(): VentaDao              // ✅ nuevo DAO

    companion object {

        @Volatile
        private var INSTANCE: AlertaStockDatabase? = null

        fun getDatabase(context: Context): AlertaStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlertaStockDatabase::class.java,
                    "alertastock_database"
                )
                    .fallbackToDestructiveMigration() // ✅ borra y recrea al cambiar versión
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}