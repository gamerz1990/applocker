package com.maliks.applocker.xtreme.data

object SystemPackages {
    fun getSystemPackages(): List<String> {
        return arrayListOf<String>().apply {
            add("com.android.packageinstaller")
        }
    }
}