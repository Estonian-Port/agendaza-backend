package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.GenericItemDTO
import jakarta.persistence.*
import org.hibernate.annotations.Proxy
import java.time.LocalDate

@Entity
@Proxy(lazy = false)
open class Clausula(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String){

    @ManyToMany(mappedBy = "listaClausula", fetch = FetchType.LAZY)
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO(): GenericItemDTO {
        return GenericItemDTO(id, nombre)
    }
}