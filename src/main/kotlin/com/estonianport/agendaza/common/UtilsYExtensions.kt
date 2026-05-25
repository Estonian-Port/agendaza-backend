package com.estonianport.agendaza.common

import com.estonianport.agendaza.model.Usuario
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utilidades para normalización de datos
 */
object NormalizationUtil {

    /**
     * Normaliza un nombre (lowercase, trimmed)
     */
    fun normalizarNombre(nombre: String): String {
        return when {
            nombre.isBlank() -> "cliente"
            else -> nombre.trim().lowercase()
        }
    }

    /**
     * Normaliza un apellido (lowercase, trimmed)
     * Si está vacío, retorna un valor por defecto
     */
    fun normalizarApellido(apellido: String, defaultValue: String = ""): String {
        return when {
            apellido.isBlank() -> defaultValue
            else -> apellido.trim().lowercase()
        }
    }

    /**
     * Normaliza un email (lowercase, trimmed)
     * Si está vacío, retorna NULL para ser creado después
     */
    fun normalizarEmail(email: String): String? {
        return when {
            email.isBlank() -> null
            else -> email.trim().lowercase()
        }
    }

    /**
     * Normaliza un celular (asegura 10 dígitos)
     * Si es 0 o vacío, retorna NULL para ser creado después
     */
    fun normalizarCelular(celular: Long): Long? {
        return when {
            celular == 0L -> null
            else -> celular
        }
    }

    /**
     * Genera un email por defecto si no se proporciona uno
     * Formato: {id}@agendaza.local
     */
    fun generarEmailPorDefecto(id: Long): String {
        return "$id@agendaza.local"
    }

    /**
     * Genera un celular por defecto si no se proporciona uno
     * Toma el ID y lo rellena con "1234567890" para obtener 10 dígitos
     */
    fun generarCelularPorDefecto(id: Long): Long {
        return (id.toString() + "1234567890").takeLast(10).toLong()
    }

    /**
     * Valida si un email tiene formato válido
     */
    fun esEmailValido(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$"
        return email.matches(emailRegex.toRegex())
    }

    /**
     * Valida si un celular tiene 10 dígitos
     */
    fun esCelularValido(celular: Long): Boolean {
        return celular.toString().length == 10
    }
}

/**
 * Utilidades para formateo de fechas
 */
object DateUtil {

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Convierte un string a LocalDateTime
     */
    fun stringToLocalDateTime(fechaString: String): LocalDateTime {
        return LocalDateTime.parse(fechaString, formatter)
    }

    /**
     * Convierte LocalDateTime a string
     */
    fun localDateTimeToString(fecha: LocalDateTime): String {
        return fecha.format(formatter)
    }

    /**
     * Formatea LocalDateTime para mostrar en UI
     * Formato: "15/06/2024 18:00"
     */
    fun formatearParaUI(fecha: LocalDateTime): String {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
    }

    /**
     * Obtiene solo la hora de un LocalDateTime
     * Formato: "18:00"
     */
    fun obtenerHora(fecha: LocalDateTime): String {
        return fecha.format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    /**
     * Obtiene solo la fecha de un LocalDateTime
     * Formato: "15/06/2024"
     */
    fun obtenerFecha(fecha: LocalDateTime): String {
        return fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}

/**
 * Utilidades para validaciones de eventos
 */
object EventoValidationUtil {

    /**
     * Valida que los datos mínimos de un evento sean válidos
     */
    fun validarDatosMinimos(
        nombre: String,
        inicio: LocalDateTime,
        fin: LocalDateTime,
        tipoEventoId: Long,
        empresaId: Long
    ): Result<Unit> {
        return when {
            nombre.isBlank() -> Result.failure(IllegalArgumentException("El nombre no puede estar vacío"))
            inicio.isAfter(fin) -> Result.failure(IllegalArgumentException("La hora de inicio debe ser antes del fin"))
            tipoEventoId <= 0L -> Result.failure(IllegalArgumentException("Tipo de evento inválido"))
            empresaId <= 0L -> Result.failure(IllegalArgumentException("Empresa inválida"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Valida que las capacidades sean válidas
     */
    fun validarCapacidades(adultos: Int, ninos: Int): Result<Unit> {
        return when {
            adultos < 0 -> Result.failure(IllegalArgumentException("Adultos no puede ser negativo"))
            ninos < 0 -> Result.failure(IllegalArgumentException("Niños no puede ser negativo"))
            adultos + ninos <= 0 -> Result.failure(IllegalArgumentException("Debe haber al menos 1 persona"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Valida que el descuento sea válido (0-100%)
     */
    fun validarDescuento(descuento: Long): Result<Unit> {
        return when {
            descuento < 0L -> Result.failure(IllegalArgumentException("Descuento no puede ser negativo"))
            descuento > 100L -> Result.failure(IllegalArgumentException("Descuento no puede ser mayor a 100%"))
            else -> Result.success(Unit)
        }
    }

    /**
     * Valida que los precios sean válidos (positivos)
     */
    fun validarPrecio(precio: Double): Result<Unit> {
        return when {
            precio < 0.0 -> Result.failure(IllegalArgumentException("El precio no puede ser negativo"))
            else -> Result.success(Unit)
        }
    }
}


/**
 * Extensiones útiles para Usuario
 */
object UsuarioExtensions {

    /**
     * Normaliza completamente un usuario (limpia todos sus campos)
     */
    fun Usuario.normalizar(): Usuario {
        return this.apply {
            nombre = NormalizationUtil.normalizarNombre(nombre)
            apellido = NormalizationUtil.normalizarApellido(apellido, id.toString())
            email = NormalizationUtil.normalizarEmail(email) ?: NormalizationUtil.generarEmailPorDefecto(id)
            if (celular == 0L) {
                celular = NormalizationUtil.generarCelularPorDefecto(id)
            }
        }
    }

    /**
     * Verifica si un usuario tiene datos válidos
     */
    fun Usuario.tienesDatosValidos(): Boolean {
        return nombre.isNotBlank() &&
                apellido.isNotBlank() &&
                email.isNotBlank() &&
                NormalizationUtil.esEmailValido(email) &&
                NormalizationUtil.esCelularValido(celular)
    }

    /**
     * Retorna una representación legible del usuario
     */
    fun Usuario.getRepresentacion(): String {
        return "$nombre $apellido ($email)"
    }
}

/**
 * Extensiones útiles para LocalDateTime
 */
object LocalDateTimeExtensions {

    /**
     * Verifica si dos rangos de tiempo se superponen
     */
    fun LocalDateTime.sobrelapaConRango(inicio2: LocalDateTime, fin2: LocalDateTime, margenMinutos: Int = 60): Boolean {
        val margen = java.time.Duration.ofMinutes(margenMinutos.toLong())
        return this.plus(margen).isAfter(inicio2) && this.minus(margen).isBefore(fin2)
    }

    /**
     * Obtiene el rango de horas en minutos desde medianoche
     */
    fun LocalDateTime.getMinutosDesdeMedianoche(): Int {
        return hour * 60 + minute
    }

    /**
     * Verifica si es dentro del próximo mes
     */
    fun LocalDateTime.isDentroDelProximesMes(): Boolean {
        val ahora = LocalDateTime.now()
        val proximoMes = ahora.plusMonths(1)
        return this.isAfter(ahora) && this.isBefore(proximoMes)
    }

    /**
     * Verifica si ya pasó
     */
    fun LocalDateTime.yaAlPasado(): Boolean {
        return this.isBefore(LocalDateTime.now())
    }
}


/**
 * Builder para construir EventoReservaDTO de forma más limpia
 */
class EventoReservaDtoBuilder {
    private var id: Long = 0
    private var nombre: String = ""
    private var tipoEventoId: Long = 0
    private var empresaId: Long = 0
    private var inicio: LocalDateTime = LocalDateTime.now()
    private var fin: LocalDateTime = LocalDateTime.now()
    private var capacidadAdultos: Int = 0
    private var capacidadNinos: Int = 0
    private var extraOtro: Double = 0.0
    private var descuento: Long = 0
    private var listaExtra: List<com.estonianport.agendaza.dto.ExtraDTO> = emptyList()
    private var listaExtraVariable: List<com.estonianport.agendaza.dto.EventoExtraVariableDTO> = emptyList()
    private var listaExtraTipoCatering: List<com.estonianport.agendaza.dto.ExtraDTO> = emptyList()
    private var listaExtraCateringVariable: List<com.estonianport.agendaza.dto.EventoExtraVariableDTO> = emptyList()
    private var cateringOtro: Double = 0.0
    private var cateringOtroDescripcion: String = ""
    private var encargadoId: Long = 0
    private var cliente: Usuario? = null
    private var codigo: String = ""
    private var estado: com.estonianport.agendaza.model.enums.Estado = com.estonianport.agendaza.model.enums.Estado.COTIZADO
    private var anotaciones: String = ""

    fun id(id: Long) = apply { this.id = id }
    fun nombre(nombre: String) = apply { this.nombre = nombre }
    fun tipoEventoId(tipoEventoId: Long) = apply { this.tipoEventoId = tipoEventoId }
    fun empresaId(empresaId: Long) = apply { this.empresaId = empresaId }
    fun inicio(inicio: LocalDateTime) = apply { this.inicio = inicio }
    fun fin(fin: LocalDateTime) = apply { this.fin = fin }
    fun capacidadAdultos(capacidadAdultos: Int) = apply { this.capacidadAdultos = capacidadAdultos }
    fun capacidadNinos(capacidadNinos: Int) = apply { this.capacidadNinos = capacidadNinos }
    fun extraOtro(extraOtro: Double) = apply { this.extraOtro = extraOtro }
    fun descuento(descuento: Long) = apply { this.descuento = descuento }
    fun listaExtra(listaExtra: List<com.estonianport.agendaza.dto.ExtraDTO>) = apply { this.listaExtra = listaExtra }
    fun listaExtraVariable(listaExtraVariable: List<com.estonianport.agendaza.dto.EventoExtraVariableDTO>) = apply { this.listaExtraVariable = listaExtraVariable }
    fun listaExtraTipoCatering(listaExtraTipoCatering: List<com.estonianport.agendaza.dto.ExtraDTO>) = apply { this.listaExtraTipoCatering = listaExtraTipoCatering }
    fun listaExtraCateringVariable(listaExtraCateringVariable: List<com.estonianport.agendaza.dto.EventoExtraVariableDTO>) = apply { this.listaExtraCateringVariable = listaExtraCateringVariable }
    fun cateringOtro(cateringOtro: Double) = apply { this.cateringOtro = cateringOtro }
    fun cateringOtroDescripcion(cateringOtroDescripcion: String) = apply { this.cateringOtroDescripcion = cateringOtroDescripcion }
    fun encargadoId(encargadoId: Long) = apply { this.encargadoId = encargadoId }
    fun cliente(cliente: Usuario) = apply { this.cliente = cliente }
    fun codigo(codigo: String) = apply { this.codigo = codigo }
    fun estado(estado: com.estonianport.agendaza.model.enums.Estado) = apply { this.estado = estado }
    fun anotaciones(anotaciones: String) = apply { this.anotaciones = anotaciones }

    fun build(): com.estonianport.agendaza.dto.EventoReservaDTO {
        return com.estonianport.agendaza.dto.EventoReservaDTO(
            id = id,
            nombre = nombre,
            tipoEventoId = tipoEventoId,
            empresaId = empresaId,
            inicio = inicio,
            fin = fin,
            capacidad = com.estonianport.agendaza.model.Capacidad(
                id = 0,
                capacidadAdultos = capacidadAdultos,
                capacidadNinos = capacidadNinos
            ),
            extraOtro = extraOtro,
            descuento = descuento,
            listaExtra = listaExtra,
            listaExtraVariable = listaExtraVariable,
            listaExtraTipoCatering = listaExtraTipoCatering,
            listaExtraCateringVariable = listaExtraCateringVariable,
            cateringOtro = cateringOtro,
            cateringOtroDescripcion = cateringOtroDescripcion,
            encargadoId = encargadoId,
            cliente = cliente ?: Usuario(0, "cliente", "", 0, ""),
            codigo = codigo,
            estado = estado,
            anotaciones = anotaciones
        )
    }
}

/**
 * Ejemplo de uso del Builder
 */
object EventoBuilderExample {

    fun ejemplo() {
        // Construcción fácil con builder
        val evento = EventoReservaDtoBuilder()
            .id(0)
            .nombre("Mi Boda")
            .tipoEventoId(1)
            .empresaId(1)
            .inicio(LocalDateTime.of(2024, 6, 15, 18, 0, 0))
            .fin(LocalDateTime.of(2024, 6, 16, 2, 0, 0))
            .capacidadAdultos(150)
            .capacidadNinos(30)
            .descuento(10)
            .cateringOtro(200.0)
            .encargadoId(1)
            .cliente(Usuario(0, "Juan", "Pérez", 5551234567, "juan@example.com"))
            .estado(com.estonianport.agendaza.model.enums.Estado.RESERVADO)
            .build()

        // Ahora puedes usarlo
        println(evento.nombre) // "Mi Boda"
    }
}

