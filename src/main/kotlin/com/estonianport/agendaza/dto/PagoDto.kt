package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.MedioDePago
import com.estonianport.agendaza.model.Pago
import java.time.LocalDateTime

class PagoDto(val id: Long, val monto : Int, val codigo : String, val medioDePago: MedioDePago,
              val nombreEvento: String, val fecha: LocalDateTime) {}

class CodigoEmpresaId(val codigo : String, val empresaId: Long) {}

class PagoEmpresaEncargado(val pago : PagoDto, val empresaId : Long, val usuarioId : Long) {}