package ru.softwarefree.atriumavto.ui.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Vehicle(
    val organization: String,
    val number: String,
    val brand: String,
    val driver: String,
    val vin: String,
    val year: String,
    val mileage: String,
    val insurance: String
): Parcelable