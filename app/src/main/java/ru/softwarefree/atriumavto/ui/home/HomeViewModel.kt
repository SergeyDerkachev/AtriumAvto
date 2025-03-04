package ru.softwarefree.atriumavto.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.softwarefree.atriumavto.ui.data.Vehicle
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class HomeViewModel : ViewModel() {

    private val _vehicles = MutableLiveData<List<Vehicle>>()
    val vehicles: LiveData<List<Vehicle>> get() = _vehicles

    fun fetchVehicleData(spreadsheetId: String, range: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sheetData = getSheetDataWithApiKey(spreadsheetId, range, apiKey)
                val vehicleList = parseSheetDataToVehicles(sheetData)
                _vehicles.postValue(vehicleList)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun getSheetDataWithApiKey(spreadsheetId: String, range: String, apiKey: String): List<List<Any>> {
        val urlString = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/$range?key=$apiKey"
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        return try {
            val inputStream = connection.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))
            val response = reader.use { it.readText() }

            val json = JSONObject(response)
            val values = json.getJSONArray("values")

            List(values.length()) { i ->
                List(values.getJSONArray(i).length()) { j ->
                    values.getJSONArray(i).get(j)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } finally {
            connection.disconnect()
        }
    }

    private fun parseSheetDataToVehicles(sheetData: List<List<Any>>): List<Vehicle> {
        return sheetData.drop(1).map { row ->
            Vehicle(
                organization = row.getOrElse(0) { "" }.toString(),
                number = row.getOrElse(1) { "" }.toString(),
                brand = row.getOrElse(2) { "" }.toString(),
                driver = row.getOrElse(3) { "" }.toString(),
                vin = row.getOrElse(4) { "" }.toString(),
                year = row.getOrElse(5) { "" }.toString(),
                mileage = row.getOrElse(6) { "" }.toString(),
                insurance = row.getOrElse(7) { "" }.toString()
            )
        }
    }
}