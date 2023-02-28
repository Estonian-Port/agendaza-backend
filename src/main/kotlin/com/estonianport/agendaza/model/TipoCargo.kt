package com.estonianport.agendaza.model

enum class TipoCargoNombre { ADMIN, ENCARGADO, EMPLEADO, CLIENTE }

interface TipoCargo{


    fun getTipoCargo(usuario : Usuario, empresa: Empresa): TipoCargoNombre {
        return empresa.getCargoOfUsuario(usuario)
    }

    // Vista como cliente
    fun getListaEventos(usuario : Usuario) : List<Evento>{
        return usuario.listaEventosContratados.toList()
    }

    // Vista de una empresa
/*    fun getListaEventos(usuario : Usuario, empresa: Empresa) : List<Evento>{

        val agendaTipoCargo = getAgendaTipoCargo()

        return agendaTipoCargo.getListaEventosByCargo(usuario, empresa)
    }*/

    fun getPanelDeAdministracion(usuario : Usuario) : MutableSet<PanelAdministracion>{
        return TODO("Opciones para administracion de usuario")
    }

    /*fun getPanelDeAdministracion(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>{
        val agendaTipoCargo = getAgendaTipoCargo()
        return agendaTipoCargo.getPanelDeAdministracionByCargo(usuario, empresa)
    }

    fun getAgendaTipoCargo(): TipoCargoClasificacion {
        if(TipoCargoNombre.ADMIN == tipoCargoNombre || TipoCargoNombre.ENCARGADO == tipoCargoNombre){
            return Administrador()
        }else{
            return Empleado()
        }
    }*/

}

interface TipoCargoClasificacion {

fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa) : List<Evento>

fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>

}

class Administrador : TipoCargoClasificacion {
override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
    return empresa.listaEvento.toList()
}

override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
    return mutableSetOf() // TODO
}
}

class Empleado : TipoCargoClasificacion {

override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
    // Retorna los eventos donde el empleado a sido asignado
    return empresa.listaEvento.filter { evento: Evento -> evento.listaEmpleado.contains(usuario) }
}

override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
    return mutableSetOf() // TODO
}

}