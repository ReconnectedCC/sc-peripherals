package io.sc3.peripherals.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import io.sc3.peripherals.client.item.PosterHeadFeatureRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HeadFeatureRenderer.class)
public abstract class HeadFeatureRendererMixin<T extends LivingEntity, M extends EntityModel<T> & ModelWithHead> {
  @Inject(
    method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
    at = @At(
      value = "INVOKE",
      target = "Lnet/minecraft/client/render/entity/feature/HeadFeatureRenderer;translate(Lnet/minecraft/client/util/math/MatrixStack;Z)V",
      shift = At.Shift.BEFORE
    ),
    cancellable = true,
    locals = LocalCapture.CAPTURE_FAILHARD
  )
  public void render(
    MatrixStack matrices,
    VertexConsumerProvider vertexConsumers,
    int light,
    T entity,
    float limbAngle,
    float limbDistance,
    float tickDelta,
    float animationProgress,
    float headYaw,
    float headPitch,
    CallbackInfo ci,
    @Local ItemStack itemStack
  ) {
    if (PosterHeadFeatureRenderer.render(matrices, vertexConsumers, entity, itemStack, light) != ActionResult.PASS) {
      matrices.pop(); // the pop gets skipped if we cancel the method
      ci.cancel();
    }
  }
}
