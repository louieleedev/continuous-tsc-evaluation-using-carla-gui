package tools.aqua.stars.carla.experiments.gui.helper

import tools.aqua.stars.carla.experiments.gui.service.TranslationService.translateSuggestionToKey
import java.awt.Color
import javax.swing.*
import java.awt.Component

class SuggestionListCellRenderer(
    private val allLeafs: Map<String, Int>  // Alle Blätter und deren Häufigkeit
) : ListCellRenderer<String> {
    private val defaultRenderer = DefaultListCellRenderer()

    override fun getListCellRendererComponent(
        list: JList<out String>,
        value: String,  // 'value' ist das tatsächliche Element in der Liste, also der Vorschlagstext
        index: Int,
        isSelected: Boolean,
        cellHasFocus: Boolean
    ): Component {
        val renderer = defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus) as JLabel

        // Überprüfen, ob der Vorschlag im aktuellen Baum gültig ist
        if (!allLeafs.containsKey(translateSuggestionToKey(value))) {
            renderer.foreground = Color.GRAY // Ungültig: Grau
        } else {
            renderer.foreground = Color.BLACK // Gültig: Schwarz
        }

        return renderer
    }
}
