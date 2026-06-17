package io.sc3.peripherals.mixin;

import io.sc3.peripherals.prints.PrintItem;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public class AnvilScreenHandlerMixin {
  @Unique
  protected CraftingResultInventory output;

  @Inject(method = "updateResult", at = @At("RETURN"))
  private void updateResult(CallbackInfo ci) {
    ItemStack stack = output.getStack(0);
    if (stack.isEmpty() || !(stack.getItem() instanceof PrintItem)) return;

    // Redirect CUSTOM_NAME component into print data label
    Text customName = stack.get(DataComponentTypes.CUSTOM_NAME);
    if (customName != null) {
      PrintItem.setCustomName(stack, customName);
      stack.remove(DataComponentTypes.CUSTOM_NAME);
    }

    // Prevent repair cost accumulation which breaks stacking
    stack.remove(DataComponentTypes.REPAIR_COST);
  }
}
