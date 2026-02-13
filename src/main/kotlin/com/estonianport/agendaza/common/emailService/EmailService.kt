package com.estonianport.agendaza.common.emailService

import com.estonianport.agendaza.errors.BusinessException
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import com.estonianport.agendaza.model.EventoExtraVariable
import com.estonianport.agendaza.model.Extra
import com.estonianport.agendaza.model.Pago
import com.estonianport.agendaza.model.Servicio
import com.estonianport.agendaza.model.enums.TipoExtra
import jakarta.mail.MessagingException
import jakarta.mail.internet.MimeMessage
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

@Service
class EmailService {

    @Autowired
    lateinit var sender: JavaMailSender

    fun isEmailValid(target: String): Boolean {
        return target.isNotEmpty() && EmailValidator.getInstance().isValid(target)
    }

    fun sendEmail(emailBody: Email) {
        if(!isEmailValid(emailBody.email)){
            throw BusinessException("Email Invalido")
        }
        sendEmailTool(emailBody.content, emailBody.email, emailBody.subject)
    }

    private fun sendEmailTool(textMessage: String, email: String, subject: String) {
        val message: MimeMessage = sender.createMimeMessage()
        val helper = MimeMessageHelper(message)
        try {
            helper.setTo(email)
            helper.setText(textMessage, true)
            helper.setSubject(subject)
            sender.send(message)
        } catch (e: MessagingException) {
            println(e.message)
            throw BusinessException("No se pudo enviar el mail")
        }
    }

    fun loadHtmlTemplate(nombreArchivo: String): String {
        val inputStream = javaClass.classLoader.getResourceAsStream("templates/email/$nombreArchivo")
                ?: throw BusinessException("No se encontró la plantilla de mail")
        return inputStream.bufferedReader().use { it.readText() }
    }

    fun renderTemplate(template: String, replacements: Map<String, String>): String {
        var result = template
        for ((key, value) in replacements) {
            result = result.replace("{{${key}}}", value)
        }
        return result
    }

    fun enviarMailComprabanteReserva(evento: Evento, action: String, empresa : Empresa) {

        if(!isEmailValid(evento.cliente.email)){
            throw BusinessException("Email Invalido")
        }

        // -------------------------- Extra --------------------------
        val listaExtra: List<Extra> = evento.listaExtra.filter { it.tipoExtra == TipoExtra.EVENTO }
        val extraMail = StringBuilder()
        if (listaExtra.isNotEmpty()) {
            var i = 0
            extraMail.append("Con los siguientes extras: ")
            for (extra in listaExtra) {
                extraMail.append(extra.nombre)
                i++
                if (i < listaExtra.size) {
                    extraMail.append(", ")
                } else {
                    extraMail.append(".")
                }
            }
        } else {
            extraMail.append("Sin ningun extra.")
        }

        // -------------------------- Extra variable --------------------------
        val listaEventoEventoExtraVariable: List<EventoExtraVariable> = evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_EVENTO }
        val extraVariableMail = StringBuilder()
        if (listaEventoEventoExtraVariable.isNotEmpty()) {
            val listaExtraVariable: MutableSet<Extra> = mutableSetOf()
            for (eventoExtraVariableSubTipoEvento in listaEventoEventoExtraVariable) {
                listaExtraVariable.add(eventoExtraVariableSubTipoEvento.extra)
            }
            if (listaExtraVariable.isNotEmpty()) {
                var i = 0
                extraVariableMail.append("Con los siguientes extras variables: ")
                for (extraVariableSubTipoEvento in listaExtraVariable) {
                    extraVariableMail.append(extraVariableSubTipoEvento.nombre)
                    i++
                    if (i < listaExtraVariable.size) {
                        extraVariableMail.append(", ")
                    } else {
                        extraVariableMail.append(".")
                    }
                }
            }
        } else {
            extraVariableMail.append("Sin ningun extra variable.")
        }

        // -------------------------- Servicio --------------------------
        val listaServicios: Set<Servicio> = evento.tipoEvento.listaServicio
        val servicioMail = StringBuilder()
        if (listaServicios.isNotEmpty()) {
            var i = 0
            servicioMail.append("El evento incluye los siguientes servicios: ")
            for (servicio in listaServicios) {
                servicioMail.append(servicio.nombre)
                i++
                if (i < listaServicios.size) {
                    servicioMail.append(", ")
                } else {
                    servicioMail.append(".")
                }
            }
        } else {
            servicioMail.append("El evento no incluye ningun otro servicio.")
        }
        val catering = StringBuilder()

        // -------------------------- Catering --------------------------
        if (evento.getPresupuestoCatering() != 0.0) {

            // ------------------- Tipo Catering -----------------------
            val listaTipoCatering: List<Extra> = evento.listaExtra.filter { it.tipoExtra == TipoExtra.TIPO_CATERING }
            val tipoCateringMail = StringBuilder()
            if (listaTipoCatering.isNotEmpty()) {
                var i = 0
                tipoCateringMail.append("Tipo de catering: ")
                for (tipoCatering in listaTipoCatering) {
                    tipoCateringMail.append(tipoCatering.nombre)
                    i++
                    if (i < listaTipoCatering.size) {
                        tipoCateringMail.append(", ")
                    } else {
                        tipoCateringMail.append(".")
                    }
                }
            } else {
                tipoCateringMail.append("El evento no incluye ningun tipo catering.")
            }

            // ------------------- Extra Catering -----------------------
            val listaCateringExtraCatering: List<EventoExtraVariable> = evento.listaEventoExtraVariable.filter { it.extra.tipoExtra == TipoExtra.VARIABLE_CATERING }
            val extraVariableCateringMail = StringBuilder()
            if (listaCateringExtraCatering.isNotEmpty()) {
                val listaExtraCatering: MutableSet<Extra> = mutableSetOf()
                for (cateringExtraVariableCatering in listaCateringExtraCatering) {
                    listaExtraCatering.add(cateringExtraVariableCatering.extra)
                }
                if (listaExtraCatering.isNotEmpty()) {
                    var i = 0
                    extraVariableCateringMail.append("Extras catering: ")
                    for (extraVariableCatering in listaExtraCatering) {
                        extraVariableCateringMail.append(extraVariableCatering.nombre)
                        i++
                        if (i < listaExtraCatering.size) {
                            extraVariableCateringMail.append(", ")
                        } else {
                            extraVariableCateringMail.append(".")
                        }
                    }
                }
            } else {
                extraVariableCateringMail.append("El evento no incluye extra catering.")
            }
            catering.append("El catering contratado es el siguiente: ")
            catering.append("<br>")
            if (tipoCateringMail.isNotEmpty()) {
                catering.append(tipoCateringMail)
                catering.append("<br>")
            }
            if (extraVariableCateringMail.isNotEmpty()) {
                catering.append(extraVariableCateringMail)
                catering.append("<br>")
            }
        } else {
            catering.append("El evento no incluye catering")
        }

        // ---------------- Dia y hora evento ---------------------
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()

        // ----------------- Armado email -------------------------
        val emailBody = Email(
            evento.cliente.email,
            "Tu evento: " + evento.nombre + " para el " + dia + ", codigo: " + evento.codigo
        )

        // ----------------- Content email -------------------------
        val template = loadHtmlTemplate("comprobante_reserva.html")
        emailBody.content = renderTemplate(template, mapOf(
                "empresa_logo" to empresa.logo,
                "evento_nombre" to evento.nombre,
                "action" to action,
                "codigo" to evento.codigo,
                "tipo_evento" to evento.tipoEvento.nombre,
                "capacidad_adultos" to evento.capacidad.capacidadAdultos.toString(),
                "capacidad_ninos" to evento.capacidad.capacidadNinos.toString(),
                "presupuesto" to evento.getPresupuestoTotal().toString(),
                "dia" to dia,
                "hora_inicio" to horaInicio,
                "hora_fin" to horaFin,
                "empresa_nombre" to empresa.nombre,
                "empresa_telefono" to empresa.telefono.toString(),
                "empresa_email" to empresa.email,
                "extras" to extraMail.toString(),
                "extras_variables" to extraVariableMail.toString(),
                "catering" to catering.toString(),
                "servicios" to servicioMail.toString(),
                "imagen_comprobante" to "https://iili.io/3SXlAe2.png",
                "imagen_empresa" to "https://iili.io/3SXl7z7.png",
                "imagen_catering" to "https://iili.io/3SXlCsn.png",
                "imagen_extras" to "https://iili.io/3SXlzgf.png",
                "imagen_servicios" to "https://iili.io/3SXlx0G.png",
                "agendaza_logo" to "https://iili.io/3SVIGuj.png",
                "url_instagram" to "https://www.instagram.com/agendaza",
                "url_web" to "https://estonian-port.github.io/estonianport-landingpage/",
                "url_linkedin" to "https://www.linkedin.com/company/estonianport",
                "imagen_ig" to "https://iili.io/3USINa4.png",
                "imagen_web" to "https://iili.io/3USIh6G.png",
                "imagen_linkedin" to "https://iili.io/3USIwFf.png",
        ))

        // ----------------- Envio email -------------------------
        sendEmail(emailBody)

    }

    fun enviarEmailPago(pago: Pago, evento: Evento, empresa : Empresa) {
        val diaPago: String = pago.fecha.toLocalDate().toString()
        val horaPago: String = pago.fecha.toLocalTime().hour.toString() + ":" + pago.fecha.toLocalTime().minute.toString()

        // ---------------- Dia y hora evento ---------------------
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()

        // ----------------- Armado email -------------------------
        val emailBody =
                Email(evento.cliente.email, "Tu pago del evento  " + evento.nombre + ", codigo: " + evento.codigo)

        // ----------------- Content email -------------------------
        val template = loadHtmlTemplate("comprobante_pago.html")
        emailBody.content = renderTemplate(template, mapOf(
                "empresa_logo" to empresa.logo,
                "evento_nombre" to evento.nombre,
                "codigo" to evento.codigo,
                "tipo_evento" to evento.tipoEvento.nombre,
                "dia" to dia,
                "hora_inicio" to horaInicio,
                "hora_fin" to horaFin,
                "empresa_nombre" to empresa.nombre,
                "empresa_telefono" to empresa.telefono.toString(),
                "empresa_email" to empresa.email,
                "dia_pago" to diaPago,
                "hora_pago" to horaPago,
                "pago_monto" to pago.monto.toString(),
                "total_pago" to evento.getTotalAbonado().toString(),
                "monto_faltante" to evento.getMontoFaltante().toString(),
                "presupuesto_total" to evento.getPresupuestoTotal().toString(),
                "imagen_pago" to "https://iili.io/3Ut3E0X.png",
                "imagen_comprobante" to "https://iili.io/3SXlAe2.png",
                "imagen_empresa" to "https://iili.io/3SXl7z7.png",
                "agendaza_logo" to "https://iili.io/3SVIGuj.png",
                "url_instagram" to "https://www.instagram.com/agendaza",
                "url_web" to "https://estonian-port.github.io/estonianport-landingpage/",
                "url_linkedin" to "https://www.linkedin.com/company/estonianport",
                "imagen_ig" to "https://iili.io/3USINa4.png",
                "imagen_web" to "https://iili.io/3USIh6G.png",
                "imagen_linkedin" to "https://iili.io/3USIwFf.png"
        ))

        sendEmail(emailBody)
    }

    fun enviarEmailEstadoCuenta(evento: Evento, empresa : Empresa) {

        // ---------------- Dia y hora evento ---------------------
        val dia: String = evento.inicio.toLocalDate().toString()
        val horaInicio: String = evento.inicio.toLocalTime().toString()
        val horaFin: String = evento.fin.toLocalTime().toString()

        // ----------------- Armado email -------------------------
        val emailBody =
                Email(evento.cliente.email, "Tu estado de cuenta del evento  " + evento.nombre + ", codigo: " + evento.codigo)

        // ----------------- Content email -------------------------
        val template = loadHtmlTemplate("comprobante_estado_cuenta.html")
        emailBody.content = renderTemplate(template, mapOf(
                "empresa_logo" to empresa.logo,
                "evento_nombre" to evento.nombre,
                "codigo" to evento.codigo,
                "tipo_evento" to evento.tipoEvento.nombre,
                "dia" to dia,
                "hora_inicio" to horaInicio,
                "hora_fin" to horaFin,
                "empresa_nombre" to empresa.nombre,
                "empresa_telefono" to empresa.telefono.toString(),
                "empresa_email" to empresa.email,
                "total_pago" to evento.getTotalAbonado().toString(),
                "monto_faltante" to evento.getMontoFaltante().toString(),
                "presupuesto_total" to evento.getPresupuestoTotal().toString(),
                "imagen_pago" to "https://iili.io/3Ut3E0X.png",
                "imagen_comprobante" to "https://iili.io/3SXlAe2.png",
                "imagen_empresa" to "https://iili.io/3SXl7z7.png",
                "agendaza_logo" to "https://iili.io/3SVIGuj.png",
                "url_instagram" to "https://www.instagram.com/agendaza",
                "url_web" to "https://estonian-port.github.io/estonianport-landingpage/",
                "url_linkedin" to "https://www.linkedin.com/company/estonianport",
                "imagen_ig" to "https://iili.io/3USINa4.png",
                "imagen_web" to "https://iili.io/3USIh6G.png",
                "imagen_linkedin" to "https://iili.io/3USIwFf.png"
        ))

        sendEmail(emailBody)
    }

}