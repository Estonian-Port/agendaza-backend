package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.TipoCargo

class EmpresaDTO(var id: Long, var nombre: String, var tipoCargo: TipoCargo,
                 var email: String, var telefono: Long, var calle: String, var numero: Int,
                 var municipio: String) {

    var tipo: String = ""

}