package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.repository.ExtraRepository
import com.estonianport.agendaza.dto.ExtraDTO
import com.estonianport.agendaza.dto.ExtraPrecioDTO
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.enums.TipoExtra
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ExtraService : GenericServiceImpl<Extra, Long>() {

    @Autowired
    private lateinit var empresaService: EmpresaService

    @Autowired
    lateinit var extraRepository: ExtraRepository

    override val dao: CrudRepository<Extra, Long>
        get() = extraRepository

    fun fromListaExtraDtoToListaExtra(listaExtraDTO: List<ExtraDTO>): List<Extra> {
        return listaExtraDTO.map { extra -> this.get(extra.id)!! }
    }


    fun contadorDeExtras(id: Long) = extraRepository.cantidadExtras(id)

    fun extras(id: Long, pageNumber: Int) = extraRepository.findAll(id, PageRequest.of(pageNumber, 10)).content
            .map { extra -> extra.toDTO() }

    fun extrasFiltrados(id: Long, pageNumber: Int, buscar: String) =
            extraRepository.extrasByNombre(id, buscar, PageRequest.of(pageNumber, 10)).content
                    .map { extra -> extra.toDTO() }


    fun contadorDeExtrasCateringFilter(id: Long, buscar: String) = extraRepository.cantidadExtrasCateringFilter(id, buscar)

    fun contadorDeExtrasCatering(id: Long) = extraRepository.cantidadExtrasCatering(id)

    fun extrasCatering(id: Long, pageNumber: Int) = extraRepository.findAllCatering(id, PageRequest.of(pageNumber, 10)).content
            .map { extra -> extra.toDTO() }

    fun extrasCateringFiltrados(id: Long, pageNumber: Int, buscar: String) =
            extraRepository.extrasCateringByNombre(id, buscar, PageRequest.of(pageNumber, 10)).content
                    .map { extra -> extra.toDTO() }


    fun contadorDeExtrasFiltrados(id: Long, buscar: String) = extraRepository.cantidadExtrasFiltrados(id, buscar)


    fun fromListaExtraToListaExtraDto(empresa: Empresa, listaExtra: List<Extra>, fechaEvento: LocalDateTime): List<ExtraDTO> {
        return listaExtra.map {
            it.toExtraPrecioDTO(empresa, fechaEvento)
        }
    }

    fun fromListaExtraToListaExtraDtoByFilter(empresa: Empresa, listaExtra: MutableSet<Extra>, fechaEvento: LocalDateTime, tipoExtra: TipoExtra): List<ExtraDTO> {
        return this.fromListaExtraToListaExtraDto(empresa, listaExtra.filter { it.tipoExtra == tipoExtra }, fechaEvento)
    }


    fun getAllEvento(): List<ExtraDTO> {
        return extraRepository.getAllEvento()
    }

    fun getAllCatering(): List<ExtraDTO> {
        return extraRepository.getAllCatering()
    }

    fun getAllExtraEventoAgregar(empresaId: Long): List<ExtraDTO> {
        return extraRepository.getAllExtraEventoAgregar(empresaId)
    }

    fun getAllExtraCaterigAgregar(empresaId: Long): List<ExtraDTO> {
        return extraRepository.getAllExtraCateringAgregar(empresaId)
    }

    fun deleteExtra(extraId: Long, empresaId: Long) {
        val empresa: Empresa = empresaService.get(empresaId)!!

        empresa.listaExtra = empresa.listaExtra.filter { extra -> extra.id != extraId }.toMutableSet()
        empresaService.save(empresa)
    }

    fun getAllExtraConPrecioByTipoEventoAndFecha(empresaId: Long, tipoEventoId: Long, fechaEvento: LocalDateTime, tipoExtra: TipoExtra): List<ExtraPrecioDTO>{
        return extraRepository.getAllExtraConPrecioByTipoEventoAndFecha(empresaId, tipoEventoId, fechaEvento, tipoExtra)
    }

}