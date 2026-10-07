package com.estonianport.agendaza.service

import com.estonianport.agendaza.common.openPDF.PdfBalanceService
import com.estonianport.agendaza.dto.BalanceMes
import com.estonianport.agendaza.dto.BalanceReporte
import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.errors.NotFoundException
import com.estonianport.agendaza.model.enums.TipoGasto
import com.estonianport.agendaza.repository.EventoRepository
import com.estonianport.agendaza.repository.GastoRepository
import com.estonianport.agendaza.repository.PagoRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.YearMonth
import java.time.temporal.ChronoUnit

@Service
class BalanceService(
    private val pagoRepository: PagoRepository,
    private val gastoRepository: GastoRepository,
    private val eventoRepository: EventoRepository,
    private val empresaService: EmpresaService,
    private val cotizacionDolarService: CotizacionDolarService,
    private val pdfBalanceService: PdfBalanceService
) {

    @Transactional(readOnly = true)
    fun generarBalancePdf(
        empresaId: Long,
        desdeMes: Int, desdeAnio: Int,
        hastaMes: Int, hastaAnio: Int
    ): ByteArray {
        if (desdeMes !in 1..12 || hastaMes !in 1..12) throw BusinessException("Mes inválido")

        val desdeYm = YearMonth.of(desdeAnio, desdeMes)
        val hastaYm = YearMonth.of(hastaAnio, hastaMes)
        if (desdeYm.isAfter(hastaYm)) throw BusinessException("El período 'desde' no puede ser posterior a 'hasta'")
        if (hastaYm.isAfter(YearMonth.now())) throw BusinessException("El período no puede incluir meses futuros")

        val cantidadMeses = ChronoUnit.MONTHS.between(desdeYm, hastaYm).toInt() + 1
        if (cantidadMeses > MAX_MESES) throw BusinessException("El período máximo es de $MAX_MESES meses")

        val empresa = empresaService.get(empresaId)
            ?: throw NotFoundException("Empresa no encontrada con id: $empresaId")

        val desde = desdeYm.atDay(1).atStartOfDay()
        val hasta = hastaYm.plusMonths(1).atDay(1).atStartOfDay()

        val pagos = pagoRepository.getAllPagoByRango(empresaId, desde, hasta)
        val gastos = gastoRepository.getAllGastoByRango(empresaId, desde, hasta)
        val eventos = eventoRepository.getEventosParaBalance(empresaId, desde, hasta)

        val pagosPorMes = pagos.groupBy { YearMonth.from(it.fecha) }
        val gastosPorMes = gastos.groupBy { YearMonth.from(it.fecha) }
        val eventosPorMes = eventos.groupBy { YearMonth.from(it.inicio) }

        val periodos = generateSequence(desdeYm) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(hastaYm) }
            .toList()
        val cotizaciones = cotizacionDolarService.primerDiaDeCadaMes(periodos)

        val meses = periodos.map { ym ->
            val gastosMes = gastosPorMes[ym].orEmpty()
            val eventosMes = eventosPorMes[ym].orEmpty()
            BalanceMes(
                periodo = ym,
                ingresos = pagosPorMes[ym].orEmpty().sumOf { it.monto },
                gastosEvento = gastosMes.filter { it.tipoGasto == TipoGasto.EVENTO }.sumOf { it.monto },
                gastosGenerales = gastosMes.filter { it.tipoGasto != TipoGasto.EVENTO }.sumOf { it.monto },
                sueldos = gastosMes.filter { it.tipoGasto == TipoGasto.SUELDOS }.sumOf { it.monto },
                cotizacion = cotizaciones[ym],
                eventos = eventosMes.size,
                personas = eventosMes.sumOf { it.capacidadAdultos + it.capacidadNinos }
            )
        }

        val gastosPorTipo = gastos
            .groupBy { it.tipoGasto }
            .mapValues { (_, lista) -> lista.sumOf { it.monto } }

        val reporte = BalanceReporte(
            empresaNombre = empresa.nombre,
            logoUrl = empresa.logo,
            desde = desdeYm,
            hasta = hastaYm,
            meses = meses,
            gastosPorTipo = gastosPorTipo,
            casaDolar = cotizacionDolarService.casa
        )

        return pdfBalanceService.generarBalance(reporte)
    }

    companion object {
        private const val MAX_MESES = 24
    }
}