package dev.ultreon.qvoxel.client.debug;

import dev.ultreon.libs.commons.v0.util.EnumUtils;
import dev.ultreon.qvoxel.client.render.Color;
import imgui.ImGui;
import imgui.flag.ImGuiDataType;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.*;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import it.unimi.dsi.fastutil.bytes.ByteConsumer;
import it.unimi.dsi.fastutil.floats.FloatConsumer;
import it.unimi.dsi.fastutil.shorts.ShortConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class ImGuiEx {
    public static void text(String label, Supplier<Object> value) {
        ImGui.text(label);
        ImGui.sameLine();
        Object o;
        try {
            o = value.get();
        } catch (Exception e) {
            o = "~@# " + e.getClass().getName() + " #@~";
        }
        ImGui.text(String.valueOf(o));
    }

    public static void editString(String label, String id, Supplier<String> value, Consumer<String> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImString i = new ImString(value.get(), 256);
            if (ImGui.inputText("##" + id, i, ImGuiInputTextFlags.EnterReturnsTrue)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editByte(String label, String id, byte value, ByteConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.U8, i)) {
                setter.accept((byte) i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editShort(String label, String id, short value, ShortConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImShort i = new ImShort(value);
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S16, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editInt(String label, String id, IntSupplier value, IntConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImInt i = new ImInt(value.getAsInt());
            if (ImGui.inputInt("##" + id, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editLong(String label, String id, LongSupplier value, LongConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImLong i = new ImLong(value.getAsLong());
            if (ImGui.inputScalar("##" + id, ImGuiDataType.S64, i)) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editFloat(String label, String id, FloatSupplier value, FloatConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImFloat i = new ImFloat(value.getFloat());
            if (ImGui.inputFloat("##" + id, i, 0, 0, "%.6f")) {
                setter.accept(i.get());
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editDouble(String label, String id, DoubleSupplier value, DoubleConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImDouble i = new ImDouble(value.getAsDouble());
        if (ImGui.inputDouble("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void editBool(String label, String id, BooleanSupplier value, BooleanConsumer setter) {
        ImGui.text(label);
        ImGui.sameLine();
        ImBoolean i = new ImBoolean(value.getAsBoolean());
        if (ImGui.checkbox("##" + id, i)) {
            setter.accept(i.get());
        }
    }

    public static void bool(String label, BooleanSupplier value) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            ImGui.checkbox("##", value.getAsBoolean());
        } catch (Exception e) {
            ImGui.text("~@# " + e.getClass().getName() + " #@~");
        }
    }

    public static void slider(String label, String id, int value, int min, int max, IntConsumer onChange) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            int[] v = new int[]{value};
            if (ImGui.sliderInt("##" + id, v, min, max)) {
                onChange.accept(v[0]);
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void button(String label, String id, Runnable func) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            if (ImGui.button("##" + id, 120, 16)) {
                func.run();
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor3(String color, String s, Supplier<Color> getter, Consumer<Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], 1f));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor3Gdx(String color, String s, Supplier<Color> getter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, 1f};
            if (ImGui.colorEdit3("##" + s, floats)) {
                c.r = floats[0];
                c.g = floats[1];
                c.b = floats[2];
                c.a = 1f;
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor4(String color, String s, Supplier<Color> getter, Consumer<Color> setter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, c.a};
            if (ImGui.colorEdit4("##" + s, floats)) {
                setter.accept(new Color(floats[0], floats[1], floats[2], floats[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editColor4Gdx(String color, String s, Supplier<Color> getter) {
        ImGui.text(color);
        ImGui.sameLine();
        try {
            @NotNull Color c = getter.get();
            float[] floats = {c.r, c.g, c.b, c.a};
            if (ImGui.colorEdit4("##" + s, floats)) {
                c.r = floats[0];
                c.g = floats[1];
                c.b = floats[2];
                c.a = floats[3];
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static <T extends Enum<T>> void editEnum(String s, String s1, Supplier<T> getter, Consumer<T> setter) {
        ImGui.text(s);
        ImGui.sameLine();
        try {
            T e = getter.get();
            ImInt index = new ImInt(e.ordinal());
            List<String> collect = new ArrayList<>();
            for (Enum<?> constant : e.getClass().getEnumConstants()) {
                collect.add(constant.name());
            }
            if (ImGui.combo("##" + s1, index, collect.toArray(String[]::new))) {
                setter.accept(EnumUtils.byOrdinal(index.get(), e));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2f(String label, String strId, Supplier<Vector2f> getter, Consumer<Vector2f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector2f v = getter.get();
            float[] vec = {v.x, v.y};
            if (ImGui.inputFloat2("##" + strId, vec)) {
                setter.accept(new Vector2f(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3f(String label, String strId, Supplier<Vector3f> getter, Consumer<Vector3f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector3f v = getter.get();
            float[] vec = {v.x, v.y, v.z};
            if (ImGui.inputFloat3("##" + strId, vec)) {
                setter.accept(new Vector3f(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4f(String label, String strId, Supplier<Vector4f> getter, Consumer<Vector4f> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector4f v = getter.get();
            float[] vec = {v.x, v.y, v.z, v.w};
            if (ImGui.inputFloat4("##" + strId, vec)) {
                setter.accept(new Vector4f(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2i(String label, String strId, Supplier<Vector2i> getter, Consumer<Vector2i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector2i v = getter.get();
            int[] vec = {v.x, v.y};
            if (ImGui.inputInt2("##" + strId, vec)) {
                setter.accept(new Vector2i(vec[0], vec[1]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3i(String label, String strId, Supplier<Vector3i> getter, Consumer<Vector3i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector3i v = getter.get();
            int[] vec = {v.x, v.y, v.z};
            if (ImGui.inputInt3("##" + strId, vec)) {
                setter.accept(new Vector3i(vec[0], vec[1], vec[2]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4i(String label, String strId, Supplier<Vector4i> getter, Consumer<Vector4i> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector4i v = getter.get();
            int[] vec = {v.x, v.y, v.z, v.w};
            if (ImGui.inputInt4("##" + strId, vec)) {
                setter.accept(new Vector4i(vec[0], vec[1], vec[2], vec[3]));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec2d(String label, String strId, Supplier<Vector2d> getter, Consumer<Vector2d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector2d v = getter.get();
            ImDouble x = new ImDouble(v.x);
            ImDouble y = new ImDouble(v.y);

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vector2d(x.get(), y.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vector2d(x.get(), y.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec3d(String label, String strId, Supplier<Vector3d> getter, Consumer<Vector3d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector3d v = getter.get();
            ImDouble x = new ImDouble(v.x);
            ImDouble y = new ImDouble(v.y);
            ImDouble z = new ImDouble(v.z);

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vector3d(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vector3d(x.get(), y.get(), z.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new Vector3d(x.get(), y.get(), z.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }

    public static void editVec4d(String label, String strId, Supplier<Vector4d> getter, Consumer<Vector4d> setter) {
        ImGui.text(label);
        ImGui.sameLine();
        try {
            Vector4d v = getter.get();
            ImDouble x = new ImDouble(v.x);
            ImDouble y = new ImDouble(v.y);
            ImDouble z = new ImDouble(v.z);
            ImDouble w = new ImDouble(v.w);

            if (ImGui.inputDouble("##" + strId + "[0]", x)) {
                setter.accept(new Vector4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[1]", y)) {
                setter.accept(new Vector4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[2]", z)) {
                setter.accept(new Vector4d(x.get(), y.get(), z.get(), w.get()));
            }

            if (ImGui.inputDouble("##" + strId + "[3]", w)) {
                setter.accept(new Vector4d(x.get(), y.get(), z.get(), w.get()));
            }
        } catch (Exception e) {
            ImGui.text(String.valueOf(e));
        }
    }
}
