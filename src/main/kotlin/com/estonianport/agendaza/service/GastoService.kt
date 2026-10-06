package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.dto.GastoDTO
import com.estonianport.agendaza.dto.TotalesPagosMes
import com.estonianport.agendaza.dto.toDTO
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Gasto
import com.estonianport.agendaza.repository.GastoRepository
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class GastoService(
    private val gastoRepository: GastoRepository,
    private val eventoService: EventoService,
    private val usuarioService: UsuarioService,
    private val empresaService: EmpresaService
) : GenericServiceImpl<Gasto, Long>() {

    override val dao: CrudRepository<Gasto, Long>
        get() = gastoRepository

    // ==================== QUERIES ====================

    @Transactional(readOnly = true)
    fun getGastoDTO(id: Long): GastoDTO {
        return gastoRepository.findById(id)
            .orElseThrow { NotFoundException("Gasto no encontrado con id: $id") }
            .toDTO()
    }

    @Transactional(readOnly = true)
    fun getAllGastoByMes(empresaId: Long, mes: Int, anio: Int): List<GastoDTO> {
        val (desde, hasta) = rangoDelMes(mes, anio)
        return gastoRepository.getAllGastoByRango(empresaId, desde, hasta)
    }

    @Transactional(readOnly = true)
    fun totalesByRango(empresaId: Long, mes: Int, anio: Int): TotalesPagosMes {
        val (desde, hasta) = rangoDelMes(mes, anio)
        return gastoRepository.totalesByRango(empresaId, desde, hasta)
    }

    // ==================== MUTATIONS ====================

    @Transactional
    fun saveGasto(gastoDTO: GastoDTO): GastoDTO {
        val empresa = empresaService.get(gastoDTO.empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: ${gastoDTO.empresaId}")

        val encargado = usuarioService.get(gastoDTO.usuarioId)
            ?: throw NotFoundException("Usuario no encontrado con id: ${gastoDTO.usuarioId}")

        // El evento es opcional. Solo se busca si viene el ID.
        val evento = if (gastoDTO.eventoId != null) {
            eventoService.findById(gastoDTO.eventoId)
        } else null

        val fecha = if (gastoDTO.fecha.toLocalDate() != LocalDate.now()) gastoDTO.fecha else LocalDateTime.now()

        val gasto = Gasto(
            id = gastoDTO.id,
            monto = gastoDTO.monto,
            tipoGasto = gastoDTO.tipoGasto,
            descripcion = gastoDTO.descripcion,
            fecha = fecha,
            evento = evento,
            empresa = empresa,
            encargado = encargado
        )

        return gastoRepository.save(gasto).toDTO()
    }

    @Transactional
    override fun delete(id: Long) {
        val gasto = gastoRepository.findById(id)
            .orElseThrow { NotFoundException("Gasto no encontrado con id: $id") }

        gasto.fechaBaja = LocalDate.now()
        gastoRepository.save(gasto)
    }

    // ==================== UTILS ====================

    private fun rangoDelMes(mes: Int, anio: Int): Pair<LocalDateTime, LocalDateTime> {
        if (mes !in 1..12) throw BusinessException("Mes inválido: $mes")
        val desde = LocalDate.of(anio, mes, 1).atStartOfDay()
        return Pair(desde, desde.plusMonths(1))
    }
}
