package com.estonianport.agendaza.dto

import java.time.LocalDateTime

class PagoDto(val id: Long, val monto : Int, val codigo : String, val medioDePago: String,
              val nombreEvento: String, val fecha: LocalDateTime) {}

class CodigoEmpresaId(val codigo : String, val empresaId: Long) {}