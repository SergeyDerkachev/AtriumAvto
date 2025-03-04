package ru.softwarefree.atriumavto.ui.consumption

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import ru.softwarefree.atriumavto.ui.data.Consumption
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ConsumptionViewModel : ViewModel() {

    private val _consumption = MutableLiveData<List<Consumption>>()
    val consumption: LiveData<List<Consumption>> get() = _consumption

    fun fetchConsumptionData(spreadsheetId: String, range: String, apiKey: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sheetData = getSheetDataWithApiKey(spreadsheetId, range, apiKey)
                val consumptionList = parseSheetDataToConsumption(sheetData)
                _consumption.postValue(consumptionList)
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

    private fun parseSheetDataToConsumption(sheetData: List<List<Any>>): List<Consumption> {
        return sheetData.drop(1).map { row ->
            Consumption(
                pointOfOrder = row.getOrElse(0) { "" }.toString(),
                carBrand = row.getOrElse(1) { "" }.toString(),
                numberOfCylinders = row.getOrElse(2) { "" }.toString(),
                power = row.getOrElse(3) { "" }.toString(),
                engineCapacity = row.getOrElse(4) { "" }.toString(),
                transmission = row.getOrElse(5) { "" }.toString(),
                fuelConsumptionRate = row.getOrElse(6) { "" }.toString()
            )
        }
    }
}