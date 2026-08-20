package com.asha.worker.ai.data

import android.content.Context
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.security.SecureRandom

/**
 * Supplies a stable 256-bit passphrase for the SQLCipher database. The passphrase is
 * generated once and stored in EncryptedSharedPreferences (Android Keystore-backed),
 * so the health PII on disk is encrypted and the key never lives in plaintext.
 */
object DbKey {

    private const val PREFS = "asha_secure_prefs"
    private const val KEY = "db_passphrase"

    fun getOrCreate(context: Context): ByteArray {
        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        val prefs = EncryptedSharedPreferences.create(
            PREFS,
            masterKeyAlias,
            context.applicationContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        prefs.getString(KEY, null)?.let { return Base64.decode(it, Base64.NO_WRAP) }

        val passphrase = ByteArray(32).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString(KEY, Base64.encodeToString(passphrase, Base64.NO_WRAP))
            .apply()
        return passphrase
    }
}
