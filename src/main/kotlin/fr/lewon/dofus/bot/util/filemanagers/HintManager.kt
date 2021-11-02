package fr.lewon.dofus.bot.util.filemanagers

import fr.lewon.dofus.bot.core.manager.VldbManager
import fr.lewon.dofus.bot.core.model.maps.DofusMap
import fr.lewon.dofus.bot.core.model.move.Direction
import fr.lewon.dofus.bot.model.hint.Hint
import fr.lewon.dofus.bot.model.hint.MoveHints
import fr.lewon.dofus.bot.model.maps.DofusMapWithDirection
import fr.lewon.dofus.bot.util.RequestProcessor
import fr.lewon.dofus.bot.util.math.LevenshteinDistanceUtil

object HintManager : VldbManager {

    private lateinit var hints: HashMap<DofusMapWithDirection, ArrayList<Hint>>
    private lateinit var hintsIdsByName: Map<String, Int>

    override fun initManager() {
        hints = HashMap()
        hintsIdsByName = RequestProcessor.getAllHintIdsByName()
    }

    fun getHint(map: DofusMap, direction: Direction, hintLabel: String): Hint? {
        var hintId = hintsIdsByName[hintLabel]
        if (hintId == null) {
            val closestLabel = LevenshteinDistanceUtil.getClosestString(hintLabel, hintsIdsByName.keys.toList())
            hintId = hintsIdsByName[closestLabel]
        }
        hintId ?: error("Couldn't find id for hint [$hintLabel]")
        return getHint(map, direction, hintId)
    }


    fun getHint(map: DofusMap, direction: Direction, hintId: Int): Hint? {
        val mapWithDirection = DofusMapWithDirection(map, direction)
        var hints = getHints(mapWithDirection)
        if (hints == null) {
            synchronize(map)
            hints = getHints(mapWithDirection)
        }
        return hints?.firstOrNull { hintId == it.n }
    }

    private fun getHints(mapWithDirection: DofusMapWithDirection): List<Hint>? {
        return hints[mapWithDirection]
    }

    private fun synchronize(map: DofusMap) {
        for (dir in Direction.values()) {
            val hintsByDirection =
                RequestProcessor.getHints(map.posX, map.posY, dir, map.worldMap)
            synchronize(map, dir, hintsByDirection)
        }
    }

    private fun synchronize(map: DofusMap, direction: Direction, newMoveHints: MoveHints) {
        val mapWithDirection = DofusMapWithDirection(map, direction)
        val hintsForMapWithDirection = hints.computeIfAbsent(mapWithDirection) { ArrayList() }
        val hintsIds = hintsForMapWithDirection.map { it.n }
        for (hint in newMoveHints.hints) {
            if (!hintsIds.contains(hint.n)) {
                hintsForMapWithDirection.add(hint)
            }
        }
    }
}