package tools.aqua.stars.carla.experiments.gui

import java.nio.file.Paths


fun main() {
//    val scenarioMap = ScenarioMap()
//    val allScenarios = scenarioMap.allScenarios
//    println("Anzahl der Szenarien: ${allScenarios.size}")
//    allScenarios.forEach { (key, value) ->
//        println("Key: $key")
//        println("Scenarios: ${value.joinToString(", ")}")
//        println("-----")
//    }

     val gui = GuiManager()
     gui.initialize()
     gui.buildGraph()

     // gui.watchDirectory(Paths.get("C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"))
     gui.watchDirectory(Paths.get("/Users/keonhyeong.lee/Documents/K. Lee/TU Dortmund/Bachelor Arbeit/analysed result"))
}