package com.estonianport.agendaza.model

abstract class Especificacion {

    abstract fun aplicar(evento: Evento)

}

class PrecioDePlatoNinos(val porcentaje: Int) : Especificacion() {
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

        evento.extraOtro = capacidadNinos * precioPlato * (porcentaje /100)
    }
}

class AgregarExtraNinoSiSuperaCapacidad(val extraNino: Extra) : Especificacion() {
    override fun aplicar(evento: Evento) {
        val capacidadNinosEvento = evento.capacidad.capacidadNinos
        val capacidadNinosTipoEvento = evento.tipoEvento.capacidad.capacidadNinos

        var ninosExtras = 0

        if (capacidadNinosEvento > capacidadNinosTipoEvento) {
            ninosExtras = capacidadNinosEvento - capacidadNinosTipoEvento
        }

        for (i in 0 until ninosExtras) {
            evento.listaExtra.add(extraNino)
        }
    }
}

class AgregarExtraCamareraSiSuperaCapacidad(val extraCamarera: Extra, private val duracionEsperada: Duracion): Especificacion() {
    override fun aplicar(evento: Evento) {
        val capacidadAdultosEvento = evento.capacidad.capacidadAdultos
        val capacidadAdultosTipoEvento = evento.tipoEvento.capacidad.capacidadAdultos

        var adultosExtras = 0

        // Verifica que la duración del evento coincida con la especificada
        if (evento.tipoEvento.duracion == duracionEsperada) {
            if (capacidadAdultosEvento > capacidadAdultosTipoEvento) {
                adultosExtras = (capacidadAdultosEvento - capacidadAdultosTipoEvento) / 10
            }
        }

        for (i in 0 until adultosExtras) {
            evento.listaExtra.add(extraCamarera)
        }
    }
}