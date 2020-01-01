@file:JvmName("Utils")
package mods.octarinecore.client.gui

import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.Style
import net.minecraft.util.text.TextFormatting
import net.minecraft.util.text.TextFormatting.AQUA
import net.minecraft.util.text.TextFormatting.GRAY

fun stripTooltipDefaultText(tooltip: MutableList<String>) {
    var defaultRows = false
    val iter = tooltip.iterator()
    while (iter.hasNext()) {
        if (iter.next().startsWith(AQUA.toString())) defaultRows = true
        if (defaultRows) iter.remove()
    }
}

fun textComponent(msg: String, color: TextFormatting = GRAY): StringTextComponent {
    val style = Style().apply { this.color = color }
    return StringTextComponent(msg).apply { this.style = style }
}