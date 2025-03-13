package icu.cykuta.beaconshield.data;

import icu.cykuta.beaconshield.BeaconShield;
import org.bukkit.NamespacedKey;

public class DataKeys {
    public static final NamespacedKey IS_BEACONSHIELD = new NamespacedKey(BeaconShield.getPlugin(), "isBeaconShield");
    public static final NamespacedKey BEACONSHIELD_INVENTORY = new NamespacedKey(BeaconShield.getPlugin(), "beaconShieldUpgrades");
}
