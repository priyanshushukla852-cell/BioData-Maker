package com.biodataai.app.network.api

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

data class CreateBiodataRequest(
    val templateId: String,
    val languagePref: String // "EN" or "HI"
)

data class BiodataResponse(
    val id: String,
    val userId: String,
    val templateId: String,
    val languagePref: String,
    val status: String, // "DRAFT" or "DONE"
    val createdAt: String,
    val updatedAt: String
)

data class BiodataListResponse(
    val biodatas: List<BiodataResponse>
)

interface BiodataService {
    @POST("/api/biodatas")
    suspend fun createBiodata(@Body request: CreateBiodataRequest): BiodataResponse

    @GET("/api/biodatas")
    suspend fun listBiodatas(): BiodataListResponse

    @GET("/api/biodatas/{id}")
    suspend fun getBiodata(@Path("id") id: String): BiodataResponse

    @PUT("/api/biodatas/{id}")
    suspend fun updateBiodata(
        @Path("id") id: String,
        @Body request: CreateBiodataRequest
    ): BiodataResponse

    @DELETE("/api/biodatas/{id}")
    suspend fun deleteBiodata(@Path("id") id: String)
}
