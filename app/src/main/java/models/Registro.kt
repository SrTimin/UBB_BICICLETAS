package models

import org.mongodb.kbson.ObjectId
import java.util.Date

data class Registro(
    val _id: ObjectId,
    val user: User,
    val bicicleta: Bicicleta,
    val fecha_ingreso: Date,
    val estacionamiento: String,
    val deleted: Boolean?
)
