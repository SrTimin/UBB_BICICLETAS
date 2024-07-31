package com.example.ubbbicicletas.database

import models.BicicletaRequest
import models.BicicletaResponse
import models.RegistroFindUser
import models.RegistroRequest
import models.RegistroResponse
import models.RegistroResponseJson
import models.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiService {
    @GET("endpoint/getuser")
    suspend fun getAllUsers(): Response<UserResponse>

    @POST("endpoint/createRegistro")
    suspend fun createRegistro(
        @Body registroRequest: RegistroRequest
    ): Response<RegistroResponseJson>

    @POST("endpoint/findRegistroByIdUser")
    suspend fun findRegistroByIdUser(
        @Body registroRequest: RegistroFindUser
    ): Response<RegistroResponseJson>

    @GET("endpoint/getRegistroByIdUser")
    suspend fun getRegistroByIdUser(
        @Query("userId") userId: String
    ): Response<RegistroResponse>

    @DELETE("endpoint/DeleteRegistro")
    suspend fun deleteRegistro(
        @Query("userId") userId: String
    ): Response<RegistroResponseJson>

    @POST("endpoint/createBicicleta")
    suspend fun createBicicleta(
        @Body bicicletaRequest: BicicletaRequest
    ): Response<RegistroResponseJson>

    @GET("endpoint/getBicicletas")
    suspend fun getBicicletas(): Response<BicicletaResponse>

    @PUT("endpoint/updateBicicleta")
    suspend fun updateBicicleta(
        @Body bicicletaRequest: BicicletaRequest
    ): Response<RegistroResponseJson>

    @GET("endpoint/getBicicletaByidUser")
    suspend fun getBicicletaById(
        @Query("user_id") id: String
    ): Response<BicicletaResponse>

}
