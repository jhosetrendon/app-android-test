package com.optimizerpro.security

/**
 * Objeto Singleton que centraliza las reglas de seguridad y advertencias
 * para proteger al usuario de realizar acciones peligrosas.
 *
 * Define listas de aplicaciones críticas y sensibles para evitar que el usuario
 * modifique componentes que puedan causar inestabilidad en el sistema o la
* pérdida de notificaciones importantes.
 */
object SecurityManager {

    // Enum para categorizar el tipo de advertencia.
    enum class WarningType {
        CRITICAL_SYSTEM,
        BANKING,
        PRODUCTIVITY,
        MESSAGING
    }

    /**
     * Contiene el tipo de advertencia y el mensaje detallado.
     */
    data class AppWarning(val type: WarningType, val message: String)

    // 1. APPS CRÍTICAS DEL SISTEMA (Intocables)
    // Un mapa que asocia el paquete con el motivo por el que es crítico.
    private val appsCriticasSistema = mapOf(
        "com.android.systemui" to "Es la interfaz principal del sistema (barra de estado, navegación). Detenerla puede bloquear el dispositivo.",
        "com.google.android.gms" to "Son los Servicios de Google, esenciales para notificaciones y el funcionamiento de casi todas las apps.",
        "android" to "Es el núcleo del sistema operativo Android. Modificarlo causará un fallo total del sistema.",
        // Launchers de fabricantes
        "com.miui.home" to "Es el launcher de Xiaomi (MIUI). Si lo detienes, no podrás acceder a tus apps.",
        "com.sec.android.app.launcher" to "Es el launcher de Samsung (One UI). Si lo detienes, no podrás acceder a tus apps.",
        "com.huawei.android.launcher" to "Es el launcher de Huawei (EMUI). Si lo detienes, no podrás acceder a tus apps."
    )

    // 2. CASUÍSTICAS DE ADVERTENCIA

    // Apps bancarias y de pagos
    private val appsBancarias = setOf(
        "com.bcp.innovacxion.yapeapp", // Yape
        "pe.com.interbank.mobile",    // Interbank (Plin)
        "com.bbva.bbvacontigo.pe",     // BBVA (Plin)
        "com.scotiabank.peru.digital.movil", // Scotiabank (Plin)
        "com.lemon.cash"              // Lemon Cash
    )

    // Apps de productividad y trabajo
    private val appsProductividad = setOf(
        "com.computrabajo.android.app", // Computrabajo
        "us.zoom.videomeetings"         // Zoom
    )

    // Apps de mensajería
    private val appsMensajeria = setOf(
        "com.whatsapp",
        "org.telegram.messenger"
    )

    /**
     * Evalúa un paquete y devuelve un objeto de advertencia si pertenece a una categoría sensible.
     *
     * @param paquete El nombre del paquete a verificar.
     * @return Un objeto [AppWarning] con el tipo y mensaje personalizado, o `null` si no hay advertencia.
     */
    fun obtenerTipoAdvertencia(paquete: String): AppWarning? {
        return when {
            appsCriticasSistema.containsKey(paquete) ->
                AppWarning(WarningType.CRITICAL_SYSTEM, "¡ADVERTENCIA CRÍTICA! Esta es una app del sistema: ${appsCriticasSistema[paquete]}")
            appsBancarias.contains(paquete) ->
                AppWarning(WarningType.BANKING, "Cuidado: si limitas esta app, podrías dejar de recibir notificaciones de pagos, transferencias o códigos de seguridad.")
            appsProductividad.contains(paquete) ->
                AppWarning(WarningType.PRODUCTIVITY, "Cuidado: si limitas esta app, podrías perderte notificaciones de reuniones, correos importantes u ofertas de empleo.")
            appsMensajeria.contains(paquete) ->
                AppWarning(WarningType.MESSAGING, "Cuidado: si limitas esta app, los mensajes y llamadas no llegarán en tiempo real.")
            else -> null
        }
    }
}