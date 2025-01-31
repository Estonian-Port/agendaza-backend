package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.MedioDePago
import java.time.LocalDateTime

class PagoDTO(val id: Long, val monto : Int, val codigo : String, val medioDePago: MedioDePago,
              val nombreEvento: String, val fecha: LocalDateTime) {}

class CodigoEmpresaId(val codigo : String, val empresaId: Long) {}

class PagoEmpresaEncargado(val pago : PagoDTO, val empresaId : Long, val usuarioId : Long) {}