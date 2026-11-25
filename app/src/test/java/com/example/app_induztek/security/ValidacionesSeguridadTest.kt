package com.example.app_induztek.security

import com.example.app_induztek.ui.util.FiltrosEntrada
import io.kotest.matchers.shouldBe
import org.junit.Test

class ValidacionesSeguridadTest {

    @Test
    fun rutConDigitoVerificadorCorrectoEsValido() {
        RutValidator.esValido("12.345.678-5") shouldBe true
    }

    @Test
    fun rutConDigitoVerificadorIncorrectoEsInvalido() {
        RutValidator.esValido("12.345.678-9") shouldBe false
    }

    @Test
    fun rutConTodosLosDigitosIgualesEsInvalido() {
        RutValidator.esValido("11.111.111-1") shouldBe false
    }

    @Test
    fun correoConSoloNumerosAntesDelArrobaEsInvalido() {
        ContactoValidator.correoValido("1111@asdas.cl") shouldBe false
        ContactoValidator.correoValido("contacto@mineranorte.cl") shouldBe true
    }

    @Test
    fun telefonoChilenoDebeTenerNueveDigitosSinRepetirse() {
        ContactoValidator.telefonoValido("+56 9 1234 5678") shouldBe true
        ContactoValidator.telefonoValido("1111111") shouldBe false
        ContactoValidator.telefonoValido("999999999") shouldBe false
    }

    @Test
    fun nombreDebeTenerSoloLetrasEntreDosYTreinta() {
        ContactoValidator.nombreValido("José") shouldBe true
        ContactoValidator.nombreValido("Pedro2") shouldBe false
        ContactoValidator.nombreValido("P") shouldBe false
    }

    @Test
    fun claveSinMayusculaNiNumeroNiEspecialNoCumpleLaPolitica() {
        val resultado = PasswordPolicy.evaluar("abcdefgh")

        resultado.longitudOk shouldBe true
        resultado.tieneMayuscula shouldBe false
        resultado.tieneNumero shouldBe false
        resultado.tieneEspecial shouldBe false
        resultado.valida shouldBe false
    }

    @Test
    fun claveConTodosLosRequisitosCumpleLaPolitica() {
        PasswordPolicy.evaluar("Tecnico123!").valida shouldBe true
    }

    @Test
    fun filtroDecimalAceptaComaLimitaEnterosYDecimales() {
        FiltrosEntrada.decimal("1234,567", maxEnteros = 3, maxDecimales = 2) shouldBe "123.56"
    }
}
