package com.commitchronicle.language

interface MessageBundle {
    fun getMessage(key: String, locale: Locale = Locale.ENGLISH): String
} 