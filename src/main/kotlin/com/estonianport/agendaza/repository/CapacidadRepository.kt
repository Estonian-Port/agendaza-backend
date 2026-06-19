package com.estonianport.agendaza.repository

import com.estonianport.agendaza.model.Capacidad
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

interface CapacidadRepository : CrudRepository<Capacidad, Long> {

    @Query("SELECT c FROM Capacidad c WHERE c.capacidadAdultos = :adultos AND c.capacidadNinos = :ninos")
    fun findByCapacidadAdultosAndCapacidadNinos(
        @Param("adultos") adultos: Int,
        @Param("ninos") ninos: Int
    ): Capacidad?
}