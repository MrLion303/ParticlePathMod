package com.mrlion303.particlepathmod.util;

import com.mojang.brigadier.StringReader;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class ParticleSpecParser {
    private static final Map<String, int[]> COLORS = new HashMap<>();
    static {
        color("black", 0x000000); color("white", 0xFFFFFF); color("red", 0xFF0000);
        color("green", 0x008000); color("blue", 0x0000FF); color("yellow", 0xFFFF00);
        color("cyan", 0x00FFFF); color("aqua", 0x00FFFF); color("magenta", 0xFF00FF);
        color("fuchsia", 0xFF00FF); color("purple", 0x800080); color("orange", 0xFFA500);
        color("pink", 0xFFC0CB); color("brown", 0xA52A2A); color("lime", 0x00FF00);
        color("navy", 0x000080); color("teal", 0x008080); color("olive", 0x808000);
        color("maroon", 0x800000); color("silver", 0xC0C0C0); color("gray", 0x808080);
        color("grey", 0x808080); color("light_blue", 0x55AAFF); color("lightblue", 0x55AAFF);
        color("dark_green", 0x006400); color("darkgreen", 0x006400);
        color("dark_blue", 0x00008B); color("darkblue", 0x00008B); color("violet", 0xEE82EE);
    }
    private ParticleSpecParser() {}
    private static void color(String name, int rgb) {
        COLORS.put(name, new int[] {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF});
    }
    public static String normalize(String specification) {
        String value = specification.trim();
        String[] parts = value.split("\\s+");
        if (parts.length >= 2 && (parts[0].equals("dust") || parts[0].equals("minecraft:dust"))
                && (parts.length == 2 || parts.length == 3)) {
            int[] rgb = parseColor(parts[1]);
            if (rgb != null) {
                double scale = parts.length == 3 ? Double.parseDouble(parts[2]) : 1.0D;
                if (scale <= 0.0D || Double.isNaN(scale) || Double.isInfinite(scale))
                    throw new IllegalArgumentException("El tamaño de dust debe ser mayor que 0.");
                return "minecraft:dust " + component(rgb[0]) + " " + component(rgb[1]) + " " + component(rgb[2]) + " " + scale;
            }
        }
        return value;
    }
    public static ParticleOptions parse(String specification) {
        try { return ParticleArgument.readParticle(new StringReader(normalize(specification)), BuiltInRegistries.PARTICLE_TYPE); }
        catch (Exception e) { return null; }
    }
    public static int[] parseColor(String value) {
        String key = value.toLowerCase(Locale.ROOT).replace('-', '_');
        int[] named = COLORS.get(key);
        if (named != null) return named;
        String hex = key.startsWith("#") ? key.substring(1) : key;
        if (hex.startsWith("0x")) hex = hex.substring(2);
        if (hex.matches("[0-9a-f]{6}")) {
            int rgb = Integer.parseInt(hex, 16);
            return new int[] {(rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF};
        }
        if (hex.matches("[0-9a-f]{3}")) {
            int r = Integer.parseInt(hex.substring(0,1) + hex.substring(0,1), 16);
            int g = Integer.parseInt(hex.substring(1,2) + hex.substring(1,2), 16);
            int b = Integer.parseInt(hex.substring(2,3) + hex.substring(2,3), 16);
            return new int[] {r, g, b};
        }
        return null;
    }
    private static String component(int value) { return Double.toString(value / 255.0D); }
}