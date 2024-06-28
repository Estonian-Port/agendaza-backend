package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface PagoRepository : CrudRepository<Pago, Long>{

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.evento.empresa.id = ?1")
    fun cantidadPagos(id : Long) : Int

    @Query("SELECT COUNT(p) FROM Pago p WHERE p.evento.empresa.id = ?1 AND (p.evento.codigo ILIKE %?2% OR p.evento.cliente.nombre ILIKE %?2%)")
    fun cantidadPagosFiltrados(id : Long, buscar: String) : Int

    @Query(value = "select p from Pago p where p.evento.empresa.id = ?1 AND p.fechaBaja IS NULL order by p.fecha desc")
    fun findAll(id: Long,  pageable : Pageable) : Page<Pago>

    @Query(value = "SELECT p FROM Pago p WHERE p.evento.empresa.id = ?1 AND (p.evento.codigo ILIKE %?2% OR p.evento.cliente.nombre ILIKE %?2%) order by p.fecha desc")
    fun pagosByNombre(id : Long, buscar : String, pageable : Pageable) : Page<Pago>
}