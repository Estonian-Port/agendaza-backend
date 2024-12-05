package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ServicioDTO
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrimaryKeyJoinColumn
import org.hibernate.annotations.Proxy
import java.time.LocalDate

@Entity
@Proxy(lazy = false)
open class Servicio(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String,

    @ManyToMany(mappedBy = "listaServicio", fetch = FetchType.LAZY)
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()){

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO(): ServicioDTO {
        return ServicioDTO(id, nombre)
    }
}