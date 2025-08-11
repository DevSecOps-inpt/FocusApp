package com.devsecopsinpt.focusapp.data.repository

import com.devsecopsinpt.focusapp.data.local.dao.BlockedAppDao
import com.devsecopsinpt.focusapp.data.local.entity.BlockedApp
import com.devsecopsinpt.focusapp.data.local.entity.BlockedAppEntity
import com.devsecopsinpt.focusapp.crypto.CryptoStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppRepository @Inject constructor(
    private val dao: BlockedAppDao,
    private val crypto: CryptoStore
) {

    suspend fun addOrUpdate(app: BlockedApp) {
        val entity = BlockedAppEntity(
            packageNameEnc = crypto.encrypt(app.packageName),
            labelEnc = crypto.encrypt(app.label),
            addedAt = app.addedAt
        )
        dao.upsert(entity)
    }

    suspend fun list(): List<BlockedApp> =
        dao.getAll().map {
            BlockedApp(
                packageName = crypto.decrypt(it.packageNameEnc),
                label = crypto.decrypt(it.labelEnc),
                addedAt = it.addedAt
            )
        }

    suspend fun remove(app: BlockedApp) {
        val entity = BlockedAppEntity(
            packageNameEnc = crypto.encrypt(app.packageName),
            labelEnc = crypto.encrypt(app.label),
            addedAt = app.addedAt
        )
        dao.delete(entity)
    }

    suspend fun getByPackageName(packageName: String): BlockedApp? {
        val encryptedPackageName = crypto.encrypt(packageName)
        return dao.getByPackageName(encryptedPackageName)?.let {
            BlockedApp(
                packageName = crypto.decrypt(it.packageNameEnc),
                label = crypto.decrypt(it.labelEnc),
                addedAt = it.addedAt
            )
        }
    }
}
