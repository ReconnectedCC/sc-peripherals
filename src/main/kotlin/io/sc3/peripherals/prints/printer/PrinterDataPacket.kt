package io.sc3.peripherals.prints.printer

import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.util.math.BlockPos
import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals.ModId
import io.sc3.peripherals.prints.PrintData
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

data class PrinterDataPacket(
  val pos: BlockPos,
  val data: PrintData?
) : ScLibraryPacket() {
  val id = PrinterDataPacket.id

  companion object {
    val CODEC: PacketCodec<RegistryByteBuf, PrinterDataPacket> = PacketCodec.tuple(
      BlockPos.PACKET_CODEC, PrinterDataPacket::pos,
      PacketCodecs.optional(PrintData.PACKET_CODEC).xmap(
        { x -> x.getOrNull() },
        { x -> Optional.ofNullable(x) }
      ), PrinterDataPacket::data,
      ::PrinterDataPacket
    )

    val id = CustomPayload.Id<PrinterDataPacket>(ModId("printer_data"))
  }

  override fun getId(): CustomPayload.Id<out CustomPayload> {
    return PrinterDataPacket.id;
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    val printer = ctx.client().world?.getBlockEntity(pos) as? PrinterBlockEntity ?: return
    if (data != null) printer.previewData = data
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}
}
