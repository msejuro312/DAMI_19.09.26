package com.cibertec.servicego.model

// Serializable permitirá transportar un objeto Servicio mediante un Intent.
import java.io.Serializable

// COMIENZA CAMBIO PARA EL CHECKPOINT 02.
// En clase se transformará esta estructura inicial en una data class con las
// propiedades que representan un servicio.
data class Servicio(
    val cliente: String,
    val descripcion: String,
    val direccion: String,
    val codigo: String,
    val estado: String,
    val prioridad: String,
    val rutaAtencion: String
) : Serializable
