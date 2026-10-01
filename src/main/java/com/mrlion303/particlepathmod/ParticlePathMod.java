package com.mrlion303.particlepathmod;

import com.mrlion303.particlepathmod.command.ParticlePathCommands;
import com.mrlion303.particlepathmod.data.ParticlePathSavedData;
import com.mrlion303.particlepathmod.event.ParticlePathEvents;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(ParticlePathMod.MOD_ID)
public class ParticlePathMod {
    public static final String MOD_ID = "particlepathmod";

    public ParticlePathMod() {
        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.register(new ParticlePathEvents());
        MinecraftForge.EVENT_BUS.register(new com.mrlion303.particlepathmod.event.SelectionCleanup());
    }

    private void registerCommands(RegisterCommandsEvent event) {
        ParticlePathCommands.register(event.getDispatcher());
    }

    public static ParticlePathSavedData getData(net.minecraft.server.level.ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                ParticlePathSavedData::load,
                ParticlePathSavedData::new,
                ParticlePathSavedData.DATA_NAME
        );
    }
}
