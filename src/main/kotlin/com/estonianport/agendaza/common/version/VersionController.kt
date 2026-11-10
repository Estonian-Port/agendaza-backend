package com.estonianport.agendaza.common.version

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/version")
class VersionController(
    @param:Value("\${app.version.web.current:1.0.0}") private val webCurrent: String,
    @param:Value("\${app.version.android.current:1.0.0}") private val androidCurrent: String,
    @param:Value("\${app.version.ios.current:1.0.0}") private val iosCurrent: String,
) {
    @GetMapping("/latest")
    fun latest() = mapOf(
        "web" to VersionInfoDTO(current = webCurrent),
        "android" to VersionInfoDTO(current = androidCurrent),
        "ios" to VersionInfoDTO(current = iosCurrent)
    )
}