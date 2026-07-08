package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu to configure the minimum role required for each territory permission.
 * Clicking a permission cycles its minimum role.
 */
public class GroupManagementGUI extends GUI {

    public GroupManagementGUI(BeaconShieldBlock beacon) {
        super(beacon, "inventory-title-group-management", 45);
    }

    @Override
    protected void populate() {
        this.addDecoration(
                0,  1,  2,  3,  4,  5,  6,  7,  8,
                9,                              17,
                18,                             26,
                27,                             35,
                    37, 38, 39, 40, 41, 42, 43, 44
        );

        this.addPermissionButton(10, "group-management-gui.permission-build", RolePermission.BUILD);
        this.addPermissionButton(11, "group-management-gui.permission-break", RolePermission.BREAK);
        this.addPermissionButton(12, "group-management-gui.permission-use", RolePermission.USE);
        this.addPermissionButton(13, "group-management-gui.permission-entity", RolePermission.ENTITY);
        this.addPermissionButton(14, "group-management-gui.permission-beacon", RolePermission.BEACON_USE);

        this.addButton(36, "global.back", click -> this.openMainGUI(click.clicker()));
    }

    private void addPermissionButton(int slot, String configPath, RolePermission permission) {
        ItemStack item = this.createPermissionItem(configPath, permission);
        this.addButton(slot, item, click -> this.cycleMinimumRole(click.clicker(), permission));
    }

    /**
     * Create the permission item with the current minimum role appended
     * to its lore.
     */
    private ItemStack createPermissionItem(String configPath, RolePermission permission) {
        PluginConfiguration lang = ConfigHandler.getInstance().getLang();

        ItemStack item = this.guiConfig.getItemStack(configPath);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        PlayerRole currentRole = this.beacon.getMinimumRoleForPermission(permission);
        String roleName = lang.getString(currentRole.getLangKey());
        String minimumRoleLine = Text.color(Text.replace(lang.getString("minimum-role"), roleName));

        List<String> lore = meta.getLore() != null ? meta.getLore() : new ArrayList<>();
        lore.add(""); // empty line for readability
        lore.add(minimumRoleLine);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Cycle the minimum role required for a permission
     * (MEMBER -> OFFICER -> OWNER -> MEMBER).
     */
    private void cycleMinimumRole(Player player, RolePermission permission) {
        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-to-use");
            return;
        }

        PlayerRole currentRole = this.beacon.getMinimumRoleForPermission(permission);
        this.beacon.setRolePermissions(permission, currentRole.getNext());
        this.refresh();
    }
}
