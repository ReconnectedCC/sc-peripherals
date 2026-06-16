package io.sc3.peripherals.prints

import io.sc3.library.ext.optCompound
import io.sc3.peripherals.Registration.ModBlocks
import io.sc3.peripherals.Registration.ModItems
import io.sc3.peripherals.ScPeripherals.ModId
import io.sc3.peripherals.ScPeripherals.modId
import net.minecraft.component.DataComponentTypes
import net.minecraft.component.type.NbtComponent
import net.minecraft.item.BlockItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.tooltip.TooltipType
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.text.Text.literal
import net.minecraft.text.Text.translatable
import net.minecraft.util.Formatting.GRAY

class PrintItem(settings: Settings) : BlockItem(ModBlocks.print, settings) {
  override fun getName(stack: ItemStack): Text {
    return printData(stack).labelText ?: return super.getName(stack)
  }

  override fun appendTooltip(stack: ItemStack, context: Item.TooltipContext, tooltip: MutableList<Text>, type: TooltipType) {
    // Don't call super here
    val data = printData(stack)
    data.tooltip?.let { tooltip.add(literal(it)) }

    fun line(key: String, vararg args: Any) {
      tooltip.add(translatable("block.$modId.print.$key", *args).formatted(GRAY))
    }

    if (data.isBeaconBlock) line("beacon_base")
    if (data.isQuiet) line("quiet")
    if (data.redstoneLevel > 0) line("redstone_level", data.redstoneLevel)
    if (data.lightLevel > 0) line("light_level", data.lightLevel)
  }

  companion object {
    val id = ModId("item/print")

    fun printData(stack: ItemStack): PrintData =
      stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt()?.optCompound("data")?.let { PrintData.fromNbt(it) } ?: PrintData()

    fun fromBlockEntity(be: PrintBlockEntity): ItemStack = ItemStack(ModItems.print).apply {
      val nbt = NbtCompound()
      nbt.put("data", be.data.toNbt())
      set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))
    }

    fun create(data: PrintData): ItemStack = ItemStack(ModItems.print).apply {
      val nbt = NbtCompound()
      nbt.put("data", data.toNbt())
      set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(nbt))
    }

    @JvmStatic
    fun hasCustomName(stack: ItemStack) =
      printData(stack).labelText != null

    @JvmStatic
    fun getCustomName(stack: ItemStack): Text =
      stack.item.getName(stack)

    @JvmStatic
    fun setCustomName(stack: ItemStack, name: Text?) {
      val outerNbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: NbtCompound()
      val data = outerNbt.optCompound("data") ?: NbtCompound().also { outerNbt.put("data", it) }
      val newLabel = PrintData.sanitiseLabel(name?.string)
      if (newLabel == null) {
        data.remove("label")
      } else {
        data.putString("label", newLabel)
      }
      outerNbt.put("data", data)
      stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(outerNbt))
    }

    @JvmStatic
    fun removeCustomName(stack: ItemStack) {
      val outerNbt = stack.get(DataComponentTypes.CUSTOM_DATA)?.copyNbt() ?: return
      val data = outerNbt.optCompound("data") ?: return
      data.remove("label")
      outerNbt.put("data", data)
      stack.set(DataComponentTypes.CUSTOM_DATA, NbtComponent.of(outerNbt))
    }
  }
}
