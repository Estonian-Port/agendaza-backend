package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Empresa
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EmpresaRepository : CrudRepository<Empresa, Long>{

    @EntityGraph(attributePaths = [
        "listaEmpleados",
        "listaEvento",
        "listaServicio",
        "listaExtra",
        "listaTipoEvento"
    ])
    override fun findAll() : List<Empresa>

    //TODO revisar
    @EntityGraph(attributePaths = [
        "listaEmpleados",
        "listaEvento.listaPago",
        "listaServicio",
        "listaExtra",
        "listaTipoEvento"
    ])
    override fun findById(id: Long) : Optional<Empresa>

    @EntityGraph(attributePaths = [
        "listaEvento",
    ])
    fun findEmpresaById(id: Long) : Optional<Empresa>
}