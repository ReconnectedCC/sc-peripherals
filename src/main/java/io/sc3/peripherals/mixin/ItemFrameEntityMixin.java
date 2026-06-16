package io.sc3.peripherals.mixin;

import io.sc3.peripherals.Registration;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameEntity.class)
public class ItemFrameEntityMixin {
  @Inject(method = "containsMap", at = @At("RETURN"), cancellable = true)
  private void containsMap(CallbackInfoReturnable<Boolean> cir) {
    ItemFrameEntity frame = (ItemFrameEntity) (Object) this;
    ItemStack stack = frame.getHeldItemStack();
    if (stack.isOf(Registration.ModItems.INSTANCE.getPoster())) {
      cir.setReturnValue(true);
    }
  }
}
