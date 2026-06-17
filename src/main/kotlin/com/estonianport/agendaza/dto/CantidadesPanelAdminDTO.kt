package com.estonianport.agendaza.dto

import java.io.Serializable

data class CantidadesPanelAdminDTO(
    var cantUsuarios: Int = 0,
    var cantTipoEvento: Int = 0,
    var cantExtras: Int = 0,
    var cantPagos: Int = 0,
    var cantEventos: Int = 0,
    var cantCliente: Int = 0,
    var cantCatering: Int = 0,
    var cantServicios: Int = 0,
    var cantClausula: Int = 0
) : Serializable