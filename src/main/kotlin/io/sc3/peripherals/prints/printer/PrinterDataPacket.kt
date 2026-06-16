package io.sc3.peripherals.prints.printer

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals.ModId
import io.sc3.peripherals.prints.PrintData
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.math.BlockPos

data class PrinterDataPacket(
  val pos: BlockPos,
  val data: PrintData?
) : ScLibraryPacket() {
  override fun getId() = ID

  fun toBytes(buf: PacketByteBuf) {
    buf.writeBlockPos(pos)
    buf.writeNullable(data?.toNbt(), PacketByteBuf::writeNbt)
  }

  override fun onServerReceive(context: ServerPlayNetworking.Context) {}

  override fun onClientReceive(context: ClientPlayNetworking.Context) {
    val client = context.client()
    val printer = client.world?.getBlockEntity(pos) as? PrinterBlockEntity ?: return
    if (data != null) printer.previewData = data
  }

  companion object {
    val ID = CustomPayload.Id<PrinterDataPacket>(ModId("printer_data"))
    val CODEC: PacketCodec<RegistryByteBuf, PrinterDataPacket> = PacketCodec.of(
      { pkt, buf -> pkt.toBytes(buf) },
      { buf -> fromBytes(buf) }
    )

    fun fromBytes(buf: PacketByteBuf) = PrinterDataPacket(
      pos = buf.readBlockPos(),
      data = buf.readNullable(PacketByteBuf::readNbt)?.let { PrintData.fromNbt(it) }
    )
  }
}
