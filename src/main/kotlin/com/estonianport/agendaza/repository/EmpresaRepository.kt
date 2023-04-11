package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Cargo
import com.estonianport.agendaza.model.Empresa
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.repository.CrudRepository

interface EmpresaRepository : CrudRepository<Empresa, Long>{

    @EntityGraph(attributePaths = ["listaEmpleados"])
    override fun findAll() : List<Empresa>
}