package tools.aqua.stars.carla.experiments.gui

import java.nio.file.Paths


fun main() {

    val gui = GuiManager()
    gui.initialize()
    gui.buildGraph()

    gui.watchDirectory(Paths.get("C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"))

}