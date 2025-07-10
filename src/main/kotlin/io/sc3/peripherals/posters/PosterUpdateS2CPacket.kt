package io.sc3.peripherals.posters

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals
import io.sc3.peripherals.client.item.PosterRenderer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class PosterUpdateS2CPacket(
  val posterId: String,
  private val updateData: PosterState.UpdateData?
) : ScLibraryPacket() {
  val id = PosterUpdateS2CPacket.id

  companion object {
    val id = CustomPayload.Id<PosterRequestC2SPacket>(ScPeripherals.ModId("poster_update"))
    val PACKET_CODEC = PacketCodec.tuple(
      PacketCodecs.STRING, PosterUpdateS2CPacket::posterId,
      PacketCodecs.optional(PosterState.UpdateData.PACKET_CODEC).xmap(
        { x -> x.getOrNull() },
        { x -> Optional.ofNullable(x) }
      ), PosterUpdateS2CPacket::updateData,
      ::PosterUpdateS2CPacket
    )
  }

  private fun apply(posterState: PosterState) {
    if (updateData != null) {
      updateData.setColorsTo(posterState)
      updateData.setPaletteTo(posterState)
    }
  }

  override fun getId(): CustomPayload.Id<out CustomPayload> {
    return id;
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {
    ctx.client().submit {
      val name = PosterItem.getPosterName(posterId)
      var posterState: PosterState? = ctx.client().world?.getPosterState(name)
      if (posterState == null) {
        posterState = PosterState()
        ctx.client().world?.putPosterState(name, posterState)
      }

      apply(posterState)
      PosterRenderer.updateTexture(posterId, posterState)
    }
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {}
}
