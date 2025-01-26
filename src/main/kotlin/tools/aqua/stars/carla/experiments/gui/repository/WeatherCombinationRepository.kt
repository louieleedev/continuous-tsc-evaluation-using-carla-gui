package tools.aqua.stars.carla.experiments.gui.repository

object WeatherCombinationRepository {

    private var missedWeatherCombinationMap: MutableMap<String, MutableList<String>> = mutableMapOf()
    private var missedWeatherCombinationMaps: MutableMap<String, MutableMap<String, MutableList<String>>> = mutableMapOf()


    fun getMissedWeatherCombinationMap() : MutableMap<String, MutableList<String>> {
        return missedWeatherCombinationMap
    }

    fun getAllMissedWeatherCombinationMap() : MutableMap<String, MutableMap<String, MutableList<String>>> {
        return missedWeatherCombinationMaps
    }
}