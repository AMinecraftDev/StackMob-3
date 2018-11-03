package uk.antiperson.stackmob.listeners.entity;

import org.bukkit.Statistic;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.entity.DeathType;
import uk.antiperson.stackmob.tools.extras.GlobalValues;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class DeathEvent implements Listener {

    private StackMob sm;

    public DeathEvent(StackMob sm) {
        this.sm = sm;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(EntityDeathEvent e) {
        LivingEntity dead = e.getEntity();
        if(!(sm.getStackTools().hasValidStackData(dead))){
            return;
        }

        int oldSize = sm.getStackTools().getSize(dead);
        int subtractAmount = 1;

        if(!dead.hasMetadata(GlobalValues.KILL_ONE_OFF)){
            if(isAllowed(DeathType.KILL_ALL, dead)){
                multiplication(dead, e.getDrops(), oldSize - 1, e.getDroppedExp());
                spawnNewEntity(oldSize, oldSize, dead);
                return;
            }
            if(isAllowed(DeathType.KILL_STEP, dead)) {
                int maxStep = sm.getCustomConfig().getInt("kill-step.max-step");
                int randomStep = ThreadLocalRandom.current().nextInt(1, maxStep);
                if (randomStep >= oldSize) {
                    subtractAmount = oldSize;
                } else {
                    subtractAmount = randomStep;
                }
                multiplication(dead, e.getDrops(), subtractAmount - 1, e.getDroppedExp());
                spawnNewEntity(oldSize, subtractAmount, dead);
                return;
            }
            if(isAllowed(DeathType.KILL_STEP_DAMAGE, dead)){
                double leftOverDamage = dead.getMetadata(GlobalValues.LEFTOVER_DAMAGE).get(0).asDouble();
                double maxHealth = dead.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double damageDivided = leftOverDamage / maxHealth;
                int killStep = (int) Math.floor(damageDivided);
                if(killStep > 1){
                    multiplication(dead, e.getDrops(), killStep - 1, e.getDroppedExp());
                }
                LivingEntity newEntity = (LivingEntity) spawnNewEntity(oldSize, killStep + 1, dead);
                if(newEntity != null){
                    double damageToDeal = (damageDivided - killStep) * maxHealth;
                    newEntity.setHealth(newEntity.getHealth() - damageToDeal);
                }
                return;
            }
        }
        spawnNewEntity(oldSize, subtractAmount, dead);
    }

    private void multiplication(LivingEntity dead, List<ItemStack> drops, int subtractAmount, int originalExperience){
        if(sm.getCustomConfig().getBoolean("multiply-drops.enabled")){
            if(dead.getKiller() != null){
                sm.dropTools.calculateDrops(drops, subtractAmount, dead, dead.getKiller().getInventory().getItemInMainHand());
            }else{
                sm.dropTools.calculateDrops(drops, subtractAmount, dead, null);
            }
        }
        if(sm.getCustomConfig().getBoolean("multiply-exp.enabled")){
            // double newExperience = subtractAmount * (originalExperience * sm.config.getCustomConfig().getDouble("multiply-exp-scaling", 1.0));
            ExperienceOrb exp = (ExperienceOrb) dead.getWorld().spawnEntity(dead.getLocation(), EntityType.EXPERIENCE_ORB);
            exp.setExperience(sm.expTools.multiplyExperience(originalExperience, subtractAmount));
        }
        if(sm.getCustomConfig().getBoolean("increase-player-stats")){
            if(dead.getKiller() != null){
                int oldStat = dead.getKiller().getStatistic(Statistic.MOB_KILLS);
                dead.getKiller().setStatistic(Statistic.MOB_KILLS, oldStat + subtractAmount);
            }
        }
    }

    private Entity spawnNewEntity(int oldSize, int subtractAmount, LivingEntity dead){
        dead.removeMetadata(GlobalValues.NO_STACK_ALL, sm);
        dead.removeMetadata(GlobalValues.CURRENTLY_BREEDING, sm);
        dead.removeMetadata(GlobalValues.KILL_ONE_OFF, sm);
        dead.removeMetadata(GlobalValues.LEFTOVER_DAMAGE, sm);
        if(oldSize != subtractAmount){
            Entity newe = sm.getTools().duplicate(dead);
            sm.getStackTools().setSize(newe,oldSize - subtractAmount);
            return newe;
        }
        return null;
    }

    private boolean isAllowed(DeathType dt, LivingEntity dead){
        String type = dt.getType();
        if(!sm.getCustomConfig().getBoolean(type + ".enabled")){
            return false;
        }
        if(sm.getCustomConfig().getBoolean("death-type-permission")){
            if(dead.getKiller() != null){
                if(!(dead.getKiller().hasPermission("stackmob." + type))){
                    return false;
                }
            }
        }
        if (sm.getCustomConfig().getStringList(type + ".reason-blacklist")
                .contains(dead.getLastDamageCause().getCause().toString())){
            return false;
        }
        return !(sm.getCustomConfig().getStringList(type + ".type-blacklist")
                .contains(dead.getType().toString()));
    }
}
