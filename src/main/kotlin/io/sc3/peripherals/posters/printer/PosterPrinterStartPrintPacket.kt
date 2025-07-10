package io.sc3.peripherals.posters.printer

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class PosterPrinterStartPrintPacket(
  val pos: BlockPos,
  val posterId: String,
) : ScLibraryPacket() {
  val id = PosterPrinterStartPrintPacket.id

  companion object {
    val id = CustomPayload.Id<PosterPrinterStartPrintPacket>(ModId("poster_printer_start_print"))

    val CODEC = PacketCodec.tuple(
      BlockPos.PACKET_CODEC, PosterPrinterStartPrintPacket::pos,
      PacketCodecs.STRING, PosterPrinterStartPrintPacket::posterId,
      ::PosterPrinterStartPrintPacket
    )
  }

  override fun getId(): CustomPayload.Id<out CustomPayload> {
    return id;
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    val printer = ctx.client().world?.getBlockEntity(pos) as? PosterPrinterBlockEntity ?: return
    printer.animatingPosterId = posterId
    printer.animationStartTime = ctx.client().world?.time ?: 0
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}
}
