package com.commitchronicle.config

import java.io.File
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import com.commitchronicle.language.Locale

@Serializable
data class UserConfig(
    val apiKey: String? = null,
    val providerType: String? = null,
    val modelName: String? = null,
    val locale: String? = null,
    val defaultDays: Int = 7,
    val defaultLimit: Int = 50
) {
    fun getLocale(): Locale? = locale?.let { Locale.fromCode(it) }

    companion object {
        private val configDir = File(System.getProperty("user.home"), ".commit-chronicle")
        private val configFile = File(configDir, "config.json")
        private val json = Json { prettyPrint = true }

        fun load(): UserConfig {
            if (!configFile.exists()) {
                return UserConfig()
            }
            return try {
                json.decodeFromString<UserConfig>(configFile.readText())
            } catch (e: Exception) {
                println("Error loading config file: ${e.message}")
                UserConfig()
            }
        }

        fun save(config: UserConfig) {
            try {
                if (!configDir.exists()) {
                    configDir.mkdirs()
                }
                configFile.writeText(json.encodeToString(config))
            } catch (e: Exception) {
                println("Error saving config file: ${e.message}")
            }
        }
    }
} 