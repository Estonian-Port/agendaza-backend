package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.GenericItemDTO
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Clausula(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String){

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO(): GenericItemDTO {
        return GenericItemDTO(id, nombre)
    }
}