package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.TipoCargo

class EmpresaAbmDTO(var id: Long, var nombre: String, var tipoCargo: TipoCargo, var email: String,
                    var telefono: Long, var calle: String, var numero: Int, var municipio: String) {


    var tipo: String = ""

}

class EmpresaDTO(var id: Long, var nombre: String, var email: String,
                       var telefono: Long, var calle: String, var numero: Int, var municipio: String) {

    var tipo: String = ""

}