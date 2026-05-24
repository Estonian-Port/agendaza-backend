package com.estonianport.agendaza.dto.response

data class CustomResponse<T>(
    val message: String,
    val data: T
)