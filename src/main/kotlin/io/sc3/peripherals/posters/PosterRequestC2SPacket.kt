package io.sc3.peripherals.posters

import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripherals
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.network.packet.CustomPayload
import net.minecraft.network.packet.CustomPayload.Id
import net.minecraft.util.Identifier
import io.sc3.peripherals.ScPeripheralsPrometheus.registry
import io.prometheus.client.Counter
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

private const val MAX_POSTER_REQUESTS_PER_PACKET = 50

data class PosterRequestC2SPacket(
  val posterIds: List<String>
): ScLibraryPacket() {
  companion object {
    val CODEC: PacketCodec<RegistryByteBuf, PosterRequestC2SPacket> =
      PacketCodec.tuple(
        PacketCodecs.STRING.collect(PacketCodecs.toList()), PosterRequestC2SPacket::posterIds,
        ::PosterRequestC2SPacket
      )

    val POSTER_REQUEST_ID: Identifier = ScPeripherals.ModId("poster_request");
    val id: Id<PosterRequestC2SPacket> = Id<PosterRequestC2SPacket>(POSTER_REQUEST_ID);

    private val requestCounter = Counter.build()
      .name("sc_peripherals_posters_requested")
      .help("Number of posters requested by clients")
      .register(registry)

    private val responseCounter = Counter.build()
      .name("sc_peripherals_posters_sent")
      .help("Number of posters sent by the server")
      .register(registry)
  }

  override fun onServerReceive(ctx: ServerPlayNetworking.Context) {
    ctx.server().submit {
      requestCounter.inc(posterIds.size.toDouble())

      for ((idx, posterId) in posterIds.withIndex()) {
        if (idx >= MAX_POSTER_REQUESTS_PER_PACKET) break

        PosterItem.getPosterState(posterId, ctx.server().overworld)?.let { state ->
          ctx.responseSender().sendPacket(state.toPacket(posterId))
          responseCounter.inc()
        }
      }
    }
  }
  override fun getId(): Id<PosterRequestC2SPacket> {
    return id
  }

  override fun onClientReceive(ctx: ClientPlayNetworking.Context) {}
}
