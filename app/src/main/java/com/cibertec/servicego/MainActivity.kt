package com.cibertec.servicego

// Bundle permite recibir el estado con el que Android crea la Activity.
import android.os.Bundle
// AppCompatActivity aporta compatibilidad y el ciclo de vida de la pantalla.
import androidx.appcompat.app.AppCompatActivity
// doAfterTextChanged ejecuta una acción después de modificar un campo de texto.
import androidx.core.widget.doAfterTextChanged
// ActivityMainBinding conecta esta clase con los componentes de activity_main.xml.
import com.cibertec.servicego.databinding.ActivityMainBinding

import android.content.Intent
import com.cibertec.servicego.model.Servicio
import com.cibertec.servicego.model.ClasificadorPrioridad
import com.cibertec.servicego.model.ServicioFactory

// COMIENZA CAMBIO PARA EL CHECKPOINT 02: IMPORTACIONES PREPARADAS.
// Intent permitirá abrir la pantalla de detalle y enviarle información.
// import android.content.Intent
// ClasificadorPrioridad contendrá la regla que determina la prioridad del servicio.
// import com.cibertec.servicego.model.ClasificadorPrioridad
// Servicio representará los datos de un registro mediante una data class.
// import com.cibertec.servicego.model.Servicio
// ServicioFactory permitirá crear objetos Servicio desde un único punto.
// import com.cibertec.servicego.model.ServicioFactory

class MainActivity : AppCompatActivity() {

    // ViewBinding expone las vistas del XML con referencias seguras desde Kotlin.
    // De esta manera no es necesario utilizar findViewById.
    private lateinit var binding: ActivityMainBinding

    // var declara una variable cuyo valor puede cambiar durante la ejecución.
    // El correlativo comienza en 1 y avanza después de cada registro exitoso.
    private var siguienteCodigo = 1

    private val clasificadorPrioridad = ClasificadorPrioridad()
    private var ultimoServicioRegistrado: Servicio? = null

    // Este indicador evita que los eventos de texto modifiquen el resumen
    // mientras se limpian los campos después de registrar un servicio.
    private var limpiandoFormulario = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflate crea en memoria los componentes definidos en activity_main.xml.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mostrarEstadoInicial()
        configurarEventosDeTexto()

        // setOnClickListener ejecuta registrarServicio cuando se pulsa el botón.
        binding.buttonRegistrarServicio.setOnClickListener {
            registrarServicio()
        }

        binding.buttonVerDetalle.setOnClickListener {
            ultimoServicioRegistrado?.let { servicio ->
                abrirDetalleServicio(servicio)
            }
        }
    }

    private fun mostrarEstadoInicial() {
        binding.textViewResumenTitulo.text = getString(R.string.resumen_inicial_titulo)
        binding.textViewResumenCliente.text = getString(R.string.resumen_cliente_placeholder)
        binding.textViewResumenDescripcion.text =
            getString(R.string.resumen_descripcion_placeholder)
        binding.textViewResumenDireccion.text = getString(R.string.resumen_direccion_placeholder)
        binding.textViewEstadoRegistro.text = getString(R.string.estado_pendiente)
        binding.textViewMensajeVisible.text = getString(R.string.mensaje_inicial)
        binding.textViewCodigoPreliminar.text =
            getString(R.string.codigo_preliminar_formato, generarCodigoCorrelativo())
        binding.textViewCostoEstimado.text =
            getString(R.string.costo_estimado_formato, getString(R.string.costo_base_inicial))
        binding.textViewIndicadoresTecnicos.text = getString(R.string.indicadores_iniciales)
        binding.buttonRegistrarServicio.isEnabled = false

        //agregado: ampliamos el resumen

        binding.textViewPrioridadResumen.text = getString(R.string.prioridad_resumen_placeholder)
        binding.buttonRegistrarServicio.isEnabled = false
    }

    private fun configurarEventosDeTexto() {
        binding.editTextCliente.doAfterTextChanged {
            binding.editTextCliente.error = null
            actualizarIndicadoresBasicos()
        }

        binding.editTextDescripcion.doAfterTextChanged {
            binding.editTextDescripcion.error = null
            actualizarIndicadoresBasicos()
        }

        binding.editTextDireccion.doAfterTextChanged {
            binding.editTextDireccion.error = null
            actualizarIndicadoresBasicos()
        }
    }

    private fun actualizarIndicadoresBasicos() {
        if (limpiandoFormulario) {
            return
        }

        // val declara valores que no se reasignan dentro de esta función.
        val cliente = binding.editTextCliente.text.toString().trim()
        val descripcion = binding.editTextDescripcion.text.toString().trim()
        val direccion = binding.editTextDireccion.text.toString().trim()

        // Boolean representa una condición con dos resultados: true o false.
        val clienteValido = cliente.isNotBlank()
        val descripcionValida = descripcion.length >= 8
        val direccionValida = direccion.isNotBlank()

        // && exige que las tres condiciones sean verdaderas al mismo tiempo.
        val listoParaRegistrar = clienteValido && descripcionValida && direccionValida

        binding.textViewCodigoPreliminar.text =
            getString(R.string.codigo_preliminar_formato, generarCodigoCorrelativo())

        if (descripcion.isBlank() || direccion.isBlank()) {
            // || permite entrar cuando falta la descripción o la dirección.
            binding.textViewCostoEstimado.text =
                getString(R.string.costo_estimado_formato, getString(R.string.costo_base_inicial))
            binding.textViewIndicadoresTecnicos.text = getString(R.string.indicadores_iniciales)
            binding.textViewMensajeVisible.text = getString(R.string.mensaje_inicial)
        } else {
            val tipoServicio = obtenerTipoServicio(descripcion)
            val modalidad = obtenerModalidad(descripcion)
            val costoEstimado = calcularCostoEstimado(tipoServicio, modalidad)
            val tiempoEstimado = calcularTiempoEstimado(tipoServicio, modalidad)
            val prioridad = obtenerPrioridad(descripcion, modalidad)

            binding.textViewCostoEstimado.text =
                getString(R.string.costo_estimado_formato, "S/ ${"%.2f".format(costoEstimado)}")
            binding.textViewIndicadoresTecnicos.text =
                getString(
                    R.string.indicadores_operativos_formato,
                    "%.1f".format(tiempoEstimado),
                    tipoServicio,
                    modalidad
                )
            binding.textViewMensajeVisible.text =
                getString(R.string.mensaje_estimacion_previa, tipoServicio.lowercase())
            binding.textViewPrioridadResumen.text = getString(R.string.prioridad_resumen_formato, prioridad)
        }

        binding.buttonRegistrarServicio.isEnabled = listoParaRegistrar

        if (listoParaRegistrar) {
            binding.textViewEstadoRegistro.text = getString(R.string.estado_listo)
        } else {
            binding.textViewEstadoRegistro.text = getString(R.string.estado_pendiente)
        }
    }

    private fun generarCodigoCorrelativo(): String {
        // String.format convierte el Int en un código de cuatro posiciones.
        // %04d completa con ceros a la izquierda: 1 se muestra como 0001.
        return "SG-%04d".format(siguienteCodigo)
    }

    private fun contieneAlgunTermino(
        texto: String,
        terminoUno: String,
        terminoDos: String,
        terminoTres: String,
        terminoCuatro: String
    ): Boolean {
        // El operador || devuelve true si al menos un término aparece en el texto.
        return terminoUno in texto ||
            terminoDos in texto ||
            terminoTres in texto ||
            terminoCuatro in texto
    }

    private fun obtenerTipoServicio(descripcion: String): String {
        val descripcionNormalizada = descripcion.lowercase()
        val atiendeRed = contieneAlgunTermino(
            descripcionNormalizada,
            "red",
            "wifi",
            "router",
            "internet"
        )
        val atiendeImpresora = contieneAlgunTermino(
            descripcionNormalizada,
            "impresora",
            "toner",
            "tinta",
            "escaner"
        )
        val requiereInstalacion = contieneAlgunTermino(
            descripcionNormalizada,
            "instalacion",
            "instalar",
            "configurar",
            "montaje"
        )

        // when evalúa las reglas en orden y devuelve el primer caso verdadero.
        return when {
            atiendeRed && atiendeImpresora -> getString(R.string.tipo_servicio_mixto)
            atiendeRed -> getString(R.string.tipo_servicio_red)
            atiendeImpresora -> getString(R.string.tipo_servicio_impresion)
            requiereInstalacion -> getString(R.string.tipo_servicio_instalacion)
            else -> getString(R.string.tipo_servicio_diagnostico)
        }
    }

    private fun obtenerModalidad(descripcion: String): String {
        val descripcionNormalizada = descripcion.lowercase()
        val servicioUrgente = contieneAlgunTermino(
            descripcionNormalizada,
            "urgente",
            "caído",
            "sin servicio",
            "no enciende"
        )

        // if / else selecciona una de dos modalidades posibles.
        return if (servicioUrgente) {
            getString(R.string.modalidad_prioritaria)
        } else {
            getString(R.string.modalidad_programada)
        }
    }

    private fun calcularCostoEstimado(tipoServicio: String, modalidad: String): Double {
        // Double permite trabajar con importes que incluyen decimales.
        val costoBase = when (tipoServicio) {
            getString(R.string.tipo_servicio_red) -> 70.0
            getString(R.string.tipo_servicio_impresion) -> 60.0
            getString(R.string.tipo_servicio_instalacion) -> 90.0
            getString(R.string.tipo_servicio_mixto) -> 110.0
            else -> 45.0
        }
        val recargoPrioridad =
            if (modalidad == getString(R.string.modalidad_prioritaria)) 25.0 else 0.0

        // + suma el costo base y el recargo operativo, cuando corresponde.
        return costoBase + recargoPrioridad
    }

    private fun calcularTiempoEstimado(tipoServicio: String, modalidad: String): Double {
        val tiempoBase = when (tipoServicio) {
            getString(R.string.tipo_servicio_red) -> 2.0
            getString(R.string.tipo_servicio_impresion) -> 1.5
            getString(R.string.tipo_servicio_instalacion) -> 3.0
            getString(R.string.tipo_servicio_mixto) -> 3.5
            else -> 1.0
        }
        val recargoPrioridad =
            if (modalidad == getString(R.string.modalidad_prioritaria)) 0.5 else 0.0

        return tiempoBase + recargoPrioridad
    }

    private fun registrarServicio() {
        val cliente = binding.editTextCliente.text.toString().trim()
        val descripcion = binding.editTextDescripcion.text.toString().trim()
        val direccion = binding.editTextDireccion.text.toString().trim()

        // ! invierte el resultado Boolean: true se convierte en false y viceversa.
        if (!validarFormulario(cliente, descripcion, direccion)) {
            binding.textViewMensajeVisible.text = getString(R.string.mensaje_validacion_basica)
            return
        }

        val codigoRegistrado = generarCodigoCorrelativo()
        val tipoServicio = obtenerTipoServicio(descripcion)
        val modalidad = obtenerModalidad(descripcion)
        val costoEstimado = calcularCostoEstimado(tipoServicio, modalidad)
        val tiempoEstimado = calcularTiempoEstimado(tipoServicio, modalidad)

        //Agregado: Cada dato nuevo se calcula una sola vez antes de construir el objeto que viajará hacia la pantalla detalle
        val  prioridad = obtenerPrioridad(descripcion, modalidad)
        val estado = determinarEstadoServicio(prioridad)
        val rutaAtencion = determinarRutaAtencion (direccion)
        val servicio = ServicioFactory.crearServicio(
            cliente = cliente,
            descripcion  = descripcion,
            direccion = direccion,
            codigo = codigoRegistrado,
            estado = estado,
            prioridad = prioridad,
            rutaAtencion = rutaAtencion
        )

        // La interpolación inserta los valores dentro de textos legibles.
        val costoFormateado = "S/ ${"%.2f".format(costoEstimado)}"

        binding.textViewResumenTitulo.text = getString(R.string.resumen_actualizado_titulo)
        binding.textViewResumenCliente.text =
            getString(R.string.resumen_cliente_formato, cliente)
        binding.textViewResumenDescripcion.text =
            getString(R.string.resumen_descripcion_formato, descripcion)
        binding.textViewResumenDireccion.text =
            getString(R.string.resumen_direccion_formato, direccion)
        binding.textViewCodigoPreliminar.text =
            getString(R.string.codigo_preliminar_formato, codigoRegistrado)
        binding.textViewCostoEstimado.text =
            getString(R.string.costo_estimado_formato, costoFormateado)
        binding.textViewIndicadoresTecnicos.text =
            getString(
                R.string.indicadores_operativos_formato,
                "%.1f".format(tiempoEstimado),
                tipoServicio,
                modalidad
            )
        binding.textViewEstadoRegistro.text = getString(R.string.estado_registrado)
        binding.textViewMensajeVisible.text =
            getString(R.string.mensaje_registro_exitoso, "$codigoRegistrado - $cliente")

        // += combina la suma y la asignación; el siguiente registro usará otro código.
        siguienteCodigo += 1
        limpiarFormulario()
    }

    private fun validarFormulario(
        cliente: String,
        descripcion: String,
        direccion: String
    ): Boolean {
        // Se valida cada campo por separado para mostrar el error correspondiente.
        var esValido = true

        binding.editTextCliente.error = null
        binding.editTextDescripcion.error = null
        binding.editTextDireccion.error = null

        if (cliente.isBlank()) {
            binding.editTextCliente.error = getString(R.string.error_cliente_requerido)
            esValido = false
        }

        if (descripcion.isBlank()) {
            binding.editTextDescripcion.error = getString(R.string.error_descripcion_requerida)
            esValido = false
        } else if (descripcion.length < 8) {
            binding.editTextDescripcion.error = getString(R.string.error_descripcion_corta)
            esValido = false
        }

        if (direccion.isBlank()) {
            binding.editTextDireccion.error = getString(R.string.error_direccion_requerida)
            esValido = false
        }

        return esValido
    }

    private fun limpiarFormulario() {
        // La limpieza prepara una nueva captura sin borrar el último resumen registrado.
        limpiandoFormulario = true
        binding.editTextCliente.text?.clear()
        binding.editTextDescripcion.text?.clear()
        binding.editTextDireccion.text?.clear()
        binding.editTextCliente.error = null
        binding.editTextDescripcion.error = null
        binding.editTextDireccion.error = null
        binding.buttonRegistrarServicio.isEnabled = false
        limpiandoFormulario = false
    }

    // COMIENZA CAMBIO PARA EL CHECKPOINT 02.
    // A partir de este punto se agregarán las funciones de prioridad, estado,
    // ruta de atención y navegación hacia DetalleServicioActivity.
}
