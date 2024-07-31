package models

import org.mongodb.kbson.ObjectId

data class RegistroFindUser(
    val userId: ObjectId
)
