package com.alertastock.utils

/**
 * Constantes globales de la aplicación
 */
object AppConstants {
    // Base de datos
    const val DATABASE_NAME = "alertastock_database"

    // Validaciones de email
    const val EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@(.+)$"

    // Validaciones de contraseña
    const val MIN_PASSWORD_LENGTH_LOGIN = 6
    const val MIN_PASSWORD_LENGTH_REGISTER = 8

    // Filtros de productos
    const val FILTER_ALL = "TODOS"
    const val FILTER_CRITICAL = "CRITICO"
    const val FILTER_LOW = "BAJO"
    const val FILTER_EXPIRING = "POR_VENCER"
    const val FILTER_GOOD = "BUEN_ESTADO"
}

