package io.sc3.peripherals.posters

import io.netty.buffer.ByteBuf
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.registry.RegistryWrapper
import net.minecraft.world.PersistentState
import java.io.File

class PosterState : PersistentState() {
  var colors = ByteArray(16384)
  var palette = getDefaultPalette() // Default to map colors

  override fun save(file: File, registryLookup: RegistryWrapper.WrapperLookup) {
    file.parentFile.mkdirs()
    super.save(file, registryLookup)
  }

  fun setColor(x: Int, z: Int, color: Byte) {
    colors[x + z * 128] = color
    isDirty = true
  }

  companion object {
    fun fromNbt(nbt: NbtCompound): PosterState {
      val posterState = PosterState()
      val colorArray = nbt.getByteArray("colors")
      if (colorArray.size == 16384) {
        posterState.colors = colorArray
      }
      val paletteArray = nbt.getIntArray("palette")
      if (paletteArray.size <= 64) {
        posterState.palette = paletteArray
      }

      return posterState
    }
  }

  override fun writeNbt(nbt: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup): NbtCompound = nbt.apply {
    putByteArray("colors", colors)
    putIntArray("palette", palette)
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as PosterState

    if (!colors.contentEquals(other.colors)) return false
    if (!palette.contentEquals(other.palette)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = colors.contentHashCode()
    result = 31 * result + palette.contentHashCode()
    return result
  }

  fun toPacket(id: String) = PosterUpdateS2CPacket(
    id,
    UpdateData(0, 0, 128, 128, colors, palette)
  )

  class UpdateData(
    val startX: Int,
    val startZ: Int,
    val width: Int,
    val height: Int,
    val colors: ByteArray,
    val palette: IntArray
  ) {
    companion object {
      val PACKET_CODEC = PacketCodec.tuple(
        PacketCodecs.INTEGER, UpdateData::startX,
        PacketCodecs.INTEGER, UpdateData::startZ,
        PacketCodecs.INTEGER, UpdateData::width,
        PacketCodecs.INTEGER, UpdateData::height,
        PacketCodecs.BYTE_ARRAY, UpdateData::colors,
        PacketCodecs.INTEGER.collect(PacketCodecs.toList()).xmap(
          { x -> x.toIntArray()},
          { x -> x.toList() }
        ), UpdateData::palette,
        ::UpdateData
      )
    }
    fun setColorsTo(posterState: PosterState) {
      for (i in 0 until width) {
        for (j in 0 until height) {
          posterState.setColor(startX + i, startZ + j, colors[i + j * width])
        }
      }
    }

    fun setPaletteTo(posterState: PosterState) {
      posterState.palette = palette
      posterState.isDirty = true
    }
  }
}
