# BeaconShield — Core

The base plugin: beacon-based chunk protection with an in-game menu, member roles, fuel, grief protection, upgrades and economy integration.

Everything on this page ships with `BeaconShield-core.jar`. For raids, see the [Raid Expansion](../raid-expansion/README.md).

## Installation

1. Drop `BeaconShield-core.jar` into your server's `plugins` folder.
2. Restart the server.

Requires Spigot/Paper `1.20+`. Optional plugins: **Vault** (economy), **PlaceholderAPI** (placeholders), **WorldGuard** (region compatibility), **Dynmap** / **Squaremap** (map rendering).

## How it works

1. **Get a Beacon Shield**: craft it (recipe configurable in `config.yml`) or receive it with `/bsd give`.
2. **Place it**: the chunk where it is placed becomes the *core chunk* of your territory and is protected immediately.
3. **Right-click the beacon** to open the menu, where you can:
   - **Territory**: view a chunk map, preview chunk borders (right-click a chunk) and claim adjacent chunks (left-click). If economy is enabled, claiming costs money.
   - **Members**: invite players and manage their roles.
   - **Group management**: choose the minimum role required for each action inside the territory (see [Roles](#roles-and-territory-permissions)).
   - **Fuel slot**: store fuel items to keep the protection active (only if the fuel system is enabled).
   - **Upgrade slots**: place upgrade items to activate their effects.
   - **Destroy**: remove the beacon and get the item back (owner only). Beacons cannot be mined; this is the only way to remove them.

Players entering or leaving a territory see configurable greeting/farewell messages.

## Commands

The main command is `/beaconshield`, with `/bsd` as alias.

| Command | Description | Permission | Default |
|---|---|---|---|
| `/bsd` | Show the help message. | `beaconshield.command` | Everyone |
| `/bsd invite <player>` | Add a player as member of the territory you are standing in (owner only). | `beaconshield.invite` | Everyone |
| `/bsd kick <player>` | Remove a member from the territory you are standing in (officer or higher). | `beaconshield.kick` | Everyone |
| `/bsd give [player]` | Give a Beacon Shield item. | `beaconshield.give` | OP |
| `/bsd upgrade <upgrade> [player]` | Give an upgrade item. | `beaconshield.upgrade` | OP |
| `/bsd list [page]` | List all beacons on the server. | `beaconshield.list` | OP |
| `/bsd reload` | Reload the configuration files. | `beaconshield.reload` | OP |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `beaconshield.command` | Use the `/bsd` base command. | Everyone |
| `beaconshield.invite` | Use `/bsd invite`. | Everyone |
| `beaconshield.kick` | Use `/bsd kick`. | Everyone |
| `beaconshield.give` | Give Beacon Shield items. | OP |
| `beaconshield.upgrade` | Give upgrade items. | OP |
| `beaconshield.list` | List all beacons. | OP |
| `beaconshield.reload` | Reload the configuration. | OP |
| `beaconshield.bypass` | Bypass all territory protection checks. | OP |
| `beaconshield.max-beacons.<N>` | Override `max-beacons-per-player` for a player/group. The highest granted number wins. | — |
| `beaconshield.max-chunks.<N>` | Override `max-chunks-per-beacon` for a player/group. The highest granted number wins. | — |

Example with LuckPerms: `/lp user Steve permission set beaconshield.max-beacons.3` lets Steve place 3 beacons regardless of the config value.

## Roles and territory permissions

Each member of a territory has one of three roles:

| Role | Capabilities |
|---|---|
| **Owner** | Full control: claim chunks, destroy the beacon, manage all members, edit group permissions. |
| **Officer** | Manage regular members (invite/kick), plus everything a member can do. |
| **Member** | Interact inside the territory according to the group permissions. |

In the **Group management** menu the owner chooses the minimum role required for each action inside the territory:

- **Build**: place blocks (also required to light fires with flint and steel).
- **Break**: break blocks.
- **Use**: interact with blocks (doors, buttons, chests, ...).
- **Entity**: interact with entities.
- **Beacon use**: open the beacon menu.

Players without the required role (and without `beaconshield.bypass`) cannot perform the action inside the territory.

## Fuel system

The beacon consumes 1 fuel per second while active. Fuel items are stored in the fuel slot of the beacon menu and are consumed automatically when the current fuel runs out. **If the beacon runs out of fuel, the protection is disabled** until it is refueled.

- Fuel items and burn times are configurable (`fuel-items` in `config.yml`).
- A formula can scale burn time with territory size (`fuel-use-formula`, `fuel-formula`).
- The particle effect of an active beacon is configurable (`fuel-particles`, `fuel-particle`).
- The whole system can be turned off with `fuel-enabled: false`: beacons then never consume fuel, the fuel slot disappears from the menu and protection lasts forever.

## Grief protection

While a territory is protected (active beacon), it is guarded against environmental damage. Each check can be toggled in `config.yml`:

| Option | Protects against |
|---|---|
| `protection-explosions` | Creepers, TNT, withers, beds, respawn anchors, ... Blocks inside the territory are not destroyed. |
| `protection-fire` | Fire ignition and fire spread/burning. Members with build permission can still use flint and steel. |
| `protection-mob-grief` | Endermen stealing blocks, ravagers and silverfish. |

## Upgrades

Upgrade items are placed in the upgrade slots of the beacon menu and affect the whole territory:

- `disable_pvp`
- `disable_drowning`
- `disable_fall_damage`
- `disable_mob_spawning`

Obtain them with `/bsd upgrade <upgrade> [player]` or through their crafting recipes (`upgrade.yml`).

> With the Raid Expansion installed, upgrades can be neutralised while a protection is being raided (`ignore-upgrades-during-raid`).

## Economy (Vault)

If Vault and an economy plugin are installed, claiming chunks costs money:

- `base-price`: base price of a chunk.
- `economy-use-formula` / `economy-formula`: optional formula using `%base_price%`, `%distance%` (Manhattan distance from the core chunk) and `%chunks_owned%`.

Without Vault, claiming is free.

## PlaceholderAPI

With PlaceholderAPI installed the following placeholders are available:

| Placeholder | Value |
|---|---|
| `%beaconshield_beacons%` | Number of beacons owned by the player. |
| `%beaconshield_chunks%` | Total chunks protected by the player's beacons. |
| `%beaconshield_here_protected%` | `true`/`false`: whether the player's current chunk is protected. |
| `%beaconshield_here_owner%` | Owner of the territory the player is standing in. |
| `%beaconshield_here_chunks%` | Size (in chunks) of that territory. |
| `%beaconshield_here_fuel_time%` | Remaining protection time of that territory. |

The `here_*` placeholders return an empty string outside a territory.

## Configuration

All settings live in `plugins/BeaconShield/`:

| File | Contents |
|---|---|
| `config.yml` | Limits, recipe, fuel, protection toggles, economy, greeting/farewell messages, metrics. |
| `lang.yml` | Every message shown to players. |
| `gui.yml` | Items, names and lore of the menus. |
| `upgrade.yml` | Items and recipes of the upgrades. |

Expansions keep their own files under `plugins/BeaconShield/expansions/<expansion-name>/`.

Use `/bsd reload` to apply changes without restarting the server.

### Key `config.yml` options

| Option | Default | Description |
|---|---|---|
| `metrics-enabled` | `true` | Send anonymous stats to bStats. Requires a restart. |
| `max-beacons-per-player` | `1` | Beacons a player may place (see the `max-beacons.<N>` permission). |
| `max-chunks-per-beacon` | `9` | Chunks a single beacon may protect, core chunk included. |
| `preview-block` | `minecraft:gold_block` | Block used to draw chunk borders in preview mode. |
| `preview-particle` / `preview-particles-per-block` | `END_ROD` / `0.5` | Corner beams of the preview, and their density per block of height. |
| `beacon-name` / `beacon-lore` | — | Display name and lore of the beacon item. |
| `beacon-recipe` | enabled | Shaped recipe of the beacon item (`shape` + `ingredients`). |
| `greeting` / `farewell` | — | Message, title and subtitle on entering/leaving a territory. Set to `""` to disable. |
| `protection-explosions` / `protection-fire` / `protection-mob-grief` | `true` | Grief protection toggles. |
| `base-price` | `100` | Base price of a chunk (needs Vault). |
| `economy-use-formula` / `economy-formula` | `true` / `%base_price% * %distance%` | Chunk price formula. |
| `fuel-enabled` | `true` | Whole fuel system on/off. |
| `fuel-particles` / `fuel-particle` | `true` / `FLAME` | Particles shown while the beacon burns fuel. |
| `fuel-use-formula` / `fuel-formula` | `false` / `%burn_time% / %chunks_owned%` | Scale burn time with territory size. |
| `fuel-items` | coal, coal block | Accepted fuel items, with `burn-time` (seconds) and `custom-model-data`. |

## Map integrations

Optional companion jars that draw territories on your web map. Install the core first, then drop in the one that matches your map plugin:

- `BeaconShield-dynmap.jar` — requires **Dynmap**.
- `BeaconShield-squaremap.jar` — requires **Squaremap**.

**WorldGuard** is supported directly by the core: players cannot claim chunks inside a WorldGuard region.

## Metrics

The plugin collects anonymous usage statistics through BStats (server count, player count, server version, ...). No personal data is collected. You can opt out by setting `metrics-enabled: false` in `config.yml` (requires a server restart), or disable bStats globally in `plugins/bStats/config.yml`.

## API

To use this plugin as a dependency in your own project, add the JitPack repository to your build file.

```xml
	<repositories>
		<repository>
		    <id>jitpack.io</id>
		    <url>https://jitpack.io</url>
		</repository>
	</repositories>
```

Add the dependency.

```xml
	<dependency>
	    <groupId>com.github.cykvta</groupId>
	    <artifactId>BeaconShield</artifactId>
	    <version>{version}</version>
	</dependency>
```

Get the plugin instance.

```java
BeaconShieldAPI api = BeaconShield.getAPI(); // Get the plugin instance
```

Public events:

- `BeaconShieldDestroyedEvent`: Called when a beacon is destroyed.
- `BeaconShieldPlacedEvent`: Called when a beacon is placed.
- `ProtectionChunkAddedEvent`: Called when a player adds a chunk to the protection list.
- `ProtectionChunkRemovedEvent`: Called when a player removes a chunk from the protection list.

The API also exposes the hooks expansions use to alter core behaviour: `setUpgradeSuppressor`, `setProtectionBypass` and `setBeaconInteractionGuard`. The [Raid Expansion](../raid-expansion/README.md) is built entirely on them.
