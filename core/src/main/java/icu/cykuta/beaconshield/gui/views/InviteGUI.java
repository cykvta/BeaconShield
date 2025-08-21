package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.HeadHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;

public class InviteGUI extends PaginationGUI {
    public InviteGUI() {
        super("inventory-title-invite-member");
    }

    /**
     * Render server player list as heads
     */
    @Override
    protected void render() {
        // Back button
        this.addInventoryButton(36, "global.back",
                (guiClick) -> this.openGUI(guiClick.clicker(), new MembersGUI()));
        this.addDecorationSlot(40);

        Player[] rawOnlinePlayers = Bukkit.getOnlinePlayers().toArray(new Player[0]);

        // Copy the array
        ArrayList<Player> onlinePlayers = new ArrayList<>();
        Collections.addAll(onlinePlayers, rawOnlinePlayers);

        for (int i = 0; i < renderSlots.size(); i++) {
            int slot = renderSlots.get(i);

            if (this.offset + i < onlinePlayers.size()) {
                // Create head item
                Player selectedPlayer = onlinePlayers.get(this.offset + i);

                // Skip if player is already a member
                if (this.getBeaconBlock().hasMember(selectedPlayer)) {
                    // remove the player from the list
                    onlinePlayers.remove(selectedPlayer);
                    i--;
                    continue;
                }

                ItemStack head = HeadHelper.getHead(selectedPlayer);
                this.addInventoryButton(slot, head, (guiClick) -> this.addMember(guiClick.clicker(), selectedPlayer));
            }
        }
    }

    private void addMember(Player player, Player selectedPlayer) {
        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        BeaconShieldBlock bsd = this.getBeaconBlock();
        bsd.addAllowedPlayer(selectedPlayer, PlayerRole.MEMBER);
        Chat.send(player, "member-added", selectedPlayer.getName());
        this.openGUI(player, new MembersGUI());
    }
}
