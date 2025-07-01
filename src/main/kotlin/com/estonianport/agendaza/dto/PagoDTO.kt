package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.enums.Concepto
import com.estonianport.agendaza.model.enums.MedioDePago
import java.time.LocalDateTime

class PagoDTO(val id: Long, val monto : Double, val codigo : String, val medioDePago: MedioDePago,
              val nombreEvento: String, val fecha: LocalDateTime, ) {

    val concepto : Concepto = Concepto.CUOTA
    val empresaId : Long = 0
    val usuarioId : Long = 0
}

class CodigoEmpresaId(val codigo : String, val empresaId: Long) {}