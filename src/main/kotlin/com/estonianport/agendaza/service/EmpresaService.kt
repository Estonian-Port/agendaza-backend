package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.dto.*
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.enums.Duracion
import com.estonianport.agendaza.repository.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmpresaService : GenericServiceImpl<Empresa, Long>() {

    @Autowired
    lateinit var empresaRepository: EmpresaRepository

    @Autowired
    lateinit var eventoRepository: EventoRepository

    @Autowired
    lateinit var cargoRepository: CargoRepository

    @Autowired
    lateinit var tipoEventoRepository: TipoEventoRepository

    @Autowired
    lateinit var extraRepository: ExtraRepository

    @Autowired
    lateinit var pagoRepository: PagoRepository

    @Autowired
    lateinit var servicioRepository: ServicioRepository

    @Autowired
    lateinit var clausulaRepository: ClausulaRepository

    override val dao: CrudRepository<Empresa, Long>
        get() = empresaRepository

    fun findById(id: Long): Empresa {
        return empresaRepository.findById(id).orElseThrow {
            IllegalArgumentException("Empresa no encontrada con el ID: $id")
        }
    }

    @Cacheable(value = ["empresaDTO"], key = "#id")
    @Transactional(readOnly = true)
    fun getEmpresaDTO(id: Long): EmpresaDTO {
        return empresaRepository.findDTOById(id).orElseThrow {
            NotFoundException("Empresa con ID $id no encontrada")
        }
    }

    @CacheEvict(value = ["empresaDTO"], key = "#empresaDTO.id")
    fun save(empresaDTO: EmpresaDTO): GenericItemDTO {
        val empresa = empresaRepository.findById(empresaDTO.id).orElseThrow {
            NotFoundException("Empresa con ID " + empresaDTO.id + " no encontrada.")
        }

        val empresaActualizada = empresa.copy(
            empresaDTO.nombre, empresaDTO.telefono, empresaDTO.email,
            empresaDTO.calle, empresaDTO.numero, empresaDTO.municipio
        )

        return empresaRepository.save(empresaActualizada).toGenericItemDTO()
    }

    fun getAllEventoByEmpresaId(id: Long, pageNumber : Int): List<EventoDTO> {
        return eventoRepository.eventosByEmpresa(id, PageRequest.of(pageNumber,10))
            .content
    }

    fun getAllEventoByFilterName(id : Long, pageNumber : Int, buscar: String): List<EventoDTO>{
        return eventoRepository.eventosByNombre(id, buscar, PageRequest.of(pageNumber,10))
            .content
    }

    @Cacheable(value = ["panelAdminCantidades"], key = "#id")
    @Transactional(readOnly = true)
    fun getAllCantidadesForPanelAdminByEmpresaId(id: Long): CantidadesPanelAdminDTO {
        val cargos = cargoRepository.countActivosByEmpresaId(id)
        val tiposEvento = tipoEventoRepository.countActivosByEmpresaId(id)

        val extrasEvento = extraRepository.countEvento(id)
        val extrasCatering = extraRepository.countCatering(id)

        val pagos = pagoRepository.countActivosByEmpresaId(id)
        val eventos = eventoRepository.countActivosByEmpresaId(id)
        val clientes = eventoRepository.countClientesByEmpresaId(id)
        val servicios = servicioRepository.countActivosByEmpresaId(id)
        val clausulas = clausulaRepository.countByEmpresaId(id)

        return CantidadesPanelAdminDTO(
            cantUsuarios = cargos,
            cantTipoEvento = tiposEvento,
            cantExtras = extrasEvento,
            cantPagos = pagos,
            cantEventos = eventos,
            cantCliente = clientes,
            cantCatering = extrasCatering,
            cantServicios = servicios,
            cantClausula = clausulas
        )
    }

    fun getEspecificaciones(id: Long): List<EspecificacionDTO> {
        return empresaRepository.getEspecificaciones(id).map { it.toDTO() }
    }

    fun getAllPrecioConFechaByExtraId(empresaId: Long, extraId: Long): List<PrecioConFechaDTO> {
        return empresaRepository.getAllPrecioConFechaByExtraId(empresaId, extraId)
    }

    fun getAllPrecioConFechaByTipoEvento(empresaId: Long, tipoEventoId: Long): List<PrecioConFechaDTO> {
        return empresaRepository.getAllPrecioConFechaByTipoEventoId(empresaId, tipoEventoId)
    }

    @Transactional(readOnly = true)
    fun getTiposEventoByDuracion(empresaId: Long, duracionStr: String): List<TipoEventoDTO> {

        val duracionEnum = try {
            Duracion.valueOf(duracionStr)
        } catch (e: IllegalArgumentException) {
            throw BusinessException("La duración '$duracionStr' no es válida")
        }

        return empresaRepository.findByEmpresaIdAndDuracion(empresaId, duracionEnum)
    }
}