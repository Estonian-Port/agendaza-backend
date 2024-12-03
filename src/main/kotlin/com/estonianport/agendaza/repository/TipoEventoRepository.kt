package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.TipoEventoDTO
import com.estonianport.agendaza.model.TipoEvento
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface TipoEventoRepository : CrudRepository<TipoEvento, Long> {
    override fun findAll(): List<TipoEvento>

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.listaExtra e " +
            "where e.id = :extraId")
    fun getAllByExtra(extraId : Long): MutableList<TipoEventoDTO>

    @Query("SELECT new com.estonianport.agendaza.dto.TipoEventoDTO(t.id, t.nombre, t.cantidadDuracion, " +
            "t.duracion, t.capacidad, t.empresa.id) FROM TipoEvento t INNER JOIN t.listaServicio s " +
            "where s.id = :servicioId")
    fun getAllByServicio(servicioId: Long): MutableList<TipoEventoDTO>

}