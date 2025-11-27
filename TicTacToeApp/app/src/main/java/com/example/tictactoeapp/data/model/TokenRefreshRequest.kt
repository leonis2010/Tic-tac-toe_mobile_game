package com.example.tictactoeapp.data.model

import com.google.gson.annotations.SerializedName

data class TokenRefreshRequest(
    @SerializedName("refreshToken") val refreshToken: String
)