package com.mrlion303.particlepathmod.data;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.*;
public class ParticlePathSavedData extends SavedData {
    public static final String DATA_NAME="particlepathmod_paths";
    private final Map<String,ParticlePath> paths=new LinkedHashMap<>();
    public static Factory<ParticlePathSavedData> factory(){return new Factory<>(ParticlePathSavedData::new,ParticlePathSavedData::load,null);}
    public static ParticlePathSavedData load(CompoundTag tag){
        ParticlePathSavedData d=new ParticlePathSavedData();
        ListTag list=tag.getList("Paths",Tag.TAG_COMPOUND);
        for(int i=0;i<list.size();i++){
            CompoundTag t=list.getCompound(i); String name=t.getString("Name"); String particle=t.getString("Particle");
            List<BlockPos> pts=new ArrayList<>(); ListTag pl=t.getList("Points",Tag.TAG_COMPOUND);
            for(int p=0;p<pl.size();p++){CompoundTag q=pl.getCompound(p);pts.add(new BlockPos(q.getInt("X"),q.getInt("Y"),q.getInt("Z")));}
            if(!name.isBlank()&&!pts.isEmpty())d.paths.put(name,new ParticlePath(name,particle,pts,t.getBoolean("Visible")));
        } return d;
    }
    public CompoundTag save(CompoundTag tag){
        ListTag list=new ListTag();
        for(ParticlePath p:paths.values()){
            CompoundTag t=new CompoundTag();t.putString("Name",p.getName());t.putString("Particle",p.getParticle());t.putBoolean("Visible",p.isVisible());
            ListTag pl=new ListTag(); for(BlockPos pos:p.getPoints()){CompoundTag q=new CompoundTag();q.putInt("X",pos.getX());q.putInt("Y",pos.getY());q.putInt("Z",pos.getZ());pl.add(q);}
            t.put("Points",pl);list.add(t);
        } tag.put("Paths",list);return tag;
    }
    public Map<String,ParticlePath> getPaths(){return paths;}
    public ParticlePath get(String name){return paths.get(name);}
    public boolean contains(String name){return paths.containsKey(name);}
    public void put(ParticlePath p){paths.put(p.getName(),p);setDirty();}
    public ParticlePath remove(String name){ParticlePath p=paths.remove(name);if(p!=null)setDirty();return p;}
}
