package tools.aqua.stars.carla.experiments.gui.service

object TranslationService {

    fun translateSuggestionLog(suggestion: String): String {
        val parts = suggestion.split('.')
        if (parts.size < 5) return "Ungültiges Format"  // Überprüfen, ob die Suggestion genug Teile hat

        val roadTypeTranslation = parts[2]
        val categoryTranslation = parts[3]
        val actionTranslation = translateActionLog(parts[4])

        return "$roadTypeTranslation -> $categoryTranslation -> $actionTranslation"
    }

    fun translateActionLog(action: String): String {
        return when (action) {
            "FOLLOWINGLEADINGVEHICLE" -> "FOLLOWING LEADING VEHICLE"
            "PEDESTRIANCROSSED" -> "PEDESTRIAN CROSSED"
            "MUSTYIELD" -> "MUST YIELD"
            "ONCOMINGTRAFFIC" -> "ONCOMING TRAFFIC"
            "LANEFOLLOW" -> "LANE FOLLOW"
            "LANECHANGE" -> "LANE CHANGE"
            "RIGHTTURN" -> "RIGHT TURN"
            "LEFTTURN" -> "LEFT TURN"
            "HASREDLIGHT" -> "RED LIGHT"
            "HASSTOPSIGN" -> "STOP SIGN"
            "HASYIELDSIGN" -> "YIELD SIGN"
            else -> action
        }
    }

    fun translateSuggestionMessage(suggestion: String): String {
        val parts = suggestion.split('.')
        val roadType = when (parts[2]) {
            "SINGLE-LANE" -> "einzelspurigen Straße"
            "JUNCTION" -> "Kreuzung"
            "MULTI-LANE" -> "mehrspurigen Straße"
            else -> "Straße"
        }
        val category = when (parts[3]) {
            "DYNAMICRELATION" -> "dynamische Beziehung"
            "MANEUVER" -> "Manöver"
            "STOPTYPE" -> "Stopp-Typ"
            else -> ""
        }
        val actionTranslation = translateAction(parts.last())

        return "Auf der $roadType wurde das $category '$actionTranslation' noch nicht gesehen."
    }

    fun translateAction(action: String): String {
        return when (action) {
            "FOLLOWINGLEADINGVEHICLE" -> "Folgen eines vorausfahrenden Fahrzeugs"
            "PEDESTRIANCROSSED" -> "Überqueren von Fußgängern"
            "MUSTYIELD" -> "Vorfahrt gewähren"
            "ONCOMINGTRAFFIC" -> "Entgegenkommender Verkehr"
            "OVERTAKING" -> "Überholen"
            "LANEFOLLOW" -> "Spurfolgen"
            "LANECHANGE" -> "Spurwechsel"
            "RIGHTTURN" -> "Rechtsabbiegen"
            "LEFTTURN" -> "Linksabbiegen"
            "HASREDLIGHT" -> "Ampel mit rotem Licht"
            "HASSTOPSIGN" -> "Stoppschild"
            "HASYIELDSIGN" -> "Vorfahrtsschild"
            else -> action.lowercase().replace('_', ' ').capitalize()
        }
    }

    fun translateWeatherToKey(weather: String): String {
        return when (weather) {
            "CLEAR" -> "ROOT.WEATHER.CLEAR"
            "CLOUDY" -> "ROOT.WEATHER.CLOUDY"
            "WET" -> "ROOT.WEATHER.WET"
            "WETCLOUDY" -> "ROOT.WEATHER.WETCLOUDY"
            "SOFTRAIN" -> "ROOT.WEATHER.SOFTRAIN"
            "MIDRAIN" -> "ROOT.WEATHER.MIDRAIN"
            "HARDRAIN" -> "ROOT.WEATHER.HARDRAIN"
            else -> weather
        }
    }

    fun translateWeatherkeyToDisplay(key: String): String {
        return when (key) {
            "ROOT.WEATHER.CLEAR" -> "CLEAR"
            "ROOT.WEATHER.CLOUDY" -> "CLOUDY"
            "ROOT.WEATHER.WET" -> "WET"
            "ROOT.WEATHER.WETCLOUDY" -> "WETCLOUDY"
            "ROOT.WEATHER.SOFTRAIN" -> "SOFTRAIN"
            "ROOT.WEATHER.MIDRAIN" -> "MIDRAIN"
            "ROOT.WEATHER.HARDRAIN" -> "HARDRAIN"
            else -> key
        }
    }

}