package io.sc3.peripherals.util

import net.minecraft.block.Block
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import io.sc3.library.Tooltips.addDescLines
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType

abstract class BaseBlock(settings: Settings) : Block(settings) {
  override fun appendTooltip(
    stack: ItemStack,
    context: Item.TooltipContext,
    tooltip: MutableList<Text>,
    options: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, options)
    addDescLines(tooltip, translationKey)
  }
}
