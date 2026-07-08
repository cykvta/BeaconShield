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

    private static final String BYPASS_PERMISSION = "beaconshield.bypass";

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
     * Create a new BeaconShieldBlock.
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
     * Set the default minimum role for every territory permission.
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
     * Add a protected chunk to this beacon.
     */
    public void addProtectedChunk(Chunk chunk) {
        addProtectedChunk(new ProtectedChunk(chunk));
    }

    /**
     * Add a protected chunk to this beacon.
     */
    public void addProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.add(protectedChunk);
        ProtectionHandler.addChunk(protectedChunk, this);
    }

    /**
     * Remove a protected chunk from this beacon.
     */
    public void removeProtectedChunk(Chunk chunk) {
        removeProtectedChunk(new ProtectedChunk(chunk));
    }

    /**
     * Remove a protected chunk from this beacon.
     */
    public void removeProtectedChunk(ProtectedChunk protectedChunk) {
        this.protectedChunks.remove(protectedChunk);
        ProtectionHandler.removeChunk(protectedChunk);
    }

    /**
     * Get all protected chunks.
     */
    public List<ProtectedChunk> getProtectedChunks() {
        return Collections.unmodifiableList(protectedChunks);
    }

    /**
     * Set the fuel level.
     */
    public void setFuelLevel(int fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    /**
     * Check if the beacon can protect. When the fuel system is disabled
     * the protection is permanent; otherwise it depends on the fuel level.
     */
    public boolean canProtect() {
        return !isFuelSystemEnabled() || fuelLevel != -1;
    }

    /**
     * Check if the fuel system is enabled in the config.
     */
    public static boolean isFuelSystemEnabled() {
        return ConfigHandler.getInstance().getConfig().getBoolean("fuel-enabled", true);
    }

    /**
     * Get the fuel level.
     */
    public int getFuelLevel() {
        return fuelLevel;
    }

    /**
     * Set the owner of the beacon, demoting nobody: the previous owner
     * entry is removed.
     */
    public void setOwner(@NotNull OfflinePlayer player) {
        setOwner(player.getUniqueId());
    }

    /**
     * Set the owner of the beacon.
     */
    public void setOwner(UUID uuid) {
        allowedPlayers.entrySet().removeIf(entry -> entry.getValue() == PlayerRole.OWNER);
        allowedPlayers.put(uuid, PlayerRole.OWNER);
    }

    /**
     * Get the owner of the beacon.
     *
     * @throws NoSuchElementException If no owner is found.
     */
    public OfflinePlayer getOwner() throws NoSuchElementException {
        return Bukkit.getOfflinePlayer(
                allowedPlayers.entrySet().stream()
                        .filter(entry -> entry.getValue() == PlayerRole.OWNER)
                        .findFirst()
                        .orElseThrow(() -> new NoSuchElementException("No owner found"))
                        .getKey()
        );
    }

    /**
     * Add a player to the list of allowed players.
     */
    public void addAllowedPlayer(@NotNull OfflinePlayer player, PlayerRole role) {
        this.allowedPlayers.put(player.getUniqueId(), role);
    }

    /**
     * Remove a player from the list of allowed players.
     */
    public void removeAllowedPlayer(@NotNull OfflinePlayer player) {
        this.allowedPlayers.remove(player.getUniqueId());
    }

    /**
     * Check if a player can perform an action in the territory, based on
     * their role and the minimum role required for the permission.
     * Operators and players with the bypass permission are always allowed.
     */
    public boolean isAllowedPlayer(@NotNull RolePermission permission, @NotNull OfflinePlayer player) {
        if (hasBypass(player)) {
            return true;
        }

        PlayerRole role = allowedPlayers.get(player.getUniqueId());
        if (role == null) {
            return false;
        }

        PlayerRole requiredRole = getMinimumRoleForPermission(permission);
        return role.getPermissionLevel() >= requiredRole.getPermissionLevel();
    }

    /**
     * Check if a player is a member of this beacon.
     */
    public boolean hasMember(@NotNull OfflinePlayer player) {
        return this.allowedPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Get all allowed players.
     */
    public List<OfflinePlayer> getAllowedPlayers() {
        return allowedPlayers.keySet().stream()
                .map(Bukkit::getOfflinePlayer)
                .collect(Collectors.toList());
    }

    /**
     * Get the block associated with this BeaconShield.
     */
    public Block getBlock() {
        return this.getWorld().getBlockAt(x, y, z);
    }

    /**
     * Check if this beacon shield is at the position of the given block.
     */
    public boolean isAt(@NotNull Block block) {
        return block.getX() == x
                && block.getY() == y
                && block.getZ() == z
                && block.getWorld().getName().equals(world);
    }

    /**
     * Get the world where the BeaconShield is located.
     */
    public World getWorld() {
        return Bukkit.getWorld(world);
    }

    /**
     * Get the unique ID of the BeaconShield.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the core chunk protected by the BeaconShield.
     */
    public ProtectedChunk getCoreChunk() {
        return coreChunk;
    }

    /**
     * Check if a chunk is the core chunk.
     */
    public boolean isCoreChunk(Chunk chunk) {
        return isCoreChunk(new ProtectedChunk(chunk));
    }

    /**
     * Check if a chunk is the core chunk.
     */
    public boolean isCoreChunk(ProtectedChunk chunk) {
        return coreChunk.equals(chunk);
    }

    /**
     * Check if a chunk is protected by this beacon.
     */
    public boolean isProtectedChunk(Chunk chunk) {
        return isProtectedChunk(new ProtectedChunk(chunk));
    }

    /**
     * Check if a chunk is protected by this beacon.
     */
    public boolean isProtectedChunk(ProtectedChunk chunk) {
        return protectedChunks.contains(chunk);
    }

    /**
     * Get the PDC manager for this BeaconShield.
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
     * Check if a player has at least the given role in this beacon.
     * This is a pure role check: bypass permissions are ignored, so use
     * it to validate the <em>target</em> of an action.
     */
    public boolean hasRole(@NotNull OfflinePlayer player, @NotNull PlayerRole role) {
        PlayerRole playerRole = getPlayerRole(player);
        return playerRole != null && playerRole.getPermissionLevel() >= role.getPermissionLevel();
    }

    /**
     * Check if a player is allowed to act with at least the given role.
     * Operators and players with the bypass permission are always allowed,
     * so use it to validate the <em>actor</em> of an action.
     */
    public boolean hasPermissionLevel(@NotNull OfflinePlayer player, @NotNull PlayerRole role) {
        return hasBypass(player) || hasRole(player, role);
    }

    /**
     * Check if a player bypasses the beacon permissions (operator or
     * bypass permission). Offline players never bypass.
     */
    private static boolean hasBypass(@NotNull OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        return onlinePlayer != null
                && (onlinePlayer.isOp() || onlinePlayer.hasPermission(BYPASS_PERMISSION));
    }

    /**
     * Get the role of a player, or null if they have no role.
     */
    @Nullable
    public PlayerRole getPlayerRole(@NotNull OfflinePlayer player) {
        return allowedPlayers.get(player.getUniqueId());
    }

    /**
     * Set the role of a player.
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
         */
        public Map<Integer, ItemStack> getStoredItems() {
            return this.pdc.getOrDefault(DataKeys.BEACONSHIELD_INVENTORY,
                    DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), new HashMap<>());
        }

        /**
         * Set the items stored in the PDC.
         */
        public void setStoredItems(Map<Integer, ItemStack> storedItems) {
            this.pdc.set(DataKeys.BEACONSHIELD_INVENTORY,
                    DataType.asMap(DataType.INTEGER, DataType.ITEM_STACK), storedItems);
        }

        /**
         * Remove all BeaconShield data from the block (stored items and
         * the beacon shield mark).
         */
        public void clear() {
            this.pdc.remove(DataKeys.BEACONSHIELD_INVENTORY);
            this.pdc.remove(IS_BEACONSHIELD);
        }
    }

    /**
     * Get the burn time of a fuel item, or 0 if the item is not a
     * valid fuel.
     */
    public int getBurnTime(ItemStack fuel) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        boolean useFormula = config.getBoolean("fuel-use-formula");
        String formula = config.getString("fuel-formula");

        for (Map<?, ?> fuelEntry : config.getMapList("fuel-items")) {
            Material material = Material.matchMaterial(String.valueOf(fuelEntry.get("item")));
            if (material == null || fuel.getType() != material) {
                continue;
            }

            if (!matchesCustomModelData(fuel, fuelEntry)) {
                continue;
            }

            int burnTime = fuelEntry.get("burn-time") instanceof Number number ? number.intValue() : 0;

            if (useFormula && formula != null) {
                burnTime = (int) MathUtils.eval(formula
                        .replace("%burn_time%", String.valueOf(burnTime))
                        .replace("%chunks_owned%", String.valueOf(this.protectedChunks.size()))
                );
            }

            return burnTime;
        }

        return 0;
    }

    /**
     * Check if a fuel item matches the custom model data required by a
     * fuel config entry (0 or missing means no requirement).
     */
    private static boolean matchesCustomModelData(ItemStack fuel, Map<?, ?> fuelEntry) {
        int required = fuelEntry.get("custom-model-data") instanceof Number number ? number.intValue() : 0;
        if (required == 0) {
            return true;
        }

        ItemMeta meta = fuel.getItemMeta();
        return meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == required;
    }

    /**
     * Get the minimum role required for a territory permission.
     */
    public PlayerRole getMinimumRoleForPermission(RolePermission permission) {
        return rolePermissions.getOrDefault(permission, PlayerRole.MEMBER);
    }

    /**
     * Set the minimum role required for a territory permission.
     */
    public void setRolePermissions(RolePermission permission, PlayerRole role) {
        this.rolePermissions.put(permission, role);
    }

    // STATIC METHODS

    /**
     * Get the BeaconShieldBlock registered at the position of a block.
     */
    public static BeaconShieldBlock getBeaconShieldBlock(Block block) {
        return BeaconHandler.getInstance().getBeaconShieldBlock(block);
    }

    /**
     * Create a BeaconShield item.
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

    /**
     * Create the crafting recipe of the BeaconShield item, or null if
     * the recipe is disabled in the config.
     */
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
