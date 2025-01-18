package tools.aqua.stars.carla.experiments.gui

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tools.aqua.stars.core.tsc.TSC
import tools.aqua.stars.core.tsc.edge.TSCEdge
import tools.aqua.stars.core.tsc.instance.TSCInstanceNode
import tools.aqua.stars.core.tsc.node.TSCNode
import tools.aqua.stars.data.av.dataclasses.*
import java.io.File

class JsonHelper {

    fun tscToJson(tsc: TSC<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds>): String {
        val gson = Gson()
        // val map = tscToMap(tsc)
        return gson.toJson(tsc)
    }

    fun saveTSCToFile(tsc: TSC<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds>, filename: String) {
        val jsonString = tscToJson(tsc)
        File(filename).writeText(jsonString)
    }

//    fun loadTSCFromFile(filename: String): TSC<Actor, TickData, Segment, TickDataUnitSeconds, TickDataDifferenceSeconds> {
//        val jsonString = File(filename).readText()
//        return jsonToTSC(jsonString)
//    }

}