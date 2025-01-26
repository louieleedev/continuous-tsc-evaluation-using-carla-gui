package tools.aqua.stars.carla.experiments.gui.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import tools.aqua.stars.carla.experiments.gui.dataclass.WeatherCombinationData
import tools.aqua.stars.carla.experiments.gui.repository.WeatherCombinationRepository.getAllMissedWeatherCombinationMap
import tools.aqua.stars.carla.experiments.gui.repository.WeatherCombinationRepository.getMissedWeatherCombinationMap
import java.io.File

class WeatherCombinationService {

    private var missedWeatherCombinationMap = getMissedWeatherCombinationMap()
    private var missedWeatherCombinationMaps = getAllMissedWeatherCombinationMap()

    fun readJsonFile(filePath: String): WeatherCombinationData {
        val mapper = jacksonObjectMapper()
        return mapper.readValue(File(filePath))
    }

    fun convertToWeatherRoadtypeMap(data: WeatherCombinationData): MutableMap<String, MutableList<String>> {
        return data.value
            .filter { pair ->
                pair.first.contains("Weather") &&
                        pair.second.contains("Road Type") &&
                        pair.second.count { it == '\n' } >= 5
            }
            .groupBy { pair ->
                convertToKeyNotation(pair.first.trim()) // Gruppiert nach dem konvertierten 'first' Wert
            }
            .mapValues { (_, value) ->
                value.map { pair ->
                    convertToKeyNotation(pair.second.trim()) // Wandelt alle 'second' Werte um
                }.toMutableList()  // Konvertiere die Liste in eine MutableList
            }
            .toMutableMap()  // Konvertiere die Map in eine MutableMap
    }

    fun updateMissingWeatherCombinationMap(newMissingCombinationMap: Map<String, List<String>>) {
        // Behalte nur Schlüssel, die in beiden Maps vorhanden sind
        missedWeatherCombinationMap.keys.retainAll(newMissingCombinationMap.keys)

        // Für jeden Schlüssel, behalte nur die Elemente, die in beiden Listen vorhanden sind
        missedWeatherCombinationMap.forEach { (key, list) ->
            list.retainAll(newMissingCombinationMap[key] ?: emptyList())
        }

        // Entferne Wetter, die Listen leer geworden sind
        val keysToRemove = missedWeatherCombinationMap.filterValues { it.isEmpty() }.keys
        missedWeatherCombinationMap.keys.removeAll(keysToRemove)
    }

    fun analyseMissingWeatherCombination(filePath: String){
        val data = readJsonFile(filePath)
        val newCombinations = convertToWeatherRoadtypeMap(data)

        if (missedWeatherCombinationMap.isEmpty()) {
            missedWeatherCombinationMap.putAll(newCombinations)
        } else {
            updateMissingWeatherCombinationMap(newCombinations)
        }

        missedWeatherCombinationMap.forEach { (key, values) ->
            println(key + ": mit " + values.size + " Elementen")
            values.forEach { value ->
                println("  -> $value")
            }
        }
    }

    fun analyseAllMissingWeatherCombination(filePath: String, tscIdentifier: String){
        val data = readJsonFile(filePath)
        val newCombinations = convertToWeatherRoadtypeMap(data)

        if (missedWeatherCombinationMaps[tscIdentifier].isNullOrEmpty()) {
            missedWeatherCombinationMaps[tscIdentifier] = mutableMapOf<String, MutableList<String>>().apply {
                putAll(newCombinations)
            }
            println(tscIdentifier + " has not been initialized yet.")
            // println("The Value is: " + missedWeatherCombinationMaps[tscIdentifier]?.getValue(tscIdentifier))
        } else {
            updateMissingWeatherCombinationMap(newCombinations, tscIdentifier)
        }

        missedWeatherCombinationMaps[tscIdentifier]?.forEach { (key, values) ->
            println(key + ": mit " + values.size + " Elementen")
            values.forEach { value ->
                println("  -> $value")
            }
        }
    }

    fun updateMissingWeatherCombinationMap(newMissingCombinationMap: Map<String, List<String>>, tscIdentifier: String) {

        // Suche den entsprechenden MutableMap<String, MutableList<String>> aus missedWeatherCombinationMaps
        val missedWeatherCombinationMap = missedWeatherCombinationMaps[tscIdentifier] ?: mutableMapOf()

        // Behalte nur Schlüssel, die in beiden Maps vorhanden sind
        missedWeatherCombinationMap.keys.retainAll(newMissingCombinationMap.keys)

        // Für jeden Schlüssel, behalte nur die Elemente, die in beiden Listen vorhanden sind
        missedWeatherCombinationMap.forEach { (key, list) ->
            list.retainAll(newMissingCombinationMap[key] ?: emptyList())
        }

        // Entferne Wetter, die Listen leer geworden sind
        val keysToRemove = missedWeatherCombinationMap.filterValues { it.isEmpty() }.keys
        missedWeatherCombinationMap.keys.removeAll(keysToRemove)
    }

    fun convertToKeyNotation(input: String): String {
        var result = input
            .split("\n")
            .map { it.trim() }
            .filter { it.startsWith("-->") }
            .joinToString(".") { it.removePrefix("--> ").replace(" ", "").toUpperCase() }

        result = result.replace("TSCROOT", "ROOT")

        return result
    }

    fun getMissingCombinationMap(): Map<String, List<String>> {
        return missedWeatherCombinationMap
    }

    fun getMissingCombinationMap(tscIdentifier: String): MutableMap<String, MutableList<String>>? {
        return missedWeatherCombinationMaps[tscIdentifier]
    }

}