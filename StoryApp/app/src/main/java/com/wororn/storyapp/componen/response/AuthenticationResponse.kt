package com.wororn.storyapp.componen.response

import com.google.gson.annotations.SerializedName

data class LoginResult(
    val token: String,
    val userId: String,
    val name: String,
    )

data class LoginResponse(

    @field:SerializedName("loginResult")
    val loginResult: LoginResult? = null,

    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)


data class RegisterResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String
)

