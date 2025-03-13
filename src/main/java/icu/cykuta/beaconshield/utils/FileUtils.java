package icu.cykuta.beaconshield.utils;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;

import javax.annotation.Nullable;
import java.io.*;

public class FileUtils {

    /**
     * Write the BeaconShieldBlock to file.
     * @param beacon The BeaconShieldBlock.
     */
    public static void writeBeaconToFile(BeaconShieldBlock beacon) {
        File pluginFolder = BeaconShield.getPlugin().getDataFolder();
        File dataFolder = new File(pluginFolder, "data");

        try (FileOutputStream fos = new FileOutputStream(new File(dataFolder, beacon.getId() + ".beacon"))) {
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(beacon);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Read the BeaconShieldBlock from file.
     * @param file The file.
     * @return The BeaconShieldBlock.
     */
    @Nullable
    public static BeaconShieldBlock readBeaconFromFile(File file) {
        File pluginFolder = BeaconShield.getPlugin().getDataFolder();
        File dataFolder = new File(pluginFolder, "data");
        String fileName = file.getName();

        // Check if String fileName is end with ".beacon"
        if (!fileName.endsWith(".beacon")) {
            return null;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File(dataFolder, fileName)))) {
            Object obj = ois.readObject();

            if (obj instanceof BeaconShieldBlock) {
                BeaconShieldBlock block = (BeaconShieldBlock) obj;
                block.reinitializePDC();

                return block;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Delete the file for the BeaconShieldBlock.
     * @param beacon The BeaconShieldBlock.
     */
    public static void deleteBeaconFile(BeaconShieldBlock beacon) {
        File file = new File(BeaconShield.getPlugin().getDataFolder() + "/data", beacon.getId() + ".beacon");
        file.delete();
    }
}
