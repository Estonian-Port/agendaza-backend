package com.estonianport.agendaza.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.JoinTable
import jakarta.persistence.ManyToMany
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.PrimaryKeyJoinColumn
import java.time.LocalDateTime

@Entity
data class Extra(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long,

    @Column
    val nombre: String,

    @Column
    @Enumerated(EnumType.STRING)
    val tipoExtra : TipoExtra,

    @ManyToOne
    @PrimaryKeyJoinColumn
    val empresa: Empresa){

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tipo_evento_extra",
        joinColumns = arrayOf(JoinColumn(name = "extra_id")),
        inverseJoinColumns = arrayOf(JoinColumn(name = "tipo_evento_id"))
    )
    var listaTipoEvento: MutableSet<TipoEvento> = mutableSetOf()

    @JsonIgnore
    @OneToMany(mappedBy = "extra")
    val listaPrecioConFecha: MutableSet<PrecioConFechaExtra> = mutableSetOf()

    fun getPrecioByFecha(fecha : LocalDateTime): Double {
        if(listaPrecioConFecha.isNotEmpty() && listaPrecioConFecha.any { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) } ) {
            return listaPrecioConFecha.find { it.desde == fecha || it.desde.isBefore(fecha) && it.hasta.isAfter(fecha) }!!.precio
        }else{
            return 0.0
        }
    }

    companion object {
        fun getPrecioByFechaOfListaExtra(listaExtra: List<Extra>, fecha: LocalDateTime): Double {
            return listaExtra.sumOf { it.getPrecioByFecha(fecha) }
        }
    }
}