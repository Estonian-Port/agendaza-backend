package com.estonianport.agendaza.model

import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded

@Embeddable
abstract class TipoUsuario() {

    abstract fun obtenerAgenda() : Any

    abstract fun obtenerOpcionesPanelDeAdminitracion() : Any
}

class Owner : TipoUsuario() {

    override fun obtenerAgenda(): Int {
        return 1
        TODO("return lista de eventos del salon que administra")
    }

    override fun obtenerOpcionesPanelDeAdminitracion(): Any {
        TODO("Todos los paneles")
    }

}

class Encargado : TipoUsuario() {

    override fun obtenerAgenda(): Int {
        return 1
        TODO("return lista de eventos del salon que administra")
    }

    override fun obtenerOpcionesPanelDeAdminitracion(): Any {
        TODO("Todo lo de arriba menos modificar precio")
    }

}

class Empleado : TipoUsuario() {

    override fun obtenerAgenda(): Int {
        return 1
        TODO("lista de eventos asignados por el owner o encargado")
    }

    override fun obtenerOpcionesPanelDeAdminitracion(): Any {
        TODO("Ver horas trabajadas y pagos")
    }
}

class Cliente : TipoUsuario() {

    override fun obtenerAgenda(): Int {
        return 1
        TODO("lista de eventos asignados desde evento")
    }

    override fun obtenerOpcionesPanelDeAdminitracion(): Any {
        TODO("No tiene panel de control?")
    }

}
