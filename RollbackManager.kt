package com.optimizerpro.rollback

import android.content.Context
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Gestiona un log de transacciones para permitir la funcionalidad de rollback.
 * Todas las acciones que modifican el estado del sistema se registran aquí
 * para que puedan ser revertidas por el usuario.
 *
 * @param context El contexto de la aplicación para acceder al almacenamiento interno.
 */
class RollbackManager(private val context: Context) {

    companion object {
        private const val TAG = "RollbackManager"
        private const val FILENAME = "rollback_log.json"
    }

    /**
     * Data class que representa una única transacción registrada.
     *
     * @param id Identificador único de la transacción.
     * @param fecha Timestamp (en milisegundos) de cuándo se realizó la acción.
     * @param paquete El nombre del paquete afectado.
     * @param accion La acción realizada (ej. "SUSPEND", "APP_OP_RUN_IN_BACKGROUND").
     * @param valorOriginal El estado del sistema antes de la acción (ej. "enabled", "allow").
     */
    data class TransaccionRecord(
        val id: String,
        val fecha: Long,
        val paquete: String,
        val accion: String,
        val valorOriginal: String
    )

    /**
     * Registra una nueva transacción en el archivo JSON.
     *
     * @param paquete El paquete afectado.
     * @param accion La acción realizada.
     * @param valorOriginal El valor previo a la modificación.
     */
    fun registrarTransaccion(paquete: String, accion: String, valorOriginal: String) {
        try {
            val registros = obtenerRegistrosJson()
            val nuevaTransaccion = JSONObject().apply {
                put("id", UUID.randomUUID().toString())
                put("fecha", System.currentTimeMillis())
                put("paquete", paquete)
                put("accion", accion)
                put("valorOriginal", valorOriginal)
            }
            registros.put(nuevaTransaccion)
            guardarRegistrosJson(registros)
        } catch (e: Exception) {
            Log.e(TAG, "Error al registrar la transacción", e)
        }
    }

    /**
     * Lee el archivo JSON y devuelve una lista de todas las transacciones guardadas.
     *
     * @return Una lista de objetos [TransaccionRecord].
     */
    fun obtenerRegistros(): List<TransaccionRecord> {
        return try {
            val jsonArray = obtenerRegistrosJson()
            val lista = mutableListOf<TransaccionRecord>()
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                lista.add(
                    TransaccionRecord(
                        id = jsonObject.getString("id"),
                        fecha = jsonObject.getLong("fecha"),
                        paquete = jsonObject.getString("paquete"),
                        accion = jsonObject.getString("accion"),
                        valorOriginal = jsonObject.getString("valorOriginal")
                    )
                )
            }
            lista
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener los registros", e)
            emptyList()
        }
    }

    /**
     * Elimina un registro de transacción del archivo JSON usando su ID único.
     *
     * @param id El ID de la transacción a eliminar.
     */
    fun eliminarRegistro(id: String) {
        try {
            val registrosActuales = obtenerRegistrosJson()
            val registrosNuevos = JSONArray()
            for (i in 0 until registrosActuales.length()) {
                if (registrosActuales.getJSONObject(i).getString("id") != id) {
                    registrosNuevos.put(registrosActuales.getJSONObject(i))
                }
            }
            guardarRegistrosJson(registrosNuevos)
        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar el registro con id: $id", e)
        }
    }

    private fun obtenerRegistrosJson(): JSONArray {
        val file = File(context.filesDir, FILENAME)
        if (!file.exists()) return JSONArray()
        return JSONArray(file.readText())
    }

    private fun guardarRegistrosJson(jsonArray: JSONArray) {
        File(context.filesDir, FILENAME).writeText(jsonArray.toString(4)) // 4 para indentación
    }
}