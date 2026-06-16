package io.sc3.peripherals.posters.printer

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class PosterPrinterInkPacket(
  val pos: BlockPos,
  val ink: Int
) : ScLibraryPacket() {
  override fun getId() = ID

  fun toBytes(buf: PacketByteBuf) {
    buf.writeBlockPos(pos)
    buf.writeInt(ink)
  }

  override fun onServerReceive(context: ServerPlayNetworking.Context) {}

  override fun onClientReceive(context: ClientPlayNetworking.Context) {
    val client = context.client()
    val printer = client.world?.getBlockEntity(pos) as? PosterPrinterBlockEntity ?: return
    printer.ink = ink
  }

  companion object {
    val ID = CustomPayload.Id<PosterPrinterInkPacket>(ModId("poster_printer_ink"))
    val CODEC: PacketCodec<RegistryByteBuf, PosterPrinterInkPacket> = PacketCodec.of(
      { pkt, buf -> pkt.toBytes(buf) },
      { buf -> fromBytes(buf) }
    )

    fun fromBytes(buf: PacketByteBuf) = PosterPrinterInkPacket(
      pos = buf.readBlockPos(),
      ink = buf.readInt()
    )
  }
}
