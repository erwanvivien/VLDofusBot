package fr.lewon.dofus.bot.gui.main.exploration.map.helper

import fr.lewon.dofus.bot.core.model.maps.DofusMap
import fr.lewon.dofus.bot.core.model.maps.DofusSubArea
import fr.lewon.dofus.bot.gui.util.UiResource

object MainWorldMapHelper : WorldMapHelper("Main world map", UiResource.UPPER_LAYER) {

    override fun isMapValid(map: DofusMap): Boolean = map.isInOverlay()

    override fun isDisplayedOnMap(subArea: DofusSubArea): Boolean =
        subArea.capturable || subArea.displayOnWorldMap || subArea.basicAccountAllowed

    override fun getPriorityMap(maps: List<DofusMap>): DofusMap? =
        maps.lastOrNull { it.hasPriorityOnWorldMap } ?: maps.firstOrNull()

}