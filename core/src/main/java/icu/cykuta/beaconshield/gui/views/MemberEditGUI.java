package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.HeadHelper;
import icu.cykuta.beaconshield.config.PluginConfiguration;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class MemberEditGUI extends GUI {
    private final OfflinePlayer selectedPlayer;

    public MemberEditGUI(OfflinePlayer selectedPlayer) {
        super("inventory-title-edit-member", 9);
        this.selectedPlayer = selectedPlayer;
    }

    @Override
    public void populateInventory() {
        this.setDecorationSlots(1, 7);
        this.addInventoryButton(0, HeadHelper.getHead(selectedPlayer), (guiClick) -> {});
        this.addInventoryButton(2, "give-ownership", this::giveOwner);
        this.addInventoryButton(4, "kick", this::kick);
        this.addInventoryButton(8, "back", (guiClick) -> this.openGUI(guiClick.clicker(), new MembersGUI()));

        switch (this.getBeaconBlock().getPlayerRole(this.selectedPlayer)) {
            case MEMBER:
                this.addInventoryButton(3, "promote", (guiClick) -> this.setRole(guiClick.clicker(), PlayerRole.OFFICER));
                break;
            case OFFICER, OWNER:
                this.addInventoryButton(3, "demote", (guiClick) -> this.setRole(guiClick.clicker(), PlayerRole.MEMBER));
                break;
        }
    }

    private void setRole(Player player, PlayerRole role) {
        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.getBeaconBlock().hasPermissionLevel(this.selectedPlayer, PlayerRole.OWNER, true)) {
            Chat.send(player, "cannot-demote-owner");
            return;
        }

        this.getBeaconBlock().setPlayerRole(this.selectedPlayer, role);

        PluginConfiguration lang = ConfigHandler.getInstance().getLang();
        Chat.send(player,
                "member-role-updated",
                this.selectedPlayer.getName(),
                Text.color(lang.getString(role.getLangKey())));

        this.openGUI(player, new MembersGUI());
    }

    private void giveOwner(GUIClick click) {
        Player player = click.clicker();

        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.getBeaconBlock().hasPermissionLevel(this.selectedPlayer, PlayerRole.OWNER)) {
            Chat.send(player, "already-owner", this.selectedPlayer.getName());
            return;
        }


        this.openConfirmationGUI(player, (guiClick) -> {
            // Set the old owner to member
            OfflinePlayer oldOwner = this.getBeaconBlock().getOwner();
            this.getBeaconBlock().setPlayerRole(oldOwner, PlayerRole.MEMBER);

            // Set the new owner
            this.getBeaconBlock().setOwner(this.selectedPlayer);
            guiClick.clicker().closeInventory();
            Chat.send(guiClick.clicker(), "owner-given", this.selectedPlayer.getName());
        });
    }

    private void kick(GUIClick guiClick) {
        Player player = guiClick.clicker();

        if (!this.getBeaconBlock().hasPermissionLevel(player, PlayerRole.OFFICER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.getBeaconBlock().hasPermissionLevel(this.selectedPlayer, PlayerRole.OFFICER)) {
            Chat.send(player, "cannot-kick-officer");
            return;
        }

        if (this.getBeaconBlock().hasPermissionLevel(this.selectedPlayer, PlayerRole.OWNER, true)) {
            Chat.send(player, "cannot-kick-owner");
            return;
        }

        this.openConfirmationGUI(player, (confirmationClick) -> {
            Player confirmationPlayer = confirmationClick.clicker();
            Chat.send(confirmationPlayer, "member-removed", this.selectedPlayer.getName());
            this.getBeaconBlock().removeAllowedPlayer(this.selectedPlayer);
            this.openGUI(confirmationPlayer, new MembersGUI());
        });
    }
}
