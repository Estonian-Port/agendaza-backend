package com.estonianport.agendaza

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableCaching
@EnableScheduling
class AgendazaApplication : SpringBootServletInitializer()

     fun main(args: Array<String>) {
        runApplication<AgendazaApplication>(*args)
}
