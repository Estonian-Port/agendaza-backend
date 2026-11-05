package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.GenericServiceImpl
import com.estonianport.agendaza.common.limpiarNombre
import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.Clausula
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.repository.ClausulaRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ClausulaService(
    val empresaService: EmpresaService,
    val clausulaRepository: ClausulaRepository) : GenericServiceImpl<Clausula, Long>(){

    override val dao: CrudRepository<Clausula, Long>
        get() = clausulaRepository

    @Transactional
    fun delete(clausulaId : Long, empresaId: Long){
        val empresa: Empresa = empresaService.get(empresaId)!!

        empresa.listaClausula = empresa.listaClausula.filter { clausula -> clausula.id != clausulaId }.toMutableSet()
        empresaService.save(empresa)
    }

    @Transactional
    fun saveClausula(dto: GenericItemDTO): GenericItemDTO {
        var clausula = if (dto.id != 0L) {
            clausulaRepository.findById(dto.id).orElseThrow {
                NotFoundException("Cláusula no encontrada con id: ${dto.id}")
            }
        } else {
            Clausula(0, dto.nombre)
        }

        clausula = clausulaRepository.save(clausula)

        val empresa = empresaService.findById(dto.empresaId)
        empresa.listaClausula.add(clausula)
        empresaService.save(empresa)

        return clausula.toDTO()
    }

    fun getPage(empresaId: Long, page: Int, buscar: String?): List<GenericItemDTO> =
        clausulaRepository.getPage(
            empresaId,
            buscar?.limpiarNombre()?.ifBlank { null },
            PageRequest.of(page, 10)
        )

    fun count(empresaId: Long, buscar: String?): Int =
        clausulaRepository.count(empresaId, buscar?.limpiarNombre()?.ifBlank { null })

}
