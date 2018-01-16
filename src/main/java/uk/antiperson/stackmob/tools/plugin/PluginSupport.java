package uk.antiperson.stackmob.tools.plugin;

import com.kirelcodes.miniaturepets.api.APIUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import uk.antiperson.stackmob.StackMob;
import uk.antiperson.stackmob.tools.extras.GlobalValues;

/**
 * This could do with being completely redone.
 */
public class PluginSupport {

    private StackMob sm;
    private int wgVersion;
    private MythicSupport mythicSupport;
    private WorldGuardSupport worldGuardSupport;
    private ProtocolSupport protocolSupport;
    public PluginSupport(StackMob sm){
        this.sm = sm;
    }

    public void setupWorldGuard(){
        Plugin pl = Bukkit.getPluginManager().getPlugin("WorldGuard");
        if(pl != null){
            worldGuardSupport = new WorldGuardSupport(sm);
            wgVersion = Integer.valueOf(pl.getDescription().getVersion().replaceAll("\\D+",""));
        }
    }

    public void startup(){
        Plugin pl = Bukkit.getPluginManager().getPlugin("ProtocolLib");
        if(pl != null && pl.isEnabled()){
            protocolSupport = new ProtocolSupport(sm);
        }
        Plugin pl2 = Bukkit.getPluginManager().getPlugin("MythicMobs");
        if(sm.config.getCustomConfig().getBoolean("mythicmobs.enabled")){
            if(pl2 != null && pl2.isEnabled()){
                mythicSupport = new MythicSupport(sm);
            }
        }
    }

    public void setMcmmoMetadata(Entity entity){
        if(sm.config.getCustomConfig().getBoolean("mcmmo.no-experience.enabled") && sm.getServer().getPluginManager().getPlugin("mcMMO") != null){
            if(!sm.config.getCustomConfig().getStringList("mcmmo.no-experience.blacklist")
                    .contains(entity.getType().toString()) && sm.getServer().getPluginManager().isPluginEnabled("mcMMO")){
                entity.setMetadata(GlobalValues.MCMMO_META, new FixedMetadataValue(sm.getServer().getPluginManager().getPlugin("mcMMO"), false));
            }
        }
    }

    public boolean isMiniPet(Entity entity){
        if(sm.config.getCustomConfig().getBoolean("check.is-miniature-pet") && sm.getServer().getPluginManager().getPlugin("MiniaturePets") != null){
            if(sm.getServer().getPluginManager().isPluginEnabled("MiniaturePets")){
                return APIUtils.isEntityMob(entity);
            }
        }
        return false;
    }

    public MythicSupport getMythicSupport(){
        return mythicSupport;
    }

    public ProtocolSupport getProtocolSupport(){
        return protocolSupport;
    }

    public WorldGuardSupport getWorldGuard(){
        return worldGuardSupport;
    }

    public boolean isProtocolSupportEnabled(){
        return getProtocolSupport() != null;
    }

    public boolean isWorldGuardEnabled(){
        return getWorldGuard() != null && getWorldGuardVersion() > 620;
    }

    public int getWorldGuardVersion(){
        return wgVersion;
    }
}
