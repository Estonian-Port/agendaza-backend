package com.estonianport.agendaza.dao

import com.estonianport.agendaza.model.Empresa
import org.springframework.data.repository.CrudRepository

interface EmpresaDao : CrudRepository<Empresa, Long>