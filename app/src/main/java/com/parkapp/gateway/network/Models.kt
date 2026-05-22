package com.parkapp.gateway.network

data class GateTask(
    val id: Int,
    val type: String,
    val gate_phone: String,
)

data class NextTaskResponse(
    val task: GateTask?,
)
