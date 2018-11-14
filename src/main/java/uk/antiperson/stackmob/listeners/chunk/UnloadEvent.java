package uk.antiperson.stackmob.listeners.chunk;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.StackTools;

public class UnloadEvent implements Listener {

    private StackMob sm;

    public UnloadEvent(StackMob sm) {
        this.sm = sm;
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent e) {
        if(sm.getCustomConfig().getStringList("no-stack-worlds")
                .contains(e.getWorld().getName())){
            return;
        }
        for(Entity currentEntity : e.getChunk().getEntities()){
            // Check if entity is a mob, since they despawn on chunk unload.
            if(currentEntity instanceof Monster){
                continue;
            }
            // Add to storage
            if(StackTools.hasValidData(currentEntity)){
                int stackSize = StackTools.getSize(currentEntity);
                StackTools.removeSize(currentEntity);
                if(sm.getCustomConfig().getBoolean("remove-chunk-unload")){
                    currentEntity.remove();
                    return;
                }
                sm.getCache().put(currentEntity.getUniqueId(), stackSize);
            }
        }
    }

}
