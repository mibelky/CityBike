package com.belki.citybike.domain

data class Station(
    val id: String,
    val emptySlots: Int,
    val freeBikes: Int,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    var distance: Int
)
