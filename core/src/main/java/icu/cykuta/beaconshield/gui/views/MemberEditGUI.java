package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.gui.GUI;
import icu.cykuta.beaconshield.gui.GUIClick;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.HeadHelper;
import icu.cykuta.beaconshield.utils.Text;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Menu to manage a single member: promote/demote, kick or transfer
 * the beacon ownership.
 */
public class MemberEditGUI extends GUI {
    private final OfflinePlayer member;

    public MemberEditGUI(BeaconShieldBlock beacon, OfflinePlayer member) {
        super(beacon, "inventory-title-edit-member", 9);
        this.member = member;
    }

    @Override
    protected void populate() {
        this.addDecoration(1, 7);
        this.addButton(0, HeadHelper.getHead(this.member), click -> { });
        this.addButton(2, "member-gui.give-ownership", this::giveOwnership);
        this.addButton(4, "member-gui.kick", this::kick);
        this.addButton(8, "global.back", click -> new MembersGUI(this.beacon).open(click.clicker()));

        if (this.beacon.hasRole(this.member, PlayerRole.OFFICER)) {
            this.addButton(3, "member-gui.demote", click -> this.setRole(click.clicker(), PlayerRole.MEMBER));
        } else {
            this.addButton(3, "member-gui.promote", click -> this.setRole(click.clicker(), PlayerRole.OFFICER));
        }
    }

    private void setRole(Player player, PlayerRole role) {
        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.beacon.hasRole(this.member, PlayerRole.OWNER)) {
            Chat.send(player, "cannot-demote-owner");
            return;
        }

        this.beacon.setPlayerRole(this.member, role);

        String roleName = ConfigHandler.getInstance().getLang().getString(role.getLangKey());
        Chat.send(player, "member-role-updated", this.member.getName(), Text.color(roleName));

        new MembersGUI(this.beacon).open(player);
    }

    private void giveOwnership(GUIClick click) {
        Player player = click.clicker();

        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.beacon.hasRole(this.member, PlayerRole.OWNER)) {
            Chat.send(player, "already-owner", this.member.getName());
            return;
        }

        this.openConfirmation(player, confirm -> {
            // Demote the old owner and transfer the ownership
            OfflinePlayer oldOwner = this.beacon.getOwner();
            this.beacon.setPlayerRole(oldOwner, PlayerRole.MEMBER);
            this.beacon.setOwner(this.member);

            confirm.clicker().closeInventory();
            Chat.send(confirm.clicker(), "owner-given", this.member.getName());
        });
    }

    private void kick(GUIClick click) {
        Player player = click.clicker();

        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OFFICER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        if (this.beacon.hasRole(this.member, PlayerRole.OWNER)) {
            Chat.send(player, "cannot-kick-owner");
            return;
        }

        // Officers can only kick plain members; kicking another officer
        // requires owner permission (or admin bypass).
        if (this.beacon.hasRole(this.member, PlayerRole.OFFICER)
                && !this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "cannot-kick-officer");
            return;
        }

        this.openConfirmation(player, confirm -> {
            this.beacon.removeAllowedPlayer(this.member);
            Chat.send(confirm.clicker(), "member-removed", this.member.getName());
            new MembersGUI(this.beacon).open(confirm.clicker());
        });
    }
}
