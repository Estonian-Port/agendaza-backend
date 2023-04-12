package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Empresa
import org.springframework.data.repository.CrudRepository

interface EmpresaRepository : CrudRepository<Empresa, Long>