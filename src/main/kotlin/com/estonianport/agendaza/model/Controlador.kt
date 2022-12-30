package com.estonianport.agendaza.model

abstract class Controlador {

    fun getTipoCargo(usuario : Usuario, empresa: Empresa): TipoCargo {
        return empresa.getCargoOfUsuario(usuario)
    }

    fun getListaEventos(usuario : Usuario, empresa: Empresa) : List<Evento>{
        val tipoCargo = getTipoCargo(usuario, empresa)
        return getListaEventosByCargo(usuario, empresa)
    }

    abstract fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa) : List<Evento>

    fun getPanelDeAdministracion(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>{
        val tipoCargo = getTipoCargo(usuario, empresa)
        return getPanelDeAdministracionByCargo(usuario, empresa)
    }

    abstract fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>


}

class AgendaAdminitrador : Controlador(){
    override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
        return empresa.listaEvento.toList()
    }

    override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
        return mutableSetOf() // TODO
    }


}

class AgendaEmpleado : Controlador(){
    override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
        // Retorna los eventos donde el empleado a sido asignado
        return empresa.listaEvento.filter { evento: Evento -> evento.listaEmpleado.contains(usuario) }
    }

    override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
        return mutableSetOf() // TODO
    }


}