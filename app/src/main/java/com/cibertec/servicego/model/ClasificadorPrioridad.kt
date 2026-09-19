package com.cibertec.servicego.model

// COMIENZA CAMBIO PARA EL CHECKPOINT 02.
// Esta clase se completará con una función que utilizará when para determinar
// la prioridad según las condiciones operativas del servicio.
class ClasificadorPrioridad {
    fun determinarPrioridad(
        descripcion: String,
        modalidad: String,
        modalidadPrioritaria: String,
        prioridadAlta: String,
        prioridadMedia: String,
        prioridadBaja: String,
    ): String {
        val descripcionNormalizada = descripcion.lowercase()
        val presentaFallaOperativa =
            "falla" in descripcionNormalizada ||
                    "error" in descripcionNormalizada ||
                    "intermitente" in descripcionNormalizada ||
                    "lento" in descripcionNormalizada ||
                    "atasco" in descripcionNormalizada

            return when {
                modalidad == modalidadPrioritaria -> prioridadAlta
                presentaFallaOperativa -> prioridadMedia
                else -> prioridadBaja
            }
    }

}
