package fr.lewon.dofus.bot.scripts.impl

import fr.lewon.dofus.bot.core.d2o.managers.map.HintManager
import fr.lewon.dofus.bot.core.d2o.managers.map.MapManager
import fr.lewon.dofus.bot.core.d2o.managers.map.WorldMapManager
import fr.lewon.dofus.bot.core.logs.LogItem
import fr.lewon.dofus.bot.core.model.maps.DofusCoordinates
import fr.lewon.dofus.bot.core.model.maps.DofusMap
import fr.lewon.dofus.bot.core.model.maps.DofusWorldMap
import fr.lewon.dofus.bot.model.characters.parameters.ParameterValues
import fr.lewon.dofus.bot.scripts.DofusBotScriptBuilder
import fr.lewon.dofus.bot.scripts.DofusBotScriptStat
import fr.lewon.dofus.bot.scripts.parameters.DofusBotParameter
import fr.lewon.dofus.bot.scripts.parameters.impl.BooleanParameter
import fr.lewon.dofus.bot.scripts.parameters.impl.ChoiceParameter
import fr.lewon.dofus.bot.scripts.parameters.impl.MultiCoordinatesParameter
import fr.lewon.dofus.bot.scripts.parameters.impl.StringParameter
import fr.lewon.dofus.bot.scripts.tasks.impl.transport.ReachMapTask
import fr.lewon.dofus.bot.util.game.TravelUtil
import fr.lewon.dofus.bot.util.network.info.GameInfo

object FollowCircuitScriptBuilder : DofusBotScriptBuilder("Follow Circuit") {

    private val WORLD_MAPS = WorldMapManager.getAllWorldMaps()
        .filter { it.viewableEverywhere && it.visibleOnMap }
    private val DEFAULT_WORLD_MAP = WorldMapManager.getWorldMap(1)
        ?: error("Default world map not found")

    private var currentPathIndex = 0;

    private val coordinatesParameter = MultiCoordinatesParameter(
        "x1,y1;x2,y2;...",
        "Coordinates of the circuit to follow. Goes to first coordinates when starting the script",
        emptyList(),
        parametersGroup = 1,
    )

    private val useCurrentWorldMapParameter = BooleanParameter(
        "Use current world map",
        "Reach destination coordinates in current world map",
        false,
        parametersGroup = 1
    )

    private val worldMapParameter = ChoiceParameter(
        "World map",
        "Destination world map",
        DEFAULT_WORLD_MAP,
        getAvailableValues = { WORLD_MAPS.sortedBy { it.name } },
        displayCondition = { !it.getParamValue(useCurrentWorldMapParameter) },
        parametersGroup = 1,
        itemValueToString = { it.name },
        stringToItemValue = {
            WORLD_MAPS.firstOrNull { worldMap -> worldMap.name == it } ?: error("World map not found : $it")
        }
    )

    private val useZaapsParameter = BooleanParameter(
        "Use zaaps",
        "Check if you want to allow zaaps for the travel",
        true,
        parametersGroup = 2
    )

    val runForeverParameter = BooleanParameter(
        "Run forever",
        "Explores this area until you manually stop",
        false,
        parametersGroup = 3
    )

    override fun getParameters(): List<DofusBotParameter<*>> {
        return listOf(
            coordinatesParameter,
            useCurrentWorldMapParameter,
            worldMapParameter,
            useZaapsParameter,
            runForeverParameter
        )
    }

    override fun getDefaultStats(): List<DofusBotScriptStat> {
        return listOf()
    }

    override fun getDescription(): String {
        return "(beta) Follows the circuit you input using this nomenclature:\nx1,y1;x2,y2"
    }

    override fun doExecuteScript(
        logItem: LogItem,
        gameInfo: GameInfo,
        parameterValues: ParameterValues,
        statValues: HashMap<DofusBotScriptStat, String>,
    ) {
        val useZaaps = parameterValues.getParamValue(useZaapsParameter)
        val availableZaaps = if (useZaaps) TravelUtil.getAllZaapMaps() else emptyList()

        do  {
            val runForever = parameterValues.getParamValue(ExploreAreaScriptBuilder.runForeverParameter)

            val destMaps = getDestMaps(gameInfo, parameterValues)
            if (!ReachMapTask(destMaps, availableZaaps).run(logItem, gameInfo)) {
                error("Failed to reach destination")
            }
        } while (runForever)
    }

    private fun getDestMaps(gameInfo: GameInfo, parameterValues: ParameterValues): List<DofusMap> {
        val circuitCoordinates = parameterValues.getParamValue(coordinatesParameter)
        val currentMapCoordinates = gameInfo.currentMap.coordinates

        if (currentMapCoordinates == circuitCoordinates[currentPathIndex]) {
            currentPathIndex = (currentPathIndex + 1) % circuitCoordinates.size
        }

        val currentTarget = circuitCoordinates[currentPathIndex]
        val (x, y) = currentTarget

        println("""
            currentMapCoordinates: $currentMapCoordinates
            currentTarget: $currentTarget
            x,y: $x,$y
        """.trimIndent())
        
        val worldMap = getDestinationWorldMap(gameInfo.currentMap, parameterValues)
        val maps = MapManager.getDofusMaps(x, y)
        val mapsOnWorldMap = maps.filter { it.coordinates == currentTarget }
        if (mapsOnWorldMap.isEmpty()) {
            error("Can't find a map with coordinates ($x, $y) in world map [${worldMap.name}]")
        }

        return mapsOnWorldMap
    }

    private fun getDestinationWorldMap(currentMap: DofusMap, parameterValues: ParameterValues): DofusWorldMap {
        return if (parameterValues.getParamValue(useCurrentWorldMapParameter)) {
            currentMap.worldMap
                ?: WorldMapManager.getWorldMap(1)
                ?: error("No world map found")
        } else parameterValues.getParamValue(worldMapParameter)
    }

    enum class ReachMapType(val label: String) {
        BY_COORDINATES("By Coordinates"),
        BY_MAP_ID("By Map ID"),
        BY_DUNGEON("By Dungeon");

        companion object {

            fun fromLabel(label: String): ReachMapType {
                return values().firstOrNull { it.label == label }
                    ?: error("Reach map type does not exist : $label")
            }
        }
    }
}