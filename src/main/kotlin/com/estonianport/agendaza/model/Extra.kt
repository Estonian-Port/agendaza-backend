package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ExtraDTO
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.Proxy
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Proxy(lazy = false)
open class Extra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    @Enumerated(EnumType.STRING)
    val tipoExtra : TipoExtra,

    @ManyToMany(mappedBy = "listaExtra", fetch = FetchType.LAZY)
    val listaEmpresa: MutableSet<Empresa> = mutableSetOf()){

    @Column
    var fechaBaja : LocalDate? = null

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "tipo_evento_extra",
            joinColumns = arrayOf(JoinColumn(name = "extra_id") ),
            inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    fun toDTO(): ExtraDTO {
        return ExtraDTO(id, nombre, tipoExtra)
    }

    fun toExtraPrecioDTO(empresa: Empresa, fechaEvento: LocalDateTime): ExtraDTO {
        val extraDTO = ExtraDTO(id, nombre, tipoExtra)
        extraDTO.precio = empresa.getPrecioOfExtraByFecha(this, fechaEvento)
        return extraDTO
    }
}