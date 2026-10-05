import com.estonianport.agendaza.common.emailService.EmailService
import io.mockk.spyk
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EmailServiceTest {

    private lateinit var service: EmailService

    @BeforeEach
    fun setUp() {
        service = spyk(EmailService("test-api-key"))
    }

    @Test
    fun `isEmailValid acepta direcciones validas`() {
        assertTrue(service.isEmailValid("persona@example.com"))
        assertTrue(service.isEmailValid("nombre.apellido+agenda@sub.example.com.ar"))
    }

    @Test
    fun `isEmailValid rechaza direcciones vacias o con formato invalido`() {
        assertFalse(service.isEmailValid(""))
        assertFalse(service.isEmailValid("persona"))
        assertFalse(service.isEmailValid("persona@"))
        assertFalse(service.isEmailValid("persona@example"))
        assertFalse(service.isEmailValid("persona @example.com"))
    }

    @Test
    fun `isEmailValid rechaza emails marcados como inexistentes`() {
        assertFalse(service.isEmailValid("sin-email-123@agendaza.com.ar"))
    }

    @Test
    fun `loadHtmlTemplate carga las plantillas reales`() {
        listOf(
            "comprobante_reserva.html",
            "comprobante_pago.html",
            "comprobante_estado_cuenta.html"
        ).forEach { assertTrue(service.loadHtmlTemplate(it).isNotBlank()) }
    }

}