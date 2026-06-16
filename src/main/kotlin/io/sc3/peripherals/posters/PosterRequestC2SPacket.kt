package io.sc3.peripherals.posters

import io.prometheus.client.Counter
import io.sc3.library.networking.ScLibraryPacket
import io.sc3.peripherals.ScPeripheralsPrometheus.registry
import io.sc3.peripherals.ScPeripherals.ModId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload

data class PosterRequestC2SPacket(
  val posterIds: List<String>
) : ScLibraryPacket() {
  override fun getId() = ID

  fun toBytes(buf: PacketByteBuf) {
    buf.writeCollection(posterIds) { b, it -> b.writeString(it) }
  }

  override fun onClientReceive(context: ClientPlayNetworking.Context) {}

  override fun onServerReceive(context: ServerPlayNetworking.Context) {
    val server = context.server()
    val responseSender = context.responseSender()
    server.submit {
      requestCounter.inc(posterIds.size.toDouble())

      for ((idx, posterId) in posterIds.withIndex()) {
        if (idx >= MAX_POSTER_REQUESTS_PER_PACKET) break

        PosterItem.getPosterState(posterId, server.overworld)?.let { state ->
          responseSender.sendPacket(state.toPacket(posterId))
          responseCounter.inc()
        }
      }
    }
  }

  companion object {
    val ID = CustomPayload.Id<PosterRequestC2SPacket>(ModId("poster_request"))
    val CODEC: PacketCodec<RegistryByteBuf, PosterRequestC2SPacket> = PacketCodec.of(
      { pkt, buf -> pkt.toBytes(buf) },
      { buf -> fromBytes(buf) }
    )

    private val requestCounter = Counter.build()
      .name("sc_peripherals_posters_requested")
      .help("Number of posters requested by clients")
      .register(registry)

    private val responseCounter = Counter.build()
      .name("sc_peripherals_posters_sent")
      .help("Number of posters sent by the server")
      .register(registry)

    fun fromBytes(buf: PacketByteBuf) = PosterRequestC2SPacket(
      posterIds = buf.readList { it.readString() }
    )
  }
}

private const val MAX_POSTER_REQUESTS_PER_PACKET = 50
