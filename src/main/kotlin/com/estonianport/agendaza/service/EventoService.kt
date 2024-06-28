package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.dto.EventoDto
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.dto.EventoReservaDto
import com.estonianport.agendaza.dto.UsuarioEmpresaDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.TipoEvento
import com.estonianport.agendaza.model.Usuario
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.util.CollectionUtils
import java.time.LocalDateTime
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.collections.ArrayList

@Service
class EventoService : GenericServiceImpl<Evento, Long>() {

    @Autowired
    lateinit var eventoRepository: EventoRepository

    override val dao: CrudRepository<Evento, Long>
        get() = eventoRepository

    fun findAllByEmpresa(empresa : Empresa) : List<Evento>{
       return eventoRepository.findAllByEmpresa(empresa)
    }

    fun getEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto: UsuarioEmpresaDto): List<Evento> {
        return eventoRepository.getEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto.usuarioId, usuarioEmpresaDto.empresaId)
    }

    fun getCantEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto: UsuarioEmpresaDto): Int {
        return eventoRepository.getCantEventosByUsuarioIdAndEmpresaId(usuarioEmpresaDto.usuarioId, usuarioEmpresaDto.empresaId)
    }

    fun findById(id : Long): Evento {
        return eventoRepository.findById(id).get()
    }

    fun contadorDeEventos(id : Long): Int {
        return eventoRepository.cantidadDeEventos(id)
    }
    fun contadorDeEventosFiltrados(id : Long, buscar : String): Int {
        return eventoRepository.cantidadDeEventosFiltrados(id,buscar)
    }

    fun listaEventoToListaEventoDto(listaEvento : MutableList<Evento>?) : List<EventoDto>?{
        return listaEvento!!.map { it.toDto() }
    }

    fun generateCodigoForEventoOfEmpresa(empresa : Empresa) : String{
        var codigo : String = CodeGeneratorUtil.base26Only4Letters

        try{
            while (this.existCodigoInEmpresa(codigo, empresa)){
                codigo = CodeGeneratorUtil.base26Only4Letters
            }
        }catch (error : NullPointerException){
            return codigo
        }

        return codigo
    }

    fun existCodigoInEmpresa(codigo : String, empresa : Empresa) : Boolean{
        return empresa.listaEvento.any{ it.codigo == codigo}
    }

    fun findAllByInicioBetweenAndListaEmpresa(empresa: Empresa, desde: LocalDateTime, hasta: LocalDateTime): List<Evento> {

        // Crea la hora inicio y la hora final de un dia para buscar todos los eventos en X dia
        val inicio: LocalDateTime = LocalDateTime.of(desde.year, desde.month, desde.dayOfMonth, 0,0 )
        val fin: LocalDateTime = LocalDateTime.of(desde.year, desde.month, desde.dayOfMonth, 23,59 )

        return eventoRepository.findAllByInicioBetweenAndEmpresa(inicio, fin, empresa)
    }

    fun getHorarioDisponible(listaEvento: List<Evento>, desde : LocalDateTime, hasta : LocalDateTime) : Boolean{
        
        // En caso de no existir ningun evento para esa fecha devolver disponible
        if (listaEvento.isNotEmpty()) {

            // lista de todas las lista de rangos horarios
            val listaDeRangos: MutableList<List<Int>> = ArrayList()

            // Variable usada para obtener la hora final del evento
            var horaFinal : String

            // Obtiene el rango horario de los eventos agendados
            for (evento in listaEvento) {
                if (evento.inicio.plusDays(1).dayOfMonth == evento.fin.dayOfMonth) {
                    horaFinal = suma24Horas(evento.fin)
                } else {
                    horaFinal = evento.fin.toLocalTime().toString()
                }
                listaDeRangos.add(getRango(evento.inicio.toLocalTime().toString(), horaFinal))
            }

            // Obtiene el rango horario del nuevo evento a agendar
            if (desde.dayOfMonth != hasta.dayOfMonth) {
                horaFinal = suma24Horas(hasta)
            } else {
                horaFinal = hasta.toLocalTime().toString()
            }
            val rangoEventoNuevo = getRangoConMargen(desde.toLocalTime().toString(), horaFinal)

            // Si intercepta algun rango de hora
            for (rangos in listaDeRangos) {
                if (CollectionUtils.containsAny(rangos, rangoEventoNuevo)){
                    return false
                }
            }
        }
        // Si no intercepta a ninguna da el disponible
        return true
    }
    
    private fun getRango(inicio: String, fin: String): List<Int> {
        val horaInicioSplit = inicio.split(":")
        val horaFinSplit = fin.split(":")

        val horaInicio = (horaInicioSplit[0] + horaInicioSplit[1]).toInt()
        val horaFin = (horaFinSplit[0] + horaFinSplit[1]).toInt()

        return IntStream.range(horaInicio, horaFin).boxed().collect(Collectors.toList())
    }

    private fun getRangoConMargen(inicio: String, fin: String): List<Int> {
        val horaInicioSplit = inicio.split(":")

        val horaFinSplit = fin.split(":")

        var horaInicio = (horaInicioSplit[0] + horaInicioSplit[1]).toInt()
        var horaFin = (horaFinSplit[0] + horaFinSplit[1]).toInt()

        // Le agrega una hora antes y una hora despues para tener margen
        horaInicio -= 100
        horaFin += 100
        return IntStream.range(horaInicio, horaFin).boxed().collect(Collectors.toList())
    }

    private fun suma24Horas(fechaFin: LocalDateTime): String {
        var horaFin: String = fechaFin.toLocalTime().hour.toString()
        val minutosFin: String = fechaFin.toLocalTime().minute.toString()
        val finHoraEventos = horaFin.toInt() + 24
        horaFin = Integer.toString(finHoraEventos)
        return "$horaFin:$minutosFin"
    }

    fun fromEventoReservaDtoToEvento(eventoReservaDto : EventoReservaDto,
                                     tipoEvento : TipoEvento,
                                     listaExtra : MutableSet<Extra>,
                                     listaEventoExtraVariable : MutableSet<EventoExtraVariable>,
                                     encargado : Usuario,
                                     empresa : Empresa): Evento {
        return Evento(
            eventoReservaDto.id,
            eventoReservaDto.nombre,
            tipoEvento,
            eventoReservaDto.inicio,
            eventoReservaDto.fin,
            eventoReservaDto.capacidad,
            eventoReservaDto.extraOtro,
            eventoReservaDto.descuento,
            listaExtra,
            listaEventoExtraVariable,
            eventoReservaDto.cateringOtro,
            eventoReservaDto.cateringOtroDescripcion,
            encargado,
            eventoReservaDto.cliente,
            eventoReservaDto.codigo,
            eventoReservaDto.estado,
            eventoReservaDto.anotaciones,
            empresa)
    }
}