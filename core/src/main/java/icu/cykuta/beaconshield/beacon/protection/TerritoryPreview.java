package icu.cykuta.beaconshield.beacon.protection;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Client-side preview of a territory: fake border blocks on the surface
 * and particle beams (from min to max world height) at the corners of
 * the protection. Everything is only visible to the previewing player
 * and reverts automatically.
 */
public class TerritoryPreview {
    private static final int PREVIEW_SECONDS = 5;

    /**
     * Preview the whole territory: only the outer border is shown (edges
     * shared with another chunk of the same protection are skipped).
     */
    public static void showTerritory(Player player, BeaconShieldBlock beacon) {
        List<ProtectedChunk> territory = beacon.getProtectedChunks();
        List<Location> edges = new ArrayList<>();
        Set<Location> corners = new LinkedHashSet<>();

        for (ProtectedChunk chunk : territory) {
            edges.addAll(chunk.getOuterEdges(territory));
            corners.addAll(getVisibleCorners(chunk, territory));
        }

        showBlockEdges(player, edges);
        showCornerBeams(player, corners);
    }

    /**
     * Preview a single chunk with its full border.
     */
    public static void showChunk(Player player, BeaconShieldBlock beacon, ProtectedChunk chunk) {
        showBlockEdges(player, chunk.getChunkEdges());
        showCornerBeams(player, getVisibleCorners(chunk, beacon.getProtectedChunks()));
    }

    /**
     * Get the corner points of a chunk where a beam should be shown.
     * A corner is hidden when the other three chunks sharing it belong
     * to the territory (an interior corner of the protection).
     */
    private static List<Location> getVisibleCorners(ProtectedChunk chunk, Collection<ProtectedChunk> territory) {
        World world = chunk.getWorld();
        List<Location> corners = new ArrayList<>();

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                // Chunk coordinates of the diagonal neighbour on this corner
                int neighbourX = chunk.getX() + (dx == 0 ? -1 : 1);
                int neighbourZ = chunk.getZ() + (dz == 0 ? -1 : 1);

                boolean surrounded = territory.contains(new ProtectedChunk(neighbourX, chunk.getZ(), world))
                        && territory.contains(new ProtectedChunk(chunk.getX(), neighbourZ, world))
                        && territory.contains(new ProtectedChunk(neighbourX, neighbourZ, world));

                if (!surrounded) {
                    // World position of the corner point (chunk grid intersection)
                    int cornerX = (chunk.getX() + dx) << 4;
                    int cornerZ = (chunk.getZ() + dz) << 4;
                    corners.add(new Location(world, cornerX, world.getMinHeight(), cornerZ));
                }
            }
        }

        return corners;
    }

    /**
     * Show the fake border blocks to the player and revert them after
     * the preview time.
     */
    private static void showBlockEdges(Player player, List<Location> edges) {
        Material material = getPreviewMaterial();
        edges.forEach(edge -> player.sendBlockChange(edge, material.createBlockData()));

        BeaconShield.getPlugin().getServer().getScheduler().runTaskLater(BeaconShield.getPlugin(), () ->
                edges.forEach(edge -> player.sendBlockChange(edge, edge.getBlock().getBlockData())),
                PREVIEW_SECONDS * 20L);
    }

    /**
     * Spawn a particle beam from the minimum to the maximum world height
     * at every corner, refreshed every second while the preview lasts.
     */
    private static void showCornerBeams(Player player, Collection<Location> corners) {
        if (corners.isEmpty()) {
            return;
        }

        Particle particle = getPreviewParticle();
        double spacing = getBeamParticleSpacing();

        new BukkitRunnable() {
            private int elapsedSeconds = 0;

            @Override
            public void run() {
                if (this.elapsedSeconds++ >= PREVIEW_SECONDS || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                for (Location corner : corners) {
                    World world = corner.getWorld();
                    for (double y = world.getMinHeight(); y <= world.getMaxHeight(); y += spacing) {
                        player.spawnParticle(particle, corner.getX(), y, corner.getZ(), 1, 0, 0, 0, 0);
                    }
                }
            }
        }.runTaskTimer(BeaconShield.getPlugin(), 0, 20);
    }

    /**
     * Get the vertical distance between beam particles, derived from the
     * "preview-particles-per-block" density. The density is clamped so a
     * bad value cannot disable the beam or flood the client.
     */
    private static double getBeamParticleSpacing() {
        double perBlock = ConfigHandler.getInstance().getConfig().getDouble("preview-particles-per-block", 0.5);
        perBlock = Math.min(Math.max(perBlock, 0.1), 10);
        return 1.0 / perBlock;
    }

    /**
     * Get the block configured in "preview-block", falling back to gold.
     */
    private static Material getPreviewMaterial() {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        Material material = Material.matchMaterial(config.getString("preview-block", "minecraft:gold_block"));
        return material != null ? material : Material.GOLD_BLOCK;
    }

    /**
     * Get the particle configured in "preview-particle", falling back
     * to END_ROD when the name is invalid.
     */
    private static Particle getPreviewParticle() {
        PluginConfiguration config = ConfigHandler.getInstance().getConfig();
        String name = config.getString("preview-particle", "END_ROD").toUpperCase(Locale.ROOT);

        try {
            return Particle.valueOf(name);
        } catch (IllegalArgumentException e) {
            return Particle.END_ROD;
        }
    }
}
