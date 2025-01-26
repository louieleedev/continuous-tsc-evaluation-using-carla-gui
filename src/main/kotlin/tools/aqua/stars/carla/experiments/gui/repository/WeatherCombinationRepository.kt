package tools.aqua.stars.carla.experiments.gui.repository

object WeatherCombinationRepository {

    private var missedWeatherCombinationMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun getMissedWeatherCombinationMap() : MutableMap<String, MutableList<String>> {
        return missedWeatherCombinationMap
    }
}