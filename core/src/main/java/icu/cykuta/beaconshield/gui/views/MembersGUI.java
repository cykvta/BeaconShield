package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.HeadHelper;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class MembersGUI extends PaginationGUI {
    public MembersGUI() {
        super("inventory-title-members");
    }

    /**
     * Render member list as heads
     */
    @Override
    protected void render() {
        PluginConfiguration lang = ConfigHandler.getInstance().getLang();

        // Add buttons
        this.addInventoryButton(36, "back", (guiClick) -> this.openGUI(guiClick.clicker(), new BeaconGUI()));
        this.addInventoryButton(40, "add-member", (guiClick) -> this.openGUI(guiClick.clicker(), new InviteGUI()));

        List<OfflinePlayer> players = this.getBeaconBlock().getAllowedPlayers();

        for (int i = 0; i < renderSlots.size(); i++) {
            int slot = renderSlots.get(i);

            if (this.offset + i < players.size()) {
                // Create head item
                OfflinePlayer selectedPlayer = players.get(this.offset + i);
                PlayerRole role = this.getBeaconBlock().getPlayerRole(selectedPlayer);
                String lore = lang.getString(role.getLangKey());
                ItemStack head = HeadHelper.getHead(selectedPlayer, selectedPlayer.getName(), lore);
                this.addInventoryButton(slot, head, (guiClick) -> openGUI(guiClick.clicker(), new MemberEditGUI(selectedPlayer)));
            }
        }
    }
}
