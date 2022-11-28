package com.estonianport.agendaza

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableEncryptableProperties
class AgendazaApplication

fun main(args: Array<String>) {
    runApplication<AgendazaApplication>(*args)
}
