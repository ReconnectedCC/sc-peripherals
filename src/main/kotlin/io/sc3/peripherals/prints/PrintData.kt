package io.sc3.peripherals.prints

import com.mojang.datafixers.util.Function6
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import io.netty.buffer.ByteBuf
import io.sc3.library.ext.optBoolean
import io.sc3.library.ext.optString
import io.sc3.library.ext.putOptString
import io.sc3.peripherals.config.ScPeripheralsConfig.config
import io.sc3.peripherals.util.PacketCodecBuilder
import net.fabricmc.fabric.api.util.NbtType.COMPOUND
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.RegistryByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.codec.PacketCodecs
import net.minecraft.text.Text
import net.minecraft.util.StringHelper
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.util.shape.VoxelShape
import java.util.*
import java.util.function.Function
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

const val MAX_LABEL_LENGTH = 48
const val MAX_TOOLTIP_LENGTH = 256

data class PrintData(
  private val initialLabel: String? = null,
  var tooltip: String? = null,

  var isButton      : Boolean = false,
  var collideWhenOn : Boolean = true,
  var collideWhenOff: Boolean = true,
  var lightWhenOn   : Boolean = true,
  var lightWhenOff  : Boolean = true,

  var lightLevel   : Int     = 0,
  var redstoneLevel: Int     = 0,
  var isBeaconBlock: Boolean = false,
  var isQuiet      : Boolean = false,

  val shapesOff: Shapes = Shapes(),
  val shapesOn : Shapes = Shapes(),

  var seatPos: Vec3d? = null,
) {
  var label: String? = initialLabel
    set(value) {
      field = sanitiseLabel(value)
      labelText = field?.let { Text.of(it) }
    }

  var labelText: Text? = initialLabel?.let { Text.of(it) }
    private set

  private val voxelShapesOff = mutableMapOf<Direction, VoxelShape>()
  private val voxelShapesOn = mutableMapOf<Direction, VoxelShape>()

  fun voxelShape(direction: Direction, on: Boolean): VoxelShape {
    val voxelShapes = if (on) voxelShapesOn else voxelShapesOff
    return voxelShapes.getOrPut(direction) {
      val shapes = if (on) shapesOn else shapesOff
      shapes.toVoxelShape(direction)
    }
  }

  fun computeCosts(): Pair<Int, Int>? {
    val totalVolume = shapesOff.totalVolume + shapesOn.totalVolume
    val totalSurface = shapesOff.totalSurfaceArea + shapesOn.totalSurfaceArea

    // Invalid print data
    if (totalVolume <= 0) return null

    val redstoneCost = if (redstoneLevel in 1..14) customRedstoneCost else 0
    val noclipCost = if (!collideWhenOff || !collideWhenOn) noclipCostMultiplier else 1

    val chamelium = ((totalVolume / 2.0).coerceAtLeast(1.0) + redstoneCost) * noclipCost
    val ink = (totalSurface / 6.0).coerceAtLeast(1.0)

    return chamelium.toInt() to ink.toInt()
  }

  fun toNbt(): NbtCompound {
    val nbt = NbtCompound()

    nbt.putOptString("label", sanitiseLabel(label))
    nbt.putOptString("tooltip", sanitiseTooltip(tooltip))
    nbt.putBoolean("isButton", isButton)
    nbt.putBoolean("collideWhenOn", collideWhenOn)
    nbt.putBoolean("collideWhenOff", collideWhenOff)
    nbt.putBoolean("lightWhenOn2", lightWhenOn)
    nbt.putBoolean("lightWhenOff2", lightWhenOff)
    nbt.putInt("lightLevel", lightLevel)
    nbt.putInt("redstoneLevel", redstoneLevel)
    nbt.putBoolean("isBeaconBlock", isBeaconBlock)
    nbt.putBoolean("isQuiet", isQuiet)
    nbt.put("shapesOff", shapesOff.toNbt())
    nbt.put("shapesOn", shapesOn.toNbt())

    seatPos?.let {
      nbt.putDouble("seatX", it.x)
      nbt.putDouble("seatY", it.y)
      nbt.putDouble("seatZ", it.z)
    }

    return nbt
  }

  companion object {
    val customRedstoneCost: Int = config.get("printer.custom_redstone_cost")
    val noclipCostMultiplier: Int = config.get("printer.noclip_cost_multiplier")

    val VEC3D_PACKET_CODED: PacketCodec<RegistryByteBuf, Vec3d> = PacketCodec.tuple(
      PacketCodecs.DOUBLE, Vec3d::x,
      PacketCodecs.DOUBLE, Vec3d::y,
      PacketCodecs.DOUBLE, Vec3d::z,
      ::Vec3d
    )

    fun optionalStringCodec(): PacketCodec<RegistryByteBuf, String?> =
      PacketCodecs.optional(PacketCodecs.STRING).xmap(
        { it.getOrNull() },
        { Optional.ofNullable(it) }
      ).cast()

    fun optionalBool(default: Boolean): PacketCodec<RegistryByteBuf, Boolean> =
      PacketCodecs.optional(PacketCodecs.BOOL).xmap(
        { it.getOrDefault(default) },
        { Optional.ofNullable(it) }
      ).cast()

    fun optionalInt(default: Int): PacketCodec<RegistryByteBuf, Int> =
      PacketCodecs.optional(PacketCodecs.INTEGER).xmap(
        { it.getOrDefault(default) },
        { Optional.ofNullable(it) }
      ).cast()

    fun optionalShapes(): PacketCodec<RegistryByteBuf, Shapes> =
      PacketCodecs.optional(Shapes.PACKET_CODEC).xmap(
        { it.getOrDefault(Shapes()) },
        { Optional.ofNullable(it) }
      )

    fun optionalVec3d(): PacketCodec<RegistryByteBuf, Vec3d?> =
      PacketCodecs.optional(VEC3D_PACKET_CODED).xmap(
        { it.getOrNull() },
        { Optional.ofNullable(it) }
      )


    val PACKET_CODEC = PacketCodecBuilder<RegistryByteBuf, PrintData> { fields ->
      PrintData(
        initialLabel    = fields[0] as String?,
        tooltip         = fields[1] as String?,
        isButton        = fields[2] as Boolean,
        collideWhenOn   = fields[3] as Boolean,
        collideWhenOff  = fields[4] as Boolean,
        lightWhenOn     = fields[5] as Boolean,
        lightWhenOff    = fields[6] as Boolean,
        lightLevel      = fields[7] as Int,
        redstoneLevel   = fields[8] as Int,
        isBeaconBlock   = fields[9] as Boolean,
        isQuiet         = fields[10] as Boolean,
        shapesOff       = fields[11] as Shapes,
        shapesOn        = fields[12] as Shapes,
        seatPos         = fields[13] as Vec3d?
      )
    }.apply {
      field(optionalStringCodec(), PrintData::initialLabel)
      field(optionalStringCodec(), PrintData::tooltip)
      field(optionalBool(false), PrintData::isButton)
      field(optionalBool(true), PrintData::collideWhenOn)
      field(optionalBool(true), PrintData::collideWhenOff)
      field(optionalBool(true), PrintData::lightWhenOn)
      field(optionalBool(true), PrintData::lightWhenOff)
      field(optionalInt(0), PrintData::lightLevel)
      field(optionalInt(0), PrintData::redstoneLevel)
      field(optionalBool(false), PrintData::isBeaconBlock)
      field(optionalBool(false), PrintData::isQuiet)
      field(optionalShapes(), PrintData::shapesOff)
      field(optionalShapes(), PrintData::shapesOn)
      field(optionalVec3d(), PrintData::seatPos)
    }.build()


    val CODEC: Codec<PrintData> = RecordCodecBuilder.create { i ->
      i.group(
        Codec.STRING.optionalFieldOf("initialLabel", null).forGetter { it.initialLabel },
        Codec.STRING.optionalFieldOf("tooltip", null).forGetter { it.tooltip },
        Codec.BOOL.optionalFieldOf("isButton", false).forGetter { it.isButton },
        Codec.BOOL.optionalFieldOf("collideWhenOn", true).forGetter { it.collideWhenOn },
        Codec.BOOL.optionalFieldOf("collideWhenOff", true).forGetter { it.collideWhenOff },
        Codec.BOOL.optionalFieldOf("lightWhenOn", true).forGetter { it.lightWhenOn },
        Codec.BOOL.optionalFieldOf("lightWhenOff", true).forGetter { it.lightWhenOff },
        Codec.INT.optionalFieldOf("lightLevel", 0).forGetter { it.lightLevel },
        Codec.INT.optionalFieldOf("redstoneLevel", 0).forGetter { it.redstoneLevel },
        Codec.BOOL.optionalFieldOf("isBeaconBlock", false).forGetter { it.isBeaconBlock },
        Codec.BOOL.optionalFieldOf("isQuiet", false).forGetter { it.isQuiet },
        Shapes.CODEC.optionalFieldOf("shapesOff", Shapes()).forGetter { it.shapesOff },
        Shapes.CODEC.optionalFieldOf("shapesOn", Shapes()).forGetter { it.shapesOn },
        Vec3d.CODEC.optionalFieldOf("seatPos", null).forGetter { it.seatPos }
      ).apply(i, ::PrintData)
    }

    fun fromNbt(nbt: NbtCompound) = PrintData(
      initialLabel   = nbt.optString("label")?.takeIf { isValidLabel(it) }, // Cheaper than sanitiseLabel
      tooltip        = nbt.optString("tooltip")?.takeIf { isValidTooltip(it) },
      isButton       = nbt.getBoolean("isButton"),
      collideWhenOn  = nbt.getBoolean("collideWhenOn"),
      collideWhenOff = nbt.getBoolean("collideWhenOff"),
      lightWhenOn    = nbt.optBoolean("lightWhenOn2") ?: true,
      lightWhenOff   = nbt.optBoolean("lightWhenOff2") ?: true,
      lightLevel     = nbt.getInt("lightLevel"),
      redstoneLevel  = nbt.getInt("redstoneLevel"),
      isBeaconBlock  = nbt.getBoolean("isBeaconBlock"),
      isQuiet        = nbt.getBoolean("isQuiet"),
      shapesOff      = nbt.getShapeSet("shapesOff"),
      shapesOn       = nbt.getShapeSet("shapesOn"),
      seatPos        = nbt.getSeatPos()?.takeIf { isValidSeatPos(it) }
    )

    private fun NbtCompound.getShapeSet(key: String): Shapes =
      getList(key, COMPOUND)
        .map { Shape.fromNbt(it as NbtCompound) }
        .toCollection(Shapes())

    private fun NbtCompound.getSeatPos(): Vec3d? {
      if (!contains("seatX") || !contains("seatY") || !contains("seatZ")) return null
      val x = getDouble("seatX")
      val y = getDouble("seatY")
      val z = getDouble("seatZ")
      return if (x.isFinite() && y.isFinite() && z.isFinite()) Vec3d(x, y, z) else null
    }

    private fun isValidLabel(s: String?) = s?.length in 1..MAX_LABEL_LENGTH
    private fun isValidTooltip(s: String?) = s?.length in 1..MAX_TOOLTIP_LENGTH

    fun sanitiseLabel(s: String?) = s?.takeIf { isValidLabel(it) }
      ?.let { stripInvalidChars(it) }
    fun sanitiseTooltip(s: String?) = s?.takeIf { isValidTooltip(it) }
      ?.let { stripInvalidChars(it, true) }

    /** Strip invalid characters, but allow the section sign. If sc-networking is installed, this will also strip
     * private-use font characters, but let's allow the Krist symbol (U+E000) */
    private fun stripInvalidChars(s: String, allowNewlines: Boolean = false) =
      s.filter { StringHelper.isValidChar(it) || it == '§' || it == '\uE000' || (allowNewlines && it == '\n') }

    fun isValidSeatPos(pos: Vec3d) =
      pos.x in 0.1..0.9 && pos.y in 0.1..0.9 && pos.z in 0.1..0.9
  }
}
