package tools.aqua.stars.carla.experiments.gui

import com.mxgraph.view.mxGraph
import com.mxgraph.model.mxCell
import com.mxgraph.swing.mxGraphComponent
import com.mxgraph.util.mxConstants
import java.lang.Thread.sleep

class GraphManager(private val graph: mxGraph) {

    private var graphComponent = mxGraphComponent(graph)
    private val parent = graph.defaultParent
    private val rootNode = graph.insertVertex(parent, "ROOT", "Root", 5.0, 450.0, 80.0, 30.0, "fillColor=white;strokeColor=black;")
    private val frequencyMap: MutableMap<String, Int> = mutableMapOf()
    private val colorPriority = mapOf(
        "#69a4a7" to 1,
        "#5da7ab" to 2,
        "#49acb1" to 3,
        "#4dacae" to 4,
        "#52acaa" to 5,
        "#57aca7" to 6,
        "#5bada4" to 7,
        "#61ada0" to 8,
        "#6fad97" to 9,
        "#84ae87" to 10,
        "#a5af71" to 11,
        "#d5b052" to 12,
        "#e5af48" to 13,
        "#e69f4c" to 14,
        "#e79050" to 15,
        "#e78453" to 16,
        "#e87b56" to 17,
        "white" to 0)

    // Custom Vertex und Edge Style: https://gist.github.com/davidjgraph/7749607

    fun insertVertex(id: String?, label: String, x: Double, y: Double, width: Double, height: Double): Any {
        graph.model.beginUpdate()
        try {
            // val style = "fillColor=$color;shape=ellipse;strokeColor=black;"
            val style = "fillColor=white;strokeColor=black;"
            return graph.insertVertex(parent, id, label, x, y, width, height, style) as mxCell
        } finally {
            graph.model.endUpdate()
        }
    }

    fun insertEdge(id: String?, value: String, source: Any, target: Any) {
        graph.model.beginUpdate()
        try {
            graph.insertEdge(parent, id, value, source, target, "strokeColor=black;endSize=0") as mxCell
        } finally {
            graph.model.endUpdate()
        }
    }

    fun updatePathToRootIfLeaf(vertexId: String, frequency: Int) {
        val vertex = findVertexById(vertexId)
        if (vertex != null && isLeaf(vertex)) {
            val color = getColorByFrequency(frequency)
            colorPathToRoot(vertex, color)
            updateVertexLabel(vertexId)
        }
//        else if (vertex != null) {
//            val childCount = graph.model.getChildCount(vertex)
//            var maxVorkommen = 0
//            for (i in 0 until childCount) {
//                val count = frequencyMap.getOrDefault(vertex.getChildAt(i).id,0)
//                if(count > maxVorkommen) {
//                    maxVorkommen = count
//                }
//            }
//            val color = getColorByFrequency(maxVorkommen)
//            colorPathToRoot(vertex, color)
//        }
    }

    fun clearGraph() {
        graph.model.beginUpdate()
        try {
            // Entferne alle Zellen
            val cells = graph.getChildCells(parent, true, true)

            // Wurzel wird nicht entfernt
            val cellsToRemove = cells.filter { it != rootNode }
            graph.removeCells(cellsToRemove.toTypedArray())
            // frequencyMap.clear()
            // graph.removeCells(cells)
        } finally {
            graph.model.endUpdate()
            graph.refresh()
            graphComponent.refresh()
        }
    }

    private fun findVertexById(id: String): mxCell? {
        val parent = graph.defaultParent
        val childCount = graph.model.getChildCount(parent)
        for (i in 0 until childCount) {
            val child = graph.model.getChildAt(parent, i) as mxCell
            if (child.isVertex && child.id == id) {
                return child
            }
        }
        return null
    }

    fun incrementFrequency(id: String) {
        val currentCount = frequencyMap.getOrDefault(id, 0)
        frequencyMap[id] = currentCount + 1
    }

    fun getFrequencyMap(): MutableMap<String, Int> {
        return frequencyMap
    }

    private fun getFrequencyByVertexId(id: String): Int {
        return frequencyMap.getOrDefault(id, 0)
    }

    private fun isLeaf(vertex: mxCell): Boolean {
        return graph.model.getEdgeCount(vertex) == 0 ||
                (0 until graph.model.getEdgeCount(vertex)).all { i ->
                    val edge = graph.model.getEdgeAt(vertex, i) as mxCell
                    // Prüft, dass keine ausgehenden Kanten vorhanden sind
                    graph.model.getTerminal(edge, true) != vertex
                }
    }

    private fun colorPathToRoot(vertex: mxCell, color: String) {
        println("Color Update Start")
        var i = 1
        var currentVertex: mxCell? = vertex
        while (currentVertex != null) {
            println("Current vertex $i : " + currentVertex.id)

            println(currentVertex.id + " ist " + getFrequencyByVertexId(currentVertex.id) + " mal vorgekommen.")
            println(vertex.id + " ist " + getFrequencyByVertexId(vertex.id) + " mal vorgekommen." )

            // Falls Elternknoten bereits höhere Farbe hat, höre mit der Färbung auf.
            if(!shouldRecolor(extractColor(graph.getCellStyle(currentVertex)), color)) { break }

//            if(getFrequencyByVertexId(currentVertex.id) > getFrequencyByVertexId(vertex.id) && currentVertex.id != vertex.id) {
//                break;
//            }

            val fillColor = color
            val textColor = if (color in listOf("green", "blue", "red")) "white" else "black"

            graph.setCellStyles(mxConstants.STYLE_FILLCOLOR, fillColor, arrayOf(currentVertex))
            graph.setCellStyles(mxConstants.STYLE_FONTCOLOR, textColor, arrayOf(currentVertex))
            println("Vertex: " + currentVertex.id + " wird in " + fillColor + " gefärbt.")
            // sleep(200)

            val incomingEdges = getIncomingEdges(currentVertex)
            if (incomingEdges.isNotEmpty()) {
                val parentEdge = incomingEdges.first()
                val parentVertex = graph.model.getTerminal(parentEdge, true) as mxCell?
                println("Incomming Edge: " + parentVertex!!.value)
                graph.setCellStyles(mxConstants.STYLE_STROKECOLOR, color, arrayOf(parentEdge))
                println("Edge: (" + currentVertex.id + ", " + parentVertex!!.id + ") wird in " + fillColor + " gefärbt.")
                // sleep(200)

                currentVertex = parentVertex
            } else {
                currentVertex = null
            }
            i += 1;
        }
        println("")
    }

    private fun getIncomingEdges(vertex: mxCell): List<mxCell> {
        return (0 until graph.model.getEdgeCount(vertex)).mapNotNull { i ->
            val edge = graph.model.getEdgeAt(vertex, i) as mxCell
            if (graph.model.getTerminal(edge, false) == vertex) edge else null
        }
    }

    // Color palette: https://www.footfallcam.com/de/people-counting/knowledge-base/chapter-16-heat-map-configuration/

    private fun getColorByFrequency(frequency: Int): String {
        return when {
            frequency == 1  -> "#69a4a7"
            frequency == 2  -> "#5da7ab"
            frequency == 3  -> "#49acb1"
            frequency == 4  -> "#4dacae"
            frequency == 5  -> "#52acaa"
            frequency == 6  -> "#57aca7"
            frequency == 7  -> "#5bada4"
            frequency == 8  -> "#61ada0"
            frequency == 9  -> "#6fad97"
            frequency == 10 -> "#84ae87"
            frequency == 11 -> "#a5af71"
            frequency == 12 -> "#d5b052"
            frequency == 13 -> "#e5af48"
            frequency == 14 -> "#e69f4c"
            frequency == 15 -> "#e79050"
            frequency == 16 -> "#e78453"
            frequency >= 17 -> "#e87b56"
            else -> "white"
        }
    }

    private fun shouldRecolor(currentColor: String?, newColor: String): Boolean {
        val currentPriority = colorPriority[currentColor] ?: 0
        val newPriority = colorPriority[newColor] ?: 0
        return newPriority > currentPriority
    }

    private fun extractColor(styleMap: Map<String, Any>): String {
        return styleMap["fillColor"] as? String ?: "none"
    }

    private fun updateVertexLabel(vertexId: String) {
        val frequency = frequencyMap.getOrDefault(vertexId, 0)

        val vertex = findVertexById(vertexId)
        if (vertex != null) {
            val lable = getVertexLableById(vertexId)
            val newLabel = "$lable ($frequency)"
            graph.model.beginUpdate()

            try {
                graph.model.setValue(vertex, newLabel)
            } finally {
                graph.model.endUpdate()
            }
            graph.refresh()
        }
    }

    private fun getVertexLableById(vertexId: String): String {
        return vertexId.split(".").last().toLowerCase().capitalize()
    }

    fun graphRefresh() {
        graph.model.endUpdate()
        graphComponent.refresh()
    }

    fun getGraph(): mxGraph {
        return graph
    }

    fun getRoot():Any {
        return rootNode
    }

    fun getParent(): Any {
        return parent
    }
}
