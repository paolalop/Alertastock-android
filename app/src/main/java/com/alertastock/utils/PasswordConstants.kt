package com.alertastock.utils

/**
 * Constantes para validación y fortaleza de contraseñas
 */
object PasswordConstants {
    // Longitudes mínimas
    const val MIN_LENGTH_LOGIN = 6
    const val MIN_LENGTH_REGISTER = 8

    // Umbrales de fortaleza
    const val STRENGTH_MIN_LENGTH = 8  // Mínimo para contraseña fuerte

    // Evaluación de fortaleza (0-3)
    const val STRENGTH_EMPTY = 0
    const val STRENGTH_WEAK = 1
    const val STRENGTH_MEDIUM = 2
    const val STRENGTH_STRONG = 3

    /**
     * Calcula el nivel de fortaleza de una contraseña
     * @return 0: vacía, 1: débil, 2: media, 3: fuerte
     */
    fun evaluateStrength(password: String): Int {
        return when {
            password.isEmpty() -> STRENGTH_EMPTY
            password.length >= STRENGTH_MIN_LENGTH &&
            password.any { it.isUpperCase() } &&
            password.any { it.isDigit() } -> STRENGTH_STRONG
            password.length >= MIN_LENGTH_LOGIN -> STRENGTH_MEDIUM
            else -> STRENGTH_WEAK
        }
    }
}

