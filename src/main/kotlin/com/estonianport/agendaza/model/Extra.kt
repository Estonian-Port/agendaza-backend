package com.estonianport.agendaza.model

import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.model.enums.TipoExtra
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
class Extra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long,

    @Column
    var nombre: String,

    @Column
    @Enumerated(EnumType.STRING)
    var tipoExtra : TipoExtra){

    @ManyToMany(mappedBy = "listaExtra", fetch = FetchType.LAZY)
    var listaEmpresa: MutableSet<Empresa> = mutableSetOf()

    @Column
    var fechaBaja : LocalDate? = null

    @JsonIgnore
    @ManyToMany
    @JoinTable(
            name = "tipo_evento_extra",
            joinColumns = [JoinColumn(name = "extra_id")],
            inverseJoinColumns = [JoinColumn(name = "tipo_evento_id")]
    )
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    fun toDTO(): ExtraDTO {
        return ExtraDTO(id, nombre, tipoExtra)
    }

    fun toExtraPrecioDTO(empresa: Empresa, fechaEvento: LocalDateTime): ExtraDTO {
        var extraDTO = ExtraDTO(id, nombre, tipoExtra)
        extraDTO.precio = empresa.getPrecioOfExtraByFecha(this, fechaEvento)
        return extraDTO
    }
}