package tools.aqua.stars.carla.experiments.gui

import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCNode
import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCOccurrence
import tools.aqua.stars.core.metric.utils.getJsonContentOfFile
import java.io.File
import java.nio.file.Paths


fun main() {

    val gui = GuiManager()
    gui.initialize()
    gui.buildGraph()

    gui.watchDirectory(Paths.get("C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"))

}

fun printTree(node: SerializableTSCNode, depth: Int = 0) {
    val indent = "\t".repeat(depth)
    println("$indent${node.label}")
    for (edge in node.outgoingEdges) {
        printTree(edge.destination, depth + 1)
        println(edge.destination.label)
    }
}