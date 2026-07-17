# BeaconShield — Raid Expansion

Makes protections raidable. Attackers capture a territory **chunk by chunk** by standing in it, and once every chunk has fallen the beacon becomes a **nexus** they must break to bring the whole protection down.

Requires the [Core](../core/README.md) plugin.

## Installation

1. Install `BeaconShield-core.jar` first.
2. Drop `BeaconShield-RaidExpansion.jar` into your `plugins` folder.
3. Restart the server.

The expansion keeps its files inside the core's folder, not its own:

```
plugins/BeaconShield/expansions/BeaconShield-RaidExpansion/
├── config.yml
├── lang.yml
└── raids.db
```

Raiding starts **disabled** (`raiding-enabled: false`). Turn it on with `/bsraid toggle on`, or let the [schedule](#automatic-schedule) drive it.

## How a raid works

1. **Raiding must be on.** The global switch is `/bsraid toggle`, or automatic via the schedule.
2. **Start it**: stand inside someone else's protection and run `/bsraid start`. The plugin checks that enough of its members are [online](#online-defenders-required), then charges the [start cost](#start-cost) through Vault. You cannot raid a protection you belong to.
3. **Freeze window** (`join.freeze-seconds`, default 30s): the raid has not begun yet. Nearby players get a clickable **[Request to join]** message; the leader answers with `/bsraid accept|reject`, or adds people directly with `/bsraid invite`. This is the only window in which the party can change.
4. **Capture phase**: each non-core chunk is taken by standing in it. A chunk only progresses while attackers inside **outnumber the defenders** and meet `capture.min-attackers`. Holding it for `capture.time-seconds` captures it. Lose the chunk and progress pauses, resets or decays depending on `capture.contested-behavior`.
5. **Nexus phase**: once every capturable chunk is taken, the beacon is exposed. Attackers hit it `nexus.health` times to destroy it — each hit drains 1 HP. Left alone for `nexus.regen-seconds`, it heals back to full.
6. **The protection falls**: the beacon is destroyed and, if `nexus.drop-loot` is on, drops the beacon item and its stored contents.

The **core chunk** (where the beacon sits) is never captured — it *is* the nexus.

### During a raid

- **Raiders may grief the whole protection** once the freeze ends, not just captured chunks.
- **Nobody can open the beacon menu** while a raid exists on it, freeze included.
- **Territory upgrades are neutralised** (`ignore-upgrades-during-raid`), so PvP and mob spawning work again. The upgrade items stay in the beacon and come back when the raid ends.
- A **directed display** shows the countdown, capture progress and nexus HP — different text for raiders and defenders (see [Display](#display)).

### How a raid ends

| Outcome | Cause |
|---|---|
| **Attackers win** | The nexus is broken: the protection falls and the beacon is destroyed. |
| **Defenders win** | No raider stands in any protected chunk for `defenders-win-seconds` (default 120s). The beacon is kept. |
| **Beacon runs out of fuel** | The protection falls on its own; the beacon is destroyed. |
| **Leader leaves** | `/bsraid leave` by the leader cancels the raid. |
| **Cancelled** | `/bsraid cancel` by the leader or an admin. |
| **Defender removes the beacon** | The raid ends with the beacon gone. |
| **Raiding turned off** | Every raid is reset if `reset-on-disable` is on. |

Raids are saved to `raids.db` and **survive restarts and crashes** (flushed every minute and on shutdown).

## Commands

The command is `/bsraid`. It is registered through the core's command framework.

| Command | Description | Permission |
|---|---|---|
| `/bsraid` | Show the help list. | — |
| `/bsraid start` | Start a raid on the protection you stand in (pays the start cost). | `beaconshield.raid.start` |
| `/bsraid request [raid]` | Ask the leader to let you join. Freeze window only, within `join.radius`. Without the argument, uses the raid where you stand. | `beaconshield.raid.join` |
| `/bsraid accept <player>` | Leader: accept a pending join request (freeze only). | — (leader) |
| `/bsraid reject <player>` | Leader: reject a pending join request (freeze only). | — (leader) |
| `/bsraid invite <player>` | Leader: add an online player to the party directly (freeze only). | — (leader) |
| `/bsraid kick <player>` | Leader: remove a member from the party (freeze only). | — (leader) |
| `/bsraid leave` | Leave your raid, from anywhere. If the leader leaves, the raid is cancelled. | `beaconshield.raid.join` |
| `/bsraid cancel` | Cancel the raid: the leader from anywhere, an admin on the protection they stand in. | — (leader or admin) |
| `/bsraid status` | Global raiding state, or full detail of the raid you are standing in. | — |
| `/bsraid toggle [on\|off]` | Enable/disable raiding globally. No argument flips it. | `beaconshield.raid.admin` |
| `/bsraid force` | Instantly capture every chunk of the protection you stand in and expose the nexus. Skips the online-members check. | `beaconshield.raid.admin` |
| `/bsraid reload` | Reload `config.yml` and `lang.yml`. | `beaconshield.raid.admin` |

Leader-only commands are not permission-gated: they check that you lead the raid.

## Permissions

| Permission | Description | Default |
|---|---|---|
| `beaconshield.raid.start` | Start a raid with `/bsraid start`. | Everyone |
| `beaconshield.raid.join` | Request to join a raid and leave it. | Everyone |
| `beaconshield.raid.admin` | Toggle raiding, force/cancel raids and reload the expansion. | OP |

## Configuration

`plugins/BeaconShield/expansions/BeaconShield-RaidExpansion/config.yml`. Apply changes with `/bsraid reload`.

| Option | Default | Description |
|---|---|---|
| `raiding-enabled` | `false` | Whether raiding is allowed right now. Toggled at runtime by `/bsraid toggle`; the schedule overrides it when enabled. |
| `reset-on-disable` | `true` | Turning raiding off resets every in-progress raid (restores protection, clears progress). |
| `defenders-win-seconds` | `120` | Seconds with no raider inside any protected chunk before the defenders win. `0` disables. |
| `ignore-upgrades-during-raid` | `true` | Ignore all territory upgrades inside a protection while its raid is running. |

### Capture

| Option | Default | Description |
|---|---|---|
| `capture.time-seconds` | `60` | Seconds attackers must hold a chunk to capture it. |
| `capture.min-attackers` | `1` | Minimum attackers in a chunk for capture to advance at all. |
| `capture.contested-behavior` | `DECAY` | What happens to progress while a chunk is not actively being taken: `PAUSE` (keep it), `RESET` (drop to zero), `DECAY` (lose progress gradually). |
| `capture.decay-per-second` | `1` | Seconds of progress lost per second, with `DECAY`. |

A chunk only advances while attackers inside **outnumber** the defenders inside it.

### Nexus

| Option | Default | Description |
|---|---|---|
| `nexus.health` | `10` | Hits needed to destroy the beacon once every chunk is captured. |
| `nexus.drop-loot` | `true` | Whether the destroyed nexus drops the beacon item and its stored items. |
| `nexus.regen-seconds` | `30` | Seconds without damage before the nexus fully regenerates. `0` disables. |

### Online defenders required

Controls whether a protection can be raided while its members are offline. `/bsraid force` ignores this.

| Option | Default | Description |
|---|---|---|
| `online-requirement.allow-offline` | `false` | Allow raiding with none of the protection's members online. When `true`, `required` is ignored. |
| `online-requirement.required` | `"50%"` | Members that must be online otherwise: a percentage of the member count, rounded up (`"50%"`), or an exact count (`"2"`). A percentage is always at least 1 and never more than the member count. |

With `50%`: 1 member needs 1 online, 2 → 1, 3 → 2, 5 → 3, 10 → 5.

### Start cost

Charged to the player running `/bsraid start`, through Vault. Free if no economy plugin is installed.

| Option | Default | Description |
|---|---|---|
| `start-cost.enabled` | `true` | Whether starting a raid costs money. |
| `start-cost.formula` | `"100"` | Cost formula. Supports `+ - * / ^ ( )` and `sqrt/sin/cos/tan`. A malformed formula logs a warning and costs 0. |

Formula placeholders:

| Placeholder | Value |
|---|---|
| `%chunks%` | Total chunks of the target protection, core chunk included. |
| `%capturable%` | Non-core chunks that must be captured. |
| `%members%` | Players allowed in the protection. |
| `%online_members%` | How many of those are online right now. |

Example — scale the price with the size of the base: `formula: "100 + (%capturable% * 50)"`.

### Freeze window & joining

| Option | Default | Description |
|---|---|---|
| `join.freeze-seconds` | `30` | Length of the freeze window. `0` starts the raid at once (no joining, no party changes). |
| `join.radius` | `10` | Players farther than this from where the raid started cannot request to join and get no announcement. |
| `join.invite` | `true` | Whether starting a raid broadcasts the clickable **[Request to join]** to nearby outsiders. With it off, players can still join via `/bsraid request` or a leader invite; the freeze window is unaffected. |

### Display

Shown to raiders and defenders: the countdown during the freeze, then capture progress and nexus HP. Text templates live in `lang.yml` (`freeze-display-*`, `capture-display-*`, `nexus-display-*`, `defenders-winning-*`).

| Option | Default | Description |
|---|---|---|
| `display.type` | `ACTIONBAR` | `ACTIONBAR`, `BOSSBAR`, `CHAT` or `NONE`. |
| `display.chat-interval-seconds` | `5` | For `CHAT`: minimum seconds between messages. |
| `display.bossbar-color` | `RED` | `PINK`, `BLUE`, `RED`, `GREEN`, `YELLOW`, `PURPLE`, `WHITE`. |
| `display.bossbar-style` | `SEGMENTED_10` | `SOLID`, `SEGMENTED_6`, `SEGMENTED_10`, `SEGMENTED_12`, `SEGMENTED_20`. |

Raiders see the status of the chunk they are standing on; defenders see how many chunks they have lost.

### Automatic schedule

When enabled, raiding is turned on inside the windows and off outside them, **overriding the manual on/off state**. Times use the server's local time in 24h `HH:mm`; days are `MON,TUE,WED,THU,FRI,SAT,SUN`. A window whose `to` is earlier than its `from` wraps past midnight. Malformed windows are skipped with a warning.

```yaml
schedule:
  enabled: false
  windows:
    - days: [FRI, SAT, SUN]
      from: "18:00"
      to: "22:00"
```

## Messages

Every message lives in `lang.yml`, with `&` color codes and a `prefix`. Set any value to `""` to silence it. It covers the start/join flow, the clickable request buttons, the display templates, and the broadcasts (`chunk-captured`, `nexus-exposed`, `nexus-damaged`, `nexus-destroyed`, and the raid-ended messages).

## API

Events fired by the expansion:

- `ChunkCapturedEvent`: attackers finished capturing a chunk. The chunk's protection is already suppressed when this fires.
- `ProtectionRaidedEvent`: fired right before a protection falls — every chunk captured and the nexus broken. Exposes the player who landed the final hit (may be `null`). The beacon is destroyed immediately after.

The expansion is built on the core's public hooks (`setUpgradeSuppressor`, `setProtectionBypass`, `setBeaconInteractionGuard`) — see the [Core API](../core/README.md#api).
