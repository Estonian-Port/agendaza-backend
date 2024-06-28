package com.estonianport.agendaza

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication
@EnableEncryptableProperties
class AgendazaApplication : SpringBootServletInitializer()

     fun main(args: Array<String>) {
        runApplication<AgendazaApplication>(*args)
}
