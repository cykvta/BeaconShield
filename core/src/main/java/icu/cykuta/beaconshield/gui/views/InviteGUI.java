package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.Chat;
import icu.cykuta.beaconshield.utils.HeadHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Paginated list of online players that are not members yet.
 * Clicking a head invites the player as a member.
 */
public class InviteGUI extends PaginationGUI<Player> {

    public InviteGUI(BeaconShieldBlock beacon) {
        super(beacon, "inventory-title-invite-member");
    }

    @Override
    protected void addControls() {
        this.addButton(36, "global.back", click -> new MembersGUI(this.beacon).open(click.clicker()));
    }

    @Override
    protected List<Player> getItems() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> !this.beacon.hasMember(player))
                .collect(Collectors.toList());
    }

    @Override
    protected void renderItem(int slot, Player candidate) {
        this.addButton(slot, HeadHelper.getHead(candidate), click -> this.addMember(click.clicker(), candidate));
    }

    private void addMember(Player player, Player candidate) {
        if (!this.beacon.hasPermissionLevel(player, PlayerRole.OWNER)) {
            Chat.send(player, "no-permission-action");
            return;
        }

        this.beacon.addAllowedPlayer(candidate, PlayerRole.MEMBER);
        Chat.send(player, "member-added", candidate.getName());
        new MembersGUI(this.beacon).open(player);
    }
}
