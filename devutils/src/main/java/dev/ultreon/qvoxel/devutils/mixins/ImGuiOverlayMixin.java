package dev.ultreon.qvoxel.devutils.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ultreon.qvoxel.client.QuantumClient;
import dev.ultreon.qvoxel.client.debug.ImGuiOverlay;
import dev.ultreon.qvoxel.devutils.ImGuiHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ImGuiOverlay.class)
public class ImGuiOverlayMixin {
    @Inject(method = "renderWindows", at = @At("RETURN"))
    private static void renderWindows(QuantumClient client, CallbackInfo ci) {
        ImGuiHandler.renderWindows(client);
    }

    @Inject(method = "showGame", at = @At(value = "INVOKE", target = "Limgui/ImGui;image(JFFFFFFFFFF)V", shift = At.Shift.BEFORE))
    private static void renderDisplay(CallbackInfo ci) {
        ImGuiHandler.renderPreGame();
    }

    @WrapMethod(method = "blitGameFboToTex")
    private static void captureGame(int targetWidth, int targetHeight, Operation<Void> original) {
        if (ImGuiHandler.isPaused()) {
            return;
        }

        original.call(targetWidth, targetHeight);
    }

    @Inject(method = "renderImGui", at = @At("RETURN"))
    private static void renderImGui(CallbackInfo ci) {
        ImGuiHandler.renderPostGame();
    }

    @WrapMethod(method = "extensions")
    private static boolean renderWindows(Operation<Boolean> ci) {
        ImGuiHandler.renderMenuBar();
        ci.call();
        return true;
    }
}
