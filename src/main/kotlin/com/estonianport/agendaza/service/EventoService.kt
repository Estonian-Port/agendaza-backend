package com.estonianport.agendaza.service

import GenericServiceImpl
import com.estonianport.agendaza.common.codeGeneratorUtil.CodeGeneratorUtil
import com.estonianport.agendaza.dao.EventoDao
import com.estonianport.agendaza.model.Empresa
import com.estonianport.agendaza.model.Evento
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Service

@Service
class EventoService : GenericServiceImpl<Evento, Long>() {

    @Autowired
    lateinit var eventoDao: EventoDao

    override val dao: CrudRepository<Evento, Long>
        get() = eventoDao

    fun generateCodigoForEventoOfEmpresa(empresa : Empresa) : String{
        var codigo : String = CodeGeneratorUtil.base26Only4Letters

        try{
            while (this.existCodigoInEmpresa(codigo, empresa)){
                codigo = CodeGeneratorUtil.base26Only4Letters
            }
        }catch (error : NullPointerException){
            return codigo
        }

        return codigo
    }

    fun existCodigoInEmpresa(codigo : String, empresa : Empresa) : Boolean{
        return empresa.listaEvento.any{ it.codigo == codigo}
    }
}