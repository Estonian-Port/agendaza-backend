package com.estonianport.agendaza

import com.estonianport.agendaza.model.*
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.time.LocalDateTime
import java.time.LocalTime

class EspecificacionSpec: DescribeSpec({

    val empresa = Salon(1, "Saveur", 1212121212, "a@gmail.com", "Urquiza", 1, "3F")

    // =================== Extras ========================

    val extraNino = Extra(1, "Niños", TipoExtra.VARIABLE_EVENTO)
    val extraCamareraCorto = Extra(2, "Camarera Corto", TipoExtra.VARIABLE_EVENTO)
    val extraCamareraLargo = Extra(3, "Camarera Largo", TipoExtra.VARIABLE_EVENTO)

    // ===================================================

    // ================ Especificacion ===================

    val porcentajePrecioPlatoNinos = 60

    val precioDePlatoNinos = PrecioDePlatoNinos(0, empresa, porcentajePrecioPlatoNinos)
    empresa.listaEspecificacion.add(precioDePlatoNinos)

    val agregarExtraNinoSiSuperaCapacidad = AgregarExtraNinoSiSuperaCapacidad(0, empresa, extraNino)
    empresa.listaEspecificacion.add(agregarExtraNinoSiSuperaCapacidad)

    val agregarExtraCamareraSiSuperaCapacidadCorto = AgregarExtraCamareraSiSuperaCapacidad(0, empresa, extraCamareraCorto, Duracion.CORTO)
    empresa.listaEspecificacion.add(agregarExtraCamareraSiSuperaCapacidadCorto)

    val agregarExtraCamareraSiSuperaCapacidadLargo = AgregarExtraCamareraSiSuperaCapacidad(0, empresa, extraCamareraLargo, Duracion.LARGO)
    empresa.listaEspecificacion.add(agregarExtraCamareraSiSuperaCapacidadLargo)

    // ===================================================

    // ================== Capacidad ======================

    val capacidadTipoEvento = Capacidad(1, 30, 30)

    val capacidadEventoSuperaNinoYAdultos = Capacidad(1, 40, 40)
    val capacidadEventoNoSuperaNinoYAdultos = Capacidad(1, 30, 30)
    val capacidadEventoSuperaNino = Capacidad(1, 30, 40)
    val capacidadEventoSuperaAdultos = Capacidad(1, 40, 30)

    // ===================================================

    // ================ Tipo Evento ======================

    val tipoEventoCorto = TipoEvento(1, "Cumpleaños", Duracion.CORTO, capacidadTipoEvento, LocalTime.now(), empresa)
    tipoEventoCorto.listaExtra.add(extraNino)
    tipoEventoCorto.listaExtra.add(extraCamareraCorto)

    val tipoEventoLargo = TipoEvento(1, "Casamiento", Duracion.LARGO, capacidadTipoEvento, LocalTime.now(), empresa)
    tipoEventoLargo.listaExtra.add(extraNino)
    tipoEventoLargo.listaExtra.add(extraCamareraLargo)

    // ===================================================

    // ================== Usuario ========================

    val encargado = Usuario(1, "Pedro", "Rodriguez", 1212121212, "a@gamil.com")
    val cliente = Usuario(1, "Juan", "Rodriguez", 1212121212, "a@gamil.com")

    // ===================================================

    // =================== Evento ========================

    val eventoCorto = Evento(1, "Cumpleaños Juan", tipoEventoCorto, LocalDateTime.now(), LocalDateTime.now(),
            capacidadEventoSuperaNinoYAdultos, 0.0, 0, mutableSetOf(), mutableSetOf(), 0.0, "",
            encargado, cliente, "AAAA", Estado.RESERVADO, "", empresa)

    val eventoLargo = Evento(1, "Casamiento Juana", tipoEventoLargo, LocalDateTime.now(), LocalDateTime.now(),
            capacidadEventoSuperaNinoYAdultos, 0.0, 0, mutableSetOf(), mutableSetOf(), 0.0, "",
            encargado, cliente, "BBBB", Estado.RESERVADO, "", empresa)

    // ===================================================

    describe("Dado una empresa con especificaciones extra nino y extra camarera") {

        beforeEach {
            // Limpiar las listas de extras de cada evento entre cada test
            eventoCorto.listaEventoExtraVariable.clear()
            eventoLargo.listaEventoExtraVariable.clear()
        }

        it(name = "Agregar extra de niños si supera la capacidad de niños en tipo evento corto") {
            // ============== Act =============

            empresa.recorrerEspecificaciones(eventoCorto)

            // ================================

            // ============ Assert ============

            // Debe agregar el extra de niños y extra camarera corto
            // porque el evento cuenta con capacidadEventoSuperaNinoYAdultos
            eventoCorto.listaEventoExtraVariable.size.shouldBe(2)

            // La lista de extras debe contener extraNino y extraCamareraCorto

            eventoCorto.listaEventoExtraVariable.any { it.extra == extraNino }
            val extraVariableNino = eventoCorto.listaEventoExtraVariable.find { it.extra == extraNino }!!
            extraVariableNino.cantidad.shouldBe(10)

            eventoCorto.listaEventoExtraVariable.any { it.extra == extraCamareraCorto }
            val extraVariableCamarera = eventoCorto.listaEventoExtraVariable.find { it.extra == extraCamareraCorto }!!
            extraVariableCamarera.cantidad.shouldBe(1)

            // ================================

        }

        it(name = "No agregar extra de niños si no supera la capacidad de niños en tipo evento corto") {

            // ============== Act =============

            // Cambiar capacidad del evento para no superar el límite
            eventoCorto.capacidad = capacidadEventoNoSuperaNinoYAdultos

            // Aplicar especificaciones
            empresa.recorrerEspecificaciones(eventoCorto)

            // ================================

            // ============ Assert ============

            // No debería agregar el extra de niños
            eventoCorto.listaEventoExtraVariable.shouldBeEmpty()

            // ================================

        }

        it(name = "Agregar extra de camarera corto si supera la capacidad de adultos en tipo evento corto") {

            // ============== Act =============

            // Cambiar capacidad del evento para que supere capacidad adultos
            eventoCorto.capacidad = capacidadEventoSuperaAdultos

            empresa.recorrerEspecificaciones(eventoCorto)

            // ================================

            // ============ Assert ============

            // Debe agregar el extra de camarera corto
            eventoCorto.listaEventoExtraVariable.size.shouldBe(1)

            // El extra debe ser el extraCamareraCorto
            eventoCorto.listaEventoExtraVariable.any { it.extra == extraCamareraCorto }


            // ================================

        }

        it(name = "Agrega solo extraNino porque la capacidad solo supera en niños") {

            // ============== Act =============

            // Cambiar capacidad del evento para no superar el límite de adultos
            eventoCorto.capacidad = capacidadEventoSuperaNino

            // Aplicar especificaciones
            empresa.recorrerEspecificaciones(eventoCorto)

            // ================================

            // ============ Assert ============

            // Solo debería agregar el extra de niños
            eventoCorto.listaEventoExtraVariable.size.shouldBe(1)

            // El único extra debería ser el extraNino
            eventoCorto.listaEventoExtraVariable.any { it.extra == extraNino }

            // ================================

        }

        it(name = "Agregar extra de camarera largo si supera la capacidad de adultos en tipo evento largo") {

            // ============== Act =============

            // Cambiar capacidad del evento para no superar el límite de adultos
            eventoLargo.capacidad = capacidadEventoSuperaAdultos

            // Aplicar especificaciones
            empresa.recorrerEspecificaciones(eventoLargo)

            // ================================

            // ============ Assert ============

            // Solo debería agregar el extra de niños
            eventoLargo.listaEventoExtraVariable.size.shouldBe(1)

            // El único extra debería ser el extraNino
            eventoLargo.listaEventoExtraVariable.any { it.extra == extraCamareraCorto }

            // ================================

        }

        it(name = "Agregar extra de camarera largo y niños si supera la capacidad de adultos y niños en tipo evento largo") {
            // ============== Act =============

            // Cambiar capacidad del evento para no superar el límite de adultos
            eventoLargo.capacidad = capacidadEventoSuperaNinoYAdultos

            empresa.recorrerEspecificaciones(eventoLargo)

            // ================================

            // ============ Assert ============

            // Debería agregar el extra de camarera largo
            eventoLargo.listaEventoExtraVariable.size.shouldBe(2)

            // El último extra debería ser el extraCamareraLargo
            eventoLargo.listaEventoExtraVariable.any { it.extra == extraNino }
            eventoLargo.listaEventoExtraVariable.any { it.extra == extraCamareraLargo }

            // ================================

        }
    }

    describe("Dado una empresa con especificaciones extra otro por cant de ninos por plato"){
        it(name = "No aplicar ningún valor cuando no hay cateringOtro ni extra de tipo TIPO_CATERING") {
            // ============== Act =============

            // No asignar ni cateringOtro ni extra de tipo TIPO_CATERING
            eventoLargo.cateringOtro = 0.0

            // Recorrer las especificaciones para aplicar los ajustes
            empresa.recorrerEspecificaciones(eventoLargo)

            // ================================

            // ============ Assert ============

            // El valor de extraOtro debería ser 0, ya que no hay catering ni extra
            eventoLargo.extraOtro.shouldBe(0.0)

            // ================================

        }


        /*it(name = "Aplicar cateringOtro en el evento y calcular el presupuesto") {
            // ============== Act =============

            // Asignar un valor específico para cateringOtro
            val precioDePlato = 1000.0
            eventoLargo.cateringOtro = precioDePlato

            // Recorrer las especificaciones para aplicar los ajustes
            empresa.recorrerEspecificaciones(eventoLargo)

            // ================================

            // ============ Assert ============

            // Debería aplicarse el cálculo con el precio de cateringOtro
            eventoLargo.extraOtro.shouldBe(precioDePlato * eventoLargo.capacidad.capacidadNinos * (porcentajePrecioPlatoNinos / 100))

            // ================================

        }

        it(name = "Aplicar extra de tipo TIPO_CATERING y calcular el presupuesto") {
            // ============== Act =============

            // Crear un extra de tipo TIPO_CATERING con un precio

            val precioDePlato = 1000.0

            val extraCateringPizzaParty = Extra(1, "Pizza Party", TipoExtra.TIPO_CATERING, empresa)

            // Crear precio con fecha para el extra
            val extraCateringPizzaPartyPrecioConFecha = PrecioConFechaExtra(1, precioDePlato,
                    LocalDateTime.now(), LocalDateTime.now(), empresa, extraCateringPizzaParty)

            // Agregar el extra y su precio con fecha a la empresa
            eventoLargo.listaExtra.add(extraCateringPizzaParty)
            empresa.listaPrecioConFechaExtra.add(extraCateringPizzaPartyPrecioConFecha)

            // Recorrer las especificaciones para aplicar los ajustes
            empresa.recorrerEspecificaciones(eventoLargo)

            // ================================

            // ============ Assert ============

            // Debería aplicarse el cálculo con el precio del extra de tipo catering
            eventoLargo.extraOtro.shouldBe(eventoLargo.capacidad.capacidadNinos * precioDePlato * (porcentajePrecioPlatoNinos / 100))

            // ================================

        }*/
    }
})