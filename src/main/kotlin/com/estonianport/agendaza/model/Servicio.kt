package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ServicioDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate

@Entity
class Servicio(
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String){

    @ManyToMany(mappedBy = "listaServicio", fetch = FetchType.LAZY)
    var listaEmpresa: MutableSet<Empresa> = mutableSetOf()

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "tipo_evento_servicio",
            joinColumns = [JoinColumn(name = "servicio_id")],
            inverseJoinColumns = [JoinColumn(name = "tipo_evento_id")]
    )
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    fun toDTO(): ServicioDTO {
        return ServicioDTO(id, nombre)
    }
}