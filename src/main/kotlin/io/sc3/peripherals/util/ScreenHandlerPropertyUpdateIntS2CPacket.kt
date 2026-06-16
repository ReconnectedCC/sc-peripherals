package io.sc3.peripherals.util

import io.sc3.peripherals.ScPeripherals.modId
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.util.Identifier

class ScreenHandlerPropertyUpdateIntS2CPacket(
  val syncId: Int,
  val propertyId: Int,
  val value: Int
) : CustomPayload {
  override fun getId() = ID

  fun send(handler: ServerPlayNetworkHandler) {
    ServerPlayNetworking.send(handler.player, this)
  }

  companion object {
    val ID = CustomPayload.Id<ScreenHandlerPropertyUpdateIntS2CPacket>(
      Identifier.of(modId, "screen-handler-property-update-int"))

    val CODEC: PacketCodec<RegistryByteBuf, ScreenHandlerPropertyUpdateIntS2CPacket> = PacketCodec.of(
      { pkt, buf ->
        buf.writeByte(pkt.syncId)
        buf.writeInt(pkt.propertyId)
        buf.writeInt(pkt.value)
      },
      { buf -> ScreenHandlerPropertyUpdateIntS2CPacket(
        buf.readUnsignedByte().toInt(),
        buf.readInt(),
        buf.readInt()
      )}
    )

    fun registerReceiver() {
      ClientPlayNetworking.registerGlobalReceiver(ID) { payload, context ->
        context.client().execute {
          val player = context.client().player ?: return@execute
          val handler = player.currentScreenHandler
          if (handler.syncId == payload.syncId) {
            handler.setProperty(payload.propertyId, payload.value)
          }
        }
      }
    }
  }
}
