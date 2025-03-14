package icu.cykuta.beaconshield.beacon;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.config.FileHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.DataKeys;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.BeaconDataManager;
import icu.cykuta.beaconshield.data.UpgradeHandler;
import icu.cykuta.beaconshield.gui.views.BeaconGUI;
import icu.cykuta.beaconshield.upgrade.Upgrade;
import icu.cykuta.beaconshield.utils.FileUtils;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;

import static icu.cykuta.beaconshield.data.DataKeys.IS_BEACONSHIELD;

public class BeaconShieldBlock implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final int x, y, z;
    private final String world;
    private final ArrayList<ProtectedChunk> protectedChunks;
    private final ProtectedChunk coreChunk;
    private final Map<UUID, PlayerRole> allowedPlayers;
    private int fuelLevel;
    private transient PersistentDataContainer pdc;

    public BeaconShieldBlock(@NotNull Block block, Player owner) {
        this.id = UUID.randomUUID().toString();
        this.x = block.getX();
        this.y = block.getY();
        this.z = block.getZ();
        this.world = block.getWorld().getName();
        this.coreChunk = new ProtectedChunk(block.getChunk());
        this.protectedChunks = new ArrayList<>();
        this.allowedPlayers = new HashMap<>();
        this.fuelLevel = -1;
        this.addAllowedPlayer(owner, PlayerRole.OWNER);
        this.pdc = new CustomBlockData(block, BeaconShield.getPlugin());
    }

    public void place() {
        Chunk coreChunk = this.getBlock().getChunk();
        addProtectedChunk(coreChunk);
    }

    public void register() {
        // Add all protected chunks to the protection handler
        for (ProtectedChunk protectedChunk : protectedChunks) {
            World world = Bukkit.getWorld(this.world);
            if (world == null) {
                continue;
            }

            Chunk chunk = protectedChunk.toChunk();
            ProtectionHandler.addChunk(chunk, this);
        }
    }

    public void destroy() {
        // Remove all protected chunks from the protection handler
        for (ProtectedChunk protectedChunk : protectedChunks) {
            World world = Bukkit.getWorld(this.world);
            if (world == null) {
                continue;
            }

            Chunk chunk = protectedChunk.toChunk();
            ProtectionHandler.removeChunk(chunk);
        }

        // Remove the beacon from the data manager
        BeaconDataManager beaconDataManager = BeaconShield.getPlugin().getBeaconDataManager();
        beaconDataManager.removeBeaconShieldBlock(this);

        // Delete the file
        FileUtils.deleteBeaconFile(this);

        // Remove the inventory from the PDC
        this.pdc.remove(DataKeys.BEACONSHIELD_INVENTORY);
    }

    public void consumeFuel() {
        fuelLevel--;
    }

    public void save() {
        FileUtils.writeBeaconToFile(this);
    }

    public void addProtectedChunk(Chunk chunk) {
        ProtectedChunk protectedChunk = new ProtectedChunk(chunk);
        addProtectedChunk(protectedChunk);
    }

    public void addProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.add(protectedChunk); // Add the chunk to the list of protected chunks
        ProtectionHandler.addChunk(protectedChunk.toChunk(), this); // Add the chunk to the protection handler
    }

    public void removeProtectedChunk(Chunk chunk) {
        ProtectedChunk protectedChunk = new ProtectedChunk(chunk);
        removeProtectedChunk(protectedChunk);
    }

    public void removeProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.remove(protectedChunk); // Remove the chunk from the list of protected chunks
        ProtectionHandler.removeChunk(protectedChunk.toChunk()); // Remove the chunk from the protection handler
    }

    public List<ProtectedChunk> getProtectedChunks() {
        return protectedChunks;
    }

    public void setFuelLevel(int fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public boolean canProtect() {
        return fuelLevel != -1;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public void setOwner(@NotNull OfflinePlayer player) {
        setOwner(player.getUniqueId());
    }

    public void setOwner(UUID uuid) {
        // Remove the old owner
        allowedPlayers.entrySet().removeIf(entry -> entry.getValue().equals(PlayerRole.OWNER));

        // Add the new owner
        addAllowedPlayer(Bukkit.getOfflinePlayer(uuid), PlayerRole.OWNER);
    }

    public OfflinePlayer getOwner() throws NoSuchElementException {
        return Bukkit.getOfflinePlayer(
                allowedPlayers.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(PlayerRole.OWNER))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No owner found"))
                        .getKey()
        );
    }

    public void addAllowedPlayer(@NotNull OfflinePlayer player, PlayerRole role) {
        this.allowedPlayers.put(player.getUniqueId(), role);
    }

    public void removeAllowedPlayer(@NotNull OfflinePlayer player) {
        this.allowedPlayers.remove(player.getUniqueId());
    }

    public boolean isAllowedPlayer(@NotNull OfflinePlayer player) {
        return this.allowedPlayers.containsKey(player.getUniqueId());
    }

    public OfflinePlayer[] getAllowedPlayers() {
        List<OfflinePlayer> players = new ArrayList<>();
        for (UUID uuid : allowedPlayers.keySet()) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            players.add(player);
        }
        return players.toArray(new OfflinePlayer[0]);
    }

    public Block getBlock() {
        return this.getWorld().getBlockAt(x, y, z);
    }

    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    public String getId() {
        return id;
    }

    public ProtectedChunk getCoreChunk() {
        return coreChunk;
    }

    public boolean isCoreChunk(Chunk chunk) {
        return coreChunk.toChunk().equals(chunk);
    }

    public boolean isProtectedChunk(Chunk chunk) {
        return protectedChunks.stream().anyMatch(protectedChunk -> protectedChunk.toChunk().equals(chunk));
    }

    public Map<Integer, ItemStack> getStoredItemsFromPDC() {
        return this.pdc.getOrDefault(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), new HashMap<>());
    }

    public boolean hasInventoryPDC() {
        return this.pdc.has(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK));
    }

    public void setStoredItemsToPDC(Map<Integer, ItemStack> storedItems) {
        this.pdc.set(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), storedItems);
    }

    public void reinitializePDC() {
        this.pdc = new CustomBlockData(this.getBlock(), BeaconShield.getPlugin());
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        reinitializePDC();
    }

    /**
     * Check if the user has the required permission level.
     * @param player The player.
     * @param role The required role.
     * @return If the player has the required permission level.
     */
    public boolean hasPermissionLevel(OfflinePlayer player, PlayerRole role, boolean serverAdminBypass) {
        PlayerRole playerRole = getPlayerRole(player);

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null && onlinePlayer.hasPermission("beaconshield.bypass") && !serverAdminBypass) {
            return true;
        }

        return playerRole != null && playerRole.getPermissionLevel() >= role.getPermissionLevel();
    }

    public boolean hasPermissionLevel(OfflinePlayer player, PlayerRole role) {
        return hasPermissionLevel(player, role, false);
    }

    @Nullable
    public PlayerRole getPlayerRole(@NotNull OfflinePlayer player) {
        return allowedPlayers.get(player.getUniqueId());
    }

    public void setPlayerRole(@NotNull OfflinePlayer player, PlayerRole role) {
        allowedPlayers.put(player.getUniqueId(), role);
    }

    // STATIC METHODS

    /**
     * Get the BeaconShieldBlock from a block.
     * @param block The block.
     * @return The BeaconShieldBlock.
     */
    public static BeaconShieldBlock getBeaconShieldBlock(Block block) {
        BeaconDataManager beaconDataManager = BeaconShield.getPlugin().getBeaconDataManager();
        return beaconDataManager.getBeaconShieldBlock(block);
    }

    /**
     * Create a BeaconShield item.
     * @return The BeaconShield item.
     */
    public static @NotNull ItemStack createBeaconItem() {
        PluginConfiguration config = BeaconShield.getPlugin().getFileHandler().getConfig();

        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(Text.color(config.getString("beacon-name")));
        meta.setLore(config.getStringList("beacon-lore"));
        meta.getPersistentDataContainer().set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }
}
