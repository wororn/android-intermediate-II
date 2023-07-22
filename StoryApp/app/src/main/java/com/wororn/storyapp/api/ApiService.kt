package com.wororn.storyapp.api

import com.wororn.storyapp.componen.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface ApiService {
    @FormUrlEncoded
    @POST("register")
    suspend fun register(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): RegisterResponse

    @FormUrlEncoded
    @POST("login")
    suspend fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): LoginResponse

    @GET("stories")
    suspend fun listStory(
        @Header("Authorization") token: String,
        @Query("page") page: Int,
        @Query("size") size: Int,
        @Query("Location") location: Int=0
    ): TableStoriesResponse


    @Multipart
    @POST("stories")
    suspend fun addFieldStories(
        @Header("Authorization") Authorization: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat:RequestBody?=null,
        @Part("lon") lon:RequestBody?=null
    ): DataStoriesResponse

    @GET("stories")
    suspend fun getStoriesWiththePosition(
        @Header("Authorization") token: String,
        @Query("location") location: Int

    ): TableStoriesResponse
}