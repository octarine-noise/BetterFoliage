@file:JvmName("Utils")
package mods.octarinecore.client.gui

import net.minecraft.util.EnumChatFormatting

fun stripTooltipDefaultText(tooltip: MutableList<String>) {
    var defaultRows = false
    val iter = tooltip.iterator()
    while (iter.hasNext()) {
        if (iter.next().startsWith(EnumChatFormatting.AQUA.toString())) defaultRows = true
        if (defaultRows) iter.remove()
    }
}