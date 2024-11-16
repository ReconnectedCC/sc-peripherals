package io.sc3.peripherals.util

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.world.World
import io.sc3.library.Tooltips.addDescLines
import io.sc3.peripherals.ScPeripherals.modId
import net.minecraft.item.tooltip.TooltipType

abstract class BaseItem(
  private val itemName: String,
  settings: Settings
) : Item(settings) {
  override fun getTranslationKey() = itemTranslationKey(itemName)

  override fun appendTooltip(
    stack: ItemStack,
    context: TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, getTranslationKey(stack))
  }

  companion object {
    fun itemTranslationKey(name: String) = "item.$modId.$name"
  }
}
