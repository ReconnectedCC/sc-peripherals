package io.sc3.peripherals

import net.fabricmc.api.ModInitializer
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import io.sc3.peripherals.config.ScPeripheralsConfig
import net.minecraft.block.entity.BlockEntity
import net.minecraft.block.entity.BlockEntityTicker
import net.minecraft.block.entity.BlockEntityType

object ScPeripherals : ModInitializer {
  internal val log = LoggerFactory.getLogger("ScPeripherals")!!

  internal const val modId = "sc-peripherals"
  internal fun ModId(value: String) = Identifier.of(modId, value)

  override fun onInitialize() {
    log.info("sc-peripherals initializing")

    // Initialize the default config file if it does not yet exist
    ScPeripheralsConfig.config.load()

    ScPeripheralsPrometheus.init()

    Registration.init()
  }

  fun <T : BlockEntity?, P : BlockEntity?> checkTypeForTicker(
    placedType: BlockEntityType<P>,
    tickerType: BlockEntityType<T>,
    ticker: BlockEntityTicker<in T>?
  ): BlockEntityTicker<P>? {
    return if (tickerType === placedType) ticker as BlockEntityTicker<P> else null
  }
}
