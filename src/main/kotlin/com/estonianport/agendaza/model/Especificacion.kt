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
    var id: Long,

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
        var porcentaje: Int) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadNinos = evento.capacidadNinos
        var precioPlato = 0.0

        if(evento.cateringOtro != 0.0){
            precioPlato = evento.cateringOtro

        }else if(evento.listaEventoExtra.any { it.extra.tipoExtra == TipoExtra.TIPO_CATERING }){
            val extra = evento.listaEventoExtra.find { it.extra.tipoExtra == TipoExtra.TIPO_CATERING }

            if (extra != null) {
                precioPlato = extra.precio
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
        var extraNino: Extra) : Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadNinosEvento = evento.capacidadNinos
        val capacidadNinosTipoEvento = evento.tipoEvento.capacidadNinos

        if (capacidadNinosEvento > capacidadNinosTipoEvento) {
            val ninosExtras = capacidadNinosEvento - capacidadNinosTipoEvento

            val precio = empresa.getPrecioOfExtraByFecha(extraNino, evento.inicio)
            evento.listaEventoExtra.removeAll { it.extra.id == extraNino.id && it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
            evento.listaEventoExtra.add(EventoExtra(0, evento, extraNino, precio, ninosExtras))
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
        var extraCamarera: Extra,

        @Column
        @Enumerated(EnumType.STRING)
        private var duracionEsperada: Duracion): Especificacion(id, empresa) {

    override fun aplicar(evento: Evento) {
        val capacidadAdultosEvento = evento.capacidadAdultos
        val capacidadAdultosTipoEvento = evento.tipoEvento.capacidadAdultos

        // Verifica que la duración del evento coincida con la especificada
        if (evento.tipoEvento.duracion == duracionEsperada) {
            if (capacidadAdultosEvento > capacidadAdultosTipoEvento) {
                val adultosExtras = (capacidadAdultosEvento - capacidadAdultosTipoEvento) / 10

                val precio = empresa.getPrecioOfExtraByFecha(extraCamarera, evento.inicio)
                evento.listaEventoExtra.removeAll { it.extra.id == extraCamarera.id && it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
                evento.listaEventoExtra.add(EventoExtra(0, evento, extraCamarera, precio, adultosExtras))
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
