package com.optimizerpro

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.optimizerpro.databinding.ActivityMainBinding
import com.optimizerpro.shizuku.ShizukuHelper
import com.optimizerpro.shizuku.ShizukuManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Verificar permisos de Shizuku al iniciar
        ShizukuManager.init()
        // Aquí puedes observar ShizukuManager.status para reaccionar a cambios de estado
        // (ej. mostrar un diálogo si los permisos no están concedidos).

        // 2. Configurar el FloatingActionButton
        setupFab()

        // 3. Configurar los filtros de checkboxes
        setupFilters()
    }

    private fun setupFab() {
        binding.fabCleanCache.setOnClickListener {
            // Usamos el scope del ciclo de vida para lanzar una corrutina
            lifecycleScope.launch(Dispatchers.IO) {
                // Ejecutamos la tarea pesada (comando shell) en un hilo de fondo
                val result = ShizukuHelper.trimCaches()

                // Cambiamos al hilo principal para mostrar el resultado en la UI
                withContext(Dispatchers.Main) {
                    if (result != null && result.exitCode == 0) {
                        Toast.makeText(this@MainActivity, "Limpieza de caché completada", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Error al limpiar la caché", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupFilters() {
        val listener = { _: Any, _: Any -> updateAppList() }
        binding.checkboxUserApps.setOnCheckedChangeListener(listener)
        binding.checkboxSystemApps.setOnCheckedChangeListener(listener)
    }

    /**
     * Esta función se llamará cada vez que cambie el estado de un checkbox.
     * Aquí es donde se debe invocar la lógica para filtrar la lista en el RecyclerView.
     */
    private fun updateAppList() {
        val showUserApps = binding.checkboxUserApps.isChecked
        val showSystemApps = binding.checkboxSystemApps.isChecked

        // TODO: Reemplazar este Toast con la llamada al ViewModel o al Adapter para filtrar la lista.
        val filterMessage = "Filtro actualizado: Usuario=${showUserApps}, Sistema=${showSystemApps}"
        Toast.makeText(this, filterMessage, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        ShizukuManager.destroy()
    }
}