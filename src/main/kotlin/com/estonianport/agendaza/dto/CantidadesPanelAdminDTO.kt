package com.estonianport.agendaza.dto

import java.io.Serializable

class CantidadesPanelAdminDTO(
    var cantUsuarios: Long = 0,
    var cantTipoEvento: Long = 0,
    var cantExtras: Long = 0,
    var cantPagos: Long = 0,
    var cantEventos: Long = 0,
    var cantCliente: Long = 0,
    var cantCatering: Long = 0,
    var cantServicios: Long = 0,
    var cantClausula: Long = 0
) : Serializable {

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}