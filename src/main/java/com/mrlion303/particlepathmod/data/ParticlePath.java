package com.mrlion303.particlepathmod.data;
import net.minecraft.core.BlockPos;
import java.util.ArrayList;
import java.util.List;
public class ParticlePath {
    private final String name;
    private final List<BlockPos> points;
    private String particle;
    private boolean visible;
    public ParticlePath(String name, String particle, List<BlockPos> points, boolean visible) {
        this.name=name; this.particle=particle; this.points=new ArrayList<>(points); this.visible=visible;
    }
    public String getName(){return name;} public List<BlockPos> getPoints(){return points;}
    public String getParticle(){return particle;} public boolean isVisible(){return visible;}
    public void setVisible(boolean value){visible=value;} public void addPoint(BlockPos pos){points.add(pos.immutable());}
}
