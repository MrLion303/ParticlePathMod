package com.mrlion303.particlepathmod.fabric;

import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public final class ParticlePathStore {
    private static final String FILE="particlepathmod_paths.nbt";
    private final MinecraftServer server;
    private final Map<String,Path> paths=new LinkedHashMap<>();
    private ParticlePathStore(MinecraftServer s){server=s;load();}
    private static final Map<MinecraftServer,ParticlePathStore> INSTANCES=new WeakHashMap<>();
    public static synchronized ParticlePathStore get(MinecraftServer s){return INSTANCES.computeIfAbsent(s,ParticlePathStore::new);}
    public record Path(String name,String particle,List<BlockPos> points,boolean visible){}
    public Collection<Path> paths(){return paths.values();}
    public Set<String> names(){return paths.keySet();}
    public boolean has(String n){return paths.containsKey(n);}
    public void put(String n,String particle,List<BlockPos> pts,boolean v){paths.put(n,new Path(n,particle,pts,v));save();}
    public void setParticle(String n,String p){Path x=paths.get(n);paths.put(n,new Path(n,p,x.points,x.visible));save();}
    public void setVisible(String n,boolean v){Path x=paths.get(n);paths.put(n,new Path(n,x.particle,x.points,v));save();}
    public boolean remove(String n){if(paths.remove(n)!=null){save();return true;}return false;}
    private void load(){Path f=server.getSavePath(WorldSavePath.ROOT).resolve(FILE);if(!Files.exists(f))return;try(DataInputStream in=new DataInputStream(Files.newInputStream(f))){int count=in.readInt();for(int i=0;i<count;i++){String n=in.readUTF(),p=in.readUTF();boolean v=in.readBoolean();int c=in.readInt();List<BlockPos> pts=new ArrayList<>();for(int j=0;j<c;j++)pts.add(new BlockPos(in.readInt(),in.readInt(),in.readInt()));paths.put(n,new Path(n,p,pts,v));}}catch(IOException ignored){}}
    private void save(){Path f=server.getSavePath(WorldSavePath.ROOT).resolve(FILE);try(DataOutputStream out=new DataOutputStream(Files.newOutputStream(f))){out.writeInt(paths.size());for(Path p:paths.values()){out.writeUTF(p.name);out.writeUTF(p.particle);out.writeBoolean(p.visible);out.writeInt(p.points.size());for(BlockPos b:p.points){out.writeInt(b.getX());out.writeInt(b.getY());out.writeInt(b.getZ());}}}catch(IOException e){e.printStackTrace();}}
}
