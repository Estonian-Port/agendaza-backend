package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.EspecificacionDTO
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.model.enums.TipoExtra
import jakarta.persistence.*

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
abstract class Especificacion(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long,

    @ManyToOne(fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    var empresa: Empresa){

    abstract fun aplicar(evento: Evento)

    abstract fun toDTO(): EspecificacionDTO

}

@Entity
class PrecioDePlatoNinos(
        id: Long,
        empresa: Empresa,

        @Column
        open var porcentaje: Int) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        var capacidadNinos = evento.capacidad.capacidadNinos
        var precioPlato = 0.0

        if(evento.cateringOtro != 0.0){
            precioPlato = evento.cateringOtro

        }else if(evento.listaExtra.any { it.tipoExtra == TipoExtra.TIPO_CATERING }){
            var extra = evento.listaExtra.find { it.tipoExtra == TipoExtra.TIPO_CATERING }

            if (extra != null) {
                precioPlato = evento.empresa.getPrecioOfExtraByFecha(extra, evento.inicio)
            }
        }

        evento.extraOtro = capacidadNinos * precioPlato * (porcentaje.toDouble() /100)
    }

    override fun toDTO(): EspecificacionDTO{
        return EspecificacionDTO(
                nombre = "El precio del plato para niños representa un % del varor del plato",
                detalle = "Porcentaje: $porcentaje%"
        )
    }
}

@Entity
class AgregarExtraNinoSiSuperaCapacidad(
        id: Long,
        empresa: Empresa,

        @ManyToOne(fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        open var extraNino: Extra) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        var capacidadNinosEvento = evento.capacidad.capacidadNinos
        var capacidadNinosTipoEvento = evento.tipoEvento.capacidad.capacidadNinos

        if (capacidadNinosEvento > capacidadNinosTipoEvento) {
            var ninosExtras = capacidadNinosEvento - capacidadNinosTipoEvento

            var extraEventoVariableNino = EventoExtraVariable(0,extraNino,ninosExtras)
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
class AgregarExtraCamareraSiSuperaCapacidad(
        id: Long,
        empresa: Empresa,

        @ManyToOne(fetch = FetchType.LAZY)
        @PrimaryKeyJoinColumn
        open var extraCamarera: Extra,

        @Column
        @Enumerated(EnumType.STRING)
        private var duracionEsperada: Duracion): Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        var capacidadAdultosEvento = evento.capacidad.capacidadAdultos
        var capacidadAdultosTipoEvento = evento.tipoEvento.capacidad.capacidadAdultos

        // Verifica que la duración del evento coincida con la especificada
        if (evento.tipoEvento.duracion == duracionEsperada) {
            if (capacidadAdultosEvento > capacidadAdultosTipoEvento) {
                var adultosExtras = (capacidadAdultosEvento - capacidadAdultosTipoEvento) / 10

                var extraEventoVariableNino = EventoExtraVariable(0,extraCamarera,adultosExtras)
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