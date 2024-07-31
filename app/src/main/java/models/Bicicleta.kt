package models

import org.mongodb.kbson.ObjectId

data class Bicicleta(
    val _id: ObjectId,
    val user_id: ObjectId,
    val marca: String,
    val modelo: String,
    val color: String
)
