package com.estonianport.agendaza.service

import com.estonianport.agendaza.dto.GenericItemDTO
import com.estonianport.agendaza.model.Clausula
import com.estonianport.agendaza.model.Salon
import com.estonianport.agendaza.repository.ClausulaRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.mockito.kotlin.anyOrNull
import org.springframework.data.domain.PageRequest

class ClausulaServiceTest {

    private val repository = mock<ClausulaRepository>()
    private val empresaService = mock<EmpresaService>()
    private lateinit var service: ClausulaService
    private val clausulasGuardadas = mutableListOf<Clausula>()

    private val empresa = Salon(1L, "Salon", 123L, "salon@test.com", "Calle", 1, "Ciudad")

    @BeforeEach
    fun setUp() {
        empresa.listaClausula.clear()
        clausulasGuardadas.clear()
        service = ClausulaService(empresaService, repository)

        whenever(empresaService.findById(empresa.id)).thenReturn(empresa)
        whenever(repository.save(any<Clausula>())).thenAnswer { invocation ->
            val clausula = invocation.getArgument<Clausula>(0)
            if (clausula.id == 0L) clausula.id = (clausulasGuardadas.size + 1).toLong()
            clausulasGuardadas.add(clausula)
            clausula
        }
        whenever(repository.getPage(any(), anyOrNull(), any())).thenAnswer { invocation ->
            val buscar = invocation.getArgument<String?>(1)
            val pageable = invocation.getArgument<PageRequest>(2)
            clausulasGuardadas
                .filter { it in empresa.listaClausula }
                .filter { buscar == null || it.nombre.contains(buscar, ignoreCase = true) }
                .sortedByDescending { it.id }
                .drop(pageable.offset.toInt())
                .take(pageable.pageSize)
                .map { GenericItemDTO(it.id, it.nombre) }
        }
    }

    @Test
    fun `crea clausulas y la segunda pagina devuelve la que falta`() {
        val creadas = (1..11).map { numero ->
            service.saveClausula(
                GenericItemDTO(0L, "Cláusula $numero").apply { empresaId = empresa.id }
            )
        }

        val primeraPagina = service.getPage(empresa.id, 0, null)
        val segundaPagina = service.getPage(empresa.id, 1, null)

        assertEquals(11, creadas.size)
        assertEquals(10, primeraPagina.size)
        assertEquals(listOf(creadas.last().id), primeraPagina.map { it.id }.take(1))
        assertEquals(listOf(creadas.first().id), segundaPagina.map { it.id })
    }

    @Test
    fun `getPage aplica el texto de busqueda`() {
        service.saveClausula(GenericItemDTO(0L, "Cancelación").apply { empresaId = empresa.id })
        service.saveClausula(GenericItemDTO(0L, "Seña").apply { empresaId = empresa.id })

        assertEquals(
            listOf("Cancelación"),
            service.getPage(empresa.id, 0, "cancel").map { it.nombre }
        )
    }

    @Test
    fun `count devuelve la cantidad de clausulas segun la busqueda`() {
        whenever(repository.count(empresa.id, null)).thenReturn(11)
        whenever(repository.count(empresa.id, "reserva")).thenReturn(2)

        assertEquals(11, service.count(empresa.id, null))
        assertEquals(2, service.count(empresa.id, "reserva"))
    }

    @Test
    fun `delete quita la clausula de la empresa y guarda los cambios`() {
        empresa.listaClausula.add(Clausula(10L, "A cancelar"))
        empresa.listaClausula.add(Clausula(11L, "Otra cláusula"))
        whenever(empresaService.get(1L)).thenReturn(empresa)

        service.delete(10L, 1L)

        assertEquals(listOf(11L), empresa.listaClausula.map { it.id })
    }
}
