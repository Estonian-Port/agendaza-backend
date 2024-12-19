package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.EspecificacionDTO
import jakarta.persistence.*
import org.hibernate.annotations.Proxy

@Entity
@Proxy(lazy = false)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Especificacion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open val id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    abstract fun aplicar(evento: Evento)

    abstract fun toDTO(): EspecificacionDTO

}

@Entity
open class PrecioDePlatoNinos(
        id: Long,
        empresa: Empresa,

        @Column
        open val porcentaje: Int) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadNinos = evento.capacidad.capacidadNinos
        var precioPlato = 0.0

        if(evento.cateringOtro != 0.0){
            precioPlato = evento.cateringOtro

        }else if(evento.listaExtra.any { it.tipoExtra == TipoExtra.TIPO_CATERING }){
            val extra = evento.listaExtra.find { it.tipoExtra == TipoExtra.TIPO_CATERING }

            if (extra != null) {
                precioPlato = evento.empresa.getPrecioOfExtraByFecha(extra, evento.inicio)
            }
        }

        evento.extraOtro = capacidadNinos * precioPlato * (porcentaje.toDouble() /100)
    }

    override fun toDTO(): EspecificacionDTO{
        return EspecificacionDTO(
                nombre = "El precio del plato para niños representa un % del valor del plato",
                detalle = "Porcentaje: $porcentaje%"
        )
    }
}

@Entity
open class AgregarExtraNinoSiSuperaCapacidad(
        id: Long,
        empresa: Empresa,

        @ManyToOne(fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        open val extraNino: Extra) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadNinosEvento = evento.capacidad.capacidadNinos
        val capacidadNinosTipoEvento = evento.tipoEvento.capacidad.capacidadNinos

        if (capacidadNinosEvento > capacidadNinosTipoEvento) {
            val ninosExtras = capacidadNinosEvento - capacidadNinosTipoEvento

            val extraEventoVariableNino = EventoExtraVariable(0,extraNino,ninosExtras)
            evento.listaEventoExtraVariable.add(extraEventoVariableNino)
        }
    }

    override fun toDTO(): EspecificacionDTO {
        return EspecificacionDTO(
                nombre = "Agregar un Niño por cada niño que supere lo establecido en tipo evento",
                detalle = "Extra: " + extraNino.nombre + " / Tipo: " + extraNino.tipoExtra
        )
    }
}

@Entity
open class AgregarExtraCamareraSiSuperaCapacidad(
        id: Long,
        empresa: Empresa,

        @ManyToOne(fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        open val extraCamarera: Extra,

        @Column
        @Enumerated(EnumType.STRING)
        private val duracionEsperada: Duracion): Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadAdultosEvento = evento.capacidad.capacidadAdultos
        val capacidadAdultosTipoEvento = evento.tipoEvento.capacidad.capacidadAdultos

        // Verifica que la duración del evento coincida con la especificada
        if (evento.tipoEvento.duracion == duracionEsperada) {
            if (capacidadAdultosEvento > capacidadAdultosTipoEvento) {
                val adultosExtras = (capacidadAdultosEvento - capacidadAdultosTipoEvento) / 10

                val extraEventoVariableNino = EventoExtraVariable(0,extraCamarera,adultosExtras)
                evento.listaEventoExtraVariable.add(extraEventoVariableNino)
            }

        }
    }

    override fun toDTO(): EspecificacionDTO {
        return EspecificacionDTO(
                nombre = "Agregar una camarera cada 10 adultos que superen lo establecido en Tipo Evento",
                detalle = "Extra: " + extraCamarera.nombre + " / Tipo: " + extraCamarera.tipoExtra
        )
    }
}