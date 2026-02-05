package dev.ultreon.qvoxel.devutils.mixins;

import dev.ultreon.qvoxel.client.render.Mesh;
import dev.ultreon.qvoxel.devutils.DebugRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mesh.class)
public class MeshMixin {
    @Inject(method = "<init>*", at = @At("CTOR_HEAD"))
    private void init(CallbackInfo ci) {
        DebugRegistries.MESHES.add((Mesh) (Object) this);
    }

    @Inject(method = "delete", at = @At("TAIL"))
    private void delete(CallbackInfo ci) {
        DebugRegistries.MESHES.remove(this);
    }
}
