package io.sc3.peripherals.mixin;

import io.sc3.peripherals.prints.PrintItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
  @Inject(
    method = "getName",
    at = @At("HEAD"),
    cancellable = true
  )
  private void getName(CallbackInfoReturnable<Text> cir) {
    ItemStack stack = (ItemStack) (Object) this;
    if (stack.getItem() instanceof PrintItem) {
      cir.setReturnValue(PrintItem.getCustomName(stack));
    }
  }
}
