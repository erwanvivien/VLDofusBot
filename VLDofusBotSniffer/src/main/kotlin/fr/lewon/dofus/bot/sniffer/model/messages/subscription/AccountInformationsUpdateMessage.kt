package fr.lewon.dofus.bot.sniffer.model.messages.subscription

import fr.lewon.dofus.bot.core.io.stream.ByteArrayReader
import fr.lewon.dofus.bot.sniffer.model.messages.NetworkMessage
import fr.lewon.dofus.bot.sniffer.model.types.NetworkType
import fr.lewon.dofus.bot.sniffer.model.ProtocolTypeManager
import fr.lewon.dofus.bot.core.io.stream.BooleanByteWrapper

open class AccountInformationsUpdateMessage : NetworkMessage() {
	var subscriptionEndDate: Double = 0.0
	override fun deserialize(stream: ByteArrayReader) {
		super.deserialize(stream)
		subscriptionEndDate = stream.readDouble().toDouble()
	}
	override fun getNetworkMessageId(): Int = 7360
}