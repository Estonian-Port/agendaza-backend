package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.AgendaDto
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.PanelAdministracion
import com.estonianport.agendaza.model.TipoCargo
import com.estonianport.agendaza.model.Usuario
import org.springframework.stereotype.Service

@Service
class ControladorSevice {

    fun getTipoCargo(usuario : Usuario, empresa: Empresa): TipoCargo {
        return empresa.getCargoOfUsuario(usuario)
    }

    // Vista como cliente
    fun getListaEventos(usuario : Usuario) : List<Evento>{
        return usuario.listaEventosContratados.toList()
    }

    // Vista de una empresa
    fun getListaEventos(usuario : Usuario, empresa: Empresa) : List<Evento>{
        val tipoCargo = getTipoCargo(usuario, empresa)

        val agendaTipoCargo = getAgendaTipoCargo(tipoCargo)

        return agendaTipoCargo.getListaEventosByCargo(usuario, empresa)
    }

    fun getPanelDeAdministracion(usuario : Usuario) : MutableSet<PanelAdministracion>{
        return TODO("Opciones para administracion de usuario")
    }

    fun getPanelDeAdministracion(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>{
        val tipoCargo = getTipoCargo(usuario, empresa)
        val agendaTipoCargo = getAgendaTipoCargo(tipoCargo)
        return agendaTipoCargo.getPanelDeAdministracionByCargo(usuario, empresa)
    }

    fun getAgendaTipoCargo(tipoCargo: TipoCargo): AgendaTipoCargo {
        if(TipoCargo.OWNER == tipoCargo || TipoCargo.ENCARGADO == tipoCargo){
            return AgendaAdminitrador()
        }else{
            return AgendaEmpleado()
        }
    }

}

interface AgendaTipoCargo {

    fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa) : List<Evento>

    fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa) : MutableSet<PanelAdministracion>

}

class AgendaAdminitrador : AgendaTipoCargo{
    override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
        return empresa.listaEvento.toList()
    }

    override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
        return mutableSetOf() // TODO
    }


}

@Service
class AgendaService {
    fun getListaAgendasByUsuario(usuario : Usuario): MutableSet<AgendaDto> {
        var listaAgendaDto : MutableSet<AgendaDto> = mutableSetOf()

        usuario.listaCargo.forEach {
            var agendaDto = AgendaDto(it.id, it.empresa.nombre, it.tipoCargo.toString())

            listaAgendaDto.add(agendaDto)
        }

        return listaAgendaDto

    }

}

class AgendaEmpleado : AgendaTipoCargo{
    override fun getListaEventosByCargo(usuario : Usuario, empresa: Empresa): List<Evento> {
        // Retorna los eventos donde el empleado a sido asignado
        return empresa.listaEvento.filter { evento: Evento -> evento.listaEmpleado.contains(usuario) }
    }

    override fun getPanelDeAdministracionByCargo(usuario : Usuario, empresa: Empresa): MutableSet<PanelAdministracion> {
        return mutableSetOf() // TODO
    }

}