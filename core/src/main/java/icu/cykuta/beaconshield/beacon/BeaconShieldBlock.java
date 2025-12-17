package icu.cykuta.beaconshield.beacon;

import com.jeff_media.customblockdata.CustomBlockData;
import com.jeff_media.morepersistentdatatypes.DataType;
import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.DataKeys;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.data.BeaconHandler;
import icu.cykuta.beaconshield.utils.MathUtils;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static icu.cykuta.beaconshield.data.DataKeys.IS_BEACONSHIELD;

public class BeaconShieldBlock implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private final String id;
    private final int x, y, z;
    private final String world;
    private final List<ProtectedChunk> protectedChunks;
    private final ProtectedChunk coreChunk;
    private final Map<UUID, PlayerRole> allowedPlayers;
    private int fuelLevel;
    private transient BeaconPDCManager pdcManager;
    private final Map<RolePermission, PlayerRole> rolePermissions = new HashMap<>();

    /**
     * Constructor to create a new BeaconShieldBlock.
     *
     * @param block The block representing the BeaconShield.
     * @param owner The player who owns the BeaconShield.
     */
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
        this.pdcManager = new BeaconPDCManager(block);
        this.setDefaultPermissions();
    }

    /**
     * Set default permissions for the BeaconShieldBlock.
     * This method sets the default permissions for building, breaking, and using the BeaconShieldBlock
     */
    public void setDefaultPermissions() {
        this.rolePermissions.put(RolePermission.BUILD, PlayerRole.MEMBER);
        this.rolePermissions.put(RolePermission.BREAK, PlayerRole.MEMBER);
        this.rolePermissions.put(RolePermission.USE, PlayerRole.MEMBER);
        this.rolePermissions.put(RolePermission.ENTITY, PlayerRole.MEMBER);
        this.rolePermissions.put(RolePermission.BEACON_USE, PlayerRole.OFFICER);
    }

    /**
     * Decrease the fuel level by 1.
     */
    public void consumeFuel() {
        fuelLevel--;
    }

    /**
     * Add a protected chunk to the list of protected chunks.
     *
     * @param chunk The chunk to protect.
     */
    public void addProtectedChunk(Chunk chunk) {
        addProtectedChunk(new ProtectedChunk(chunk));
    }

    /**
     * Add a protected chunk to the list of protected chunks.
     *
     * @param protectedChunk The protected chunk.
     */
    public void addProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.add(protectedChunk);
        ProtectionHandler.addChunk(protectedChunk.toChunk(), this);
    }

    /**
     * Remove a protected chunk from the list of protected chunks.
     *
     * @param chunk The chunk to remove.
     */
    public void removeProtectedChunk(Chunk chunk) {
        removeProtectedChunk(new ProtectedChunk(chunk));
    }

    /**
     * Remove a protected chunk from the list of protected chunks.
     *
     * @param protectedChunk The protected chunk.
     */
    public void removeProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.remove(protectedChunk);
        ProtectionHandler.removeChunk(protectedChunk.toChunk());
    }

    /**
     * Get all protected chunks.
     *
     * @return The list of protected chunks.
     */
    public List<ProtectedChunk> getProtectedChunks() {
        return Collections.unmodifiableList(protectedChunks);
    }

    /**
     * Set the fuel level.
     *
     * @param fuelLevel The fuel level.
     */
    public void setFuelLevel(int fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    /**
     * Check if the beacon can protect, based on the fuel level.
     *
     * @return true if the beacon can protect, false otherwise.
     */
    public boolean canProtect() {
        return fuelLevel != -1;
    }

    /**
     * Get the fuel level.
     *
     * @return The fuel level.
     */
    public int getFuelLevel() {
        return fuelLevel;
    }

    /**
     * Set the owner of the beacon.
     *
     * @param player The owner player.
     */
    public void setOwner(@NotNull OfflinePlayer player) {
        setOwner(player.getUniqueId());
    }

    /**
     * Set the owner of the beacon.
     *
     * @param uuid The UUID of the owner player.
     */
    public void setOwner(UUID uuid) {
        allowedPlayers.entrySet().removeIf(entry -> entry.getValue().equals(PlayerRole.OWNER));
        addAllowedPlayer(Bukkit.getOfflinePlayer(uuid), PlayerRole.OWNER);
    }

    /**
     * Get the owner of the beacon.
     *
     * @return The owner of the beacon.
     * @throws NoSuchElementException If no owner is found.
     */
    public OfflinePlayer getOwner() throws NoSuchElementException {
        return Bukkit.getOfflinePlayer(
                allowedPlayers.entrySet().stream()
                        .filter(entry -> entry.getValue().equals(PlayerRole.OWNER))
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No owner found"))
                        .getKey()
        );
    }

    /**
     * Add a player to the list of allowed players.
     *
     * @param player The player.
     * @param role The role of the player.
     */
    public void addAllowedPlayer(@NotNull OfflinePlayer player, PlayerRole role) {
        this.allowedPlayers.put(player.getUniqueId(), role);
    }

    /**
     * Remove a player from the list of allowed players.
     *
     * @param player The player.
     */
    public void removeAllowedPlayer(@NotNull OfflinePlayer player) {
        this.allowedPlayers.remove(player.getUniqueId());
    }

    /**
     * Check if a player is allowed to perform a specific action based on their role and the required permission.
     *
     * @param player The player.
     * @return true if the player is allowed, false otherwise.
     */
    public boolean isAllowedPlayer(@NotNull RolePermission permission, @NotNull OfflinePlayer player) {
        if (player.isOp() || Objects.requireNonNull(player.getPlayer()).hasPermission("beaconshield.bypass")) {
            return true;
        }

        if (!hasMember(player)) {
            return false;
        }

        PlayerRole role = allowedPlayers.get(player.getUniqueId());
        if (role == null) {
            return false;
        }

        PlayerRole requiredRole = getMinimumRoleForPermission(permission);
        if (requiredRole == null) {
            return false;
        }

        return role.getPermissionLevel() >= requiredRole.getPermissionLevel();
    }

    /**
     * Check if a player is in the list of allowed players.
     *
     * @param player The player to check.
     * @return true if the player is a member, false otherwise.
     */
    public boolean hasMember(@NotNull OfflinePlayer player) {
        return this.allowedPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Get all allowed players.
     *
     * @return A list of allowed players.
     */
    public List<OfflinePlayer> getAllowedPlayers() {
        return allowedPlayers.keySet().stream()
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());
    }

    /**
     * Get the block associated with this BeaconShield.
     *
     * @return The block.
     */
    public Block getBlock() {
        return this.getWorld().getBlockAt(x, y, z);
    }

    /**
     * Get the world where the BeaconShield is located.
     *
     * @return The world.
     */
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * Get the unique ID of the BeaconShield.
     *
     * @return The ID.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the core chunk protected by the BeaconShield.
     *
     * @return The core chunk.
     */
    public ProtectedChunk getCoreChunk() {
        return coreChunk;
    }

    /**
     * Check if a chunk is the core chunk.
     *
     * @param chunk The chunk to check.
     * @return true if it is the core chunk, false otherwise.
     */
    public boolean isCoreChunk(Chunk chunk) {
        return coreChunk.toChunk().equals(chunk);
    }

    /**
     * Check if a chunk is protected by the BeaconShield.
     *
     * @param chunk The chunk to check.
     * @return true if it is protected, false otherwise.
     */
    public boolean isProtectedChunk(Chunk chunk) {
        return protectedChunks.stream().anyMatch(protectedChunk -> protectedChunk.toChunk().equals(chunk));
    }

    /**
     * Get the PDC manager for this BeaconShield.
     *
     * @return The PDC manager.
     */
    public BeaconPDCManager getPdcManager() {
        return pdcManager;
    }

    @Serial
    private void readObject(@NotNull ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.pdcManager = new BeaconPDCManager(this.getBlock());
    }

    /**
     * Check if a player has the required permission level.
     *
     * @param player The player.
     * @param role The required role.
     * @param serverAdminBypass Whether server admins can bypass permissions.
     * @return true if the player has the permission, false otherwise.
     */
    public boolean hasPermissionLevel(OfflinePlayer player, PlayerRole role, boolean serverAdminBypass) {
        PlayerRole playerRole = getPlayerRole(player);

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer != null && onlinePlayer.hasPermission("beaconshield.bypass") && !serverAdminBypass) {
            return true;
        }

        return playerRole != null && playerRole.getPermissionLevel() >= role.getPermissionLevel();
    }

    /**
     * Check if a player has the required permission level.
     *
     * @param player The player.
     * @param role The required role.
     * @return true if the player has the permission, false otherwise.
     */
    public boolean hasPermissionLevel(OfflinePlayer player, PlayerRole role) {
        return hasPermissionLevel(player, role, false);
    }

    /**
     * Get the role of a player.
     *
     * @param player The player.
     * @return The role of the player, or null if they have no role.
     */
    @Nullable
    public PlayerRole getPlayerRole(@NotNull OfflinePlayer player) {
        return allowedPlayers.get(player.getUniqueId());
    }

    /**
     * Set the role of a player.
     *
     * @param player The player.
     * @param role The role.
     */
    public void setPlayerRole(@NotNull OfflinePlayer player, PlayerRole role) {
        allowedPlayers.put(player.getUniqueId(), role);
    }

    /**
     * Inner class to manage PersistentDataContainer (PDC) operations.
     */
    @ApiStatus.Internal
    public static class BeaconPDCManager {
        private final PersistentDataContainer pdc;

        public BeaconPDCManager(@NotNull Block block) {
            this.pdc = new CustomBlockData(block, BeaconShield.getPlugin());
        }

        /**
         * Get the items stored in the PDC.
         *
         * @return A map of stored items.
         */
        public Map<Integer, ItemStack> getStoredItems() {
            return this.pdc.getOrDefault(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), new HashMap<>());
        }

        /**
         * Check if there are items stored in the PDC.
         *
         * @return true if there are stored items, false otherwise.
         */
        public boolean hasInventory() {
            return this.pdc.has(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK));
        }

        /**
         * Set the items stored in the PDC.
         *
         * @param storedItems The map of items to store.
         */
        public void setStoredItems(Map<Integer, ItemStack> storedItems) {
            this.pdc.set(DataKeys.BEACONSHIELD_INVENTORY, DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), storedItems);
        }

        /**
         * Remove the stored items from the PDC.
         */
        public void removeStoredItems() {
            this.pdc.remove(DataKeys.BEACONSHIELD_INVENTORY);
        }
    }

    /**
     * Get the burning time of the fuel.
     * @param fuel The fuel to get the burning time
     * @return The burning time of the fuel
     */
    public int getBurnTime(ItemStack fuel) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        List<Map<?, ?>> fuelList = config.getMapList("fuel-items");

        for (Map<?, ?> fuelEntry : fuelList) {
            String itemName = (String) fuelEntry.get("item");
            int burnTime = (int) fuelEntry.get("burn-time");
            int customModelData = (int) fuelEntry.get("custom-model-data"); // 0 if not present
            boolean useFormula = config.getBoolean("fuel-use-formula");
            String formula = config.getString("fuel-formula");

            if (useFormula) {
                burnTime = (int) MathUtils.eval(formula
                        .replace("%burn_time%", String.valueOf(burnTime))
                        .replace("%chunks_owned%", String.valueOf(this.protectedChunks.size()))
                );
            }

            // Add the namespace if it doesn't have one
            if (!itemName.startsWith("minecraft:")) {
                itemName = "minecraft:" + itemName;
            }

            // Compare the custom model data
            if (customModelData != 0) { // 0 if not present
                // fuel not have custom model data, continue
                // fuel have custom model data, but not equal to the custom model data in the config, continue
                if (fuel.getItemMeta() == null || fuel.getItemMeta().getCustomModelData() != customModelData) {
                    continue;
                }
            }

            // Get the material
            Material material = Material.matchMaterial(itemName);
            if (material == null) {
                continue;
            }

            // Check if the fuel is the same as the material
            if (fuel.getType() == material) {
                return burnTime;
            }
        }

        return 0;
    }

    /**
     * Get the role permissions for this BeaconShieldBlock.
     *
     * @return A map of role permissions.
     */
    public PlayerRole getMinimumRoleForPermission(RolePermission permission) {
        return rolePermissions.getOrDefault(permission, PlayerRole.MEMBER);
    }

    /**
     * Set the role permissions for this BeaconShieldBlock.
     *
     * @param role The role permission to set.
     * @param permission The role permission to associate with the role.
     */
    public void setRolePermissions(RolePermission permission, PlayerRole role) {
        this.rolePermissions.put(permission, role);
    }

    // STATIC METHODS

    /**
     * Get the BeaconShieldBlock associated with a block.
     *
     * @param block The block.
     * @return The associated BeaconShieldBlock.
     */
    public static BeaconShieldBlock getBeaconShieldBlock(Block block) {
        return BeaconHandler.getInstance().getBeaconShieldBlock(block);
    }

    /**
     * Create a BeaconShield item.
     *
     * @return The BeaconShield item.
     */
    public static @NotNull ItemStack createBeaconItem() {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();

        ItemStack item = new ItemStack(Material.BEACON);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(Text.color(config.getString("beacon-name")));
        meta.setLore(config.getStringList("beacon-lore").stream().map(Text::color).collect(Collectors.toList()));
        meta.getPersistentDataContainer().set(IS_BEACONSHIELD, PersistentDataType.BOOLEAN, true);
        item.setItemMeta(meta);
        return item;
    }

    public static @Nullable ShapedRecipe createRecipe() {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        if (!config.getBoolean("beacon-recipe.enabled", true)) {
            return null;
        }

        NamespacedKey key = new NamespacedKey(BeaconShield.getPlugin(), "beacon_recipe");
        ShapedRecipe recipe = new ShapedRecipe(key, createBeaconItem());

        List<String> shape = config.getStringList("beacon-recipe.shape");
        if (shape.isEmpty()) {
            throw new IllegalArgumentException("Beacon recipe shape is empty!");
        }

        ConfigurationSection ingredientsSection = config.getConfigurationSection("beacon-recipe.ingredients");
        if (ingredientsSection == null) {
            throw new IllegalArgumentException("Beacon recipe ingredients are missing or not properly formatted!");
        }

        Map<String, Object> ingredients = ingredientsSection.getValues(false);
        if (ingredients.isEmpty()) {
            throw new IllegalArgumentException("Beacon recipe ingredients are empty!");
        }

        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<String, Object> entry : ingredients.entrySet()) {
            String keyChar = entry.getKey();
            String materialString = entry.getValue().toString();
            Material material = Material.matchMaterial(materialString);
            if (material == null) {
                throw new IllegalArgumentException("Invalid material in beacon recipe: " + materialString);
            }
            recipe.setIngredient(keyChar.charAt(0), material);
        }

        return recipe;
    }
}