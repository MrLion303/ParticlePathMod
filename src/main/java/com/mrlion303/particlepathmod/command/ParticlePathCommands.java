package com.mrlion303.particlepathmod.command;
import com.mrlion303.particlepathmod.ParticlePathMod;
import com.mrlion303.particlepathmod.data.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.*;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import java.util.List;
public final class ParticlePathCommands {
    public static void register(CommandDispatcher<CommandSourceStack> d){
        d.register(Commands.literal("particlepath").requires(s->s.hasPermission(2))
        .then(Commands.literal("create").then(Commands.argument("name",StringArgumentType.word())
        .then(Commands.argument("particle",StringArgumentType.greedyString()).executes(c->create(c.getSource(),StringArgumentType.getString(c,"name"),StringArgumentType.getString(c,"particle"))))))
        .then(Commands.literal("show").then(Commands.argument("name",StringArgumentType.word()).executes(c->visible(c.getSource(),StringArgumentType.getString(c,"name"),true))))
        .then(Commands.literal("hide").then(Commands.argument("name",StringArgumentType.word()).executes(c->visible(c.getSource(),StringArgumentType.getString(c,"name"),false))))
        .then(Commands.literal("remove").then(Commands.argument("name",StringArgumentType.word()).executes(c->remove(c.getSource(),StringArgumentType.getString(c,"name")))))
        .then(Commands.literal("list").executes(c->list(c.getSource()))));
    }
    private static int create(CommandSourceStack s,String name,String particle){
        ParticlePathSavedData d=ParticlePathMod.getData(s.getLevel());
        if(d.contains(name)){s.sendFailure(Component.literal("Ya existe el camino '"+name+"'. Usa otro nombre."));return 0;}
        try{
            var player=s.getPlayerOrException(); List<BlockPos> pts=PathSelectionManager.consumeSelection(player.getUUID());
            if(pts.size()<2){s.sendFailure(Component.literal("Marca A y al menos B con el palo antes de crear el camino."));return 0;}
            com.mojang.brigadier.StringReader reader=new com.mojang.brigadier.StringReader(particle);
            net.minecraft.commands.arguments.ParticleArgument.readParticle(reader,net.minecraft.core.registries.BuiltInRegistries.PARTICLE_TYPE);
            d.put(new ParticlePath(name,particle,pts,false));
            s.sendSuccess(()->Component.literal("Camino '"+name+"' creado con "+pts.size()+" puntos."),true);return 1;
        }catch(Exception e){s.sendFailure(Component.literal("Partícula inválida: "+particle));return 0;}
    }
    private static int visible(CommandSourceStack s,String name,boolean value){
        ParticlePath p=ParticlePathMod.getData(s.getLevel()).get(name);
        if(p==null){s.sendFailure(Component.literal("No existe el camino '"+name+"'."));return 0;}
        p.setVisible(value);ParticlePathMod.getData(s.getLevel()).setDirty();
        s.sendSuccess(()->Component.literal("Camino '"+name+"' "+(value?"mostrado.":"ocultado.")),true);return 1;
    }
    private static int remove(CommandSourceStack s,String name){
        if(ParticlePathMod.getData(s.getLevel()).remove(name)==null){s.sendFailure(Component.literal("No existe el camino '"+name+"'."));return 0;}
        s.sendSuccess(()->Component.literal("Camino '"+name+"' eliminado."),true);return 1;
    }
    private static int list(CommandSourceStack s){var d=ParticlePathMod.getData(s.getLevel());s.sendSuccess(()->Component.literal(d.getPaths().isEmpty()?"No hay caminos creados.":"Caminos: "+String.join(", ",d.getPaths().keySet())),false);return d.getPaths().size();}
}
