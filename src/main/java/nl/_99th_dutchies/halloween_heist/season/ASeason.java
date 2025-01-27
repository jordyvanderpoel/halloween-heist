package nl._99th_dutchies.halloween_heist.season;

import nl._99th_dutchies.halloween_heist.HalloweenHeistPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.text.MessageFormat;
import java.util.Random;

public abstract class ASeason {
    protected final HalloweenHeistPlugin plugin;

    public ASeason(HalloweenHeistPlugin plugin) {
        this.plugin = plugin;
    }

    public Material getHeistObjectSpawncontainer() {
        return null;
    }

    public EntityType getHeistObjectSpawnentity() {
        return null;
    }

    public abstract Material getHeistObjectMaterial();
    public abstract String getHeistObjectName();

    public abstract void spawnHeistObject(boolean isRespawn);

    public void sendWinnerMessage(Player winner) {
        if(winner == null) {
            for(Player p : this.plugin.getServer().getOnlinePlayers()) {
                p.sendTitle(
                        MessageFormat.format("{0}Happy Halloween!", ChatColor.GOLD),
                        MessageFormat.format("{0}Technically, there was no winner.", ChatColor.GRAY),
                        10,
                        100,
                        20);
            }

            Bukkit.broadcastMessage(MessageFormat.format("{0}So, since nobody properly obtained the " + this.getHeistObjectName() + "{0}, no one really won the heist this year.", ChatColor.BLUE));
        } else {
            for(Player p : this.plugin.getServer().getOnlinePlayers()) {
                p.sendTitle(
                        MessageFormat.format("{0}Happy Halloween!", ChatColor.GOLD),
                        MessageFormat.format("{0}{1}{2} is an amazing player / genius.", ChatColor.BLUE, ChatColor.BOLD, winner.getDisplayName()),
                        10,
                        100,
                        20);
            }

            Bukkit.broadcastMessage(MessageFormat.format("{0}{1}ATTENTION, EVERYONE!", ChatColor.BLUE, ChatColor.BOLD));
            Bukkit.broadcastMessage(MessageFormat.format("{0}{1}{2} is an amazing player / genius.", ChatColor.BLUE, ChatColor.BOLD, winner.getDisplayName()));
        }
    }

    protected Location generateLocation() {
        return this.generateLocation(true, true);
    }

    protected Location generateLocation(boolean allowUnderground, boolean allowInWater) {
        int itemOffset = this.plugin.config.getInt("itemOffset");
        int worldDimensions = this.plugin.config.getBoolean("worldBorder.shrinking") ? 200 : this.plugin.config.getInt("worldDimensions");

        World world = this.plugin.mainWorld;
        int dropX;
        int dropY;
        int dropZ;
        Block block;

        do {
            Random rand = new Random();

            dropX = (rand.nextInt(worldDimensions - itemOffset) + itemOffset) * (rand.nextBoolean() ? 1 : -1);
            dropZ = (rand.nextInt(worldDimensions - itemOffset) + itemOffset) * (rand.nextBoolean() ? 1 : -1);

            dropY = world.getHighestBlockYAt(dropX, dropZ);
            if (allowUnderground) {
                dropY = rand.nextInt(dropY - 4) + 5;
            } else {
                dropY += 1; // To prevent spawning in top layer
            }

            block = world.getBlockAt(dropX, dropY -1, dropZ);

            System.out.println("Block at [" + dropX + "," + (dropY-1) + "," + dropZ + "] is " + block.getType().name() + " and is " + (block.isLiquid() ? "" : "not ") + "liquid");
        } while(!allowInWater && block.isLiquid());

        return new Location(world, dropX, dropY, dropZ);
    }

    protected void heistObjectSpawned(InventoryHolder i, Location l) {
        this.plugin.heistObjectLocation.resetPlayer();
        this.plugin.heistObjectLocation.updateInventoryHolder(i);

        this.plugin.heistState.set("itemLoaded", true);
        System.out.println("Dropped " + this.getHeistObjectName() + " at [" + l.getX() + "," + l.getY() + "," + l.getZ() + "]");
    }

    public boolean isHeistObject(ItemStack itemStack) {
        if (!itemStack.getType().equals(this.getHeistObjectMaterial())) {
            return false;
        }
        if (itemStack.getItemMeta() == null) {
            return false;
        }
        if (!itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "isHeistObject"), PersistentDataType.INTEGER)) {
            return false;
        }
        if (itemStack.getItemMeta().getPersistentDataContainer().getOrDefault(new NamespacedKey(plugin, "isHeistObject"), PersistentDataType.INTEGER, 0) != 1) {
           return false;
        }

        return true;
    }
}
