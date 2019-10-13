package uk.antiperson.stackmob.tools;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.events.StackDropLootEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Class to calculate the correct amount of entity drops.
 */
public class DropTools {

    private StackMob sm;
    public DropTools(StackMob sm){
        this.sm = sm;
    }

    public void calculateDrops(List<ItemStack> drops, int multiplier, LivingEntity dead, ItemStack itemInHand){
        for(ItemStack itemStack : drops){
            if(itemStack == null){
                continue;
            }

            if(dropIsArmor(dead, itemStack)){
                continue;
            }

            if(!sm.config.getCustomConfig().getStringList("multiply-drops.drops-whitelist")
                    .contains(itemStack.getType().toString())){
                continue;
            }
            if(sm.config.getCustomConfig().isInt("multiply-drops.entity-limit")){
                if(multiplier > sm.config.getCustomConfig().getInt("multiply-drops.entity-limit")){
                    multiplier = sm.config.getCustomConfig().getInt("multiply-drops.entity-limit");
                }
            }
            if(sm.config.getCustomConfig().getStringList("multiply-drops.drop-one-per")
                    .contains(itemStack.getType().toString())){
                dropDrops(itemStack, multiplier, dead.getLocation(), dead.getType());
                continue;
            }
            if(itemInHand != null && itemInHand.getEnchantments().containsKey(Enchantment.LOOT_BONUS_MOBS)) {
                double enchantmentTimes = 1 + itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS) * 0.5;
                dropDrops(itemStack, (int) Math.round(calculateAmount(multiplier) * enchantmentTimes), dead.getLocation(), dead.getType());
                continue;
            }
            dropDrops(itemStack, calculateAmount(multiplier), dead.getLocation(), dead.getType());
        }
    }

    // Calculate a random drop amount.
    public int calculateAmount(int multiplier){
        return (int) Math.round((0.75 + ThreadLocalRandom.current().nextDouble(2)) * multiplier);
    }

    public void dropDrops(ItemStack drop, int amount, Location dropLocation, EntityType entityType){
        dropAllDrops(drop, amount, dropLocation, false, entityType);
    }

    public void dropEggs(ItemStack drop, int amount, Location dropLocation, EntityType entityType){
        dropAllDrops(drop, amount, dropLocation, true, entityType);
    }

    // Method to drop the correct amount of drops.
    private void dropAllDrops(ItemStack drop, int amount, Location dropLocation, boolean addEnchantment, EntityType entityType){
        double inStacks = (double) amount / (double) drop.getMaxStackSize();
        double floor = Math.floor(inStacks);
        double leftOver = inStacks - floor;
        for(int i = 1; i <= floor; i++){
            ItemStack newStack = drop.clone();
            newStack.setAmount(drop.getMaxStackSize());
            if(addEnchantment){
                newStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            }

            StackDropLootEvent stackDropLootEvent = new StackDropLootEvent(newStack, dropLocation, entityType);
            Bukkit.getPluginManager().callEvent(stackDropLootEvent);

            if(stackDropLootEvent.isCancelled()) return;

            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
        if(leftOver > 0){
            ItemStack newStack = drop.clone();
            newStack.setAmount((int) Math.round(leftOver * drop.getMaxStackSize()));
            if(addEnchantment){
                newStack.addUnsafeEnchantment(Enchantment.DIG_SPEED, 1);
            }

            StackDropLootEvent stackDropLootEvent = new StackDropLootEvent(newStack, dropLocation, entityType);
            Bukkit.getPluginManager().callEvent(stackDropLootEvent);

            if(stackDropLootEvent.isCancelled()) return;

            dropLocation.getWorld().dropItemNaturally(dropLocation, newStack);
        }
    }

    private boolean dropIsArmor(LivingEntity entity, ItemStack drop){
        if(sm.getVersionId() > 1){
            if(entity.getEquipment().getItemInMainHand().equals(drop) || entity.getEquipment().getItemInOffHand().equals(drop)){
                return true;
            }
        }else {
            if(entity.getEquipment().getItemInHand().equals(drop)){
                return true;
            }
        }
        for(ItemStack itemStack : entity.getEquipment().getArmorContents()){
            if(itemStack.equals(drop)){
                return true;
            }
        }
        return false;
    }
}
