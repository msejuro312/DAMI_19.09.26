package com.cibertec.servicego

// Bundle permite recibir el estado con el que Android crea la Activity.
import android.os.Bundle
// AppCompatActivity aporta compatibilidad y el ciclo de vida de la pantalla.
import androidx.appcompat.app.AppCompatActivity
// ActivityDetalleServicioBinding conecta la clase con activity_detalle_servicio.xml.
import com.cibertec.servicego.databinding.ActivityDetalleServicioBinding

// COMIENZA CAMBIO PARA EL CHECKPOINT 02: IMPORTACIONES PREPARADAS.
// IntentCompat permitirá recuperar un Serializable sin usar métodos obsoletos.
// import androidx.core.content.IntentCompat
// Servicio representará el objeto recibido desde MainActivity.
// import com.cibertec.servicego.model.Servicio

class DetalleServicioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetalleServicioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetalleServicioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // finish cierra esta Activity y permite volver a la pantalla anterior.
        binding.buttonVolver.setOnClickListener {
            finish()
        }
    }

    // En clase se agregarán aquí las funciones que reciben y muestran Servicio.
}
