package com.mrlion303.particlepathmod.command;
import net.minecraft.core.BlockPos;
import java.util.*;
public final class PathSelectionManager {
    private static final Map<UUID,List<BlockPos>> selections=new HashMap<>();
    private PathSelectionManager(){}
    public static void reset(UUID id,BlockPos pos){selections.put(id,new ArrayList<>(List.of(pos.immutable())));}
    public static boolean append(UUID id,BlockPos pos){List<BlockPos> points=selections.get(id);if(points==null)return false;points.add(pos.immutable());return true;}
    public static List<BlockPos> get(UUID id){return selections.getOrDefault(id,List.of());}
    public static List<BlockPos> consumeSelection(UUID id){List<BlockPos> p=new ArrayList<>(get(id));selections.remove(id);return p;}
    public static void clear(UUID id){selections.remove(id);}
}
