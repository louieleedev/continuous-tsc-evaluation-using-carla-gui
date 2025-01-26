package tools.aqua.stars.carla.experiments.gui

import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.view.mxGraph
import tools.aqua.stars.carla.experiments.gui.service.WeatherCombinationService
import tools.aqua.stars.carla.experiments.gui.service.TranslationService.translateSuggestionLog
import tools.aqua.stars.carla.experiments.gui.service.TranslationService.translateSuggestionMessage
import tools.aqua.stars.carla.experiments.gui.service.TranslationService.translateWeatherToKey
import tools.aqua.stars.carla.experiments.gui.service.TranslationService.translateWeatherkeyToDisplay
import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCNode
import tools.aqua.stars.core.metric.serialization.tsc.SerializableTSCOccurrence
import tools.aqua.stars.core.metric.utils.getJsonContentOfFile
import java.awt.*
import java.io.File
import java.lang.Thread.sleep
import java.nio.file.FileSystems
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds.ENTRY_CREATE
import java.nio.file.WatchEvent
import javax.swing.*
import kotlin.math.floor


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

    private var layer: String = "full TSC"

    companion object {
        const val FRAME_WIDTH = 1800.0
        const val FRAME_HEIGHT = 1000.0
    }

    private val allLeafs: MutableMap<String, Int> = mutableMapOf()
    private var updatesCount = 0
    private var countCriteria = 2

    private val suggestionsMap = mutableMapOf<String, String>() // Speichert Vorschläge
    private val suggestionsListModel = DefaultListModel<String>() // Model für JList
    private val suggestionsList = JList(suggestionsListModel) // JList, die die Vorschläge anzeigt

    private val weatherCombinationService = WeatherCombinationService()
    private val combinationListModel = DefaultListModel<String>()
    private val combinationList = JList<String>(combinationListModel)
    private var selectedWeather: String = "Wähle ein Wetter aus..."
    private val weatherDropdown = JComboBox<String>(
        arrayOf(
            "Wähle ein Wetter aus",
            "CLEAR",
            "CLOUDY",
            "WET",
            "WETCLOUDY",
            "SOFTRAIN",
            "MIDRAIN",
            "HARDRAIN"
        )
    )


    fun initialize() {

        frame.layout = BorderLayout()

        // Graph
        graphComponent = mxGraphComponent(graph)
        frame.add(graphComponent, BorderLayout.CENTER)
        frame.contentPane.add(graphComponent)

        // TSC-Layer Drop Box
        comboBox.addActionListener { e ->
            val selectedLayer = comboBox.selectedItem as String
            updateGraph(selectedLayer)
        }
        frame.add(comboBox, BorderLayout.NORTH)

        // Vorschläge
        val rightPanel = JPanel()
        rightPanel.layout = GridLayout(2, 1)
        setupSuggestionsPanel(rightPanel)
        setupWeatherCombinationPanel(rightPanel)
        frame.add(rightPanel, BorderLayout.EAST)

        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.setSize(FRAME_WIDTH.toInt(), FRAME_HEIGHT.toInt())
        frame.isVisible = true
    }


    private fun setupSuggestionsPanel(parent: JPanel) {
        val suggestionsPanel = JPanel(BorderLayout())
        suggestionsPanel.border = BorderFactory.createTitledBorder("Vorschläge")

        val fixedWidth = 300
        val fixedPanelDimension = Dimension(fixedWidth, frame.height)

        suggestionsPanel.preferredSize = fixedPanelDimension
        suggestionsPanel.minimumSize = fixedPanelDimension
        suggestionsPanel.maximumSize = fixedPanelDimension

        suggestionsList.visibleRowCount = 10 // Anzahl der sichtbaren Zeilen in der Liste

        val scrollPane = JScrollPane(suggestionsList)
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED // Horizontale Scrollbar nach Bedarf
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED // Vertikale Scrollbar nach Bedarf
        suggestionsPanel.add(scrollPane, BorderLayout.CENTER)

        parent.add(suggestionsPanel)
    }

    private fun setupWeatherCombinationPanel(parent: JPanel) {
        val weatherCombinationPanel = JPanel(BorderLayout())

        val fixedWidth = 300
        val fixedPanelDimension = Dimension(fixedWidth, frame.height)

        weatherCombinationPanel.preferredSize = fixedPanelDimension
        weatherCombinationPanel.minimumSize = fixedPanelDimension
        weatherCombinationPanel.maximumSize = fixedPanelDimension

        val combinationScrollPane = JScrollPane(combinationList)

        combinationScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        combinationScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED

        weatherDropdown.addActionListener { e ->
            val selectedItem = weatherDropdown.selectedItem
            if (selectedItem is String) {
                selectedWeather = extractWeatherType(selectedItem)
                println("selectedWeather: " + selectedWeather)
                updateWeatherCombinationList(selectedWeather, combinationListModel)
            }
        }

        weatherCombinationPanel.add(weatherDropdown, BorderLayout.NORTH)
        weatherCombinationPanel.add(combinationScrollPane, BorderLayout.CENTER)
        weatherCombinationPanel.border = BorderFactory.createTitledBorder("Fehlende Wetterkombinationen")
        parent.add(weatherCombinationPanel)

        // Initialisiere die Dropdown-Box
        initializeWeatherDropdown(weatherDropdown)
    }

    private fun initializeWeatherDropdown(weatherDropdown: JComboBox<String>) {

        val currentSelection = weatherDropdown.selectedItem as? String ?: "Wähle ein Wetter aus"

        weatherDropdown.removeAllItems()

        // Standardhinweis
        weatherDropdown.addItem("Wähle ein Wetter aus")

        // Füge alle Wettersorten mit der Anzahl der Kombinationen hinzu
        weatherCombinationService.getMissingCombinationMap().forEach { (key, values) ->
            val weatherKey = translateWeatherkeyToDisplay(key)
            weatherDropdown.addItem("$weatherKey (${values.size})")
        }

        // Stelle die vorherige Auswahl wieder her
        if (weatherDropdown.getItemCount() > 0) {
            val index = (0 until weatherDropdown.getItemCount()).find {
                weatherDropdown.getItemAt(it) == currentSelection || weatherDropdown.getItemAt(it).startsWith(extractWeatherType(currentSelection))
            } ?: 0
            weatherDropdown.selectedIndex = index
        }
    }

    fun updateWeatherDropdown() {
        SwingUtilities.invokeLater {
            initializeWeatherDropdown(weatherDropdown)
        }
    }

    private fun updateWeatherCombinationList(weatherType: String, model: DefaultListModel<String>) {
        println("weatherType" + weatherType)
        SwingUtilities.invokeLater {
            model.clear()
            val combinations = weatherCombinationService.getMissingCombinationMap()[translateWeatherToKey(weatherType)]
            if (combinations != null) {
                combinations.forEach { model.addElement(translateSuggestionLog(it)) }
            }
            combinationList.revalidate()
            combinationList.repaint()
        }
    }

    fun buildGraph() {
        graphManager.clearGraph()
        allLeafs.clear()
        val tsc = tscList.find { it.identifier == getLayer() } ?: tscList.first()
        val y = FRAME_HEIGHT / tsc.rootNode.edges.size

        for((i1, edge1) in tsc.rootNode.edges.withIndex()) {
            val id1 = convertToID("ROOT", edge1.destination.label)
            // println("\tID1: ${id1}")
            if(edge1.destination.label != "Road Type") {
                val nodeT1 = graphManager.insertVertex(id1, edge1.destination.label, 250.0, y*i1+(y/2), 80.0, 30.0)
                graphManager.insertEdge(null, "", root, nodeT1)

                for((i2, edge2) in edge1.destination.edges.withIndex()) {
                    val pos2 = calculatePosition(y*i1, y, 5.0, edge1.destination.edges.size, i2)
                    val id2 = convertToID(id1, edge2.destination.label)
                    // println("\tID2: ${id2}")
                    val nodeT2 = graphManager.insertVertex(id2, edge2.destination.label, 450.0,  pos2, 100.0, 30.0)
                    graphManager.insertEdge(null, "", nodeT1, nodeT2)

                    // Blätter aktualisieren
                    updateLeafs(id2)
                }
            } else {
                // ROAD TYPE
                val nodeT1 = graphManager.insertVertex(id1, edge1.destination.label, 550.0, 450.0, 80.0, 30.0)
                graphManager.insertEdge(null, "", root, nodeT1)

                for((i2, edge2) in edge1.destination.edges.withIndex()) {
                    val pos2 = calculatePosition(450.0-325.0, 650.0, 300.0, 3, i2)
                    val id2 = convertToID(id1, edge2.destination.label)
                    // println("\tID2: ${id2}")
                    val nodeT2 = graphManager.insertVertex(id2, edge2.destination.label, 800.0, pos2, 80.0, 30.0)
                    graphManager.insertEdge(null, "", nodeT1, nodeT2)

                    for((i3, edge3) in edge2.destination.edges.withIndex()) {
                        val pos3 = calculatePosition( pos2-170, 340.0, 90.0, edge2.destination.edges.size, i3)
                        val id3 = convertToID(id2, edge3.destination.label)
                        // println("\tID3: ${id3}")
                        val nodeT3 = graphManager.insertVertex(id3, edge3.destination.label, 1000.0, pos3, 90.0, 30.0)
                        graphManager.insertEdge(null, "", nodeT2, nodeT3)

                        for((i4, edge4) in edge3.destination.edges.withIndex()) {
                            val pos4 = calculatePosition( pos3-75, 150.0, 5.0, edge3.destination.edges.size, i4)
                            val id4 = convertToID(id3, edge4.destination.label)
                            // println("\tID4: ${id4}")
                            val nodeT4 = graphManager.insertVertex(id4, edge4.destination.label, 1300.0, pos4, 140.0, 30.0)
                            graphManager.insertEdge(null, "", nodeT3, nodeT4)

                            // Blätter aktualisieren
                            updateLeafs(id4)
                        }
                    }
                }
            }
        }
        updateColor()
    }

    private fun updateLeafs(id: String) {
        val currentCount = allLeafs.getOrDefault(id, 0)
        allLeafs[id] = currentCount + 1
    }

    private fun readResultFromJson(pathWithNewData: String) {
        val jsonFile = File(pathWithNewData)
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
    }

    private fun checkAndUpdateSuggestions() {

        val unseenNodes = mutableListOf<String>()
        val seenLeafs = graphManager.getFrequencyMap().keys.filter { id ->
            val vertex = graphManager.findVertexById(id)
            graphManager.isLeaf(vertex!!)
        }.toSet()
        println("Folgende Knoten wurden gesehen: $seenLeafs")

        allLeafs.keys.forEach { leafId ->
            if (!seenLeafs.contains(leafId)) { // Prüfe, ob das Blatt in seenLeafs enthalten ist
                unseenNodes.add(leafId) // Füge das nicht gesehene Blatt zu unseenNodes hinzu
            }
        }
        println("Folgende Knoten wurden noch nicht gesehen: $unseenNodes")

        // Filtere die Knoten, die nicht zu den Kategorien WEATHER, TRAFFICDENSITY oder TIMEOFDAY gehören
        val filteredUnseenNodes = unseenNodes.filterNot {
            it.contains("WEATHER") || it.contains("TRAFFICDENSITY") || it.contains("TIMEOFDAY")
        }

        if (updatesCount > countCriteria) {

            if (unseenNodes.isNotEmpty()) {
                // Gui Vorschlag
                // Zufälligen Knoten auswählen
                val randomLeaf = filteredUnseenNodes.random()
                addSuggestion(randomLeaf)

                SwingUtilities.invokeLater {
                    try {
                        val dialog = JDialog(frame, "Vorschlag", true)
                        dialog.layout = BorderLayout()

                        // Erstellen des JLabels mit Zentrierung
                        val label = JLabel(translateSuggestionMessage(randomLeaf))
                        label.horizontalAlignment = JLabel.CENTER
                        label.verticalAlignment = JLabel.CENTER
                        val labelFont = label.font
                        label.font = Font(labelFont.name, labelFont.style, 20)

                        // Verwendung von GridBagLayout für vollständige Zentrierung
                        val panel = JPanel(GridBagLayout())
                        val gbc = GridBagConstraints()
                        gbc.gridx = 0
                        gbc.gridy = 0
                        gbc.weightx = 1.0
                        gbc.weighty = 1.0
                        gbc.fill = GridBagConstraints.BOTH
                        panel.add(label, gbc)

                        // Hinzufügen des Panels zum Dialog
                        dialog.add(panel, BorderLayout.CENTER)
                        dialog.setSize(1000, 300)
                        dialog.setLocationRelativeTo(frame)

                        // Timer zum automatischen Schließen des Dialogs
                        val timer = Timer(5000) { e -> dialog.dispose() }
                        timer.isRepeats = false
                        timer.start()

                        dialog.isVisible = true
                    } catch (e: Exception) {
                        e.printStackTrace() // Druckt die Stack Trace im Fehlerfall
                    }
                }
            }
            countCriteria = countCriteria + 1
        }

        if (suggestionsMap.isNotEmpty()) {
            println("Aktuelle Vorschläge: ")
            print(suggestionsMap)
            println("")

            val iterator = suggestionsMap.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (seenLeafs.contains(entry.key)) {
                    println("Da der Knoten ${entry.key} gesehen wurde, wird er von der Vorschlagliste entfernt.")
                    iterator.remove() // Sicher entfernen mit dem Iterator
                    suggestionsListModel.removeElement(entry.value) // Auch aus der GUI-Liste entfernen
                }
            }
        }

        val i = countCriteria - updatesCount
        println("In $i. ten Untersuchung wird ein Vorschlag wieder generiert.")
    }

    private fun addSuggestion(suggestion: String) {
        // Füge den Vorschlag zum Map und zur JList hinzu
        if (!suggestionsMap.containsKey(suggestion)) {
            suggestionsMap[suggestion] = translateSuggestionLog(suggestion)        // Speichert den Vorschlag
            suggestionsListModel.addElement(translateSuggestionLog(suggestion))    // Fügt den Vorschlag zur GUI hinzu
        }
    }

    private fun extractWeatherType(displayString: String): String {
        return displayString.split(" ")[0]
    }

    fun removeSuggestion(suggestion: String) {
        // Entferne den Vorschlag vom Map und von der JList
        if (suggestionsMap.containsKey(suggestion)) {
            suggestionsMap.remove(suggestion)
            suggestionsListModel.removeElement(suggestion)
        }
    }

    private fun buildPath(tscIdentifier: String, foldernameInResult: String, newData: String): String {

        val resultPath = "C:\\Lee\\TU-Dortmund\\Bachelorarbeit\\Code\\stars-carla-experiments\\serialized-results\\results"
        val resultsDir = File(resultPath)
        sleep(1000)

        if (resultsDir.exists() && resultsDir.isDirectory) {

            var finalResult = Paths.get(resultPath, newData, foldernameInResult, "$tscIdentifier.json").toString()
            val jsonFile = File(finalResult)

            if (jsonFile.exists()) {
                println("Datei gefunden: ${jsonFile.path}")
                return finalResult
            } else {
                println("Datei nicht gefunden: ${jsonFile.path}")
                return "ERROR"
            }

        } else {
            println("Das angegebene Verzeichnis existiert nicht oder ist kein Verzeichnis: $resultPath")
        }
        return "ERROR"
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

                    println("Neue Datei erstellt: ${filename.toString()}")
                    val pathWithNewData = buildPath("full TSC", "valid-tsc-instances-per-tsc", filename.toString())

                    // Überprüfen, ob der Pfad gültig ist, bevor weitere Schritte unternommen werden
                    if (pathWithNewData != "ERROR") {
                        readResultFromJson(pathWithNewData)
                        updateColor()
                        updatesCount++
                        checkAndUpdateSuggestions()

                        // MissedPredicateCombinations für Kombination Wetter und Straßentypen
                        val pathForMissedPredicateCombinations = buildPath("full TSC", "missed-predicate-combinations", filename.toString())
                        weatherCombinationService.analyseMissingWeatherCombination(pathForMissedPredicateCombinations)
                        updateWeatherDropdown()
                        updateWeatherCombinationList(selectedWeather, combinationListModel)
                    } else {
                        println("Ungültiger Pfad: $pathWithNewData, Überspringe Verarbeitung für diese Datei.")
                    }
                }
            }

            val valid = key.reset()
            if (!valid) {
                println("Key ist nicht mehr gültig, Überwachung beendet.")
                break
            }
        }
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

}