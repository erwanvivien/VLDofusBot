package fr.lewon.dofus.bot.gui2.main.scripts.scripts.tabcontent.logs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import fr.lewon.dofus.bot.gui2.custom.AnimatedButton
import fr.lewon.dofus.bot.gui2.custom.CommonText
import fr.lewon.dofus.bot.gui2.custom.CustomShapes
import fr.lewon.dofus.bot.gui2.custom.handPointerIcon
import fr.lewon.dofus.bot.gui2.util.AppColors
import fr.lewon.dofus.bot.gui2.util.UiResource
import fr.lewon.dofus.bot.util.filemanagers.impl.CharacterManager

@Composable
fun LogsContent(loggerType: LoggerUIType, characterName: String) {
    val character = CharacterManager.getCharacter(characterName)
        ?: error("Character not found : $characterName")
    val logger = loggerType.loggerGetter(character)
    val loggerUIState = LogsUIUtil.getLoggerUIState(characterName, loggerType)
    val logItems = loggerUIState.value.logs
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.height(30.dp)) {
            val autoScrollEnabled = loggerUIState.value.autoScroll
            AnimatedButton(
                { loggerUIState.value = loggerUIState.value.copy(autoScroll = !autoScrollEnabled) },
                "Auto scroll",
                UiResource.AUTO_SCROLL.imagePainter,
                Modifier.width(100.dp),
                CustomShapes.buildTrapezoidShape(bottomRightDeltaRatio = 0.15f),
                if (autoScrollEnabled) AppColors.primaryLightColor else Color.Gray,
                if (autoScrollEnabled) Color.Black else Color.White
            )
            Spacer(Modifier.weight(1f))
            if (loggerType.canBePaused) {
                val pauseEnabled = loggerUIState.value.pauseLogs
                AnimatedButton(
                    { loggerUIState.value = loggerUIState.value.copy(pauseLogs = !pauseEnabled) },
                    "Pause",
                    UiResource.PAUSE.imagePainter,
                    Modifier.width(100.dp),
                    CustomShapes.buildTrapezoidShape(bottomRightDeltaRatio = 0.15f, bottomLeftDeltaRatio = 0.15f),
                    if (pauseEnabled) AppColors.primaryLightColor else Color.Gray,
                    if (pauseEnabled) Color.Black else Color.White
                )
                Spacer(Modifier.weight(1f))
            }
            AnimatedButton(
                { logger.clearLogs() },
                "Clear",
                UiResource.ERASE.imagePainter,
                Modifier.width(100.dp),
                CustomShapes.buildTrapezoidShape(bottomLeftDeltaRatio = 0.15f),
                Color.Gray
            )
        }
        Box(Modifier.fillMaxWidth().padding(5.dp)) {
            val logsScrollState = loggerUIState.value.scrollState
            Column(Modifier.verticalScroll(logsScrollState).padding(end = 14.dp)) {
                val expandedLogItems = loggerUIState.value.expandedLogItems
                for (logItem in logItems) {
                    if (logItem.description.isEmpty()) {
                        SelectionContainer {
                            CommonText(logItem.toString())
                        }
                    } else {
                        val expanded = expandedLogItems.contains(logItem)
                        val descriptionScrollState = remember(logItem) { ScrollState(0) }
                        Column {
                            Row {
                                Button(
                                    {
                                        val newExpandedLogItems = expandedLogItems.toMutableList()
                                        if (expanded) {
                                            newExpandedLogItems.remove(logItem)
                                        } else {
                                            newExpandedLogItems.add(logItem)
                                        }
                                        loggerUIState.value = loggerUIState.value.copy(
                                            expandedLogItems = newExpandedLogItems
                                        )
                                    },
                                    modifier = Modifier.size(12.dp).align(Alignment.CenterVertically),
                                    contentPadding = PaddingValues(0.dp),
                                    colors = ButtonDefaults.buttonColors(backgroundColor = AppColors.backgroundColor)
                                ) {
                                    Text(
                                        if (expanded) "-" else "+",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        modifier = Modifier.align(Alignment.CenterVertically).fillMaxSize()
                                            .handPointerIcon(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                SelectionContainer {
                                    CommonText(logItem.toString())
                                }
                            }
                            if (expanded) {
                                val height = remember { mutableStateOf(0f) }
                                Box(
                                    Modifier.padding(start = 12.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .fillMaxWidth()
                                        .height(minOf(height.value + 10, 250f).dp)
                                        .background(AppColors.backgroundColor)
                                        .padding(5.dp)
                                ) {
                                    Column(Modifier.verticalScroll(descriptionScrollState).padding(end = 10.dp)) {
                                        SelectionContainer {
                                            CommonText(
                                                logItem.description,
                                                Modifier.onGloballyPositioned { coordinates ->
                                                    height.value = coordinates.size.toSize().height
                                                })
                                        }
                                    }
                                    VerticalScrollbar(
                                        modifier = Modifier.fillMaxHeight().width(8.dp).align(Alignment.CenterEnd),
                                        adapter = rememberScrollbarAdapter(descriptionScrollState),
                                    )
                                }
                            }
                        }
                    }
                }
            }
            VerticalScrollbar(
                modifier = Modifier.fillMaxHeight().width(8.dp).align(Alignment.CenterEnd),
                adapter = rememberScrollbarAdapter(logsScrollState),
            )
        }
    }
}