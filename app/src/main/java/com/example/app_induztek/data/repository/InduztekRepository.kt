package com.example.app_induztek.data.repository

import com.example.app_induztek.data.local.dao.InduztekDao
import com.example.app_induztek.data.local.entities.EquipoEntity
import com.example.app_induztek.data.local.entities.InstalacionEntity
import com.example.app_induztek.data.local.entities.MedicionEntity
import com.example.app_induztek.data.local.entities.ProtocoloEntity
import com.example.app_induztek.data.local.entities.UsuarioEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class InduztekRepository(
    private val dao: InduztekDao
) {


    suspend fun autenticar(correo: String, password: String): UsuarioEntity? = withContext(Dispatchers.IO) {
        dao.login(correo, password)
    }

    suspend fun getRolDeUsuario(usuarioId: Int): String? = withContext(Dispatchers.IO) {
        dao.getRolCodigoByUsuario(usuarioId)
    }



    val todasLasInstalaciones: Flow<List<InstalacionEntity>> = dao.getAllInstalaciones()


    fun getInstalacionesByCliente(clienteId: Int): Flow<List<InstalacionEntity>> {
        return dao.getInstalacionesByCliente(clienteId)
    }


    val todosLosEquipos: Flow<List<EquipoEntity>> = dao.getAllEquipos()

    fun getEquiposByInstalacion(instalacionId: Int): Flow<List<EquipoEntity>> {
        return dao.getEquiposByInstalacion(instalacionId)
    }


    val todosLosProtocolos: Flow<List<ProtocoloEntity>> = dao.getAllProtocolos()


    fun getProtocolosCliente(clienteId: Int): Flow<List<ProtocoloEntity>> {
        return dao.getProtocolosByCliente(clienteId)
    }

    val countPendientesSincronizar: Flow<Int> = dao.countPendientesSincronizar()

    fun getMedicionesByProtocolo(protocoloId: Int): Flow<List<MedicionEntity>> {
        return dao.getMedicionesByProtocolo(protocoloId)
    }

    val todasLasMediciones: Flow<List<MedicionEntity>> = dao.getAllMediciones()


    suspend fun guardarMedicionEnTerreno(
        protocolo: ProtocoloEntity,
        mediciones: List<MedicionEntity>
    ): Long = withContext(Dispatchers.IO) {
        val protocoloId = dao.insertProtocolo(protocolo)
        val medicionesConId = mediciones.map { it.copy(id_protocolo = protocoloId.toInt()) }
        dao.insertMediciones(medicionesConId)
        protocoloId
    }


    suspend fun sincronizarProtocolos() = withContext(Dispatchers.IO) {
        dao.sincronizarTodos()
    }

    suspend fun registrarCliente(cliente: UsuarioEntity) = withContext(Dispatchers.IO){
        dao.insertUsuarios(listOf(cliente))
    }
}

