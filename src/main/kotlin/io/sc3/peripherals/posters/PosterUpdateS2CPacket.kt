package io.sc3.peripherals.posters

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals.ModId
import io.sc3.peripherals.client.item.PosterRenderer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class PosterUpdateS2CPacket(
  val posterId: String,
  private val updateData: PosterState.UpdateData?
) : ScLibraryPacket() {
  override fun getId() = ID

  fun toBytes(buf: PacketByteBuf) {
    buf.writeString(posterId)
    if (updateData != null) {
      buf.writeByte(updateData.width)
      buf.writeByte(updateData.height)
      buf.writeByte(updateData.startX)
      buf.writeByte(updateData.startZ)
      buf.writeByteArray(updateData.colors)
      buf.writeIntArray(updateData.palette)
    } else {
      buf.writeByte(0)
    }
  }

  private fun apply(posterState: PosterState) {
    if (updateData != null) {
      updateData.setColorsTo(posterState)
      updateData.setPaletteTo(posterState)
    }
  }

  override fun onServerReceive(context: ServerPlayNetworking.Context) {}

  override fun onClientReceive(context: ClientPlayNetworking.Context) {
    val client = context.client()
    client.submit {
      val name = PosterItem.getPosterName(posterId)
      var posterState: PosterState? = client.world?.getPosterState(name)
      if (posterState == null) {
        posterState = PosterState()
        client.world?.putPosterState(name, posterState)
      }

      apply(posterState)
      PosterRenderer.updateTexture(posterId, posterState)
    }
  }

  companion object {
    val ID = CustomPayload.Id<PosterUpdateS2CPacket>(ModId("poster_update"))
    val CODEC: PacketCodec<RegistryByteBuf, PosterUpdateS2CPacket> = PacketCodec.of(
      { pkt, buf -> pkt.toBytes(buf) },
      { buf -> fromBytes(buf) }
    )

    fun fromBytes(buf: PacketByteBuf) = PosterUpdateS2CPacket(
      posterId = buf.readString(),
      updateData = PosterState.UpdateData(
        width = buf.readUnsignedByte().toInt(),
        height = buf.readUnsignedByte().toInt(),
        startX = buf.readUnsignedByte().toInt(),
        startZ = buf.readUnsignedByte().toInt(),
        colors = buf.readByteArray(),
        palette = buf.readIntArray()
      )
    )
  }
}
