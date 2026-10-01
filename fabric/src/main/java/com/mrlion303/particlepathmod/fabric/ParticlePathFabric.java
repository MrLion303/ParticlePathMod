package com.mrlion303.particlepathmod.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.*;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ParticlePathFabric implements ModInitializer {
    private static final Map<UUID, List<BlockPos>> SELECTIONS = new HashMap<>();
    private static final double SPACING = 0.5D;

    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!world.isClient && player.getStackInHand(hand).isOf(Items.STICK) && player instanceof ServerPlayerEntity p) {
                SELECTIONS.put(p.getUuid(), new ArrayList<>(List.of(pos.toImmutable())));
                p.sendMessage(Text.literal("Punto A seleccionado: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).styled(s -> s.withColor(0xFFD700)), true);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!world.isClient && player.getStackInHand(hand).isOf(Items.STICK) && player instanceof ServerPlayerEntity p) {
                BlockPos pos = hit.getBlockPos().toImmutable();
                List<BlockPos> points = SELECTIONS.get(p.getUuid());
                if (points == null) {
                    p.sendMessage(Text.literal("Primero selecciona el punto A con clic izquierdo.").styled(s -> s.withColor(0xFF5555)), true);
                } else {
                    points.add(pos);
                    int n = points.size();
                    String label = n <= 26 ? String.valueOf((char)('A' + n - 1)) : "P" + n;
                    p.sendMessage(Text.literal("Punto " + label + " seleccionado: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()).styled(s -> s.withColor(0xFFD700)), true);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        });

        CommandRegistrationCallback.EVENT.register(this::registerCommands);
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    private void registerCommands(CommandDispatcher<ServerCommandSource> d, CommandRegistryAccess access, net.minecraft.server.command.CommandManager.RegistrationEnvironment env) {
        d.register(literal("particlepath").requires(s -> s.hasPermissionLevel(2))
            .then(literal("create").then(argument("name", StringArgumentType.word())
                .then(argument("particle", StringArgumentType.greedyString()).suggests((c,b) -> suggestParticles(b))
                .executes(c -> create((ServerPlayerEntity)c.getSource().getEntity(), StringArgumentType.getString(c,"name"), StringArgumentType.getString(c,"particle"), access)))))
            .then(literal("modify").then(argument("name", StringArgumentType.word()).suggests((c,b) -> suggestPaths(c.getSource().getServer(), b))
                .then(argument("particle", StringArgumentType.greedyString()).suggests((c,b) -> suggestParticles(b))
                .executes(c -> modify((ServerPlayerEntity)c.getSource().getEntity(), StringArgumentType.getString(c,"name"), StringArgumentType.getString(c,"particle"), access)))))
            .then(literal("show").then(argument("name", StringArgumentType.word()).suggests((c,b) -> suggestPaths(c.getSource().getServer(), b))
                .executes(c -> setVisible((ServerPlayerEntity)c.getSource().getEntity(), StringArgumentType.getString(c,"name"), true))))
            .then(literal("hide").then(argument("name", StringArgumentType.word()).suggests((c,b) -> suggestPaths(c.getSource().getServer(), b))
                .executes(c -> setVisible((ServerPlayerEntity)c.getSource().getEntity(), StringArgumentType.getString(c,"name"), false))))
            .then(literal("remove").then(argument("name", StringArgumentType.word()).suggests((c,b) -> suggestPaths(c.getSource().getServer(), b))
                .executes(c -> remove((ServerPlayerEntity)c.getSource().getEntity(), StringArgumentType.getString(c,"name")))))
            .then(literal("list").executes(c -> {
                ServerPlayerEntity p=(ServerPlayerEntity)c.getSource().getEntity();
                send(p, ParticlePathStore.get(p.getServer()).names().isEmpty() ? "No hay caminos creados." : "Caminos: "+String.join(", ", ParticlePathStore.get(p.getServer()).names()));
                return 1;
            })));
    }

    private static int create(ServerPlayerEntity p,String name,String spec, CommandRegistryAccess access) {
        ParticlePathStore store=ParticlePathStore.get(p.getServer());
        if(store.has(name)){p.sendMessage(Text.literal("Ya existe el camino '"+name+"'.").styled(s->s.withColor(0xFF5555)),false);return 0;}
        List<BlockPos> pts=SELECTIONS.getOrDefault(p.getUuid(),List.of());
        if(pts.size()<2){p.sendMessage(Text.literal("Marca A y al menos B con el palo antes de crear el camino.").styled(s->s.withColor(0xFF5555)),false);return 0;}
        ParticleEffect effect=parse(spec, access);
        if(effect==null){p.sendMessage(Text.literal("Partícula inválida: "+spec).styled(s->s.withColor(0xFF5555)),false);return 0;}
        store.put(name,spec,new ArrayList<>(pts),false); SELECTIONS.remove(p.getUuid());
        send(p,"Camino '"+name+"' creado con "+pts.size()+" puntos."); return 1;
    }

    private static int modify(ServerPlayerEntity p,String name,String spec, CommandRegistryAccess access){
        ParticlePathStore s=ParticlePathStore.get(p.getServer());
        if(!s.has(name)){p.sendMessage(Text.literal("No existe el camino '"+name+"'.").styled(x->x.withColor(0xFF5555)),false);return 0;}
        if(parse(spec, access)==null){p.sendMessage(Text.literal("Partícula inválida: "+spec).styled(x->x.withColor(0xFF5555)),false);return 0;}
        s.setParticle(name,spec); send(p,"Partícula del camino '"+name+"' cambiada."); return 1;
    }

    private static int setVisible(ServerPlayerEntity p,String name,boolean v){
        ParticlePathStore s=ParticlePathStore.get(p.getServer());
        if(!s.has(name)){p.sendMessage(Text.literal("No existe el camino '"+name+"'.").styled(x->x.withColor(0xFF5555)),false);return 0;}
        s.setVisible(name,v); send(p,"Camino '"+name+"' "+(v?"mostrado.":"ocultado.")); return 1;
    }

    private static int remove(ServerPlayerEntity p,String name){
        ParticlePathStore s=ParticlePathStore.get(p.getServer());
        if(!s.remove(name)){p.sendMessage(Text.literal("No existe el camino '"+name+"'.").styled(x->x.withColor(0xFF5555)),false);return 0;}
        send(p,"Camino '"+name+"' eliminado."); return 1;
    }

    private static void send(ServerPlayerEntity p,String msg){p.sendMessage(Text.literal(msg).styled(s->s.withColor(0xFFD700)),false);}
    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestParticles(SuggestionsBuilder b){
        return net.minecraft.command.CommandSource.suggestMatching(Registries.PARTICLE_TYPE.getIds().stream().map(Object::toString),b);
    }
    private static java.util.concurrent.CompletableFuture<com.mojang.brigadier.suggestion.Suggestions> suggestPaths(MinecraftServer server,SuggestionsBuilder b){
        return net.minecraft.command.CommandSource.suggestMatching(ParticlePathStore.get(server).names(),b);
    }
    private static ParticleEffect parse(String spec, CommandRegistryAccess access){
        String[] a=spec.trim().split("\\s+");
        if(a.length==0)return null;
        String id=a[0].contains(":")?a[0]:"minecraft:"+a[0];
        if(id.equals("minecraft:dust")&&a.length>=2){
            int[] rgb=parseColor(a[1]); float size=a.length>=3?Float.parseFloat(a[2]):1f;
            if(rgb==null||size<=0)return null;
            return new DustParticleEffect(new Vector3f(rgb[0]/255f,rgb[1]/255f,rgb[2]/255f),size);
        }
        try {
            return ParticleEffectArgumentType.particleEffect(access).parse(new StringReader(spec));
        } catch(Exception e){return null;}
    }
    private static int[] parseColor(String v){
        Map<String,Integer> m=new HashMap<>(); m.put("red",0xFF0000);m.put("green",0x008000);m.put("blue",0x0000FF);m.put("yellow",0xFFFF00);m.put("cyan",0x00FFFF);m.put("magenta",0xFF00FF);m.put("purple",0x800080);m.put("orange",0xFFA500);m.put("pink",0xFFC0CB);m.put("white",0xFFFFFF);m.put("black",0);m.put("lime",0x00FF00);m.put("brown",0xA52A2A);
        String k=v.toLowerCase(Locale.ROOT); Integer n=m.get(k); String h=k.startsWith("#")?k.substring(1):k;
        if(n!=null)return new int[]{n>>16&255,n>>8&255,n&255};
        if(h.matches("[0-9a-f]{6}")){int x=Integer.parseInt(h,16);return new int[]{x>>16&255,x>>8&255,x&255};}
        if(h.matches("[0-9a-f]{3}")){return new int[]{Integer.parseInt(""+h.charAt(0)+h.charAt(0),16),Integer.parseInt(""+h.charAt(1)+h.charAt(1),16),Integer.parseInt(""+h.charAt(2)+h.charAt(2),16)};}
        return null;
    }

    private static void tick(MinecraftServer server){
        if(server.getTicks()%2!=0)return;
        ParticlePathStore s=ParticlePathStore.get(server);
        for(ServerWorld w:server.getWorlds()) for(ParticlePathStore.Path p:s.paths()){
            if(!p.visible()||p.points().size()<2)continue;
            ParticleEffect e=parse(p.particle(), CommandRegistryAccess.of(server.getRegistryManager(), server.getCommandSource().getEnabledFeatures())); if(e==null)continue;
            for(int i=0;i<p.points().size()-1;i++) spawn(w,p.points().get(i),p.points().get(i+1),e);
        }
    }

    private static void spawn(ServerWorld w,BlockPos a,BlockPos b,ParticleEffect p){
        Vec3d x=new Vec3d(a.getX()+.5,a.getY()+.5,a.getZ()+.5), y=new Vec3d(b.getX()+.5,b.getY()+.5,b.getZ()+.5);
        double dx=y.x-x.x,dy=y.y-x.y,dz=y.z-x.z,len=Math.sqrt(dx*dx+dy*dy+dz*dz);int n=Math.max(1,(int)Math.ceil(len/SPACING));
        for(int i=0;i<=n;i++){double t=(double)i/n;w.spawnParticles(p,x.x+dx*t,x.y+dy*t,x.z+dz*t,1,0,0,0,0);}
    }
}
