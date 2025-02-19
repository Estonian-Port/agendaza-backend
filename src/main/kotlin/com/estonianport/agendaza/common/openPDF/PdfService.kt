package com.estonianport.agendaza.common.openPDF

import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.Pago
import com.lowagie.text.*
import com.lowagie.text.pdf.PdfContentByte
import com.lowagie.text.pdf.PdfWriter
import org.springframework.stereotype.Service
import org.springframework.util.ResourceUtils
import java.awt.Color
import java.io.ByteArrayOutputStream
import java.time.LocalDate



@Service
class PdfService {

    fun generarComprobanteEvento(evento : Evento): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()

        return baos.toByteArray()
    }

    fun generarComprobanteDePago(pago : Pago): ByteArray {

        val document = Document()
        val baos = ByteArrayOutputStream()
        val writer = PdfWriter.getInstance(document, baos)
        document.open()

        document.setPageSize(PageSize.A4)

        // Estilos de fuente
        val fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 15f)
        val fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12f)
        val fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12f)

        // Logo
        val rutaImagen = ResourceUtils.getFile("classpath:static/saveur.png").absolutePath
        val logo = Image.getInstance(rutaImagen) // Reemplaza con la ruta real
        logo.scaleToFit(100f, 100f) // Ajusta el tamaño del logo
        logo.alignment = Element.ALIGN_CENTER
        document.add(logo)

        // Datos de la empresa

        val titulo = Paragraph()
        titulo.alignment = Element.ALIGN_CENTER
        titulo.add(Chunk("Mail: ${pago.evento.empresa.email} \n", fontNormal))
        titulo.add(Chunk("Teléfono: ${pago.evento.empresa.telefono} \n", fontNormal))
        titulo.add(Chunk("${pago.evento.empresa.calle} ${pago.evento.empresa.numero} - ${pago.evento.empresa.municipio}", fontNormal))

        document.add(titulo)

        // Espacio en blanco
        document.add(Paragraph(" "))

        val hoy = LocalDate.now().dayOfMonth.toString() + "/" + LocalDate.now().monthValue.toString()  + "/" + LocalDate.now().year.toString()

        // Datos del cliente
        document.add(Paragraph("Nombre: ${pago.evento.cliente.nombre}, ${pago.evento.cliente.apellido}", fontSubtitulo) )
        document.add(Paragraph("Fecha: ${hoy}", fontSubtitulo))
        document.add(Paragraph("Forma de pago: ${pago.medioDePago}", fontSubtitulo))

        val diaEvento = pago.evento.inicio.toLocalDate().dayOfMonth.toString() + "/" + pago.evento.inicio.toLocalDate().monthValue.toString()  + "/" + pago.evento.inicio.toLocalDate().year.toString()
        val horaInicio: String = pago.evento.inicio.toLocalTime().toString()

        // Descripción del evento
        document.add(Paragraph("\nDESCRIPCIÓN \n \n", fontTitulo))
        document.add(Paragraph("Evento ${pago.evento.nombre} a realizarse el ${diaEvento} desde las ${horaInicio} en ${pago.evento.empresa.nombre}", fontNormal))
        document.add(Paragraph("Descripcion: ${pago.evento.cateringOtroDescripcion}", fontNormal))
        document.add(Paragraph("Total invitados: ${pago.evento.capacidad.capacidadAdultos} adultos y ${pago.evento.capacidad.capacidadNinos} niños", fontNormal))

        // Recibo
        document.add(Paragraph("Recibimos $${pago.monto} en concepto de pago", fontSubtitulo))

        // Firma
        val firma = Paragraph()
        firma.alignment = Element.ALIGN_CENTER
        firma.add(Chunk("\n\n\n\n\n\n ${pago.evento.encargado.nombre} ${pago.evento.encargado.apellido} \n", fontNormal))
        firma.add(Chunk("${pago.evento.empresa.nombre} \n", fontNormal))
        firma.add(Chunk("Firma", fontNormal))
        document.add(firma)


        // -------------------------------------------------------

        // Obtén el PdfContentByte para dibujar detrás del texto
        val cb = writer.directContent

        // Accede a la capa de contenido
        val content: PdfContentByte = writer.directContent

        // Dibuja un rectángulo para el recuadro
        cb.setColorStroke(Color.DARK_GRAY)
        content.setLineWidth(0.5f)
        content.rectangle(30f, 565f, 530f, -140f)
        content.stroke()

        // Dibuja la línea de descripcion
        content.moveTo(30f, 525f) // Mueve a la posición donde comienza la línea
        content.lineTo(560f, 525f) // Dibuja la línea hasta el otro lado

        // Dibuja la línea de la firma
        content.moveTo(250f, 315f) // Mueve a la posición donde comienza la línea
        content.lineTo(350f, 315f) // Dibuja la línea hasta el otro lado
        content.stroke()

        // Define el grosor y el color del borde
        cb.setLineWidth(0.5f) // Usa setLineWidth() en lugar de lineWidth
        cb.setColorStroke(Color.DARK_GRAY) // Usa Color.BLACK en lugar de BaseColor.BLACK

        // ------------------------------------------------------------
        // Obtén las dimensiones de la página
        val pageSize = document.pageSize
        val pageWidth = pageSize.width
        val pageHeight = pageSize.height

        cb.setLineWidth(2f)
        cb.setColorStroke(Color.BLACK)

        cb.rectangle(10f, 10f, pageWidth - 20f, pageHeight - 20f) // Coordenadas 0, 0 y dimensiones de la página
        cb.stroke()

        document.close()
        return baos.toByteArray()
    }

    fun generarEstadoDeCuenta(evento : Evento): ByteArray {
        val document = Document()
        val baos = ByteArrayOutputStream()

        // Configurar el escritor de PDF
        PdfWriter.getInstance(document, baos)

        // Abrir el documento
        document.open()

        // Agregar contenido al PDF
        document.add(Paragraph("Estado de Cuenta"))

        document.add(Paragraph("Evento: ${evento.nombre} - ${evento.codigo}"))

        document.add(Paragraph("Lista de Pagos:"))

        evento.listaPago.forEachIndexed{ index, pago ->
            document.add(Paragraph("(${index + 1}) ${pago.fecha} ${pago.monto} ${pago.medioDePago}"))
        }

        document.add(Paragraph("Monto total abonado: $${evento.getTotalAbonado()}"))
        document.add(Paragraph("Monto faltante: $${evento.getMontoFaltante()}"))
        document.add(Paragraph("Precio total del evento: $${evento.getPresupuestoTotal()}"))

        // Cerrar el documento
        document.close()

        return baos.toByteArray()
    }
}