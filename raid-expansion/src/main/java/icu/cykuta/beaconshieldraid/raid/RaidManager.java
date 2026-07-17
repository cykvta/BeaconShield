package icu.cykuta.beaconshieldraid.raid;

import icu.cykuta.beaconshield.BeaconShieldAPI;
import icu.cykuta.beaconshield.beacon.BeaconShieldBlock;
import icu.cykuta.beaconshield.beacon.protection.ProtectedChunk;
import icu.cykuta.beaconshield.utils.MathUtils;
import icu.cykuta.beaconshieldraid.config.RaidConfig;
import icu.cykuta.beaconshieldraid.events.ChunkCapturedEvent;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Central authority of the raid expansion: holds the global raiding
 * state and the per-protection {@link Raid} progress, and drives chunk
 * captures and protection reset.
 */
public class RaidManager {

    private final BeaconShieldAPI api;
    private final RaidConfig config;

    private boolean raidingEnabled;
    private final Map<String, Raid> raids = new HashMap<>();
    private final RaidDisplay display;

    public RaidManager(BeaconShieldAPI api, RaidConfig config) {
        this.api = api;
        this.config = config;
        this.raidingEnabled = config.isInitialRaidingEnabled();
        this.display = new RaidDisplay(config);
    }

    public BeaconShieldAPI getApi() {
        return api;
    }

    public RaidConfig getConfig() {
        return config;
    }

    public RaidDisplay getDisplay() {
        return display;
    }

    public boolean isRaidingEnabled() {
        return raidingEnabled;
    }

    /**
     * Set the raiding flag with no side effects or announcement. Used when
     * restoring the persisted state on startup.
     */
    public void restoreRaidingEnabled(boolean enabled) {
        this.raidingEnabled = enabled;
    }

    /**
     * Enable or disable raiding globally. Disabling resets every raid when
     * {@code reset-on-disable} is set. Broadcasts the state change.
     */
    public void setRaidingEnabled(boolean enabled) {
        if (this.raidingEnabled == enabled) {
            return;
        }
        this.raidingEnabled = enabled;

        if (!enabled && config.isResetOnDisable()) {
            resetAllRaids();
        }

        broadcast(config.message(enabled ? "raids-enabled" : "raids-disabled"));
    }

    // Raid registry ----------------------------------------------------

    /**
     * Get the raid of a beacon, creating an empty one if needed.
     */
    public Raid getOrCreateRaid(BeaconShieldBlock beacon) {
        return raids.computeIfAbsent(beacon.getId(), id -> new Raid(beacon));
    }

    @Nullable
    public Raid getRaid(BeaconShieldBlock beacon) {
        return raids.get(beacon.getId());
    }

    /**
     * Find the raid whose party contains a player, or {@code null}. Lets a
     * raider leave/cancel from anywhere, not only inside the protection.
     */
    @Nullable
    public Raid findRaidOfPlayer(java.util.UUID uuid) {
        for (Raid raid : raids.values()) {
            if (raid.isInParty(uuid)) {
                return raid;
            }
        }
        return null;
    }

    /**
     * Cancel a raid because its leader left, announcing it.
     */
    public void leaderLeft(BeaconShieldBlock beacon) {
        broadcast(config.message("raid-ended-leader-left", "%owner%", ownerName(beacon)));
        removeRaid(beacon);
    }

    /**
     * Snapshot of every active raid, for persistence.
     */
    public List<Raid> getActiveRaids() {
        return new ArrayList<>(raids.values());
    }

    /**
     * Register a raid rebuilt from disk.
     */
    public void loadRaid(Raid raid) {
        raids.put(raid.getBeaconId(), raid);
    }

    /**
     * Drop a raid and clear its display. Raiders lose their grief access
     * (granted live through the protection bypass) as soon as it is gone.
     */
    public void removeRaid(BeaconShieldBlock beacon) {
        if (raids.remove(beacon.getId()) != null) {
            display.clear(beacon.getId());
        }
    }

    /**
     * Reset (drop) every active raid.
     */
    public void resetAllRaids() {
        for (Raid raid : new ArrayList<>(raids.values())) {
            display.clear(raid.getBeaconId());
        }
        raids.clear();
    }

    /**
     * Discard a raid that has no capture progress left, so idle beacons do
     * not stay tracked.
     */
    public void dropIfIdle(BeaconShieldBlock beacon) {
        Raid raid = raids.get(beacon.getId());
        if (raid != null && raid.isIdle()) {
            raids.remove(beacon.getId());
        }
    }

    // Capture ----------------------------------------------------------

    /**
     * Finish capturing a chunk (a step toward exposing the nexus): record
     * it and announce it. Grief access is not per-chunk; raiders can grief
     * the whole protection while the raid is active (see the protection
     * bypass registered on startup).
     */
    public void captureChunk(BeaconShieldBlock beacon, Raid raid, ProtectedChunk chunk) {
        raid.markCaptured(chunk);

        broadcast(config.message("chunk-captured",
                "%owner%", ownerName(beacon),
                "%x%", String.valueOf(chunk.getX()),
                "%z%", String.valueOf(chunk.getZ())));

        Bukkit.getPluginManager().callEvent(new ChunkCapturedEvent(beacon, chunk));

        // Announce the nexus becoming vulnerable the moment the last
        // capturable chunk falls.
        if (!getCapturableChunks(beacon).isEmpty() && isNexusVulnerable(beacon, raid)) {
            broadcast(config.message("nexus-exposed", "%owner%", ownerName(beacon)));
        }
    }

    // Start / join -----------------------------------------------------

    /**
     * Start a raid on a protection with the given initiator, who is added
     * to the attacking party. Opens the freeze window during which nearby
     * players may join, and sends the clickable join invite. The monetary
     * cost is charged by the caller.
     */
    public Raid startRaid(BeaconShieldBlock beacon, Player initiator) {
        Raid raid = getOrCreateRaid(beacon);
        raid.setActive(true);
        raid.setStartedAt(System.currentTimeMillis());
        raid.setInitiator(initiator.getUniqueId());
        raid.addToParty(initiator.getUniqueId());
        raid.setOrigin(initiator.getLocation());

        int freeze = config.getFreezeSeconds();
        raid.setFreezeEndsAt(System.currentTimeMillis() + freeze * 1000L);

        if (freeze > 0) {
            sendJoinInvite(beacon, raid, initiator);
        } else {
            // No freeze window: the raid begins immediately.
            raid.setBeganAnnounced(true);
            announceRaidBegan(beacon);
        }
        return raid;
    }

    /**
     * Add a player to an existing raid's party.
     */
    public void joinRaid(Raid raid, Player player) {
        raid.addToParty(player.getUniqueId());
    }

    /**
     * The raid of a given beacon id, or {@code null}. A raid is keyed by
     * the beacon it targets, so the beacon id identifies it. The join
     * invite carries this id in its button, letting a request name the
     * exact raid it answers instead of inferring one from where the player
     * stands.
     */
    @Nullable
    public Raid getRaidById(String beaconId) {
        return raids.get(beaconId);
    }

    /**
     * Whether a player is close enough to the raid origin to join it.
     * Allows the join when the origin cannot be resolved.
     */
    public boolean isWithinJoinRadius(Raid raid, Player player) {
        Location origin = raid.getOrigin();
        if (origin == null || origin.getWorld() == null) {
            return true;
        }
        if (!player.getWorld().equals(origin.getWorld())) {
            return false;
        }
        double radius = config.getJoinRadius();
        return player.getLocation().distanceSquared(origin) <= radius * radius;
    }

    /**
     * Announce that a raid has left the freeze window and begun.
     */
    public void announceRaidBegan(BeaconShieldBlock beacon) {
        broadcast(config.message("raid-began", "%owner%", ownerName(beacon)));
    }

    /**
     * Send the clickable "[Request to join]" announcement to every outsider
     * within the join radius of the raid origin. Clicking it requests to
     * join the raid named in the button; the leader then accepts or rejects.
     * Skipped entirely when {@code join.invite} is off in the config.
     */
    private void sendJoinInvite(BeaconShieldBlock beacon, Raid raid, Player initiator) {
        if (!config.isJoinInviteEnabled()) {
            return;
        }

        Location origin = raid.getOrigin();
        if (origin == null || origin.getWorld() == null) {
            return;
        }

        String base = config.message("raid-freeze-broadcast",
                "%initiator%", initiator.getName(),
                "%owner%", ownerName(beacon),
                "%seconds%", String.valueOf(config.getFreezeSeconds()));
        if (base == null) {
            return; // invite silenced in the config
        }

        TextComponent component = legacyComponent(base + " ");
        component.addExtra(button(
                config.colored("raid-request-button", "&a&l[Request to join]"),
                "/bsraid request " + raid.getBeaconId(),
                config.colored("raid-request-hover", "&7Click to request to join the raid")));

        double radiusSquared = config.getJoinRadius() * config.getJoinRadius();
        for (Player nearby : origin.getWorld().getPlayers()) {
            if (beacon.hasMember(nearby) || nearby.getUniqueId().equals(initiator.getUniqueId())) {
                continue;
            }
            if (nearby.getLocation().distanceSquared(origin) <= radiusSquared) {
                nearby.spigot().sendMessage(component);
            }
        }
    }

    /**
     * Notify the raid leader (if online) of a join request, with clickable
     * accept/reject buttons.
     */
    public void notifyLeaderOfRequest(BeaconShieldBlock beacon, Raid raid, Player requester) {
        if (raid.getInitiator() == null) {
            return;
        }
        Player leader = Bukkit.getPlayer(raid.getInitiator());
        if (leader == null) {
            return;
        }

        String base = config.message("raid-request-received", "%player%", requester.getName());
        if (base == null) {
            base = config.format("&6" + requester.getName() + " &ewants to join your raid:");
        }

        TextComponent component = legacyComponent(base + " ");
        component.addExtra(button(
                config.colored("raid-accept-button", "&a&l[Accept]"),
                "/bsraid accept " + requester.getName(),
                config.colored("raid-accept-hover", "&7Accept the request")));
        component.addExtra(new TextComponent(" "));
        component.addExtra(button(
                config.colored("raid-reject-button", "&c&l[Reject]"),
                "/bsraid reject " + requester.getName(),
                config.colored("raid-reject-hover", "&7Reject the request")));

        leader.spigot().sendMessage(component);
    }

    private TextComponent legacyComponent(String legacy) {
        TextComponent component = new TextComponent();
        for (BaseComponent part : TextComponent.fromLegacyText(legacy)) {
            component.addExtra(part);
        }
        return component;
    }

    private TextComponent button(String text, String command, String hover) {
        TextComponent button = new TextComponent(TextComponent.fromLegacyText(text));
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText(hover)));
        return button;
    }

    /**
     * Whether a player currently counts as an attacker on a protection:
     * never a member of it, and either in the raid party or — for an
     * admin-forced raid with no party — any outsider.
     */
    public boolean countsAsAttacker(BeaconShieldBlock beacon, @Nullable Raid raid, Player player) {
        if (raid == null || beacon.hasMember(player)) {
            return false;
        }
        return raid.getParty().isEmpty() || raid.isInParty(player.getUniqueId());
    }

    /**
     * Whether a chunk belongs to a protection with a raid past its freeze
     * window (used to suppress upgrades there).
     */
    public boolean hasActiveRaid(org.bukkit.Chunk chunk) {
        BeaconShieldBlock beacon = api.getBeaconOwning(new ProtectedChunk(chunk));
        if (beacon == null) {
            return false;
        }
        Raid raid = getRaid(beacon);
        return raid != null && raid.isActive() && !raid.isInFreeze();
    }

    /**
     * Whether a player may grief a chunk right now: it belongs to a
     * protection whose raid has begun and the player is one of its raiders.
     */
    public boolean isRaider(Player player, org.bukkit.Chunk chunk) {
        BeaconShieldBlock beacon = api.getBeaconOwning(new ProtectedChunk(chunk));
        if (beacon == null) {
            return false;
        }
        Raid raid = getRaid(beacon);
        return raid != null && raid.isActive() && !raid.isInFreeze()
                && countsAsAttacker(beacon, raid, player);
    }

    /**
     * Beacon interaction guard: lock the menu (and tell the player) while
     * the beacon has a raid, freeze included.
     */
    public boolean handleBeaconLock(BeaconShieldBlock beacon, Player player) {
        if (getRaid(beacon) == null) {
            return false;
        }
        String message = config.message("beacon-locked", "%owner%", ownerName(beacon));
        if (message != null) {
            player.sendMessage(message);
        }
        return true;
    }

    /**
     * Whether any raider is currently standing in a protected chunk of the
     * protection (used for the "defenders win" timeout).
     */
    public boolean hasRaiderInProtection(BeaconShieldBlock beacon, Raid raid, org.bukkit.World world) {
        for (Player player : world.getPlayers()) {
            if (countsAsAttacker(beacon, raid, player)
                    && beacon.isProtectedChunk(new ProtectedChunk(player.getLocation().getChunk()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * End a raid because no raiders held the protection long enough: the
     * defenders win and the raid is cancelled (beacon kept).
     */
    public void defendersWin(BeaconShieldBlock beacon) {
        broadcast(config.message("raid-ended-defenders-won", "%owner%", ownerName(beacon)));
        removeRaid(beacon);
    }

    /**
     * Online raiders (party members) — the attacker-side display audience.
     */
    public List<Player> getOnlineRaiders(Raid raid) {
        List<Player> raiders = new ArrayList<>();
        for (java.util.UUID uuid : raid.getParty()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                raiders.add(player);
            }
        }
        return raiders;
    }

    /**
     * Online protection members — the defender-side display audience.
     */
    public List<Player> getOnlineDefenders(BeaconShieldBlock beacon) {
        List<Player> defenders = new ArrayList<>();
        for (OfflinePlayer member : beacon.getAllowedPlayers()) {
            Player player = member.getPlayer();
            if (player != null) {
                defenders.add(player);
            }
        }
        return defenders;
    }

    /**
     * End a raid because the beacon ran out of fuel: announce it and
     * destroy the beacon (its {@code BeaconShieldDestroyedEvent} cleans up
     * the raid state).
     */
    public void endRaidByFuel(BeaconShieldBlock beacon) {
        broadcast(config.message("raid-ended-no-fuel", "%owner%", ownerName(beacon)));
        api.destroyBeacon(beacon, null, config.isNexusDropLoot());
    }

    /**
     * The monetary cost to start a raid on a protection, from the config
     * formula. Placeholders: %chunks%, %capturable%, %members%,
     * %online_members%. Falls back to 0 on a malformed formula.
     */
    public double computeStartCost(BeaconShieldBlock beacon) {
        if (!config.isStartCostEnabled()) {
            return 0;
        }

        String formula = config.getStartCostFormula()
                .replace("%chunks%", String.valueOf(beacon.getProtectedChunks().size()))
                .replace("%capturable%", String.valueOf(getCapturableChunks(beacon).size()))
                .replace("%members%", String.valueOf(beacon.getAllowedPlayers().size()))
                .replace("%online_members%", String.valueOf(countOnlineMembers(beacon)));

        try {
            return Math.max(0, MathUtils.eval(formula));
        } catch (RuntimeException e) {
            Bukkit.getLogger().warning("[RaidExpansion] Invalid start-cost formula '"
                    + config.getStartCostFormula() + "': " + e.getMessage());
            return 0;
        }
    }

    public int countOnlineMembers(BeaconShieldBlock beacon) {
        int online = 0;
        for (OfflinePlayer member : beacon.getAllowedPlayers()) {
            if (member.isOnline()) {
                online++;
            }
        }
        return online;
    }

    /**
     * How many of a protection's members must be online for it to be
     * raidable, per the online-requirement config.
     */
    public int requiredOnlineDefenders(BeaconShieldBlock beacon) {
        return config.requiredOnlineToRaid(beacon.getAllowedPlayers().size());
    }

    /**
     * Whether enough of a protection's members are online to start a raid.
     * Always true when offline raiding is allowed. Admin-forced raids skip
     * this check.
     */
    public boolean hasEnoughDefendersOnline(BeaconShieldBlock beacon) {
        if (config.isAllowOfflineRaid()) {
            return true;
        }
        return countOnlineMembers(beacon) >= requiredOnlineDefenders(beacon);
    }

    /**
     * Force a full breach of a protection: instantly capture every
     * non-core chunk and expose the nexus. Used by the admin force command.
     *
     * @return the number of chunks captured by this call.
     */
    public int forceBreach(BeaconShieldBlock beacon) {
        Raid raid = getOrCreateRaid(beacon);
        raid.setActive(true);
        if (raid.getStartedAt() == 0) {
            raid.setStartedAt(System.currentTimeMillis());
        }
        raid.setBeganAnnounced(true); // forced raids skip the freeze window
        int captured = 0;
        for (ProtectedChunk chunk : getCapturableChunks(beacon)) {
            if (!raid.isCaptured(chunk)) {
                raid.markCaptured(chunk);
                Bukkit.getPluginManager().callEvent(new ChunkCapturedEvent(beacon, chunk));
                captured++;
            }
        }
        broadcast(config.message("nexus-exposed", "%owner%", ownerName(beacon)));
        return captured;
    }

    /**
     * Cancel a protection's raid and clear its progress. Returns
     * {@code true} if there was a raid to cancel.
     */
    public boolean cancelRaid(BeaconShieldBlock beacon) {
        if (getRaid(beacon) == null) {
            return false;
        }
        removeRaid(beacon);
        return true;
    }

    // Helpers ----------------------------------------------------------

    /**
     * The non-core chunks of a protection (the ones captured via hot points).
     */
    public List<ProtectedChunk> getCapturableChunks(BeaconShieldBlock beacon) {
        List<ProtectedChunk> chunks = new ArrayList<>();
        for (ProtectedChunk chunk : beacon.getProtectedChunks()) {
            if (!beacon.isCoreChunk(chunk)) {
                chunks.add(chunk);
            }
        }
        return chunks;
    }

    /**
     * Whether every non-core chunk of a protection has been captured, so
     * the nexus can be broken.
     */
    public boolean isNexusVulnerable(BeaconShieldBlock beacon, @Nullable Raid raid) {
        int captured = raid == null ? 0 : raid.getCapturedCount();
        return captured >= getCapturableChunks(beacon).size();
    }

    @Nullable
    public BeaconShieldBlock getBeaconById(String id) {
        for (BeaconShieldBlock beacon : api.getBeacons()) {
            if (beacon.getId().equals(id)) {
                return beacon;
            }
        }
        return null;
    }

    public String ownerName(BeaconShieldBlock beacon) {
        try {
            String name = beacon.getOwner().getName();
            return name == null ? "?" : name;
        } catch (RuntimeException e) {
            return "?";
        }
    }

    /**
     * Broadcast a message to the whole server, ignoring null/empty ones.
     */
    public void broadcast(@Nullable String message) {
        if (message != null && !message.isEmpty()) {
            Bukkit.broadcastMessage(message);
        }
    }
}
