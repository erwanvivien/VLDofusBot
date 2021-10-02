package fr.lewon.dofus.bot.game.fight.mcts

import fr.lewon.dofus.bot.game.fight.FightBoard
import fr.lewon.dofus.bot.game.fight.FighterCharacteristic
import fr.lewon.dofus.bot.util.game.CharacteristicUtil


class MCTSState(val fightBoard: FightBoard, var fighterId: Double) {

    fun clone(): MCTSState {
        return MCTSState(fightBoard.clone(), fighterId)
    }

    fun getAllPossibleMoves() {

    }

    fun randomPlay() {
        val fighter = fightBoard.getFighterById(fighterId)
        val mp = CharacteristicUtil.getCharacteristicValue(FighterCharacteristic.MP, fighter.statsById) ?: 0
        val availablePositions = this.fightBoard.getMoveCells(mp, fighter.fightCell)
        this.fightBoard.move(this.fighterId, availablePositions.random().cellId, false)
    }

}