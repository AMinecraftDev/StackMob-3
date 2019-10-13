package uk.antiperson.stackmob.events;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

public class StackDropLootEvent extends Event implements Cancellable {

    private static HandlerList HANDLERS = new HandlerList();

    private boolean cancelled = false;
    private EntityType entityType;
    private Location location;
    private ItemStack drop;

    public StackDropLootEvent(ItemStack drops, Location location, EntityType entityType) {
        this.location = location;
        this.drop = drops;
        this.entityType = entityType;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public ItemStack getDrop() {
        return drop;
    }

    public Location getLocation() {
        return location;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
