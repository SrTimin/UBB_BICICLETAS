package models

import java.util.Date

data class RegistroRequest(
    val user: User,
    val fecha_ingreso: Date?,
    val estacionamiento: String?,
    val deleted: Boolean?,
    val bicicleta: Bicicleta
)
