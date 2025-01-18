package tools.aqua.stars.carla.experiments.gui

import com.mxgraph.model.mxCell
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCNode
import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCOccurrence
import tools.aqua.stars.core.metric.utils.getJsonContentOfFile
import java.awt.BorderLayout
import javax.swing.JComboBox
import javax.swing.JFrame

import tools.aqua.stars.data.av.dataclasses.*
import java.io.File
import java.lang.Thread.sleep
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.WatchEvent
import kotlin.math.floor
import kotlin.system.exitProcess

class GuiManager {

    private val graph = mxGraph()
    private val graphManager = GraphManager(graph)
    private val root = graphManager.getRoot()
    private var graphComponent: mxGraphComponent? = null
    private val frame = JFrame("Baumdarstellung mit JGraphX")
    private val comboBox = JComboBox<String>(
        arrayOf(
            "full TSC",
            "layer 1+2",
            "layer 4",
            "layer 1+2+4",
            "layer (4)+5",
            "pedestrian",
            "multi-lane-dynamic-relations"
        )
    )

    private val defaultTSC = tsc()
    private val tscList = defaultTSC.buildProjections()

    private var updatesCount = 0

    private var layer: String = "full TSC"

    companion object {
        const val FRAME_WIDTH = 1800.0
        const val FRAME_HEIGHT = 1000.0
    }

    fun initialize() {

        graphComponent = mxGraphComponent(graph)
        frame.layout = BorderLayout()
        frame.add(graphComponent, BorderLayout.CENTER)
        frame.contentPane.add(graphComponent)

        comboBox.addActionListener { e ->
            val selectedLayer = comboBox.selectedItem as String
            updateGraph(selectedLayer)
        }

        frame.add(comboBox, BorderLayout.NORTH)
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(FRAME_WIDTH.toInt(), FRAME_HEIGHT.toInt())
        frame.isVisible = true
    }

    fun buildGraph() {
        graphManager.clearGraph()
        val tsc = tscList.find { it.identifier == getLayer() } ?: tscList.first()
        val y = FRAME_HEIGHT / tsc.rootNode.edges.size

        for((i1, edge1) in tsc.rootNode.edges.withIndex()) {
            val id1 = convertToID("ROOT", edge1.destination.label)
            println("\tID1: ${id1}")
            if(edge1.destination.label != "Road Type") {
                val nodeT1 = graphManager.insertVertex(id1, edge1.destination.label, 250.0, y*i1+(y/2), 80.0, 30.0)
                graphManager.insertEdge(null, "", root, nodeT1)

                for((i2, edge2) in edge1.destination.edges.withIndex()) {
                    val pos2 = calculatePosition(y*i1, y, 5.0, edge1.destination.edges.size, i2)
                    val id2 = convertToID(id1, edge2.destination.label)
                    println("\tID2: ${id2}")
                    val nodeT2 = graphManager.insertVertex(id2, edge2.destination.label, 450.0,  pos2, 100.0, 30.0)
                    graphManager.insertEdge(null, "", nodeT1, nodeT2)
                }
            } else {
                // ROAD TYPE
                val nodeT1 = graphManager.insertVertex(id1, edge1.destination.label, 550.0, 450.0, 80.0, 30.0)
                graphManager.insertEdge(null, "", root, nodeT1)

                for((i2, edge2) in edge1.destination.edges.withIndex()) {
                    val pos2 = calculatePosition(450.0-325.0, 650.0, 300.0, 3, i2)
                    val id2 = convertToID(id1, edge2.destination.label)
                    println("\tID2: ${id2}")
                    val nodeT2 = graphManager.insertVertex(id2, edge2.destination.label, 800.0, pos2, 80.0, 30.0)
                    graphManager.insertEdge(null, "", nodeT1, nodeT2)

                    for((i3, edge3) in edge2.destination.edges.withIndex()) {
                        val pos3 = calculatePosition( pos2-170, 340.0, 90.0, edge2.destination.edges.size, i3)
                        val id3 = convertToID(id2, edge3.destination.label)
                        println("\tID3: ${id3}")
                        val nodeT3 = graphManager.insertVertex(id3, edge3.destination.label, 1000.0, pos3, 90.0, 30.0)
                        graphManager.insertEdge(null, "", nodeT2, nodeT3)

                        for((i4, edge4) in edge3.destination.edges.withIndex()) {
                            val pos4 = calculatePosition( pos3-75, 150.0, 5.0, edge3.destination.edges.size, i4)
                            val id4 = convertToID(id3, edge4.destination.label)
                            println("\tID4: ${id4}")
                            val nodeT4 = graphManager.insertVertex(id4, edge4.destination.label, 1300.0, pos4, 140.0, 30.0)
                            graphManager.insertEdge(null, "", nodeT3, nodeT4)
                        }
                    }
                }
            }
        }
        updateColor()
    }

    private fun readResultFromJson() {
        val jsonFile = File(buildPath2("full TSC"))
        val result = getJsonContentOfFile(jsonFile)

        // Update Frequency if a vertex is detected
        if (result.value is ArrayList<*>) {
            for ((i, instanz) in (result.value as ArrayList<*>).withIndex()) {
                if (instanz is SerializableTSCOccurrence) {
                    val rootNode: SerializableTSCNode = instanz.tscInstance

                    for ((i1, edge1) in rootNode.outgoingEdges.withIndex()) {
                        val id1 = convertToID("ROOT", edge1.destination.label)
                        graphManager.incrementFrequency(id1)

                        for ((i2, edge2) in edge1.destination.outgoingEdges.withIndex()) {
                            val id2 = convertToID(id1, edge2.destination.label)
                            graphManager.incrementFrequency(id2)

                            for ((i3, edge3) in edge2.destination.outgoingEdges.withIndex()) {
                                val id3 = convertToID(id2, edge3.destination.label)
                                graphManager.incrementFrequency(id3)

                                for ((i4, edge4) in edge3.destination.outgoingEdges.withIndex()) {
                                    val id4 = convertToID(id3, edge4.destination.label)
                                    graphManager.incrementFrequency(id4)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateColor() {
        val frequencyMap = graphManager.getFrequencyMap()
        frequencyMap.forEach{ (id, frequency) ->
            graphManager.updatePathToRootIfLeaf(id, frequency)
        }
        updatesCount++
        checkAndUpdateSuggestions()
    }

    private fun checkAndUpdateSuggestions() {
        if (updatesCount > 20) {
            val unseenNodes = mutableListOf<String>()
            val allNodes = graphManager.getGraph().getChildCells(graphManager.getParent(), true, true)
            for (node in allNodes) {
                if (node is mxCell && node.isVertex) {
                    val frequency = graphManager.getFrequencyMap().getOrDefault(node.id, 0)
                    if (frequency == 0) {
                        unseenNodes.add(node.value.toString()) // Füge nicht gesehene Knoten hinzu
                    }
                }
            }

            if (unseenNodes.isNotEmpty()) {
                // Hier können Sie die GUI aktualisieren, um die nicht gesehenen Knoten vorzuschlagen
                println("Folgende Knoten wurden noch nicht gesehen: $unseenNodes")
                // Sie könnten beispielsweise einen Dialog oder eine Benachrichtigung in Ihrem GUI anzeigen
            }
        }
    }

    private fun buildPath(tscIdentifier: String): String {

        val resultPath = "C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"
        val resultsDir = File(resultPath)
        sleep(1000)

        if (resultsDir.exists() && resultsDir.isDirectory) {

            val directories = resultsDir.listFiles { file -> file.isDirectory }
            val sortedDirectories = directories?.sortedByDescending { it.lastModified() }
            val lastDataFolder = sortedDirectories?.firstOrNull()?.name

            if (lastDataFolder != null) {
                var finalResult = Paths.get(resultPath, lastDataFolder, "valid-tsc-instances-per-tsc", "$tscIdentifier.json").toString()
                val jsonFile = File(finalResult)

//                if (jsonFile.exists()) {
//                    println("Datei gefunden: ${jsonFile.path}")
//                } else {
//                    println("Datei nicht gefunden: ${jsonFile.path}")
//                    println("Warte auf die JSON-Datei")
//                    sleep(5000)
//                }
                while(!jsonFile.exists()) {
                    println("Datei nicht gefunden: ${jsonFile.path}")
                    println("Versuche nochmal...")
                    sleep(1000)
                    finalResult = buildPath(tscIdentifier)
                }
                println("Datei gefunden: ${jsonFile.path}")
                return finalResult

            } else {
                println("Kein Verzeichnis gefunden in: $resultPath")
                return ""
            }
        } else {
            println("Das angegebene Verzeichnis existiert nicht oder ist kein Verzeichnis: $resultPath")
        }
        return ""
    }

    private fun buildPath2(
        tscIdentifier: String,
        basePath: String = "C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"): String
    {
        val resultsDir = File(basePath)

        if (!resultsDir.exists() || !resultsDir.isDirectory) {
            println("Das angegebene Verzeichnis existiert nicht oder ist kein Verzeichnis: $basePath")
            exitProcess(1)
        }

        val directories = resultsDir.listFiles { file -> file.isDirectory }?.sortedByDescending { it.lastModified() }

        val dir = directories!!.first()
        val jsonPath = Paths.get(dir.path, "valid-tsc-instances-per-tsc", "$tscIdentifier.json").toString()
        val jsonFile = File(jsonPath)

        if (jsonFile.exists()) {
            println("Datei gefunden: ${jsonFile.path}")
            return jsonPath
        } else {
            println("Datei nicht gefunden in: ${dir.path}")
        }
        println("Versuche erneut nach einer kurzen Pause...")
        sleep(500)
        return buildPath2(tscIdentifier, basePath) // Rekursive Suche wiederholen
    }

    fun watchDirectory(path: Path) {
        val watchService = FileSystems.getDefault().newWatchService()
        path.register(watchService, ENTRY_CREATE)

        println("Überwache Verzeichnis für neue Dateien: $path")

        while (true) {
            val key = watchService.take()  // Blockiert, bis ein Ereignis auftritt

            for (event in key.pollEvents()) {
                val kind = event.kind()

                if (kind == ENTRY_CREATE) {
                    @Suppress("UNCHECKED_CAST")
                    val ev = event as WatchEvent<Path>
                    val filename = ev.context()

                    println("Neue Datei erstellt: $filename")
                    readResultFromJson()
                    updateColor()
                    // buildTeilgraph(getLayer())
                }
            }

            val valid = key.reset()
            if (!valid) {
                break
            }
        }
        println("Überwachung beendet")
    }

    /**
     * calculatePosition berechnet die absolute Position in dem GUI Fenster.
     * Info: 30px = Höhe des Kinderknoten. (15px = die Hälfte)
     *
     * @param pos:          Anfangsposition des ersten Kinderknoten
     * @param y:            gesamte Höhe des Teilfensters, wo alle Knoten abgebildet werden
     * @param d:            Abstand zwischen Kinderknoten
     * @param anzahlKnoten: Anzahl der abzubildenen Kinderknoten
     */
    private fun calculatePosition(pos: Double, y: Double, d: Double, anzahlKnoten: Int, iteration: Int): Double {
        val absolutePosition = if(anzahlKnoten % 2 == 0) {
            (pos + 15 + (y/2)) - (((anzahlKnoten/2)-1)*d + d/2 + (anzahlKnoten/2)*30)
        } else {
            // println("Floor n/2 = " + floor(anzahlKnoten/2.0))
            (pos + 15 + (y/2)) - (15 + (floor(anzahlKnoten/2.0) *(30+d)))
        }
        return iteration * (30 + d) + absolutePosition
    }

    private fun updateGraph(layer: String) {
        this.layer = layer
        println("Selected layer: $layer")
        buildGraph()
        // buildTeilgraph(layer)
    }

    fun convertToID(oldId: String, newId: String): String {
        return if(oldId == "") {
            newId.replace(" ", "").uppercase()
        } else {
            oldId + "." + newId.replace(" ", "").uppercase()
        }
    }

    private fun getLayer(): String {
        return layer
    }

     fun tscProjection() {

        println("Projections:")
         defaultTSC.buildProjections().forEach {
            println("TSC for Projection $it:")
            println(defaultTSC)
            println("All possible instances:")
            println(it.possibleTSCInstances.size)
            println()
        }
        println("-----------------")
    }

}