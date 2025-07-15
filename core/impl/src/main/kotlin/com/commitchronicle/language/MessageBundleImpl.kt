package com.commitchronicle.language

import java.util.Properties

class MessageBundleImpl : MessageBundle {
    companion object {
        private val bundles = mutableMapOf<String, Properties>()

        init {
            loadBundle(Locale.ENGLISH)
            loadBundle(Locale.KOREAN)
            loadBundle(Locale.CHINESE)
            loadBundle(Locale.JAPANESE)
        }

        private fun loadBundle(locale: Locale) {
            val properties = Properties()
            val resourceName = "/messages_${locale.code}.properties"
            MessageBundleImpl::class.java.getResourceAsStream(resourceName)?.use { inputStream ->
                inputStream.reader(Charsets.UTF_8).use { reader ->
                    properties.load(reader)
                }
            }
            bundles[locale.code] = properties
        }
    }

    override fun getMessage(key: String, locale: Locale): String {
        return bundles[locale.code]?.getProperty(key)
            ?: bundles[Locale.ENGLISH.code]?.getProperty(key)
            ?: key
    }
}