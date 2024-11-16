package io.sc3.peripherals.util

import net.minecraft.block.BlockRenderType
import net.minecraft.block.BlockState
import net.minecraft.block.BlockWithEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import io.sc3.library.Tooltips.addDescLines
import net.minecraft.item.Item
import net.minecraft.item.tooltip.TooltipType

abstract class BaseBlockWithEntity(settings: Settings) : BlockWithEntity(settings) {
  override fun appendTooltip(
    stack: ItemStack,
    context: Item.TooltipContext,
    tooltip: MutableList<Text>,
    type: TooltipType
  ) {
    super.appendTooltip(stack, context, tooltip, type)
    addDescLines(tooltip, translationKey)
  }

  override fun getRenderType(state: BlockState?) = BlockRenderType.MODEL

  override fun onStateReplaced(state: BlockState, world: World, pos: BlockPos, newState: BlockState, moved: Boolean) {
    if (state.isOf(newState.block)) return

    val be = world.getBlockEntity(pos)
    if (be is BaseBlockEntity) be.onBroken()

    super.onStateReplaced(state, world, pos, newState, moved)
  }
}
