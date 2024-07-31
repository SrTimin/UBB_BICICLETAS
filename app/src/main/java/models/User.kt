package models

import org.mongodb.kbson.ObjectId
import java.util.Date

data class User(
    val _id: ObjectId,
    val nombre: String,
    val ingreso: Date,
    val carrera: String,
    val contacto: Contacto
)