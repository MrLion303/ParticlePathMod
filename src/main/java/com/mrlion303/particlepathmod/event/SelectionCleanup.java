package com.mrlion303.particlepathmod.event;
import com.mrlion303.particlepathmod.command.PathSelectionManager;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
public class SelectionCleanup {
    @SubscribeEvent public void logout(PlayerEvent.PlayerLoggedOutEvent e){PathSelectionManager.clear(e.getEntity().getUUID());}
}
