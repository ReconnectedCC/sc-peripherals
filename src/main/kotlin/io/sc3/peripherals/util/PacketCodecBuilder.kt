package io.sc3.peripherals.util

import net.minecraft.network.codec.PacketCodec
import java.util.function.BiFunction
import java.util.function.Function

class PacketCodecBuilder<B, C>(val constructor: (List<Any?>) -> C) {
  private val codecs = mutableListOf<Pair<PacketCodec<out B, *>, (C) -> Any?>>()

  fun <T> field(codec: PacketCodec<B, T>, getter: (C) -> T): PacketCodecBuilder<B, C> {
    codecs.add(codec to getter)
    return this
  }

  fun build(): PacketCodec<B, C> {
    return object : PacketCodec<B, C> {
      override fun decode(buf: B): C {
        val values = codecs.map { (codec, _) ->
          @Suppress("UNCHECKED_CAST")
          (codec as PacketCodec<B, Any?>).decode(buf)
        }
        return constructor(values)
      }

      override fun encode(buf: B, value: C) {
        codecs.forEach { (codec, getter) ->
          @Suppress("UNCHECKED_CAST")
          (codec as PacketCodec<B, Any?>).encode(buf, getter(value))
        }
      }
    }
  }
}
