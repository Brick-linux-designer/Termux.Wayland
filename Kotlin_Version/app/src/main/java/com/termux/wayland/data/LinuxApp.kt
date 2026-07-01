package com.termux.wayland.data

data class LinuxApp(
    val id: String,
    val name: String,
    val packageName: String,
    val exec: String,
    val category: String,
    val icon: String,
    val requiresDisplay: Boolean = true,
    val description: String? = null
)
