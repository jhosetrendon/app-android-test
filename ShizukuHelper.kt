package com.optimizerpro.shizuku

import android.content.pm.PackageManager
import android.util.Log
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.IOException

/**
 * Objeto Singleton que actúa como un motor para ejecutar comandos de sistema
 * de forma segura a través de la API de Shizuku.
 *
 * Centraliza la ejecución de comandos shell, proporcionando una capa de abstracción
 * a prueba de fallos para el resto de la aplicación.
 */
object ShizukuHelper {

    private const val TAG = "ShizukuHelper"

    /**
     * Representa el resultado de la ejecución de un comando shell.
     * @param exitCode El código de salida del proceso. 0 normalmente indica éxito.
     * @param output La salida estándar del comando.
     */
    data class ShellResult(val exitCode: Int, val output: String)

    /**
     * Verifica si Shizuku está activo y si la aplicación tiene los permisos necesarios.
     * @return `true` si Shizuku está listo para ser usado, `false` en caso contrario.
     */
    fun isShizukuActive(): Boolean {
        return try {
            Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar el estado de Shizuku", e)
            false
        }
    }

    /**
     * Función genérica para ejecutar un comando shell a través de Shizuku.
     *
     * @param command El comando a ejecutar.
     * @return Un objeto [ShellResult] con el código de salida y la respuesta, o null si falla la ejecución.
     */
    private fun executeShellCommand(command: String): ShellResult? {
        if (!isShizukuActive()) {
            Log.w(TAG, "Intento de ejecutar un comando sin Shizuku activo: $command")
            return null
        }

        return try {
            val process = Shizuku.newProcess(arrayOf("sh", "-c", command), null, null)
            val output = process.inputStream.bufferedReader().use(BufferedReader::readText)
            val exitCode = process.waitFor()
            ShellResult(exitCode, output)
        } catch (e: IOException) {
            Log.e(TAG, "Error de I/O al ejecutar el comando: $command", e)
            null
        } catch (e: InterruptedException) {
            Log.e(TAG, "El proceso fue interrumpido: $command", e)
            Thread.currentThread().interrupt() // Restablecer el estado de interrupción
            null
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error de estado de Shizuku al ejecutar: $command", e)
            null
        }
    }

    /**
     * Fuerza la detención de una aplicación.
     * @param packageName El nombre del paquete de la app a detener.
     */
    fun forceStopPackage(packageName: String): ShellResult? {
        return executeShellCommand("am force-stop $packageName")
    }

    /**
     * Suspende una aplicación (la "congela"). Requiere API 29+.
     * @param packageName El nombre del paquete de la app a suspender.
     */
    fun suspendPackage(packageName: String): ShellResult? {
        return executeShellCommand("pm suspend $packageName")
    }

    /**
     * Modifica una operación de App Ops para un paquete específico.
     * @param packageName El nombre del paquete.
     * @param operation La operación de App Ops a modificar (ej: RUN_IN_BACKGROUND).
     * @param mode El modo a establecer (ej: ignore, allow, deny).
     */
    fun setAppOp(packageName: String, operation: String, mode: String): ShellResult? {
        // Validar para evitar inyección de comandos
        if (packageName.contains(" ") || operation.contains(" ") || mode.contains(" ")) {
            Log.e(TAG, "Parámetros inválidos para setAppOp para evitar inyección de comandos.")
            return null
        }
        return executeShellCommand("cmd appops set $packageName $operation $mode")
    }

    /**
     * Ejecuta una limpieza de cachés del sistema.
     * El valor numérico es un umbral en bytes; un valor alto fuerza una limpieza más agresiva.
     */
    fun trimCaches(): ShellResult? {
        // Usamos un valor grande para forzar una limpieza significativa.
        val requiredBytes = 2_000_000_000L // 2GB
        return executeShellCommand("pm trim-caches $requiredBytes")
    }
}

```

### Puntos Clave de la Implementación

1.  **Seguridad (`try-catch`):** Todas las interacciones con Shizuku están envueltas en bloques `try-catch` para manejar excepciones como `IOException` o `IllegalStateException`, evitando que la aplicación se cierre inesperadamente.
2.  **Abstracción:** La función `executeShellCommand` es `private`, lo que significa que el resto de la app no puede llamarla directamente. Solo puede usar las funciones específicas y seguras como `forceStopPackage`, lo que reduce la superficie de errores.
3.  **Resultado Claro (`ShellResult`):** En lugar de devolver un simple `String` o `Boolean`, las funciones devuelven un `ShellResult`. Esto permite al código que llama (por ejemplo, un ViewModel) saber no solo la salida del comando, sino también si se ejecutó con éxito (`exitCode == 0`).
4.  **Reutilización:** La función genérica `executeShellCommand` es la base para todas las operaciones. Añadir un nuevo comando en el futuro será tan simple como crear una nueva función pública que llame a esta función base con el comando adecuado.

Con este motor de sistema, "Optimizer Pro" ya tiene la capacidad de realizar acciones potentes de optimización de forma segura y controlada.

<!--
[PROMPT_SUGGESTION]Crea un ViewModel para la pantalla de gestión de aplicaciones que use ShizukuHelper para listar y detener apps.[/PROMPT_SUGGESTION]
[PROMPT_SUGGESTION]Diseña la interfaz de usuario para el módulo de App Ops, permitiendo al usuario ver y cambiar los permisos de una app.[/PROMPT_SUGGESTION]
-->