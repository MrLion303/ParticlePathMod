package com.mrlion303.particlepathmod.command;
import net.minecraft.core.BlockPos;
import java.util.*;
public final class PathSelectionManager {
    private static final Map<UUID,List<BlockPos>> selections=new HashMap<>();
    private PathSelectionManager(){}
    public static void reset(UUID id,BlockPos pos){selections.put(id,new ArrayList<>(List.of(pos.immutable())));}
    public static void append(UUID id,BlockPos pos){selections.computeIfAbsent(id,k->new ArrayList<>()).add(pos.immutable());}
    public static List<BlockPos> get(UUID id){return selections.getOrDefault(id,List.of());}
    public static List<BlockPos> consumeSelection(UUID id){List<BlockPos> p=new ArrayList<>(get(id));selections.remove(id);return p;}
    public static void clear(UUID id){selections.remove(id);}
}
