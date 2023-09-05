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
import fr.lewon.dofus.bot.scripts.parameters.impl.IntParameter
import fr.lewon.dofus.bot.scripts.parameters.impl.StringParameter
import fr.lewon.dofus.bot.scripts.tasks.impl.harvest.HarvestAllResourcesTask
import fr.lewon.dofus.bot.scripts.tasks.impl.transport.ReachMapTask
import fr.lewon.dofus.bot.util.game.TravelUtil
import fr.lewon.dofus.bot.util.network.info.GameInfo

object FarmCurrentMapScriptBuilder : DofusBotScriptBuilder("Farm current map") {

    private val WORLD_MAPS = WorldMapManager.getAllWorldMaps()
        .filter { it.viewableEverywhere && it.visibleOnMap }
    private val DEFAULT_WORLD_MAP = WorldMapManager.getWorldMap(1)
        ?: error("Default world map not found")

    private val currentMap = BooleanParameter(
        "Current map",
        "Stay on current map",
        true,
        parametersGroup = 1
    )

    private val xParameter = IntParameter(
        "x",
        "X coordinates of destination",
        0,
        displayCondition = { !it.getParamValue(currentMap)  },
        parametersGroup = 1
    )

    private val yParameter = IntParameter(
        "y",
        "Y coordinates of destination",
        0,
        displayCondition = { !it.getParamValue(currentMap) },
        parametersGroup = 1
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


    override fun getParameters(): List<DofusBotParameter<*>> {
        return listOf(
            currentMap,
            xParameter,
            yParameter,
            useCurrentWorldMapParameter,
            worldMapParameter,
            useZaapsParameter,
        )
    }

    override fun getDefaultStats(): List<DofusBotScriptStat> {
        return listOf()
    }

    override fun getDescription(): String {
        return "(beta) Stays on map and wait for resources to pop"
    }

    override fun doExecuteScript(
        logItem: LogItem,
        gameInfo: GameInfo,
        parameterValues: ParameterValues,
        statValues: HashMap<DofusBotScriptStat, String>,
    ) {
        val useZaaps = parameterValues.getParamValue(useZaapsParameter)
        val availableZaaps = if (useZaaps) TravelUtil.getAllZaapMaps() else emptyList()

        if (!parameterValues.getParamValue(currentMap)) {
            val destMaps = getDestMaps(gameInfo, parameterValues)
            if (!ReachMapTask(destMaps, availableZaaps).run(logItem, gameInfo)) {
                error("Failed to reach destination")
            }
        }

        do {
            HarvestAllResourcesTask().run(logItem, gameInfo)
            Thread.sleep(1000)
        } while (true)
    }

    private fun getDestMaps(gameInfo: GameInfo, parameterValues: ParameterValues): List<DofusMap> {
        val x = parameterValues.getParamValue(xParameter)
        val y = parameterValues.getParamValue(yParameter)

        val worldMap = getDestinationWorldMap(gameInfo.currentMap, parameterValues)
        val maps = MapManager.getDofusMaps(x, y)
        val mapsOnWorldMap = maps.filter { it.coordinates == DofusCoordinates(x ,y) }
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