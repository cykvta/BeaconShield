package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.PlayerRole;
import icu.cykuta.beaconshield.beacon.ProtectedChunk;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.utils.*;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;
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

        PluginConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();

        // Arrow buttons
        this.addInventoryButton(4,  lang.getString("button-move-up"), Material.ARROW,
                (guiClick) -> this.moveMiddleChunk(0, -1));
        this.addInventoryButton(20, lang.getString("button-move-left"), Material.ARROW,
                (guiClick) -> this.moveMiddleChunk(-1, 0));
        this.addInventoryButton(24, lang.getString("button-move-right"), Material.ARROW,
                (guiClick) -> this.moveMiddleChunk(1, 0));
        this.addInventoryButton(40, lang.getString("button-move-down"), Material.ARROW,
                (guiClick) -> this.moveMiddleChunk(0, 1));
        this.addInventoryButton(36, lang.getString("button-back"), Material.ARROW,
                        (guiClick) -> this.openGUI(guiClick.getClicker(), new BeaconGUI()));

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
        if (ProtectionUtils.isChunkInWorldGuardRegion(chunk)) {
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
        PluginConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ProtectedChunk chunk = chunks[i][j];
                ChunkType chunkType = this.getChunkType(chunk);
                int slot = slots.get(i * 3 + j);

                Material material = switch (chunkType) {
                    case CORE -> Material.BEACON;
                    case CLAIMED -> Material.DIAMOND_BLOCK;
                    case OCCUPIED -> Material.REDSTONE_BLOCK;
                    case AVAILABLE -> Material.GRASS_BLOCK;
                    case UNREACHABLE -> Material.BARRIER;
                };

                String name = switch (chunkType) {
                    case CORE -> lang.getString("territory-core");
                    case CLAIMED -> lang.getString("territory-claimed");
                    case OCCUPIED -> lang.getString("territory-occupied");
                    case AVAILABLE -> lang.getString("territory-available");
                    case UNREACHABLE -> lang.getString("territory-unreachable");
                };

                double chunkPrice = this.getChunkPrice(chunk);
                Consumer<GUIClick> action = getGuiClickConsumer(chunk, chunkType, chunkPrice);

                String rightClickInfo = chunkType == ChunkType.AVAILABLE ?
                        Text.color(lang.getString("preview-chunk-info")) : null;

                this.addInventoryButton(slot, name, material, action,
                        Text.color(Text.replace(lang.getString("chunk-price"), String.valueOf(chunkPrice))),
                        "&7(" + chunk.getX() + ", " + chunk.getZ() + ")",
                        rightClickInfo);
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
            if (guiClick.getClickType() != ClickType.RIGHT) {
                this.openConfirmationGUI(guiClick.getClicker(), (click) -> this.claimChunk(click.getClicker(), chunk, chunkPrice) );
            } else {
                this.showChunkBorder(guiClick, chunk);
            }
        };

        return switch (chunkType) {
            case CORE -> (guiClick) -> Chat.send(guiClick.getClicker(), "claim-core-chunk");
            case CLAIMED -> (guiClick) -> Chat.send(guiClick.getClicker(), "claim-owned-chunk");
            case OCCUPIED -> (guiClick) -> Chat.send(guiClick.getClicker(), "claim-unowned-chunk");
            case UNREACHABLE -> (guiClick) -> Chat.send(guiClick.getClicker(), "claim-inaccessible-chunk");
            case AVAILABLE -> availableAction;
        };
    }

    /**
     * Show the border of the chunk.
     * @param guiClick The GUI click.
     * @param selectedChunk The chunk to show the border.
     */
    private void showChunkBorder(GUIClick guiClick, ProtectedChunk selectedChunk) {
        Player player = guiClick.getClicker();
        List<Location> highestEdges = selectedChunk.getChunkEdges();
        PluginConfiguration config = BeaconShield.getPlugin().getFileHandler().getConfig();

        Material previewBlock = Material.matchMaterial(config.getString("preview-block", "minecraft:gold_block"));
        highestEdges.forEach(edge -> player.sendBlockChange(edge, previewBlock.createBlockData()));
        player.closeInventory();
        Chat.send(player, "preview-chunk",
                String.valueOf(selectedChunk.getX()),
                String.valueOf(selectedChunk.getZ()));

        // after 5 seconds, remove the border
        BeaconShield.getPlugin().getServer().getScheduler().runTaskLater(BeaconShield.getPlugin(), () -> {
            highestEdges.forEach(edge -> player.sendBlockChange(edge, edge.getBlock().getBlockData()));
        }, 5 * 20);
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

        PluginConfiguration config = BeaconShield.getPlugin().getFileHandler().getConfig();
        if (config.getInt("max-chunks-per-beacon") <= this.getBeaconBlock().getProtectedChunks().size()) {
            Chat.send(player, "max-chunks-reached");
            return;
        }

        HookHandler hookHandler = BeaconShield.getPlugin().getHookHandler();

        if (hookHandler.economyHook.isEnabled()) {
            Economy economy = hookHandler.economyHook.getHook();
            EconomyResponse response = economy.withdrawPlayer(player, price);
            if (!response.transactionSuccess()) {
                Chat.send(player, "insufficient-funds");
                return;
            }
        }

        this.getBeaconBlock().addProtectedChunk(selectedChunk);
        this.renderChunks(); // Re-render the
        this.openGUI(player, this);
        Chat.send(player, "claim-chunk",
                String.valueOf(selectedChunk.getX()),
                String.valueOf(selectedChunk.getZ()));
    }

    /**
     * Get the price of the chunk.
     * @param selectedChunk The chunk to get the price.
     * @return The price of the chunk.
     */
    private double getChunkPrice(ProtectedChunk selectedChunk) {
        PluginConfiguration config = BeaconShield.getPlugin().getFileHandler().getConfig();

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
