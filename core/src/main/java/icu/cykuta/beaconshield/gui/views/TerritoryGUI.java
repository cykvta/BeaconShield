package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.beacon.protection.TerritoryPreview;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.data.ProtectionHandler;
import icu.cykuta.beaconshield.events.ProtectionChunkAddedEvent;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.providers.hooks.WorldGuardHook;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.MathUtils;
import icu.cykuta.beaconshield.utils.PermissionUtils;
import icu.cykuta.beaconshield.utils.Text;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.function.Consumer;

/**
 * Chunk map centered on a movable middle chunk. Players can preview
 * chunk borders and claim available chunks from here.
 */
public class TerritoryGUI extends GUI {
    private static final List<Integer> MAP_SLOTS = List.of(12, 13, 14, 21, 22, 23, 30, 31, 32);

    private ProtectedChunk middleChunk;

    public TerritoryGUI(BeaconShieldBlock beacon) {
        super(beacon, "inventory-title-territory", 45);
        this.middleChunk = beacon.getCoreChunk();
    }

    private enum ChunkType {
        CORE, CLAIMED, OCCUPIED, UNREACHABLE, AVAILABLE
    }

    @Override
    protected void populate() {
        this.addDecoration(
                0,  1,  2,  3,      5,  6,  7,  8,
                9,  10, 11,            15, 16, 17,
                18, 19,                    25, 26,
                27, 28, 29,            33, 34, 35,
                    37, 38, 39,    41, 42, 43, 44
        );

        // Navigation arrows
        this.addButton(4, "territory-gui.move-north", click -> this.moveMiddleChunk(0, -1));
        this.addButton(20, "territory-gui.move-west", click -> this.moveMiddleChunk(-1, 0));
        this.addButton(24, "territory-gui.move-east", click -> this.moveMiddleChunk(1, 0));
        this.addButton(40, "territory-gui.move-south", click -> this.moveMiddleChunk(0, 1));
        this.addButton(36, "global.back", click -> this.openMainGUI(click.clicker()));

        this.renderChunks();
    }

    /**
     * Move the map center, unless the whole new view would be unreachable.
     */
    private void moveMiddleChunk(int offsetX, int offsetZ) {
        ProtectedChunk newMiddle = new ProtectedChunk(
                this.middleChunk.getX() + offsetX,
                this.middleChunk.getZ() + offsetZ,
                this.middleChunk.getWorld());

        boolean anyReachable = false;
        for (ProtectedChunk[] row : this.getChunksAround(newMiddle)) {
            for (ProtectedChunk chunk : row) {
                if (this.getChunkType(chunk) != ChunkType.UNREACHABLE) {
                    anyReachable = true;
                    break;
                }
            }
        }

        if (anyReachable) {
            this.middleChunk = newMiddle;
            this.renderChunks();
        }
    }

    /**
     * Classify a chunk from the point of view of this beacon.
     * Works purely on coordinates, so no chunk is ever loaded.
     */
    private ChunkType getChunkType(ProtectedChunk chunk) {
        if (this.beacon.isCoreChunk(chunk)) {
            return ChunkType.CORE;
        }
        if (this.beacon.isProtectedChunk(chunk)) {
            return ChunkType.CLAIMED;
        }
        if (this.isChunkOccupied(chunk)) {
            return ChunkType.OCCUPIED;
        }
        if (this.isChunkAccessible(chunk)) {
            return ChunkType.AVAILABLE;
        }
        return ChunkType.UNREACHABLE;
    }

    /**
     * Check if the chunk is taken by another beacon or a WorldGuard region.
     */
    private boolean isChunkOccupied(ProtectedChunk chunk) {
        return ProtectionHandler.isChunkProtected(chunk)
                || WorldGuardHook.isChunkInWorldGuardRegion(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * A chunk is accessible when at least one of its 4 direct neighbours
     * is already protected by this beacon.
     */
    private boolean isChunkAccessible(ProtectedChunk chunk) {
        int x = chunk.getX();
        int z = chunk.getZ();
        World world = chunk.getWorld();

        return this.beacon.isProtectedChunk(new ProtectedChunk(x, z - 1, world))
                || this.beacon.isProtectedChunk(new ProtectedChunk(x - 1, z, world))
                || this.beacon.isProtectedChunk(new ProtectedChunk(x + 1, z, world))
                || this.beacon.isProtectedChunk(new ProtectedChunk(x, z + 1, world));
    }

    /**
     * Get the 3x3 matrix of chunks around the given chunk.
     */
    private ProtectedChunk[][] getChunksAround(ProtectedChunk center) {
        ProtectedChunk[][] chunks = new ProtectedChunk[3][3];

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                chunks[row][col] = new ProtectedChunk(
                        center.getX() + (col - 1),  // West is negative, East is positive
                        center.getZ() + (row - 1),  // North is negative, South is positive
                        center.getWorld());
            }
        }

        return chunks;
    }

    /**
     * Render the 3x3 chunk map around the middle chunk.
     */
    private void renderChunks() {
        ProtectedChunk[][] chunks = this.getChunksAround(this.middleChunk);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                ProtectedChunk chunk = chunks[row][col];
                ChunkType chunkType = this.getChunkType(chunk);
                int slot = MAP_SLOTS.get(row * 3 + col);

                ItemStack item = switch (chunkType) {
                    case CORE -> this.guiConfig.getItemStack("territory-gui.chunk-core");
                    case CLAIMED -> this.guiConfig.getItemStack("territory-gui.chunk-claimed");
                    case OCCUPIED -> this.guiConfig.getItemStack("territory-gui.chunk-occupied");
                    case AVAILABLE -> this.guiConfig.getItemStack("territory-gui.chunk-available");
                    case UNREACHABLE -> this.guiConfig.getItemStack("territory-gui.chunk-unreachable");
                };

                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setLore(Text.replace(meta.getLore(),
                            String.valueOf(this.getChunkPrice(chunk)),
                            String.valueOf(chunk.getX()),
                            String.valueOf(chunk.getZ())));
                    item.setItemMeta(meta);
                }

                this.addButton(slot, item, this.getChunkAction(chunk, chunkType));
            }
        }
    }

    /**
     * Get the click action for a chunk depending on its type.
     */
    private Consumer<GUIClick> getChunkAction(ProtectedChunk chunk, ChunkType chunkType) {
        return switch (chunkType) {
            case CORE -> this::showFullBorder;
            case CLAIMED -> click -> Chat.send(click.clicker(), "claim-owned-chunk");
            case OCCUPIED -> click -> Chat.send(click.clicker(), "claim-unowned-chunk");
            case UNREACHABLE -> click -> Chat.send(click.clicker(), "claim-inaccessible-chunk");
            case AVAILABLE -> click -> {
                if (click.clickType() == ClickType.RIGHT) {
                    this.showChunkBorder(click.clicker(), chunk);
                } else {
                    this.openConfirmation(click.clicker(), confirm -> this.claimChunk(confirm.clicker(), chunk));
                }
            };
        };
    }

    /**
     * Preview the outer borders and corner beams of the whole territory.
     */
    private void showFullBorder(GUIClick click) {
        Player player = click.clicker();
        TerritoryPreview.showTerritory(player, this.beacon);

        player.closeInventory();
        Chat.send(player, "preview-entire-territory");
    }

    /**
     * Preview the borders of a single chunk.
     */
    private void showChunkBorder(Player player, ProtectedChunk chunk) {
        TerritoryPreview.showChunk(player, this.beacon, chunk);
        player.closeInventory();
        Chat.send(player, "preview-chunk",
                String.valueOf(chunk.getX()),
                String.valueOf(chunk.getZ()));
    }

    /**
     * Claim a chunk for this beacon, charging the player if economy is enabled.
     */
    private void claimChunk(Player player, ProtectedChunk chunk) {
        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        // Re-check the chunk state: it may have been claimed while the
        // confirmation dialog was open.
        if (this.getChunkType(chunk) != ChunkType.AVAILABLE) {
            Chat.send(player, "claim-unowned-chunk");
            this.refresh();
            this.open(player);
            return;
        }

        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        int maxChunks = PermissionUtils.getLimit(player, "beaconshield.max-chunks.",
                config.getInt("max-chunks-per-beacon"));
        if (maxChunks <= this.beacon.getProtectedChunks().size()) {
            Chat.send(player, "max-chunks-reached");
            return;
        }

        HookHandler hookHandler = HookHandler.getInstance();
        if (hookHandler.economyHook.isEnabled()) {
            Economy economy = hookHandler.economyHook.getHook();
            double price = this.getChunkPrice(chunk);

            if (!economy.has(player, price)) {
                Chat.send(player, "insufficient-funds");
                player.closeInventory();
                return;
            }
            economy.withdrawPlayer(player, price);
        }

        this.beacon.addProtectedChunk(chunk);
        this.refresh();
        this.open(player);
        Chat.send(player, "claim-chunk",
                String.valueOf(chunk.getX()),
                String.valueOf(chunk.getZ()));

        Bukkit.getPluginManager().callEvent(new ProtectionChunkAddedEvent(chunk, this.beacon));
    }

    /**
     * Get the price of a chunk, optionally using the configured formula.
     */
    private double getChunkPrice(ProtectedChunk chunk) {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();

        double basePrice = config.getDouble("base-price");
        String formula = config.getString("economy-formula");

        if (!config.getBoolean("economy-use-formula") || formula == null) {
            return basePrice;
        }

        int distance = getManhattanDistance(this.beacon.getCoreChunk(), chunk);
        return MathUtils.eval(formula
                .replace("%distance%", String.valueOf(distance))
                .replace("%base_price%", String.valueOf(basePrice))
                .replace("%chunks_owned%", String.valueOf(this.beacon.getProtectedChunks().size()))
        );
    }

    private static int getManhattanDistance(ProtectedChunk first, ProtectedChunk second) {
        return Math.abs(second.getX() - first.getX()) + Math.abs(second.getZ() - first.getZ());
    }
}
