package com.commitchronicle.language

import java.util.Properties

class MessageBundleImpl : MessageBundle {
    companion object {
        private val bundles = mutableMapOf<String, Properties>()

        init {
            loadBundle(Locale.ENGLISH)
            loadBundle(Locale.KOREAN)
        }

        private fun loadBundle(locale: Locale) {
            val properties = Properties()
            val resourceName = "/messages_${locale.code}.properties"
            println("Loading bundle for locale: $locale, resource: $resourceName")
            MessageBundleImpl::class.java.getResourceAsStream(resourceName)?.use {
                properties.load(it)
                println("Loaded ${properties.size} properties for locale: $locale")
            } ?: println("Failed to load resource: $resourceName")
            bundles[locale.code] = properties
        }
    }

    override fun getMessage(key: String, locale: Locale): String {
        println("Getting message for key: $key, locale: $locale")
        val message = bundles[locale.code]?.getProperty(key)
            ?: bundles[Locale.ENGLISH.code]?.getProperty(key)
            ?: key
        println("Found message: $message")
        return message
    }
}