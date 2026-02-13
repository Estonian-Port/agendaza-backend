package com.estonianport.agendaza

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
class AgendazaApplication : SpringBootServletInitializer()

     fun main(args: Array<String>) {
        runApplication<AgendazaApplication>(*args)
}
