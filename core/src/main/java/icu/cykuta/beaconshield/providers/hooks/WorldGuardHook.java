package icu.cykuta.beaconshield.providers.hooks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import icu.cykuta.beaconshield.data.HookHandler;
import icu.cykuta.beaconshield.providers.DependencyNotEnabledException;
import icu.cykuta.beaconshield.providers.Hook;
import org.bukkit.Chunk;
import org.bukkit.World;

public class WorldGuardHook extends Hook<WorldGuard> {

    public WorldGuardHook() {
        super("WorldGuard", HookType.SOFT_DEPENDENCY);
    }

    @Override
    public void register() throws DependencyNotEnabledException {
        if (this.pluginManager.getPlugin("WorldGuard") != null) {
            this.hook = WorldGuard.getInstance();
        }
    }

    /**
     * Verify if a chunk is in a WorldGuard region
     * @param chunk the chunk to verify
     * @return true if the chunk is in a WorldGuard region
     */
    public static boolean isChunkInWorldGuardRegion(Chunk chunk) {
        return isChunkInWorldGuardRegion(chunk.getWorld(), chunk.getX(), chunk.getZ());
    }

    /**
     * Verify if a chunk is in a WorldGuard region, without loading the chunk.
     * @param world the world of the chunk
     * @param x the chunk x coordinate
     * @param z the chunk z coordinate
     * @return true if the chunk is in a WorldGuard region
     */
    public static boolean isChunkInWorldGuardRegion(World world, int x, int z) {
        HookHandler hookHandler = HookHandler.getInstance();

        if (!hookHandler.worldGuardHook.isEnabled()) {
            return false;
        }

        WorldGuard wgPlugin = hookHandler.worldGuardHook.getHook();
        RegionContainer container = wgPlugin.getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(BukkitAdapter.adapt(world));

        if (regionManager == null) {
            return false;
        }

        int chunkX = x << 4;
        int chunkZ = z << 4;

        // Define world height limits
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        // Verify if any region intersects with the chunk
        for (ProtectedRegion region : regionManager.getRegions().values()) {
            // Get region min and max points
            BlockVector3 min = region.getMinimumPoint();
            BlockVector3 max = region.getMaximumPoint();

            // Verify if the region intersects with the chunk
            if (regionIntersectsChunk(min, max, chunkX, chunkZ, minY, maxY)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verify if a region intersects with a chunk
     * @param regionMin the region coordinates
     * @param regionMax the region coordinates
     * @param chunkX the chunk coordinates
     * @param chunkZ the chunk coordinates
     * @param minY the minimum Y value of the world
     * @param maxY the maximum Y value of the world
     * @return true if the region intersects with the chunk
     */
    private static boolean regionIntersectsChunk(BlockVector3 regionMin, BlockVector3 regionMax, int chunkX, int chunkZ, int minY, int maxY) {
        boolean intersectsX = (regionMin.getX() <= chunkX + 15) && (regionMax.getX() >= chunkX);
        boolean intersectsZ = (regionMin.getZ() <= chunkZ + 15) && (regionMax.getZ() >= chunkZ);
        boolean intersectsY = (regionMin.getY() <= maxY) && (regionMax.getY() >= minY);
        return intersectsX && intersectsZ && intersectsY;
    }
}