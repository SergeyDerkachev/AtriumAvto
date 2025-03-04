package ru.softwarefree.atriumavto.ui.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Consumption(
    val pointOfOrder: String,
    val carBrand: String,
    val numberOfCylinders: String,
    val power: String,
    val engineCapacity: String,
    val transmission: String,
    val fuelConsumptionRate: String
): Parcelable
