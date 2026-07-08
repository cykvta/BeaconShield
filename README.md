# BeaconShield

BeaconShield is a Minecraft plugin that allows you to protect chunks using a custom beacon with unique functionalities. It provides additional features like upgrades, fuel management, grief protection, member roles and economy integration to enhance server control.

[![](https://jitpack.io/v/cykvta/BeaconShield.svg)](https://jitpack.io/#cykvta/BeaconShield)
[![](https://img.shields.io/badge/Spigot-BeaconShield-ED8106?logo=spigotmc)](https://www.spigotmc.org/resources/123248/)

## Features

- Beacon-based chunk protection with an in-game menu.
- Member system with roles (Owner, Officer, Member) and configurable permissions per action.
- Fuel system to keep the beacon active (optional, can be disabled).
- Protection against explosions, fire and mob griefing.
- Predefined upgrades for beacons.
- Integration with Vault for economy management.
- PlaceholderAPI support.
- Compatibility with WorldGuard to prevent claiming inside protected regions.
- Dynmap and Squaremap integrations to display territories on the map.
- Fully configurable messages and GUIs.

## Installation

1. Download the `BeaconShield.jar` file.
2. Place it in the `plugins` folder of your server.
3. Restart the server.

Optional plugins: **Vault** (economy), **PlaceholderAPI** (placeholders), **WorldGuard** (region compatibility), **Dynmap** / **Squaremap** (map rendering).

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

Obtain them with `/bsd upgrade <upgrade> [player]` or through their crafting recipes.

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

- `config.yml`: limits, recipe, fuel, protection toggles, economy, greeting/farewell messages.
- `lang.yml`: every message shown to players.
- `gui.yml`: items, names and lore of the menus.
- `upgrade.yml`: items and recipes of the upgrades.

Use `/bsd reload` to apply changes without restarting the server.

## Metrics

The plugin collects anonymous usage statistics through BStats (server count, player count, server version, ...). No personal data is collected. You can opt out by setting `metrics-enabled: false` in `config.yml` (requires a server restart), or disable bStats globally in `plugins/bStats/config.yml`.

## API

To use this plugin as a dependency in your own project. <br>
Add the JitPack repository to your build file.
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
