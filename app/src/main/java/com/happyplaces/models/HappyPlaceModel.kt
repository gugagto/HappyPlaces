package com.happyplaces.models

import java.io.Serializable

/**
 * A Data Model Class for Happy Place details. We will you this data class in all over the project even when
 * dealing with local SQLite database.
 */
data class HappyPlaceModel(
    val id: Int,
    val title: String,
    val image: String,
    val description: String,
    val date: String,
    val location: String,
    val latitude: Double,
    val longitude: Double
) : Serializable               //atributo q permite que os dados da class possa passar de um intent p outra

// pode ser serializable ou Parcelable (mais rapido)