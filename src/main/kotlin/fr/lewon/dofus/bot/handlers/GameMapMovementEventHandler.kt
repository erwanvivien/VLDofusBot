package fr.lewon.dofus.bot.handlers

import fr.lewon.dofus.bot.core.logs.VldbLogger
import fr.lewon.dofus.bot.game.GameInfo
import fr.lewon.dofus.bot.sniffer.model.messages.move.GameMapMovementMessage
import fr.lewon.dofus.bot.sniffer.store.EventHandler

object GameMapMovementEventHandler : EventHandler<GameMapMovementMessage> {

    override fun onEventReceived(socketResult: GameMapMovementMessage) {
        val fromCellId = socketResult.keyMovements.first()
        val toCellId = socketResult.keyMovements.last()
        GameInfo.fightBoard.move(socketResult.actorId, toCellId)
        VldbLogger.debug("Fighter [${socketResult.actorId}] moved from cell [$fromCellId] to cell [$toCellId]")
    }

}