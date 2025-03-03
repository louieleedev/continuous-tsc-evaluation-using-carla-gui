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

    fun translateSuggestionLog2(suggestions: List<String>): String {
        if (suggestions.isEmpty()) return "Keine Vorschläge vorhanden."

        val groupedByRoadType = mutableMapOf<String, MutableList<String>>()

        suggestions.forEach { suggestion ->
            val parts = suggestion.split('.')
            if (parts.size < 5) return "Ungültiges Format für Vorschlag: $suggestion"

            val roadType = parts[2]
            val action = parts[4] // Annahme: Das ist der relevante Teil für die Aktion

            // Füge die übersetzte Aktion dem entsprechenden Straßentyp hinzu
            groupedByRoadType.getOrPut(roadType) { mutableListOf() }.add(translateActionLog(action))
        }

        // Baue den finalen String auf
        return groupedByRoadType.entries.joinToString(separator = "\n") { entry ->
            "${entry.key}:\n  -> ${entry.value.joinToString(separator = "\n  -> ")}"
        }
    }

    fun translateSuggestionLog3(suggestions: List<String>): String {
        if (suggestions.isEmpty()) return "<html>Keine Vorschläge vorhanden.</html>"

        val groupedByRoadType = mutableMapOf<String, MutableList<String>>()

        suggestions.forEach { suggestion ->
            val parts = suggestion.split('.')
            if (parts.size < 5) return "<html>Ungültiges Format für Vorschlag: $suggestion</html>"

            val roadType = parts[2]
            val action = parts[4]  // Annahme: Das ist der relevante Teil für die Aktion

            // Füge die übersetzte Aktion dem entsprechenden Straßentyp hinzu
            groupedByRoadType.getOrPut(roadType) { mutableListOf() }.add(translateActionLog(action))
        }

        // Baue den finalen HTML String auf
        return groupedByRoadType.entries.joinToString(separator = "<br>") { entry ->
            "<html>${entry.key}:<br>&nbsp;&nbsp;-> ${entry.value.joinToString(separator = "<br>&nbsp;&nbsp;-> ")}<br>&nbsp;</html>"
        }
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

    fun translateSuggestionMessage2(suggestion : Map.Entry<String, List<String>>): String {
        if (suggestion.value.isEmpty()) return "<html>Keine Vorschläge vorhanden.</html>"

        val stringBuilder = StringBuilder("<html>Auf der ")

        // Der Straßentyp wird basierend auf dem ersten Eintrag bestimmt
        val firstPath = suggestion.value.first().split('.')
        val roadType = when (firstPath[2]) {
            "SINGLE-LANE" -> "einzelspurigen Straße"
            "JUNCTION" -> "Kreuzung"
            "MULTI-LANE" -> "mehrspurigen Straße"
            else -> "Straße"
        }
        stringBuilder.append("$roadType wurde folgende Kombinationen <br>")

        // Verarbeiten jeder Aktion in der Liste
        suggestion.value.forEach { path ->
            val parts = path.split('.')
            if (parts.size < 5) return "<html>Ungültiges Format für Vorschlag: $path</html>"

            val category = when (parts[3]) {
                "DYNAMICRELATION" -> "dynamische Beziehung"
                "MANEUVER" -> "Manöver"
                "STOPTYPE" -> "Stopp-Typ"
                else -> ""
            }
            val actionTranslation = translateAction(parts.last())

            stringBuilder.append("das $category '$actionTranslation' mit<br>")
        }

        // Entferne das letzte "<br>" und füge den abschließenden Text hinzu
        if (stringBuilder.endsWith("mit<br>")) {
            stringBuilder.setLength(stringBuilder.length - 7)  // Entfernt das letzte "mit<br>"
        }
        stringBuilder.append("<br>noch nicht gesehen.</html>")

        return stringBuilder.toString()
    }

    fun translateAction(action: String): String {
        // Beispiel für eine Übersetzungsfunktion
        return when (action) {
            "LEFTTURN" -> "Links abbiegen"
            "FOLLOWVEHICLE" -> "Fahrzeug folgen"
            else -> action
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

    fun translateSuggestionToKey(suggestion: String): String {

        val modifiedValue = when {
            "RED LIGHT" in suggestion -> suggestion.replace("RED LIGHT", "HASREDLIGHT")
            "STOP SIGN" in suggestion -> suggestion.replace("STOP SIGN", "HASSTOPSIGN")
            "YIELD SIGN" in suggestion -> suggestion.replace("YIELD SIGN", "HASYIELDSIGN")
            else -> suggestion
        }

        val cleanedValue = modifiedValue
            .replace(" -> ", ".")
            .replace(" ", "")

        return "ROOT.ROADTYPE.$cleanedValue"
    }

}