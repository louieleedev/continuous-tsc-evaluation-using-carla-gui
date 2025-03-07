package tools.aqua.stars.carla.experiments.gui

import java.nio.file.Paths


fun main() {
    val scenarioMap = ScenarioMap()
    val allScenarios = scenarioMap.allScenarios
    println("Anzahl der Szenarien: ${allScenarios.size}")
    allScenarios.forEach { (key, value) ->
        println("Key: $key")
        println("Scenarios: ${value.joinToString(", ")}")
        println("-----")
    }
//     val counts = IntArray(8)
//
//     allScenarios.forEach { (_, value) ->
//          val size = value.size
//          if (size in 0..7) {  // Sicherstellen, dass nur zulässige Indizes verarbeitet werden
//               counts[size]++
//          }
//     }
//
//     println("Anzahl der Szenarien mit 0 Elementen: ${counts[0]}")
//     println("Anzahl der Szenarien mit 1 Element: ${counts[1]}")
//     println("Anzahl der Szenarien mit 2 Elementen: ${counts[2]}")
//     println("Anzahl der Szenarien mit 3 Elementen: ${counts[3]}")
//     println("Anzahl der Szenarien mit 4 Elementen: ${counts[4]}")
//     println("Anzahl der Szenarien mit 5 Elementen: ${counts[5]}")
//     println("Anzahl der Szenarien mit 6 Elementen: ${counts[6]}")
//     println("Anzahl der Szenarien mit 7 Elementen: ${counts[7]}")

//     Anzahl der Szenarien mit 0 Elementen: 1
//     Anzahl der Szenarien mit 1 Element: 11
//     Anzahl der Szenarien mit 2 Elementen: 31
//     Anzahl der Szenarien mit 3 Elementen: 39
//     Anzahl der Szenarien mit 4 Elementen: 26
//     Anzahl der Szenarien mit 5 Elementen: 10
//     Anzahl der Szenarien mit 6 Elementen: 2
//     Anzahl der Szenarien mit 7 Elementen: 0

//     val gui = GuiManager()
//     gui.initialize()
//     gui.buildGraph()
//
//     // gui.watchDirectory(Paths.get("C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"))
//     gui.watchDirectory(Paths.get("/Users/keonhyeong.lee/Documents/K. Lee/TU Dortmund/Bachelor Arbeit/analysed result"))
}