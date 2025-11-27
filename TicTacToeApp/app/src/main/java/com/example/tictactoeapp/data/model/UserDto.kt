package com.example.tictactoeapp.data.model
import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("password")
    val password: String? = null,

    @SerializedName("accessToken")
    val accessToken: String? = null,

    @SerializedName("refreshToken")
    val refreshToken: String? = null,

    @SerializedName("expiresIn")
    val expiresIn: Long? = null

)