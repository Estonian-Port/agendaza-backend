package com.estonianport.agendaza.repository

import com.estonianport.agendaza.dto.CantidadesPanelAdmin
import com.estonianport.agendaza.model.Empresa
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import java.util.*

interface EmpresaRepository : CrudRepository<Empresa, Long>{

/*    @EntityGraph(attributePaths = [
        "listaEmpleados",
        "listaEvento",
        "listaServicio",
        "listaExtra",
        "listaTipoEvento"
    ])*/
    override fun findAll() : List<Empresa>

    /*  @EntityGraph(attributePaths = [
         "listaEmpleados",
         "listaEvento",
         "listaServicio",
         "listaExtra",
         "listaTipoEvento"
     ])*/
    override fun findById(id: Long) : Optional<Empresa>

    /* @EntityGraph(attributePaths = [
        "listaExtra",
        "listaEvento.capacidad",
        "listaEvento.tipoEvento.capacidad",
        "listaEvento.encargado",
        "listaEvento.cliente",
        "listaEvento.listaExtra",
        "listaEvento.listaEventoExtraVariable"
    ])*/
    //@EntityGraph(attributePaths = ["listaEvento"])
    fun findEmpresaById(id: Long) : Optional<Empresa>

    // TODO Revisar
    @EntityGraph(attributePaths = ["listaEvento.listaPago"])
    fun getEmpresaListaPagoById(id: Long) : Optional<Empresa>

    @Query("SELECT new com.estonianport.agendaza.dto.CantidadesPanelAdmin ( \n" +
            "( \n" +
            "SELECT COUNT(c) \n" +
            "FROM Cargo c \n" +
            "WHERE c.empresa.id = ?1), \n" +
            "( \n" +
            "SELECT COUNT(te) \n" +
            "FROM TipoEvento te \n" +
            "WHERE te.empresa.id = ?1 ), \n" +
            "( \n" +
            "SELECT COUNT(ex) \n" +
            "FROM Extra ex \n" +
            "WHERE ex.empresa.id = ?1 \n" +
            "   AND ex.fechaBaja IS NULL \n" +
            "   AND (ex.tipoExtra = 'EVENTO' \n" +
            "   OR ex.tipoExtra = 'VARIABLE_EVENTO')), \n" +
            "( \n" +
            "SELECT COUNT(p) \n" +
            "FROM Pago p \n" +
            "INNER JOIN Evento ev on \n" +
            "   ev.id = p.evento.id \n" +
            "INNER JOIN Empresa e on \n" +
            "   e.id = ev.empresa.id \n" +
            "WHERE e.id = ?1 \n" +
            "   AND p.fechaBaja is null), \n" +
            "( \n" +
            "SELECT COUNT(ev) \n" +
            "FROM Evento ev \n" +
            "WHERE ev.empresa.id = ?1 \n" +
            "AND ev.fechaBaja IS NULL), \n" +
            "( \n" +
            "SELECT COUNT(DISTINCT ev.cliente) \n" +
            "FROM Evento ev \n" +
            "INNER JOIN Empresa e ON \n" +
            "   e.id = ev.empresa.id \n" +
            "INNER JOIN Usuario u ON \n" +
            "   u.id = ev.cliente.id \n" +
            "WHERE e.id = ?1 \n" +
            "AND u.fechaBaja IS NULL), \n" +
            "( \n" +
            "SELECT COUNT(ex) \n" +
            "FROM Extra ex \n" +
            "WHERE ex.empresa.id = ?1 \n" +
            "   AND ex.fechaBaja is null \n" +
            "   AND (ex.tipoExtra = 'TIPO_CATERING' \n" +
            "   OR ex.tipoExtra = 'VARIABLE_CATERING')), \n" +
            "( \n" +
            "SELECT COUNT(s) \n" +
            "FROM Servicio s \n" +
            "WHERE s.empresa.id = ?1 \n" +
            "   AND s.fechaBaja is null )) \n" +
            "FROM Empresa e \n" +
            "WHERE e.id = ?1 ")
    fun getAllCantidadesForPanelAdminByEmpresaId(id : Long) : CantidadesPanelAdmin
}