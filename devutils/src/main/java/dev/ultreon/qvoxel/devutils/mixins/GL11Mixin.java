package dev.ultreon.qvoxel.devutils.mixins;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ultreon.qvoxel.devutils.DebugRegistries;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;

import java.nio.IntBuffer;

@Mixin(GL11.class)
public class GL11Mixin {
    @WrapMethod(method = "glGenTextures()I")
    private static int onGenTextures(Operation<Integer> original) {
        int id = original.call();
        DebugRegistries.TEXTURES.add(id);
        return id;
    }

    @WrapMethod(method = "glGenTextures([I)V")
    private static void onGenTextures(int[] textures, Operation<Void> original) {
        original.call((Object) textures);
        for (int texture : textures) {
            DebugRegistries.TEXTURES.add(texture);
        }
    }

    @WrapMethod(method = "glGenTextures(Ljava/nio/IntBuffer;)V")
    private static void onGenTextures(IntBuffer textures, Operation<Void> original) {
        original.call((Object) textures);
        int[] array = new int[textures.remaining()];
        textures.get(array);
        for (int texture : array) {
            DebugRegistries.TEXTURES.add(texture);
        }
    }

    @WrapMethod(method = "glDeleteTextures(I)V")
    private static void onDeleteTextures(int texture, Operation<Void> original) {
        DebugRegistries.TEXTURES.remove(texture);
        original.call(texture);
    }

    @WrapMethod(method = "glDeleteTextures([I)V")
    private static void onDeleteTextures(int[] textures, Operation<Void> original) {
        for (int texture : textures) {
            DebugRegistries.TEXTURES.remove(texture);
        }
        original.call((Object) textures);
    }

    @WrapMethod(method = "glDeleteTextures(Ljava/nio/IntBuffer;)V")
    private static void onDeleteTextures(IntBuffer textures, Operation<Void> original) {
        int[] array = new int[textures.remaining()];
        textures.get(array);
        for (int texture : array) {
            DebugRegistries.TEXTURES.remove(texture);
        }
        original.call(textures);
    }
}
