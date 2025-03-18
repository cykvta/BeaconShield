package icu.cykuta.beaconshielddynmap;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.BeaconShieldAPI;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerSet;

import java.util.*;

public final class BeaconShield_dynmap extends JavaPlugin {
    private BeaconShieldAPI beaconShieldAPI;
    private DynmapCommonAPI dynmapAPI;
    private MarkerSet markerSet;

    @Override
    public void onEnable() {
        beaconShieldAPI = BeaconShield.getAPI();
        dynmapAPI = (DynmapCommonAPI) getServer().getPluginManager().getPlugin("dynmap");

        this.markerSet = this.dynmapAPI.getMarkerAPI().createMarkerSet(
                "beaconshield.markerset", "BeaconShield", dynmapAPI.getMarkerAPI().getMarkerIcons(), false);

        // Update markers every 10 seconds
        getServer().getScheduler().runTaskTimer(this, this::updateMarkers, 0, 5 * 20);
    }

    public void updateMarkers() {
        // Remove old markers
        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            marker.deleteMarker();
        }

        // Create new markers
        for (BeaconShieldBlock beacon : this.beaconShieldAPI.getBeacons()) {
            String markerId = "beaconshield." + beacon.getId();
            List<ProtectedChunk> chunks = beacon.getProtectedChunks();
            List<Location> perimeterPoints = getBoundingBox(chunks);

            // Convert locations to arrays of x and z coordinates
            double[] x = new double[perimeterPoints.size()];
            double[] z = new double[perimeterPoints.size()];
            for (int i = 0; i < perimeterPoints.size(); i++) {
                Location point = perimeterPoints.get(i);
                x[i] = point.getX();
                z[i] = point.getZ();
            }

            // Create the area marker
            AreaMarker am = this.markerSet.createAreaMarker(
                    markerId, "Protection of " + beacon.getOwner().getName(), true, beacon.getWorld().getName(), x, z, false);
            am.setFillStyle(0.5, 0x0000FF); // Example: Red color with 50% opacity
            am.setLineStyle(2, 1.0, 0x0000FF); // Example: Blue border with 100% opacity
        }
    }

    private List<Location> getBoundingBox(List<ProtectedChunk> chunks) {
        // Make a set of regions
        Set<String> regions = new HashSet<>();
        for (ProtectedChunk chunk : chunks) {
            regions.add(chunk.getX() + "," + chunk.getZ());
        }

        // Find the perimeter points
        List<Location> perimeterPoints = new ArrayList<>();
        for (ProtectedChunk chunk : chunks) {
            int x = chunk.getX();
            int z = chunk.getZ();

            // Verify the edges of each chunk
            if (!regions.contains((x - 1) + "," + z)) { // Left edge
                perimeterPoints.add(new Location(chunk.getWorld(), x * 16, 0, z * 16));
                perimeterPoints.add(new Location(chunk.getWorld(), x * 16, 0, (z + 1) * 16));
            }
            if (!regions.contains((x + 1) + "," + z)) { // Right edge
                perimeterPoints.add(new Location(chunk.getWorld(), (x + 1) * 16, 0, z * 16));
                perimeterPoints.add(new Location(chunk.getWorld(), (x + 1) * 16, 0, (z + 1) * 16));
            }
            if (!regions.contains(x + "," + (z - 1))) { // Bottom edge
                perimeterPoints.add(new Location(chunk.getWorld(), x * 16, 0, z * 16));
                perimeterPoints.add(new Location(chunk.getWorld(), (x + 1) * 16, 0, z * 16));
            }
            if (!regions.contains(x + "," + (z + 1))) { // Top edge
                perimeterPoints.add(new Location(chunk.getWorld(), x * 16, 0, (z + 1) * 16));
                perimeterPoints.add(new Location(chunk.getWorld(), (x + 1) * 16, 0, (z + 1) * 16));
            }
        }

        // Sort the perimeter points in clockwise order
        return sortPerimeterPoints(perimeterPoints);
    }

    private List<Location> sortPerimeterPoints(List<Location> perimeterPoints) {
        final double centerX;
        final double centerZ;
        {
            double sumX = 0, sumZ = 0;
            for (Location point : perimeterPoints) {
                sumX += point.getX();
                sumZ += point.getZ();
            }
            centerX = sumX / perimeterPoints.size();
            centerZ = sumZ / perimeterPoints.size();
        }

        perimeterPoints.sort((a, b) -> {
            double angleA = Math.atan2(a.getZ() - centerZ, a.getX() - centerX);
            double angleB = Math.atan2(b.getZ() - centerZ, b.getX() - centerX);
            return Double.compare(angleA, angleB);
        });

        return perimeterPoints;
    }
}