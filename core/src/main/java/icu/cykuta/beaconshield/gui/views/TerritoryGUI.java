package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.events.ProtectionChunkAddedEvent;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.providers.hooks.WorldGuardHook;
import icu.cykuta.beaconshield.utils.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TerritoryGUI extends GUI {
    private ProtectedChunk middleChunk;

    public TerritoryGUI() {
        super("inventory-title-territory", 45);
    }

    private enum ChunkType {
        CORE, CLAIMED, OCCUPIED, UNREACHABLE, AVAILABLE
    }

    @Override
    public void populateInventory() {
        this.middleChunk = this.getBeaconBlock().getCoreChunk();
        this.setDecorationSlots(
                0,  1,  2,  3,      5,  6,  7,  8,
                9,  10, 11,            15, 16, 17,
                18, 19,                    25, 26,
                27, 28, 29,            33, 34, 35,
                    37, 38, 39,    41, 42, 43, 44
        );

        // Arrow buttons
        this.addInventoryButton(4, "move-north", (guiClick) -> this.moveMiddleChunk(0, -1));
        this.addInventoryButton(20, "move-west", (guiClick) -> this.moveMiddleChunk(-1, 0));
        this.addInventoryButton(24, "move-east", (guiClick) -> this.moveMiddleChunk(1, 0));
        this.addInventoryButton(40, "move-south", (guiClick) -> this.moveMiddleChunk(0, 1));
        this.addInventoryButton(36, "back", (guiClick) -> this.openGUI(guiClick.clicker(), new BeaconGUI()));

        // Render the chunks
        this.renderChunks();
    }

    /**
     * Move the middle chunk.
     * @param x The x offset.
     * @param z The z offset.
     */
    private void moveMiddleChunk(int x, int z) {
        ProtectedChunk newChunk = new ProtectedChunk(
                this.middleChunk.getX() + x,
                this.middleChunk.getZ() + z,
                this.middleChunk.getWorld());

        // Check if all 8 chunks around are inaccessible
        ProtectedChunk[][] chunks = this.getChunksAround(newChunk);
        boolean allInaccessible = true;

        for (ProtectedChunk[] chunkRow : chunks) {
            for (ProtectedChunk chunk : chunkRow) {
                if (this.getChunkType(chunk) != ChunkType.UNREACHABLE) {
                    allInaccessible = false;
                    break;
                }
            }
        }

        if (!allInaccessible) {
            this.middleChunk = newChunk;
            this.renderChunks(); // Re-render the chunks
        }
    }

    /**
     * Get the type of the chunk.
     * @param protectedChunk The chunk to check.
     * @return The type of the chunk.
     */
    private ChunkType getChunkType(ProtectedChunk protectedChunk) {
        BeaconShieldBlock beaconBlock = this.getBeaconBlock();
        ChunkType chunkType = ChunkType.UNREACHABLE;
        Chunk chunk = protectedChunk.toChunk();

        if (beaconBlock.isCoreChunk(chunk)) {
            chunkType = ChunkType.CORE;
        } else if (beaconBlock.isProtectedChunk(chunk)) {
            chunkType = ChunkType.CLAIMED;
        } else if (this.isChunkOccupied(chunk)) {
            chunkType = ChunkType.OCCUPIED;
        } else if (this.isChunkAccessible(protectedChunk)) {
            chunkType = ChunkType.AVAILABLE;
        }

        return chunkType;
    }

    /**
     * Check if chunk is OCCUPIED.
     */
    private boolean isChunkOccupied(Chunk chunk) {
        if (WorldGuardHook.isChunkInWorldGuardRegion(chunk)) {
            return true;
        }

        return ProtectionHandler.isChunkProtected(chunk);
    }

    /**
     * Check if the chunk is accessible.
     * Only the chunks in the 4 directions of a protected chunk are accessible.
     */
    private boolean isChunkAccessible(ProtectedChunk protectedChunk) {
        int x = protectedChunk.getX();
        int z = protectedChunk.getZ();
        World world = protectedChunk.getWorld();

        // Define the 4 adjacent chunks
        ProtectedChunk[] adjacentChunks = {
                new ProtectedChunk(x, z - 1, world), // North
                new ProtectedChunk(x - 1, z, world), // West
                new ProtectedChunk(x + 1, z, world), // East
                new ProtectedChunk(x, z + 1, world)  // South
        };

        // Verify if any of the adjacent chunks are protected
        for (ProtectedChunk chunk : adjacentChunks) {
            if (ProtectionHandler.isChunkProtected(chunk.toChunk())) {
                // If adjacent chunk is not protected by the same beacon block return false
                return this.getBeaconBlock().isProtectedChunk(chunk.toChunk());
            }
        }

        return false;
    }


    /**
     * Get the 8 chunks around the middle chunk.
     * @return Matrix of chunks.
     */
    private ProtectedChunk[][] getChunksAround(ProtectedChunk selectedChunk) {
        ProtectedChunk[][] chunks = new ProtectedChunk[3][3];
        int x = selectedChunk.getX();
        int z = selectedChunk.getZ();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int chunkX = x + (j - 1);  // West is negative, East is positive
                int chunkZ = z + (i - 1);  // North is negative, South is positive

                chunks[i][j] = new ProtectedChunk(chunkX, chunkZ, selectedChunk.getWorld());
            }
        }

        return chunks;
    }

    /**
     * Render the chunks around the middle chunk.
     */
    public void renderChunks() {
        List<Integer> slots = Arrays.asList(12, 13, 14, 21, 22, 23, 30, 31, 32);
        ProtectedChunk[][] chunks = this.getChunksAround(this.middleChunk);

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ProtectedChunk chunk = chunks[i][j];
                ChunkType chunkType = this.getChunkType(chunk);
                int slot = slots.get(i * 3 + j);

                ItemStack itemstack = switch (chunkType) {
                    case CORE -> this.guiConfig.getItemStack("chunk-core");
                    case CLAIMED -> this.guiConfig.getItemStack("chunk-claimed");
                    case OCCUPIED -> this.guiConfig.getItemStack("chunk-occupied");
                    case AVAILABLE -> this.guiConfig.getItemStack("chunk-available");
                    case UNREACHABLE -> this.guiConfig.getItemStack("chunk-unreachable");
                };

                double chunkPrice = this.getChunkPrice(chunk);
                Consumer<GUIClick> action = getGuiClickConsumer(chunk, chunkType, chunkPrice);

                ItemMeta meta = itemstack.getItemMeta();
                List<String> lore = Text.replace(meta.getLore(), String.valueOf(chunkPrice),
                        String.valueOf(chunk.getX()), String.valueOf(chunk.getZ()));
                meta.setLore(lore);
                itemstack.setItemMeta(meta);

                this.addInventoryButton(slot, itemstack, action);
            }
        }
    }

    /**
     * Get the consumer for the GUI click.
     * @param chunk The chunk to claim.
     * @param chunkType The type of the chunk.
     * @return The consumer for the GUI click.
     */
    private Consumer<GUIClick> getGuiClickConsumer(ProtectedChunk chunk, ChunkType chunkType, double chunkPrice) {
        Consumer<GUIClick> availableAction = (guiClick) -> {
            if (guiClick.clickType() != ClickType.RIGHT) {
                this.openConfirmationGUI(guiClick.clicker(), (click) -> this.claimChunk(click.clicker(), chunk, chunkPrice) );
            } else {
                this.showChunkBorder(guiClick, chunk);
            }
        };

        return switch (chunkType) {
            case CORE -> this::showFullBorder;
            case CLAIMED -> (guiClick) -> Chat.send(guiClick.clicker(), "claim-owned-chunk");
            case OCCUPIED -> (guiClick) -> Chat.send(guiClick.clicker(), "claim-unowned-chunk");
            case UNREACHABLE -> (guiClick) -> Chat.send(guiClick.clicker(), "claim-inaccessible-chunk");
            case AVAILABLE -> availableAction;
        };
    }

    /**
     * Show the border of entire territory.
     * @param guiClick The GUI click.
     */
    private void showFullBorder(GUIClick guiClick) {
        Player player = guiClick.clicker();
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        Material previewBlock = Material.matchMaterial(config.getString("preview-block", "minecraft:gold_block"));

        List<ProtectedChunk> chunks = this.getBeaconBlock().getProtectedChunks();
        for (ProtectedChunk chunk : chunks) {
            chunk.preview(previewBlock, player);
        }

        player.closeInventory();
        Chat.send(player, "preview-entire-territory");
    }

    /**
     * Show the border of the chunk.
     * @param guiClick The GUI click.
     * @param selectedChunk The chunk to show the border.
     */
    private void showChunkBorder(GUIClick guiClick, ProtectedChunk selectedChunk) {
        Player player = guiClick.clicker();
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        Material previewBlock = Material.matchMaterial(config.getString("preview-block", "minecraft:gold_block"));
        selectedChunk.preview(previewBlock, player);
        player.closeInventory();
        Chat.send(player, "preview-chunk",
                String.valueOf(selectedChunk.getX()),
                String.valueOf(selectedChunk.getZ()));
    }

    /**
     * Claim the chunk.
     * @param player The player who claims the chunk.
     * @param selectedChunk The chunk to claim.
     */
    private void claimChunk(Player player, ProtectedChunk selectedChunk, double price) {
        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OFFICER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        if (config.getInt("max-chunks-per-beacon") <= this.getBeaconBlock().getProtectedChunks().size()) {
            Chat.send(player, "max-chunks-reached");
            return;
        }

        HookHandler hookHandler = HookHandler.getInstance();

        if (hookHandler.economyHook.isEnabled()) {
            Economy economy = hookHandler.economyHook.getHook();

            if (economy.has(player, price)) {
                economy.withdrawPlayer(player, price);
            } else {
                Chat.send(player, "insufficient-funds");
                player.closeInventory();
                return;
            }
        }

        this.getBeaconBlock().addProtectedChunk(selectedChunk);
        this.renderChunks(); // Re-render the
        this.openGUI(player, this);
        Chat.send(player, "claim-chunk",
                String.valueOf(selectedChunk.getX()),
                String.valueOf(selectedChunk.getZ()));

        // Call api event
        BeaconShield.getPlugin().getServer().getPluginManager().callEvent(
                new ProtectionChunkAddedEvent(selectedChunk, this.getBeaconBlock()));
    }

    /**
     * Get the price of the chunk.
     * @param selectedChunk The chunk to get the price.
     * @return The price of the chunk.
     */
    private double getChunkPrice(ProtectedChunk selectedChunk) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();

        ProtectedChunk coreChunk = this.getBeaconBlock().getCoreChunk();
        int distance = this.getManhattanDistance(coreChunk, selectedChunk);
        double basePrice = config.getDouble("base-price");
        boolean useFormula = config.getBoolean("use-formula");
        String formula = config.getString("formula");
        double price = basePrice;

        if (useFormula && formula != null) {
            price = MathUtils.eval(formula
                    .replace("%distance%", String.valueOf(distance))
                    .replace("%base_price%", String.valueOf(basePrice))
                    .replace("%chunks_owned%", String.valueOf(this.getBeaconBlock().getProtectedChunks().size()))
            );
        }

        return price;
    }

    /**
     * Get the distance between two chunks.
     */
    public int getManhattanDistance(ProtectedChunk chunk1, ProtectedChunk chunk2) {
        return Math.abs(chunk2.getX() - chunk1.getX()) + Math.abs(chunk2.getZ() - chunk1.getZ());
    }

}
