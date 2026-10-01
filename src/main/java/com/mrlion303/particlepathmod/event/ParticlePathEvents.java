package com.mrlion303.particlepathmod.event;

import com.mrlion303.particlepathmod.ParticlePathMod;
import com.mrlion303.particlepathmod.command.PathSelectionManager;
import com.mrlion303.particlepathmod.data.*;
import com.mrlion303.particlepathmod.util.ParticleSpecParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;

public class ParticlePathEvents {
    private static final double SPACING=.5D;
    @SubscribeEvent public void left(PlayerInteractEvent.LeftClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItemStack().is(Items.STICK))return;
        BlockPos pos=e.getPos().immutable();e.setCanceled(true);PathSelectionManager.reset(p.getUUID(),pos);
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Punto A: "+pos.getX()+", "+pos.getY()+", "+pos.getZ()),true);
    }
    @SubscribeEvent public void right(PlayerInteractEvent.RightClickBlock e){
        if(!(e.getEntity() instanceof ServerPlayer p)||!e.getItemStack().is(Items.STICK))return;
        BlockPos pos=e.getPos().immutable();e.setCanceled(true);
        if(!PathSelectionManager.append(p.getUUID(),pos)){
            p.displayClientMessage(net.minecraft.network.chat.Component.literal("Primero selecciona el punto A con clic izquierdo."),true);return;
        }
        int n=PathSelectionManager.get(p.getUUID()).size();
        p.displayClientMessage(net.minecraft.network.chat.Component.literal("Punto "+letter(n)+": "+pos.getX()+", "+pos.getY()+", "+pos.getZ()),true);
    }
    @SubscribeEvent public void tick(TickEvent.ServerTickEvent e){
        if(e.phase!=TickEvent.Phase.END||e.getServer().getTickCount()%2!=0)return;
        for(ServerLevel level:e.getServer().getAllLevels()){
            for(ParticlePath path:ParticlePathMod.getData(level).getPaths().values()){
                if(!path.isVisible()||path.getPoints().size()<2)continue;
                ParticleOptions particle=ParticleSpecParser.parse(path.getParticle());if(particle==null)continue;
                List<BlockPos> pts=path.getPoints();
                for(int i=0;i<pts.size()-1;i++)spawn(level,pts.get(i),pts.get(i+1),particle);
            }
        }
    }
    private static void spawn(ServerLevel l,BlockPos a,BlockPos b,ParticleOptions p){
        double x1=a.getX()+.5,y1=a.getY()+.5,z1=a.getZ()+.5,x2=b.getX()+.5,y2=b.getY()+.5,z2=b.getZ()+.5;
        double dx=x2-x1,dy=y2-y1,dz=z2-z1,len=Math.sqrt(dx*dx+dy*dy+dz*dz);int n=Math.max(1,(int)Math.ceil(len/SPACING));
        for(int i=0;i<=n;i++){double t=(double)i/n;l.sendParticles(p,x1+dx*t,y1+dy*t,z1+dz*t,1,0,0,0,0);}
    }
    private static String letter(int n){return n<=26?String.valueOf((char)('A'+n-1)):"P"+n;}
}