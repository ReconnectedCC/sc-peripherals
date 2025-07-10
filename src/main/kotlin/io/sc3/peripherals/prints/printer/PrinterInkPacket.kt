package io.sc3.peripherals.prints.printer

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals
import io.sc3.peripherals.ScPeripherals.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload

data class PrinterInkPacket(
  val pos: BlockPos,
  val chamelium: Int,
  val ink: Int
) : ScLibraryPacket() {
  val id = PrinterInkPacket.id

  companion object {
    val id = CustomPayload.Id<PrinterInkPacket>(ModId("printer_ink"))
    val CODEC = PacketCodec.tuple(
      BlockPos.PACKET_CODEC, PrinterInkPacket::pos,
      PacketCodecs.INTEGER, PrinterInkPacket::chamelium,
      PacketCodecs.INTEGER, PrinterInkPacket::ink,
      ::PrinterInkPacket
      )
  }

  override fun getId(): CustomPayload.Id<out CustomPayload> {
    return id;
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    val printer = ctx.client().world?.getBlockEntity(pos) as? PrinterBlockEntity ?: return
    printer.chamelium = chamelium
    printer.ink = ink
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}
}
