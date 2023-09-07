package fr.lewon.dofus.bot.scripts.parameters.impl

import fr.lewon.dofus.bot.core.model.maps.DofusCoordinates
import fr.lewon.dofus.bot.model.characters.parameters.ParameterValues
import fr.lewon.dofus.bot.scripts.parameters.DofusBotParameter

class MultiCoordinatesParameter(
        key: String,
        description: String,
        defaultValue: List<DofusCoordinates>,
        parametersGroup: Int? = null,
        displayCondition: (parameterValues: ParameterValues) -> Boolean = { true },
) : DofusBotParameter<List<DofusCoordinates>>(key, description, defaultValue, parametersGroup, displayCondition) {

    override fun stringToValue(rawValue: String): List<DofusCoordinates>{
        return try {
            rawValue.split(";")
                    .map { it.trim().split(",") }
                    .map { DofusCoordinates(it[0].trim().toInt(), it[1].trim().toInt()) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun valueToString(value: List<DofusCoordinates>): String = value.joinToString(";") { it.toString() }
}