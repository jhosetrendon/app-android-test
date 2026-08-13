package com.optimizerpro.shizuku

import android.content.pm.PackageManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.OnBinderDeadListener
import rikka.shizuku.Shizuku.OnBinderReceivedListener
import rikka.shizuku.Shizuku.OnRequestPermissionResultListener

/**
 * Un objeto Singleton para gestionar la conexión y el estado de la API de Shizuku.
 * Centraliza toda la lógica de Shizuku para que el resto de la app no necesite
 * interactuar directamente con la API de bajo nivel.
 */
object ShizukuManager {

    // Enum para representar los posibles estados de la conexión con Shizuku.
    enum class ShizukuStatus {
        NOT_INITIALIZED, // Estado inicial antes de cualquier comprobación
        UNAVAILABLE,     // Shizuku no está instalado o no está en ejecución
        PERMISSION_NEEDED, // Shizuku está activo, pero nuestra app no tiene permiso
        AVAILABLE        // Shizuku está activo y tenemos permiso
    }

    // StateFlow para emitir el estado actual de Shizuku a los observadores (UI).
    private val _status = MutableStateFlow(ShizukuStatus.NOT_INITIALIZED)
    val status = _status.asStateFlow()

    // Código de solicitud de permiso, puede ser cualquier número.
    const val PERMISSION_REQUEST_CODE = 1001

    /**
     * Listener para cuando el servicio de Shizuku se conecta a nuestra app.
     */
    private val binderReceivedListener = OnBinderReceivedListener {
        checkShizukuStatus()
    }

    /**
     * Listener para cuando la conexión con Shizuku se pierde (ej: el servicio se detiene).
     */
    private val binderDeadListener = OnBinderDeadListener {
        _status.value = ShizukuStatus.UNAVAILABLE
    }

    /**
     * Listener que recibe el resultado de la solicitud de permiso del usuario.
     */
    private val permissionResultListener =
        OnRequestPermissionResultListener { requestCode, grantResult ->
            if (requestCode == PERMISSION_REQUEST_CODE) {
                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                    _status.value = ShizukuStatus.AVAILABLE
                } else {
                    _status.value = ShizukuStatus.PERMISSION_NEEDED
                }
            }
        }

    /**
     * Inicializa el gestor. Debe ser llamado desde la `Application` o la `MainActivity`.
     * Añade los listeners para reaccionar a los cambios en el estado de Shizuku.
     */
    fun init() {
        Shizuku.addBinderReceivedListener(binderReceivedListener)
        Shizuku.addBinderDeadListener(binderDeadListener)
        Shizuku.addRequestPermissionResultListener(permissionResultListener)
        checkShizukuStatus()
    }

    /**
     * Libera los recursos y listeners. Debe ser llamado cuando la app se cierra.
     */
    fun destroy() {
        Shizuku.removeBinderReceivedListener(binderReceivedListener)
        Shizuku.removeBinderDeadListener(binderDeadListener)
        Shizuku.removeRequestPermissionResultListener(permissionResultListener)
    }

    /**
     * Comprueba el estado actual de Shizuku y actualiza el StateFlow.
     */
    fun checkShizukuStatus() {
        if (!Shizuku.isPreV11() && Shizuku.getVersion() < 11) {
            // Shizuku no está instalado o es una versión muy antigua.
            _status.value = ShizukuStatus.UNAVAILABLE
            return
        }

        if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
            // Tenemos permiso.
            if (Shizuku.isServiceRunning()) {
                // El servicio está activo y tenemos permiso. ¡Todo listo!
                _status.value = ShizukuStatus.AVAILABLE
            } else {
                // Tenemos permiso, pero el servicio no está corriendo.
                _status.value = ShizukuStatus.UNAVAILABLE
            }
        } else {
            // No tenemos permiso.
            _status.value = ShizukuStatus.PERMISSION_NEEDED
        }
    }

    /**
     * Lanza el diálogo del sistema para solicitar el permiso de Shizuku.
     */
    fun requestPermission() {
        Shizuku.requestPermission(PERMISSION_REQUEST_CODE)
    }
}