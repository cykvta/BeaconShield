package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.beacon.protection.RolePermission;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class GroupManagementGUI extends PaginationGUI {
    PluginConfiguration lang;

    public GroupManagementGUI() {
        super("inventory-title-group-management");
        this.lang = ConfigHandler.getInstance().getLang();
    }

    @Override
    protected void render() {
        // Permissions
        this.addPermissionButton(10, "group-management-gui.permission-build", RolePermission.BUILD);
        this.addPermissionButton(11, "group-management-gui.permission-break", RolePermission.BREAK);
        this.addPermissionButton(12, "group-management-gui.permission-use", RolePermission.USE);
        this.addPermissionButton(13, "group-management-gui.permission-entity", RolePermission.ENTITY);
        this.addPermissionButton(14, "group-management-gui.permission-beacon", RolePermission.BEACON_USE);

        // Back button
        this.addInventoryButton(36, "global.back", (guiClick) -> this.openGUI(guiClick.clicker(), new BeaconGUI()));
    }

    /**
     * Add a permission button to the GUI.
     * @param slot the slot to add the button
     * @param path the path to the item in the config
     * @param permission the permission to set when the button is clicked
     */
    private void addPermissionButton(int slot, String path, RolePermission permission) {
        ItemStack item = this.getPermissionItemStack(path, permission);
        this.addInventoryButton(slot, item, (guiClick) -> this.setNextRole(guiClick.clicker(), permission));
    }

    /**
     * Get the ItemStack for a permission button.
     * @param path the path to the item in the config
     * @param permission the permission to set when the button is clicked
     * @return the ItemStack for the permission button
     */
    private ItemStack getPermissionItemStack(String path, RolePermission permission) {
        // Get the item from the config
        ItemStack item = this.guiConfig.getItemStack(path);
        ItemMeta meta = item.getItemMeta();
        PlayerRole currentRole = this.getBeaconBlock().getMinimumRoleForPermission(permission);

        // Replace the role name in the lore with the current role's name
        String roleName = this.lang.getString(currentRole.getLangKey());
        String rawLore = Text.replace(this.lang.getString("minimum-role"), roleName);
        String formatedLore = Text.color(rawLore);

        // Add the lore
        List<String> lore = meta.getLore();
        lore.add(""); // empty line for better readability
        lore.add(formatedLore);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Set the next role for a player based on the permission.
     * @param player the player who clicked the button
     * @param permission the permission to set the next role for
     */
    private void setNextRole(Player player, RolePermission permission) {
        // check if the player is owner
        if (this.getBeaconBlock().getPlayerRole(player) != PlayerRole.OWNER) {
            Chat.send(player, "no-permission-to-use");
            return;
        }

        PlayerRole currentRole = this.getBeaconBlock().getMinimumRoleForPermission(permission);
        PlayerRole newRole = currentRole.getNext();
        this.getBeaconBlock().setRolePermissions(permission, newRole);
        this.render();
    }
}
