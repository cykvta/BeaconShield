package icu.cykuta.beaconshield.gui.views;

import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.PlayerRole;
import icu.cykuta.beaconshield.config.ConfigHandler;
import icu.cykuta.beaconshield.gui.PaginationGUI;
import icu.cykuta.beaconshield.utils.HeadHelper;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Paginated list of the beacon members. Clicking a head opens the
 * member edit menu.
 */
public class MembersGUI extends PaginationGUI<OfflinePlayer> {

    public MembersGUI(BeaconShieldBlock beacon) {
        super(beacon, "inventory-title-members");
    }

    @Override
    protected void addControls() {
        this.addButton(36, "global.back", click -> this.openMainGUI(click.clicker()));
        this.addButton(40, "member-gui.add-member", click -> new InviteGUI(this.beacon).open(click.clicker()));
    }

    @Override
    protected List<OfflinePlayer> getItems() {
        return this.beacon.getAllowedPlayers();
    }

    @Override
    protected void renderItem(int slot, OfflinePlayer member) {
        PlayerRole role = this.beacon.getPlayerRole(member);
        String roleName = role != null ? ConfigHandler.getInstance().getLang().getString(role.getLangKey()) : "";

        ItemStack head = HeadHelper.getHead(member, member.getName(), roleName);
        this.addButton(slot, head, click -> new MemberEditGUI(this.beacon, member).open(click.clicker()));
    }
}
