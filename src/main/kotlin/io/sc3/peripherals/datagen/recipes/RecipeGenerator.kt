package io.sc3.peripherals.datagen.recipes

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider
import net.minecraft.advancement.AdvancementCriterion
import net.minecraft.advancement.criterion.InventoryChangedCriterion
import net.minecraft.data.server.recipe.RecipeExporter
import net.minecraft.item.Item
import net.minecraft.item.ItemConvertible
import net.minecraft.predicate.item.ItemPredicate
import net.minecraft.registry.RegistryWrapper
import net.minecraft.registry.tag.TagKey
import io.sc3.peripherals.datagen.recipes.handlers.RecipeHandlers.RECIPE_HANDLERS
import java.util.concurrent.CompletableFuture

class RecipeGenerator(
    out: FabricDataOutput,
    private val registriesFuture: CompletableFuture<RegistryWrapper.WrapperLookup>
) : FabricRecipeProvider(out, registriesFuture) {
  override fun generate(exporter: RecipeExporter) {
    val registries = registriesFuture.join()
    RECIPE_HANDLERS.forEach { it.generateRecipes(exporter, registries) }
  }
}

fun inventoryChange(vararg items: ItemConvertible): AdvancementCriterion<InventoryChangedCriterion.Conditions> =
  InventoryChangedCriterion.Conditions.items(*items)

fun inventoryChange(tag: TagKey<Item>): AdvancementCriterion<InventoryChangedCriterion.Conditions> =
  InventoryChangedCriterion.Conditions.items(ItemPredicate.Builder.create().tag(tag).build())
