package tools.aqua.stars.carla.experiments.gui

class ScenarioMap {

    var allScenarios: MutableMap<String, List<String>> = mutableMapOf()
//    val allScenariosLayer1_2: Map<String, List<String>>
//    val allScenariosLayer4: Map<String, List<String>>
//    val allScenariosLayer1_2_4: Map<String, List<String>>
//    val allScenariosLayer4_5: Map<String, List<String>>
//    val allScenariosLayerPedestrian: Map<String, List<String>>

    init {
        // Szenarien für jeden Straßentyp generieren und zusammenführen
        allScenarios = buildAllScenariosFullTSC()
//        allScenariosLayer1_2 = buildAllScenariosLayer1_2()
//        allScenariosLayer4 = buildAllScenariosLayer4()
//        allScenariosLayer1_2_4 = buildAllScenariosLayer1_2_4()
//        allScenariosLayer4_5 = buildAllScenariosLayer4_5()
//        allScenariosLayerPedestrian = buildAllScenariosPedestrian()
    }

    private fun buildAllScenariosFullTSC(): MutableMap<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        // JUNCTION
        val junctionDynamic = generateCombinations(listOf("ROOT.ROADTYPE.JUNCTION.DYNAMICRELATION.PEDESTRIANCROSSED", "ROOT.ROADTYPE.JUNCTION.DYNAMICRELATION.MUSTYIELD", "ROOT.ROADTYPE.JUNCTION.DYNAMICRELATION.FOLLOWINGLEADINGVEHICLE"), 0..3)
        val junctionManeuver = listOf(listOf("ROOT.ROADTYPE.JUNCTION.MANEUVER.LANEFOLLOW"), listOf("ROOT.ROADTYPE.JUNCTION.MANEUVER.RIGHTTURN"), listOf("ROOT.ROADTYPE.JUNCTION.MANEUVER.LEFTTURN"))
        addToScenarios(scenarios, junctionDynamic, junctionManeuver, listOf(emptyList()), "JUNCTION")

        // MULTI-LANE
        val multiLaneDynamic = generateCombinations(listOf("ROOT.ROADTYPE.MULTI-LANE.DYNAMICRELATION.ONCOMINGTRAFFIC", "ROOT.ROADTYPE.MULTI-LANE.DYNAMICRELATION.OVERTAKING", "ROOT.ROADTYPE.MULTI-LANE.DYNAMICRELATION.PEDESTRIANCROSSED", "ROOT.ROADTYPE.MULTI-LANE.DYNAMICRELATION.FOLLOWINGLEADINGVEHICLE"), 0..4)
        val multiLaneManeuver = listOf(listOf("ROOT.ROADTYPE.MULTI-LANE.MANEUVER.LANECHANGE"), listOf("ROOT.ROADTYPE.MULTI-LANE.MANEUVER.LANEFOLLOW"))
        val multiLaneStopType = listOf(emptyList(), listOf("ROOT.ROADTYPE.MULTI-LANE.STOPTYPE.HASREDLIGHT"))
        addToScenarios(scenarios, multiLaneDynamic, multiLaneManeuver, multiLaneStopType, "MULTI-LANE")

        // SINGLE-LANE
        val singleLaneDynamic = generateCombinations(listOf("ROOT.ROADTYPE.SINGLE-LANE.DYNAMICRELATION.ONCOMINGTRAFFIC", "ROOT.ROADTYPE.SINGLE-LANE.DYNAMICRELATION.PEDESTRIANCROSSED", "ROOT.ROADTYPE.SINGLE-LANE.DYNAMICRELATION.FOLLOWINGLEADINGVEHICLE"), 0..3)
        val singleLaneStopType = listOf(emptyList(), listOf("ROOT.ROADTYPE.SINGLE-LANE.STOPTYPE.HASSTOPSIGN"), listOf("ROOT.ROADTYPE.SINGLE-LANE.STOPTYPE.HASYIELDSIGN"), listOf("ROOT.ROADTYPE.SINGLE-LANE.STOPTYPE.HASREDLIGHT"))
        addToScenarios(scenarios, singleLaneDynamic, listOf(emptyList()), singleLaneStopType, "SINGLE-LANE")

        return scenarios
    }

    private fun buildAllScenariosLayer1_2(): Map<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        return scenarios
    }

    private fun buildAllScenariosLayer4(): Map<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        return scenarios
    }

    private fun buildAllScenariosLayer1_2_4(): Map<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        return scenarios
    }

    private fun buildAllScenariosLayer4_5(): Map<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        return scenarios
    }

    private fun buildAllScenariosPedestrian(): Map<String, List<String>> {
        val scenarios = mutableMapOf<String, List<String>>()

        return scenarios
    }

    private fun addToScenarios(scenarios: MutableMap<String, List<String>>, dynamic: List<List<String>>, maneuver: List<List<String>>, stopType: List<List<String>>, typePrefix: String) {
        dynamic.forEach { dyn ->
            maneuver.forEach { man ->
                stopType.forEach { stop ->
                    val scenarioList = dyn + man + stop
                    val key = "$typePrefix: ${scenarioList.joinToString(separator = ", ")}"
                    scenarios[key] = scenarioList
                }
            }
        }
    }

    private fun generateCombinations(options: List<String>, range: IntRange): List<List<String>> {
        return range.flatMap { count -> combinations(options, count) }
    }

    private fun combinations(options: List<String>, count: Int): List<List<String>> {
        if (count == 0) return listOf(emptyList())
        if (options.isEmpty()) return emptyList()
        val first = options.first()
        val rest = options.drop(1)
        val withFirst = combinations(rest, count - 1).map { it + first }
        val withoutFirst = combinations(rest, count)
        return withFirst + withoutFirst
    }
}
