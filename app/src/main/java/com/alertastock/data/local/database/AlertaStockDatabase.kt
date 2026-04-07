package com.alertastock.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.alertastock.data.local.dao.ProductoDao
import com.alertastock.data.model.Producto

@Database(
    entities = [Producto::class],
    version = 1,
    exportSchema = false
)
abstract class AlertaStockDatabase : RoomDatabase() {

    // Room genera esta implementación automáticamente
    abstract fun productoDao(): ProductoDao

    companion object {

        @Volatile
        private var INSTANCE: AlertaStockDatabase? = null

        fun getDatabase(context: Context): AlertaStockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlertaStockDatabase::class.java,
                    "alertastock_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}