package com.estonianport.agendaza.dto

import com.estonianport.agendaza.model.enums.TipoCargo
import java.io.Serializable

data class EmpresaAbmDTO(var id: Long, var nombre: String, var tipoCargo: TipoCargo, var email: String,
                    var telefono: Long, var calle: String, var numero: Int, var municipio: String): Serializable {

    var tipo: String = ""

}

data class EmpresaDTO(var id: Long, var nombre: String, var email: String,
                       var telefono: Long, var calle: String, var numero: Int, var municipio: String): Serializable {

    var tipo: String = ""

}