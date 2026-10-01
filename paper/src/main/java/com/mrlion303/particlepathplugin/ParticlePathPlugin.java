package com.mrlion303.particlepathplugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class ParticlePathPlugin extends JavaPlugin implements Listener, TabExecutor {
    private static final double SPACING=.5D;
    private final Map<UUID,List<Location>> selections=new HashMap<>();
    private final Map<String,Path> paths=new LinkedHashMap<>();
    private File dataFile;

    private static class Path {
        String name,particle; List<Location> points; boolean visible;
        Path(String n,String p,List<Location> pts,boolean v){name=n;particle=p;points=new ArrayList<>(pts);visible=v;}
    }

    @Override public void onEnable(){
        dataFile=new File(getDataFolder(),"paths.yml"); getDataFolder().mkdirs(); load();
        getServer().getPluginManager().registerEvents(this,this);
        Objects.requireNonNull(getCommand("particlepath")).setExecutor(this);
        Objects.requireNonNull(getCommand("particlepath")).setTabCompleter(this);
        getServer().getScheduler().runTaskTimer(this,this::tick,2L,2L);
    }

    @EventHandler public void interact(PlayerInteractEvent e){
        if(e.getHand()!=EquipmentSlot.HAND||!e.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.STICK))return;
        if(e.getAction()==Action.LEFT_CLICK_BLOCK){
            Block b=e.getClickedBlock(); if(b==null)return;
            Location l=b.getLocation().add(.5,.5,.5); selections.put(e.getPlayer().getUniqueId(),new ArrayList<>(List.of(l)));
            msg(e.getPlayer(),"Punto A seleccionado: "+b.getX()+", "+b.getY()+", "+b.getZ(),ChatColor.GOLD); e.setCancelled(true);
        } else if(e.getAction()==Action.RIGHT_CLICK_BLOCK){
            Block b=e.getClickedBlock(); if(b==null)return;
            List<Location> s=selections.get(e.getPlayer().getUniqueId());
            if(s==null){msg(e.getPlayer(),"Primero selecciona el punto A con clic izquierdo.",ChatColor.RED);e.setCancelled(true);return;}
            s.add(b.getLocation().add(.5,.5,.5)); int n=s.size();String label=n<=26?String.valueOf((char)('A'+n-1)):"P"+n;
            msg(e.getPlayer(),"Punto "+label+" seleccionado: "+b.getX()+", "+b.getY()+", "+b.getZ(),ChatColor.GOLD);e.setCancelled(true);
        }
    }

    @Override public boolean onCommand(CommandSender s,Command cmd,String label,String[] a){
        if(!(s instanceof Player p)){s.sendMessage(ChatColor.RED+"Este comando debe ejecutarse como jugador.");return true;}
        if(!p.hasPermission("particlepath.use")){s.sendMessage(ChatColor.RED+"Sin permiso.");return true;}
        if(a.length==0){help(p);return true;}
        switch(a[0].toLowerCase(Locale.ROOT)){
            case "create": return create(p,a);
            case "modify": return modify(p,a);
            case "show": return visibility(p,a,true);
            case "hide": return visibility(p,a,false);
            case "remove": return remove(p,a);
            case "list": msg(p,paths.isEmpty()?"No hay caminos creados.":"Caminos: "+String.join(", ",paths.keySet()),ChatColor.GOLD); return true;
            default: help(p); return true;
        }
    }

    private boolean create(Player p,String[] a){
        if(a.length<3){msg(p,"Uso: /particlepath create <nombre> <partícula>",ChatColor.RED);return true;}
        if(paths.containsKey(a[1])){msg(p,"Ya existe el camino '"+a[1]+"'.",ChatColor.RED);return true;}
        List<Location> sel=selections.getOrDefault(p.getUniqueId(),List.of());if(sel.size()<2){msg(p,"Marca A y al menos B con el palo.",ChatColor.RED);return true;}
        String spec=String.join(" ",Arrays.copyOfRange(a,2,a.length)); if(parse(spec)==null){msg(p,"Partícula inválida: "+spec,ChatColor.RED);return true;}
        paths.put(a[1],new Path(a[1],spec,new ArrayList<>(sel),false));selections.remove(p.getUniqueId());save();msg(p,"Camino '"+a[1]+"' creado con "+sel.size()+" puntos.",ChatColor.GOLD);return true;
    }

    private boolean modify(Player p,String[] a){
        if(a.length<3){msg(p,"Uso: /particlepath modify <nombre> <partícula>",ChatColor.RED);return true;}
        Path x=paths.get(a[1]);if(x==null){msg(p,"No existe el camino '"+a[1]+"'.",ChatColor.RED);return true;}
        String spec=String.join(" ",Arrays.copyOfRange(a,2,a.length));if(parse(spec)==null){msg(p,"Partícula inválida: "+spec,ChatColor.RED);return true;}
        x.particle=spec;save();msg(p,"Partícula del camino '"+a[1]+"' cambiada.",ChatColor.GOLD);return true;
    }

    private boolean visibility(Player p,String[] a,boolean v){
        if(a.length<2){msg(p,"Uso: /particlepath "+(v?"show":"hide")+" <nombre>",ChatColor.RED);return true;}
        Path x=paths.get(a[1]);if(x==null){msg(p,"No existe el camino '"+a[1]+"'.",ChatColor.RED);return true;}
        x.visible=v;save();msg(p,"Camino '"+a[1]+"' "+(v?"mostrado.":"ocultado."),ChatColor.GOLD);return true;
    }

    private boolean remove(Player p,String[] a){if(a.length<2){msg(p,"Uso: /particlepath remove <nombre>",ChatColor.RED);return true;}if(paths.remove(a[1])==null){msg(p,"No existe el camino '"+a[1]+"'.",ChatColor.RED);return true;}save();msg(p,"Camino '"+a[1]+"' eliminado.",ChatColor.GOLD);return true;}
    private void help(Player p){p.sendMessage(ChatColor.GOLD+"/particlepath create <nombre> <partícula>");p.sendMessage(ChatColor.GOLD+"/particlepath modify <nombre> <partícula>");p.sendMessage(ChatColor.GOLD+"/particlepath show <nombre>");p.sendMessage(ChatColor.GOLD+"/particlepath hide <nombre>");p.sendMessage(ChatColor.GOLD+"/particlepath remove <nombre>");p.sendMessage(ChatColor.GOLD+"/particlepath list");}

    private void msg(Player p,String m,ChatColor c){p.sendMessage(c+m);}

    @Override public List<String> onTabComplete(CommandSender s,Command cmd,String label,String[] a){
        if(a.length==1)return filter(List.of("create","modify","show","hide","remove","list"),a[0]);
        if(a[0].equalsIgnoreCase("show")||a[0].equalsIgnoreCase("hide")||a[0].equalsIgnoreCase("remove")||(a[0].equalsIgnoreCase("modify")&&a.length==2))return filter(new ArrayList<>(paths.keySet()),a[a.length-1]);
        if(a[0].equalsIgnoreCase("create")&&a.length==3||a[0].equalsIgnoreCase("modify")&&a.length==3)return filter(particleNames(),a[2]);
        return Collections.emptyList();
    }

    private List<String> particleNames(){List<String> r=new ArrayList<>();for(Particle p:Particle.values())r.add("minecraft:"+p.name().toLowerCase(Locale.ROOT));return r;}
    private List<String> filter(List<String> all,String prefix){List<String> r=new ArrayList<>();for(String x:all)if(x.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT)))r.add(x);return r;}

    private org.bukkit.Particle parse(String spec){
        String id=spec.trim().split("\\s+")[0];String n=id.substring(id.indexOf(':')+1).toUpperCase(Locale.ROOT);
        try{return Particle.valueOf(n);}catch(Exception e){return null;}
    }

    private void tick(){for(Path p:paths.values()){if(!p.visible||p.points.size()<2)continue;Particle particle=parse(p.particle);if(particle==null)continue;for(int i=0;i<p.points.size()-1;i++)spawn(p.points.get(i),p.points.get(i+1),particle);}}
    private void spawn(Location a,Location b,Particle p){Vector d=b.toVector().subtract(a.toVector());double len=d.length();int n=Math.max(1,(int)Math.ceil(len/SPACING));for(int i=0;i<=n;i++){double t=(double)i/n;a.getWorld().spawnParticle(p,a.getX()+d.getX()*t,a.getY()+d.getY()*t,a.getZ()+d.getZ()*t,1,0,0,0,0);}}

    private void save(){YamlConfiguration y=new YamlConfiguration();for(Path p:paths.values()){String k="paths."+p.name;y.set(k+".particle",p.particle);y.set(k+".visible",p.visible);List<Map<String,Integer>> pts=new ArrayList<>();for(Location l:p.points)pts.add(Map.of("x",l.getBlockX(),"y",l.getBlockY(),"z",l.getBlockZ()));y.set(k+".points",pts);}try{y.save(dataFile);}catch(Exception e){getLogger().warning("No se pudo guardar paths.yml: "+e.getMessage());}}
    private void load(){if(!dataFile.exists())return;YamlConfiguration y=YamlConfiguration.loadConfiguration(dataFile);ConfigurationSection root=y.getConfigurationSection("paths");if(root==null)return;for(String n:root.getKeys(false)){String p=y.getString("paths."+n+".particle","minecraft:flame");boolean v=y.getBoolean("paths."+n+".visible",false);List<Location> pts=new ArrayList<>();for(Map<?,?> m:y.getMapList("paths."+n+".points")){World w=Bukkit.getWorlds().get(0);pts.add(new Location(w,((Number)m.get("x")).doubleValue()+.5,((Number)m.get("y")).doubleValue()+.5,((Number)m.get("z")).doubleValue()+.5));}paths.put(n,new Path(n,p,pts,v));}}
}
