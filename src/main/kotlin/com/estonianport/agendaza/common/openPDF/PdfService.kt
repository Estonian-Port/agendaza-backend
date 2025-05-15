package com.estonianport.agendaza.common.openPDF

import com.estonianport.agendaza.model.*
import com.lowagie.text.*
import com.lowagie.text.pdf.ColumnText
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import org.springframework.stereotype.Service
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.LocalDate



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

    private fun setHeader(document: Document, empresa: Empresa, fontNormal : Font) {

        val header = Paragraph()
        header.alignment = Element.ALIGN_CENTER
        header.add(Chunk("Mail: ${empresa.email} \n", fontNormal))
        header.add(Chunk("Teléfono: ${empresa.telefono} \n", fontNormal))
        header.add(Chunk("${empresa.calle} ${empresa.numero} - ${empresa.municipio}", fontNormal))

        document.add(header)
    }

    private fun setDatosCliente(document: Document, cliente: Usuario, fontSubtitulo: Font) {
        // Datos del cliente
        document.add(Paragraph("Nombre: ${cliente.nombre}, ${cliente.apellido}", fontSubtitulo))

    }

    fun setPago(document: Document, pago: Pago, fontSubtitulo: Font){
        val hoy = LocalDate.now().dayOfMonth.toString() + "/" + LocalDate.now().monthValue.toString() + "/" + LocalDate.now().year.toString()
        document.add(Paragraph("Fecha: ${hoy}", fontSubtitulo))
        document.add(Paragraph("Forma de pago: ${pago.medioDePago}", fontSubtitulo))
    }

    private fun setDescripcion(evento: Evento, document: Document, fontTitulo: Font?, fontNormal: Font?) {
        val diaEvento = evento.inicio.toLocalDate().dayOfMonth.toString() + "/" + evento.inicio.toLocalDate().monthValue.toString() + "/" + evento.inicio.toLocalDate().year.toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()

        document.add(Paragraph("\nDESCRIPCIÓN \n \n", fontTitulo))
        document.add(Paragraph("Evento: ${evento.nombre} a realizarse el ${diaEvento} desde las ${horaInicio} en ${evento.empresa.nombre}", fontNormal))
        document.add(Paragraph("Descripcion catering: ${evento.cateringOtroDescripcion}", fontNormal))
        document.add(Paragraph("Total invitados: ${evento.capacidad.capacidadAdultos} adultos y ${evento.capacidad.capacidadNinos} niños", fontNormal))
    }

    private fun setRecibo(document: Document, pago: Pago, fontSubtitulo: Font?) {
        document.add(Paragraph("Recibimos $${pago.monto} en concepto de pago", fontSubtitulo))
    }

    private fun setFirma(document: Document, evento: Evento, fontNormal: Font, cb: PdfContentByte) {

        // Coordenadas donde querés colocar el texto
        val x = 300f
        val y = 105f

        // Nombre y apellido
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                Phrase(evento.encargado.nombre + " " + evento.encargado.apellido, fontNormal), x, y + 30, 0f)

        // Empresa
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                Phrase(evento.empresa.nombre, fontNormal), x, y + 15, 0f)

        // Firma
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                Phrase("Firma", fontNormal), x, y, 0f)

        // Dibuja la línea de la firma
        cb.moveTo(250f, 115f) // Mueve a la posición donde comienza la línea
        cb.lineTo(350f, 115f) // Dibuja la línea hasta el otro lado
        cb.stroke()
    }

    private fun setCuadroDescripcion(document : Document, cb: PdfContentByte, cuadroGrande : Boolean) {

        // Setea color y ancho del cuadro
        cb.setColorStroke(Color.DARK_GRAY)
        cb.setLineWidth(0.5f)

        // Cambia la medida del cuadro de descripcion en base al tipo de comprobante
        var cuadroAltura = -140f

        if(cuadroGrande){
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

    private fun setMargenes(document : Document, cb: PdfContentByte) {

        // Obtén las dimensiones de la página
        val pageSize = document.pageSize

        // Setea grosor y color
        cb.setLineWidth(2f)
        cb.setColorStroke(Color.BLACK)

        // Dibuja margenes
        cb.rectangle(10f, 10f, pageSize.width - 20f, pageSize.height - 20f) // Coordenadas 0, 0 y dimensiones de la página
        cb.stroke()
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
        document.add(Paragraph(" "))

        // Datos del cliente
        setDatosCliente(document, evento.cliente, fontSubtitulo)
    }

    fun generarComprobanteEvento(evento : Evento): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)

        // Setea el pageSize, logo, header y datos del cliente
        setInicioDocumento(document, evento, fontNormal, fontSubtitulo)

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Descripción del evento
        setDescripcion(evento, document, fontTitulo, fontNormal)

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Setea titulo Informacion sobre evento
        document.add(Paragraph("INFORMACION SOBRE EL EVENTO", fontTitulo))

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Agrega descripcion de extras
        val listaExtra = evento.listaExtra.filter{ it.tipoExtra == TipoExtra.EVENTO || it.tipoExtra == TipoExtra.VARIABLE_EVENTO }.map{ extra ->
            extra.nombre
        }
        if(listaExtra.isNotEmpty()){
            document.add(Paragraph("Extras: ${listaExtra}", fontNormal))
        }

        // Agrega descripcion de catering
        val listaCatering = evento.listaExtra.filter{ it.tipoExtra == TipoExtra.TIPO_CATERING || it.tipoExtra == TipoExtra.VARIABLE_CATERING }.map{ extra ->
            extra.nombre
        }
        if(listaCatering.isNotEmpty()){
            document.add(Paragraph("Catering: ${listaCatering}", fontNormal))
        }

        // Agrega descripcion de servicios
        val listaServicio = evento.tipoEvento.listaServicio.map{ servicio ->
            servicio.nombre
        }
        if(listaServicio.isNotEmpty()) {
            document.add(Paragraph("Servicios: ${listaServicio}", fontNormal))
        }

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma(document, evento, fontNormal, cb)

        // Dibuja los margenes exteriores
        setMargenes(document, cb)

        // Dibuja el cuadro de descripcion
        setCuadroDescripcion(document, cb, true)

        // Setea la firma y dibuja la linea de firma
        setFirma(document, evento, fontNormal, cb)

        // Cierra el documento
        document.close()
        return baos.toByteArray()
    }

    fun generarComprobanteDePago(pago : Pago): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

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

        // Recibo con concepto de pago
        setRecibo(document, pago, fontSubtitulo)

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma(document, pago.evento, fontNormal, cb)

        // Dibuja los margenes exteriores
        setMargenes(document, cb)

        // Dibuja el cuadro de descripcion
        setCuadroDescripcion(document, cb, false)

        // Setea la firma y dibuja la linea de firma
        setFirma(document,pago.evento,fontNormal, cb)

        // Cierra el documento
        document.close()

        return baos.toByteArray()
    }


    fun generarEstadoDeCuenta(evento : Evento): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)

        // Setea el pageSize, logo, header y datos del cliente
        setInicioDocumento(document, evento, fontNormal, fontSubtitulo)

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Descripción del evento
        setDescripcion(evento, document, fontTitulo, fontNormal)

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Setea titulo Estado de cuenta
        document.add(Paragraph("ESTADO DE CUENTA", fontTitulo))

        // Espacio en blanco
        document.add(Paragraph(" "))

        // Agrega lista de pagos del evento
        document.add(Paragraph("Lista de Pagos:"))

        //TODO mejorar concepto sacandolo de pago.concepto
        evento.listaPago.sortedBy{ it.id }.forEachIndexed{ index, pago ->
            var concepto = "Seña"
            if(true){
                if(true){
                    concepto = "Cuota (${index + 1})"
                }else{
                    concepto = "Cuota (${index + 2})"
                }
            }
            document.add(Paragraph("- ${concepto} | fecha: ${pago.fecha.dayOfMonth}-${pago.fecha.monthValue}-${pago.fecha.year} | monto: $${pago.monto.toInt()} | medio de pago: ${pago.medioDePago}"))
        }

        document.add(Paragraph("Monto total abonado: $${evento.getTotalAbonado().toInt()} | Monto faltante: $${evento.getMontoFaltante().toInt()} | Total: $${evento.getPresupuestoTotal().toInt()}", fontSubtitulo))

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Firma final de documento
        setFirma(document, evento, fontNormal, cb)

        // Dibuja los margenes exteriores
        setMargenes(document, cb)

        // Dibuja el cuadro de descripcion
        setCuadroDescripcion(document, cb, true)

        // Setea la firma y dibuja la linea de firma
        setFirma(document, evento, fontNormal, cb)

        // Cierra el documento
        document.close()

        return baos.toByteArray()
    }

}