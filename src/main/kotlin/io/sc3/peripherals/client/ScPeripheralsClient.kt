package io.sc3.peripherals.client

import io.sc3.library.ext.ItemFrameEvents
import io.sc3.library.networking.registerClientReceiver
import io.sc3.peripherals.Registration
import io.sc3.peripherals.ScPeripherals
import io.sc3.peripherals.client.block.PosterPrinterRenderer
import io.sc3.peripherals.client.block.PrintBakedModel
import io.sc3.peripherals.client.block.PrintUnbakedModel
import io.sc3.peripherals.client.block.PrinterRenderer
import io.sc3.peripherals.client.gui.PosterPrinterScreen
import io.sc3.peripherals.client.gui.PrinterScreen
import io.sc3.peripherals.client.item.PosterRenderer
import io.sc3.peripherals.config.ScPeripheralsClientConfig
import io.sc3.peripherals.posters.PosterItem
import io.sc3.peripherals.posters.PosterUpdateS2CPacket
import io.sc3.peripherals.posters.printer.PosterPrinterInkPacket
import io.sc3.peripherals.posters.printer.PosterPrinterStartPrintPacket
import io.sc3.peripherals.posters.tickPosterRequests
import io.sc3.peripherals.prints.PrintBlock
import io.sc3.peripherals.prints.PrintItem
import io.sc3.peripherals.prints.printer.PrinterDataPacket
import io.sc3.peripherals.prints.printer.PrinterInkPacket
import io.sc3.peripherals.util.ScreenHandlerPropertyUpdateIntS2CPacket
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking

import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry
import net.minecraft.client.gui.screen.ingame.HandledScreens
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

object ScPeripheralsClient : ClientModInitializer {
  private val log = LoggerFactory.getLogger("ScPeripherals/ScPeripheralsClient")!!

  override fun onInitializeClient() {
    log.info("sc-peripherals client initializing")

    // Initialize the default config file if it does not yet exist
    ScPeripheralsClientConfig.config.load()
    BlockEntityRendererFactories.register(Registration.ModBlockEntities.printer)
      { PrinterRenderer }
    BlockEntityRendererFactories.register(Registration.ModBlockEntities.posterPrinter)
      { PosterPrinterRenderer }

    HandledScreens.register(Registration.ModScreens.printer, ::PrinterScreen)
    HandledScreens.register(Registration.ModScreens.posterPrinter, ::PosterPrinterScreen)

    // Allow transparent textures to work in 3D prints
    BlockRenderLayerMap.INSTANCE.putBlock(Registration.ModBlocks.print, RenderLayer.getTranslucent())

    ModelLoadingPlugin.register { ctx ->
      ctx.addModels(Identifier.of(ScPeripherals.modId, PrintBlock.id))
    }

    registerClientReceiver(PrinterInkPacket.id)
    registerClientReceiver(PrinterDataPacket.id)

    registerClientReceiver(PosterPrinterInkPacket.id)
    registerClientReceiver(PosterPrinterStartPrintPacket.id)
    registerClientReceiver(PosterUpdateS2CPacket.id)

    ItemFrameEvents.ITEM_RENDER.register(PosterRenderer::renderItemFrame)

    // Vanilla ScreenHandlerPropertyUpdateS2CPacket sends shorts instead of ints over the wire
    ScreenHandlerPropertyUpdateIntS2CPacket.registerReceiver()

    // Clear 3D print cache on resource reload
    PrintBakedModel.init()

    PosterItem.clientInit()

    ClientTickEvents.START_WORLD_TICK.register(::tickPosterRequests)
  }
}
