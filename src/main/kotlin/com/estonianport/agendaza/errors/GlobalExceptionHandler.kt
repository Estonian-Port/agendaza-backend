package com.estonianport.agendaza.errors

import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler(value = [NotFoundException::class, BusinessException::class])
    fun handleCustomExceptions(exception: Exception, webRequest: WebRequest): ResponseEntity<Any>? {

        val status = resolveAnnotatedResponseStatus(exception)

        val body = mapOf(
            "message" to exception.message,
            "status" to status.value()
        )

        return handleExceptionInternal(
            exception,
            body,
            HttpHeaders(),
            status,
            webRequest
        )
    }

    private fun resolveAnnotatedResponseStatus(exception: Exception): HttpStatus {
        val annotation = AnnotatedElementUtils.findMergedAnnotation(exception.javaClass, ResponseStatus::class.java)
        return annotation?.value ?: HttpStatus.INTERNAL_SERVER_ERROR
    }
}