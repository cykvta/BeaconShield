package icu.cykuta.beaconshieldsquaremap;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.BeaconShieldAPI;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.jpenilla.squaremap.api.*;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.Polygon;

import java.util.*;

public final class BeaconShield_squaremap extends JavaPlugin {
    private Squaremap api;
    private SimpleLayerProvider layerProvider;
    private BeaconShieldAPI beaconShieldAPI;

    @Override
    public void onEnable() {
        this.beaconShieldAPI = BeaconShield.getAPI();
        this.api = SquaremapProvider.get();
        this.layerProvider = SimpleLayerProvider.builder("Beacon Shield")
                .showControls(true)
                .defaultHidden(false)
                .layerPriority(5)
                .zIndex(250)
                .build();

        for (World world : Bukkit.getServer().getWorlds()) {
            this.api.getWorldIfEnabled(BukkitAdapter.worldIdentifier(world)).ifPresent(mapWorld -> {
                Key key = Key.of("beacon_shield_" + world.getName());
                mapWorld.layerRegistry().register(key, this.layerProvider);
            });
        }

        // Update markers every 10 seconds
        Bukkit.getScheduler().runTaskTimer(this, this::updateMarkers, 0, 5 * 20);
    }

    private void updateMarkers() {
        for (BeaconShieldBlock beacon : beaconShieldAPI.getBeacons()) {
            List<ProtectedChunk> chunks = beacon.getProtectedChunks();
            Polygon polygon = getPolygon(chunks);
            Key key = Key.of("beacon_shield_" + beacon.getId());
            this.layerProvider.addMarker(key, polygon);
        }

        // remove markers that are no longer needed
        for (Map.Entry<Key, Marker> entry : this.layerProvider.registeredMarkers().entrySet()) {
            Key key = entry.getKey();
            if (!beaconShieldAPI.getBeacons().stream()
                    .anyMatch(beacon -> key.getKey().equals("beacon_shield_" + beacon.getId()))) {
                this.layerProvider.removeMarker(key);
            }
        }
    }

    /**
     * Get the polygon of the beacon shield
     * @param chunks The chunks that the beacon shield protects
     * @return The polygon of the beacon shield
     */
    private Polygon getPolygon(List<ProtectedChunk> chunks) {
        // Make a set of regions
        Set<String> regions = new HashSet<>();
        for (ProtectedChunk chunk : chunks) {
            regions.add(chunk.getX() + "," + chunk.getZ());
        }

        // Found the perimeter points
        List<Point> perimeterPoints = new ArrayList<>();
        for (ProtectedChunk chunk : chunks) {
            int x = chunk.getX();
            int z = chunk.getZ();

            // Verify the edges of each chunk
            if (!regions.contains((x - 1) + "," + z)) { // left edge
                perimeterPoints.add(Point.of(x * 16, z * 16));
                perimeterPoints.add(Point.of(x * 16, (z + 1) * 16));
            }
            if (!regions.contains((x + 1) + "," + z)) { // Right edge
                perimeterPoints.add(Point.of((x + 1) * 16, z * 16));
                perimeterPoints.add(Point.of((x + 1) * 16, (z + 1) * 16));
            }
            if (!regions.contains(x + "," + (z - 1))) { // Bottom edge
                perimeterPoints.add(Point.of(x * 16, z * 16));
                perimeterPoints.add(Point.of((x + 1) * 16, z * 16));
            }
            if (!regions.contains(x + "," + (z + 1))) { // Top edge
                perimeterPoints.add(Point.of(x * 16, (z + 1) * 16));
                perimeterPoints.add(Point.of((x + 1) * 16, (z + 1) * 16));
            }
        }

        // Order the points
        List<Point> sortedPoints = sortPerimeterPoints(perimeterPoints);

        return Polygon.polygon(sortedPoints);
    }

    /**
     * Order the points in the clockwise direction
     * @param perimeterPoints The points to be sorted
     * @return The sorted points
     */
    private List<Point> sortPerimeterPoints(List<Point> perimeterPoints) {
        final double centerX;
        final double centerZ;
        {
            double sumX = 0, sumZ = 0;
            for (Point point : perimeterPoints) {
                sumX += point.x();
                sumZ += point.z();
            }
            centerX = sumX / perimeterPoints.size();
            centerZ = sumZ / perimeterPoints.size();
        }

        perimeterPoints.sort((a, b) -> {
            double angleA = Math.atan2(a.z() - centerZ, a.x() - centerX);
            double angleB = Math.atan2(b.z() - centerZ, b.x() - centerX);
            return Double.compare(angleA, angleB);
        });

        return perimeterPoints;
    }
}
