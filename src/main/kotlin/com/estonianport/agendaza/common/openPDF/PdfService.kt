package com.estonianport.agendaza.common.openPDF

import com.estonianport.agendaza.model.*
import com.estonianport.agendaza.model.enums.TipoExtra
import com.lowagie.text.*
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfPCell
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfPageEventHelper
import com.lowagie.text.pdf.PdfWriter
import com.lowagie.text.pdf.draw.LineSeparator
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.LocalDate
import java.time.YearMonth

@Service
class PdfService {

    private fun setPageSize(document: Document) {
        document.setPageSize(PageSize.A4)
    }

    private fun setLogo(document: Document, logoUrl: String) {
        val logo = Image.getInstance(URL(logoUrl))

        logo.scaleToFit(100f, 100f) // Ajusta el tamaño del logo
        logo.alignment = Element.ALIGN_CENTER
        document.add(logo)
    }

    private fun setHeader(document: Document, empresa: Empresa, fontNormal: Font) {

        val header = Paragraph()
        header.alignment = Element.ALIGN_CENTER
        header.add(Chunk("Mail: ${empresa.email} \n", fontNormal))
        header.add(Chunk("Teléfono: ${empresa.telefono} \n", fontNormal))
        header.add(Chunk("${empresa.calle} ${empresa.numero} - ${empresa.municipio}", fontNormal))

        document.add(header)
    }

    private fun setDatosCliente(document: Document, cliente: Usuario, fontSubtitulo: Font) {
        // Datos del cliente
        document.add(Paragraph("Cliente: ${cliente.nombre}, ${cliente.apellido}", fontSubtitulo))

    }

    fun setPago(document: Document, pago: Pago, fontSubtitulo: Font) {
        val hoy = LocalDate.now().dayOfMonth.toString() + "/" +
                    LocalDate.now().monthValue.toString() + "/" +
                    LocalDate.now().year.toString()
        document.add(Paragraph("Fecha: $hoy", fontSubtitulo))
        document.add(Paragraph("Forma de pago: ${pago.medioDePago}", fontSubtitulo))
    }

    private fun setDescripcion(evento: Evento, document: Document, fontTitulo: Font, fontNormal: Font) {
        val diaEvento = evento.inicio.toLocalDate().dayOfMonth.toString() + "/" +
                evento.inicio.toLocalDate().monthValue.toString() + "/" +
                evento.inicio.toLocalDate().year.toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()

        //document.add(Paragraph("DESCRIPCIÓN", fontTitulo))
        recuadro(document, "DESCRIPCIÓN", fontTitulo, true)
        //lineaHorizontal(document)

        document.add(Paragraph("Evento: ${evento.nombre} a realizarse el ${diaEvento} en el horario " +
                        "${horaInicio}hrs a ${horaFin}hrs", fontNormal))
        document.add(Paragraph("En ${evento.empresa.nombre}"))
    }

    private fun setMontoCapacidadCatering(evento: Evento, document: Document, fontSubtitulo: Font, fontNormal: Font) {

        //recuadro(document,"CATERING", fontTitulo)
        espacioEnBlanco(document)
        document.add(Paragraph("CATERING", fontSubtitulo))
        lineaHorizontal(document)

        var cateringDescripcion : String

        val fechaEvento: LocalDate = evento.inicio.toLocalDate()
        val eventoAnioMes: YearMonth = YearMonth.from(fechaEvento)

        if (evento.cateringOtro != 0.0) {
            cateringDescripcion = evento.cateringOtroDescripcion + ". Precio por plato: $" + evento.cateringOtro
        } else {
            val catering = evento.listaExtra.find { it.tipoExtra == TipoExtra.TIPO_CATERING } ?: Extra(
                0, "No cuenta con Catering incluido",
                TipoExtra.TIPO_CATERING
            )

            val precioCatering = buscarPrecio(catering.id, eventoAnioMes, evento.empresa.listaPrecioConFechaExtra)

            cateringDescripcion = if (catering.id != 0L) {
                "${catering.nombre}. Precio por plato: $$precioCatering"
            } else {
                catering.nombre
            }
        }

        document.add(Paragraph("Descripcion: $cateringDescripcion", fontNormal))
        document.add(
            Paragraph(
                "Total invitados: ${evento.capacidad.capacidadAdultos} adultos y ${evento.capacidad.capacidadNinos} niños",
                fontNormal
            )
        )

        val precioTipoEvento = evento.empresa.listaPrecioConFechaTipoEvento.find { it ->
            it.tipoEvento.id == evento.tipoEvento.id &&
                    YearMonth.from(it.desde) <= eventoAnioMes &&
                    eventoAnioMes <= YearMonth.from(it.hasta)
        }

        if (precioTipoEvento != null && precioTipoEvento.precio != 0.0) {
            //recuadro(document,"COSTO DEL SALON", fontTitulo)
            espacioEnBlanco(document)
            document.add(Paragraph("COSTO DEL SALON", fontSubtitulo))
            lineaHorizontal(document)
            document.add(Paragraph("${evento.tipoEvento.nombre}. Valor: $100000", fontNormal))
        }

        //recuadro(document,"TOTAL", fontTitulo, true)
        espacioEnBlanco(document)
        document.add(Paragraph("TOTAL", fontSubtitulo))
        lineaHorizontal(document)

        document.add(Paragraph("Valor: $${evento.getPresupuestoTotal()}", fontSubtitulo))

    }

    private fun setRecibo(document: Document, pago: Pago, fontSubtitulo: Font) {
        document.add(
            Paragraph(
                "Recibimos $${pago.monto} en concepto de ${pago.concepto.getDescripcion(pago)}",
                fontSubtitulo
            )
        )
    }

    private fun setFirma(evento: Evento, fontNormal: Font, cb: PdfContentByte) {

        // Coordenadas donde querés colocar el texto
        val x = 300f
        val y = 105f

        // Nombre y apellido
        ColumnText.showTextAligned(
            cb, Element.ALIGN_CENTER,
            Phrase(evento.encargado.nombre + " " + evento.encargado.apellido, fontNormal), x, y + 30, 0f
        )

        // Empresa
        ColumnText.showTextAligned(
            cb, Element.ALIGN_CENTER,
            Phrase(evento.empresa.nombre, fontNormal), x, y + 15, 0f
        )

        // Firma
        ColumnText.showTextAligned(
            cb, Element.ALIGN_CENTER,
            Phrase("Firma", fontNormal), x, y, 0f
        )

        // Dibuja la línea de la firma
        cb.moveTo(250f, 115f) // Mueve a la posición donde comienza la línea
        cb.lineTo(350f, 115f) // Dibuja la línea hasta el otro lado
        cb.stroke()
    }

    private fun setCuadroDescripcion(cb: PdfContentByte, cuadroGrande: Boolean) {

        // Setea color y ancho del cuadro
        cb.setColorStroke(Color.DARK_GRAY)
        cb.setLineWidth(0.5f)

        // Cambia la medida del cuadro de descripcion en base al tipo de comprobante
        var cuadroAltura = -140f

        if (cuadroGrande) {
            cuadroAltura = -400f

            // Coloca una linea divisoria correspondiente al cuadro grande
            cb.moveTo(30f, 455f) // Mueve a la posición donde comienza la línea
            cb.lineTo(560f, 455f) // Dibuja la línea hasta el otro lado

            // Coloca una linea divisoria correspondiente al cuadro grande
            cb.moveTo(30f, 415f) // Mueve a la posición donde comienza la línea
            cb.lineTo(560f, 415f) // Dibuja la línea hasta el otro lado
            cb.stroke()
        }

        // Dibuja el cuadro de descripcion
        cb.rectangle(30f, 565f, 530f, cuadroAltura)
        cb.stroke()

        // Dibuja la línea divisioria de descripcion
        cb.moveTo(30f, 525f) // Mueve a la posición donde comienza la línea
        cb.lineTo(560f, 525f) // Dibuja la línea hasta el otro lado
        cb.stroke()
    }

    class MargenPageEvent : PdfPageEventHelper() {
        override fun onEndPage(writer: PdfWriter, document: Document) {
            val cb = writer.directContent
            val pageSize = document.pageSize

            cb.setLineWidth(2f)
            cb.setColorStroke(Color.BLACK)

            cb.rectangle(10f, 10f, pageSize.width - 20f, pageSize.height - 20f)
            cb.stroke()
        }
    }

    fun lineaHorizontal(document: Document) {
        val lineSeparator = LineSeparator()
        lineSeparator.lineColor = Color.BLACK
        lineSeparator.lineWidth = 1f
        lineSeparator.offset = 8f
        document.add(Chunk(lineSeparator))
    }

    fun recuadro(document: Document, titulo: String, fontTitulo: Font, saltoDeLinea : Boolean) {
        if(saltoDeLinea){
            espacioEnBlanco(document)
        }

        val table = PdfPTable(1)
        table.widthPercentage = 100f

        val cell = PdfPCell(Phrase(titulo, fontTitulo))
        cell.border = PdfPCell.BOX
        cell.setPadding(5f)
        cell.horizontalAlignment = Element.ALIGN_CENTER

        table.addCell(cell)
        document.add(table)
    }

    fun espacioEnBlanco(document: Document) {
        document.add(Paragraph(" "))
    }

    private fun setInicioDocumento(document: Document, evento: Evento, fontNormal: Font, fontSubtitulo: Font) {

        // Abre el documento
        document.open()

        // Setea el page size
        setPageSize(document)

        // Logo
        setLogo(document, evento.empresa.logo)

        // Datos de la empresa
        setHeader(document, evento.empresa, fontNormal)

        // Espacio en blanco
        espacioEnBlanco(document)

        // Datos del cliente
        setDatosCliente(document, evento.cliente, fontSubtitulo)
    }

    fun generarComprobanteEvento(evento : Evento): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

        // Dibuja los margenes en todas las hojas
        writer.pageEvent = MargenPageEvent()

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)
        val fontClausula = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8f)

        // Setea el pageSize, logo, header y datos del cliente
        setInicioDocumento(document, evento, fontNormal, fontSubtitulo)

        // Descripción del evento
        setDescripcion(evento, document, fontTitulo, fontNormal)

        // Setea el catering, el monto del salon y el total presupuesto
        setMontoCapacidadCatering(evento, document, fontSubtitulo, fontNormal)

        // Setea titulo Informacion sobre evento
        recuadro(document, "INFORMACION SOBRE EL EVENTO", fontTitulo, true)
        //document.add(Paragraph("INFORMACION SOBRE EL EVENTO", fontTitulo))
        //lineaHorizontal(document)

        val ymEvento = YearMonth.from(evento.inicio)
        val listaPreciosExtra = evento.empresa.listaPrecioConFechaExtra

        val listaExtra = evento.listaExtra
            .filter { it.tipoExtra == TipoExtra.EVENTO }
            .map { extra ->
                val precio = buscarPrecio(extra.id, ymEvento, listaPreciosExtra)
                "${extra.nombre} $${precio}"
            }

        val listaExtraVariable = evento.listaEventoExtraVariable
            .filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
            .map { extraVar ->
                val cantidad = extraVar.cantidad
                val precio = buscarPrecio(extraVar.extra.id, ymEvento, listaPreciosExtra)
                "${extraVar.extra.nombre}: $cantidad x $${precio}"
            }

        // Agrega descripcion de catering
        val listaCatering = evento.listaEventoExtraVariable
            .filter{ it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }
            .map { extraVariable->
                val cantidad = extraVariable.cantidad
                val precio = buscarPrecio(extraVariable.extra.id, ymEvento, listaPreciosExtra)
                "${extraVariable.extra.nombre}: $cantidad x $${precio}"
            }

        if(listaExtra.isNotEmpty() || listaExtraVariable.isNotEmpty() || listaCatering.isNotEmpty()) {
            //recuadro(document, "EXTRAS", fontTitulo, true)}
            espacioEnBlanco(document)
            document.add(Paragraph("ADICIONALES DEL EVENTO:", fontSubtitulo))
            lineaHorizontal(document)

            document.add(Paragraph(listaExtra.joinToString(" - "), fontNormal))
            document.add(Paragraph(listaExtraVariable.joinToString(" - "), fontNormal))
            document.add(Paragraph(listaCatering.joinToString(" - "), fontNormal))
        }

        // Agrega descripcion de servicios
        val listaServicio = evento.tipoEvento.listaServicio.map{ servicio ->
            servicio.nombre
        }

        if(listaServicio.isNotEmpty()) {
            //recuadro(document,"SERVICIOS INCLUIDOS", fontTitulo, true)
            espacioEnBlanco(document)
            document.add(Paragraph("SERVICIOS INCLUIDOS:", fontSubtitulo))
            lineaHorizontal(document)

            document.add(Paragraph(listaServicio.joinToString(" - "), fontNormal))

        }

        // Agrega clausulas
        val listaClausula = evento.empresa.listaClausula.map{ clausula ->
            clausula.nombre
        }

        if(listaClausula.isNotEmpty()) {
            // Forzar nueva página para las cláusulas si estamos aún en la primera
            if (writer.pageNumber == 1) {
                document.newPage()
            }

            //recuadro(document,"IMPORTANTE", fontTitulo, false)
            espacioEnBlanco(document)
            document.add(Paragraph("IMPORTANTE:", fontSubtitulo))
            lineaHorizontal(document)

            listaClausula.forEach { it ->
                document.add(Paragraph("* $it", fontClausula))
            }
        }

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma(evento, fontNormal, cb)

        // Dibuja el cuadro de descripcion
        //setCuadroDescripcion(cb, true)

        // Setea la firma y dibuja la linea de firma
        setFirma(evento, fontNormal, cb)

        // Cierra el documento
        document.close()
        return baos.toByteArray()
    }

    fun buscarPrecio(extraId: Long, ymEvento: YearMonth, lista: MutableSet<PrecioConFechaExtra>): Double {
        return lista.find { r ->
            r.extra.id == extraId &&
                    YearMonth.from(r.desde) <= ymEvento &&
                    ymEvento <= YearMonth.from(r.hasta)
        }?.precio ?: 0.0
    }


    fun generarComprobanteDePago(pago : Pago): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

        // Dibuja los margenes en todas las hojas
        writer.pageEvent = MargenPageEvent()

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)

        // Setea el pageSize, logo, header y datos del cliente
        setInicioDocumento(document, pago.evento, fontNormal, fontSubtitulo)

        // Datos del pago
        setPago(document, pago, fontSubtitulo)

        // Descripción del evento
        setDescripcion(pago.evento, document, fontTitulo, fontNormal)

        lineaHorizontal(document)

        // Recibo con concepto de pago
        setRecibo(document, pago, fontSubtitulo)

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma(pago.evento, fontNormal, cb)

        // Dibuja el cuadro de descripcion
        //setCuadroDescripcion(cb, false)

        // Cierra el documento
        document.close()

        return baos.toByteArray()
    }


    fun generarEstadoDeCuenta(evento : Evento): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

        // Dibuja los margenes en todas las hojas
        writer.pageEvent = MargenPageEvent()

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)

        // Setea el pageSize, logo, header y datos del cliente
        setInicioDocumento(document, evento, fontNormal, fontSubtitulo)

        // Descripción del evento
        setDescripcion(evento, document, fontTitulo, fontNormal)

        // Espacio en blanco
        espacioEnBlanco(document)

        // Setea titulo Estado de cuenta
        document.add(Paragraph("ESTADO DE CUENTA", fontTitulo))
        lineaHorizontal(document)

        // Agrega lista de pagos del evento
        document.add(Paragraph("Lista de Pagos:"))

        evento.listaPago.filter { it.fechaBaja == null }.sortedBy{ it.id }.forEach{ pago ->
            document.add(Paragraph("- ${pago.concepto.getDescripcion(pago)} | fecha: ${pago.fecha.dayOfMonth}-${pago.fecha.monthValue}-${pago.fecha.year} | monto: $${pago.monto.toInt()} | medio de pago: ${pago.medioDePago}"))
        }

        lineaHorizontal(document)

        // Agrega el total abonado hasta la fecha y faltante
        document.add(Paragraph("Monto total abonado: $${evento.getTotalAbonado().toInt()} | Monto faltante: $${evento.getMontoFaltante().toInt()} | Total: $${evento.getPresupuestoTotal().toInt()}", fontSubtitulo))

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma( evento, fontNormal, cb)

        // Dibuja el cuadro de descripcion
        //setCuadroDescripcion(cb, true)

        // Cierra el documento
        document.close()

        return baos.toByteArray()
    }

}