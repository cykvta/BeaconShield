package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.BeaconShield;
import icu.cykuta.beaconshield.beacon.PlayerRole;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.HeadManager;
import icu.cykuta.beaconshield.utils.PluginConfiguration;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

public class MembersGUI extends PaginationGUI {
    public MembersGUI() {
        super("inventory-title-members");
    }

    /**
     * Render member list as heads
     */
    @Override
    protected void render() {
        PluginConfiguration lang = BeaconShield.getPlugin().getFileHandler().getLang();

        // Add buttons
        this.addInventoryButton(36, "back", (guiClick) -> this.openGUI(guiClick.getClicker(), new BeaconGUI()));
        this.addInventoryButton(40, "add-member", (guiClick) -> this.openGUI(guiClick.getClicker(), new InviteGUI()));

        OfflinePlayer[] players = this.getBeaconBlock().getAllowedPlayers();

        for (int i = 0; i < renderSlots.size(); i++) {
            int slot = renderSlots.get(i);

            if (this.offset + i < players.length) {
                // Create head item
                OfflinePlayer selectedPlayer = players[this.offset + i];
                PlayerRole role = this.getBeaconBlock().getPlayerRole(selectedPlayer);
                String lore = lang.getString(role.getLangKey());
                ItemStack head = HeadManager.getHead(selectedPlayer, selectedPlayer.getName(), lore);
                this.addInventoryButton(slot, head, (guiClick) -> openGUI(guiClick.getClicker(), new MemberEditGUI(selectedPlayer)));
            }
        }
    }
}
