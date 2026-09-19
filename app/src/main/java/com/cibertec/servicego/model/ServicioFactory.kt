package com.cibertec.servicego.model

// COMIENZA CAMBIO PARA EL CHECKPOINT 02.
// Este object se completará con una función encargada de crear objetos Servicio.
object ServicioFactory {

    fun crearServicio (
        cliente: String,
        descripcion: String,
        direccion: String,
        codigo: String,
        estado: String,
        prioridad: String,
        rutaAtencion: String
    ): Servicio {
        return Servicio(
            cliente = cliente,
            descripcion  = descripcion,
            direccion = direccion,
            codigo = codigo,
            estado = estado,
            prioridad = prioridad,
            rutaAtencion = rutaAtencion
        )
    }
}
