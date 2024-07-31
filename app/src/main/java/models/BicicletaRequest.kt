package models

import org.mongodb.kbson.ObjectId

data class BicicletaRequest(
    val user_id: ObjectId,
    val marca: String,
    val modelo: String,
    val color: String
)
