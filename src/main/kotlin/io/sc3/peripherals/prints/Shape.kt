package io.sc3.peripherals.prints

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.sc3.library.ext.byteToDouble
import io.sc3.library.ext.optInt
import io.sc3.library.ext.optString
import io.sc3.library.ext.putOptInt
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import net.minecraft.util.math.Box
import java.util.*
import kotlin.jvm.optionals.getOrNull

data class Shape(
  val bounds: Box,
  val texture: Optional<Identifier> = Optional.empty(),
  val tint: Optional<Int> = Optional.empty(),
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Shape

    if (bounds != other.bounds) return false
    if (texture != other.texture) return false
    if (tint != other.tint) return false

    return true
  }

  override fun hashCode(): Int {
    var result = bounds.hashCode()
    result = 31 * result + (texture.getOrNull()?.hashCode() ?: 0)
    result = 31 * result + (tint.getOrNull() ?: 0)
    return result
  }

  fun toNbt(): NbtCompound {
    val nbt = NbtCompound()
    nbt.putByte("minX", bounds.minX.toInt().coerceIn(0, 16).toByte())
    nbt.putByte("minY", bounds.minY.toInt().coerceIn(0, 16).toByte())
    nbt.putByte("minZ", bounds.minZ.toInt().coerceIn(0, 16).toByte())
    nbt.putByte("maxX", bounds.maxX.toInt().coerceIn(0, 16).toByte())
    nbt.putByte("maxY", bounds.maxY.toInt().coerceIn(0, 16).toByte())
    nbt.putByte("maxZ", bounds.maxZ.toInt().coerceIn(0, 16).toByte())
    nbt.putString("tex", texture.getOrNull()?.toString() ?: "")
    nbt.putOptInt("tint", tint.getOrNull())
    return nbt
  }

  companion object {
    fun fromNbt(nbt: NbtCompound) = Shape(
      Box(
        nbt.byteToDouble("minX"), nbt.byteToDouble("minY"), nbt.byteToDouble("minZ"),
        nbt.byteToDouble("maxX"), nbt.byteToDouble("maxY"), nbt.byteToDouble("maxZ")
      ),
      nbt.optString("tex").let { if(it == null) Optional.empty() else Optional.of(Identifier.of(it)) },
      nbt.optInt("tint").let { if(it == null) Optional.empty() else Optional.of(it) }
    )
    val CODEC: MapCodec<Shape> = RecordCodecBuilder.mapCodec { i ->
      i.group(
        BOX_CODEC.fieldOf("box").forGetter{ b -> b.bounds },
        Identifier.CODEC.optionalFieldOf("texture").forGetter { b -> b.texture },
        Codec.INT.optionalFieldOf("texture").forGetter { b -> b.tint }
      ).apply(i, ::Shape)
    }

    val BOX_CODEC: MapCodec<Box> =RecordCodecBuilder.mapCodec { i ->
        i.group(
          Codec.DOUBLE.fieldOf("minX").forGetter {z -> z.minX},
          Codec.DOUBLE.fieldOf("minY").forGetter {z -> z.minY},
          Codec.DOUBLE.fieldOf("minZ").forGetter {z -> z.minZ},
          Codec.DOUBLE.fieldOf("maxX").forGetter {z -> z.maxX},
          Codec.DOUBLE.fieldOf("maxY").forGetter {z -> z.maxY},
          Codec.DOUBLE.fieldOf("maxZ").forGetter {z -> z.maxZ},
        ).apply(i, ::Box)
    }
  }
}
