# BeaconShield

BeaconShield is a Minecraft plugin that allows you to protect chunks using a custom beacon with unique functionalities. It provides additional features like upgrades, fuel management, grief protection, member roles and economy integration to enhance server control.

[![](https://jitpack.io/v/cykvta/BeaconShield.svg)](https://jitpack.io/#cykvta/BeaconShield)
[![](https://img.shields.io/badge/Spigot-BeaconShield-ED8106?logo=spigotmc)](https://www.spigotmc.org/resources/123248/)

## Modules

BeaconShield ships as a core plugin plus optional add-ons. Install the core first; each add-on is a separate jar you drop in only if you want it.

| Module | Jar | Description | Docs |
|---|---|---|---|
| **Core** | `BeaconShield-core.jar` | The plugin itself: chunk protection, beacon menu, roles, fuel, upgrades, economy. | [Read the docs](core/README.md) |
| **Raid Expansion** | `BeaconShield-RaidExpansion.jar` | Makes tiprotecons raidable: capture chunks one by one, then break the beacon like a nexus. | [Read the docs](raid-expansion/README.md) |
| **Dynmap** | `BeaconShield-dynmap.jar` | Draws territories on a Dynmap web map. | [Core → Map integrations](core/README.md#map-integrations) |
| **Squaremap** | `BeaconShield-squaremap.jar` | Draws territories on a Squaremap web map. | [Core → Map integrations](core/README.md#map-integrations) |

Each module's page documents its commands, permissions, configuration and how it works.

## Features

- Beacon-based chunk protection with an in-game menu.
- Member system with roles (Owner, Officer, Member) and configurable permissions per action.
- Fuel system to keep the beacon active (optional, can be disabled).
- Protection against explosions, fire and mob griefing.
- Predefined upgrades for beacons.
- Optional raids: capture a territory chunk by chunk and break its nexus.
- Integration with Vault for economy management.
- PlaceholderAPI support.
- Compatibility with WorldGuard to prevent claiming inside protected regions.
- Dynmap and Squaremap integrations to display territories on the map.
- Fully configurable messages and GUIs.

## Installation

1. Download `BeaconShield-core.jar` and place it in your server's `plugins` folder.
2. Add any module jar you want from the table above.
3. Restart the server.

Requires Spigot/Paper `1.20+`. Optional plugins: **Vault** (economy), **PlaceholderAPI** (placeholders), **WorldGuard** (region compatibility), **Dynmap** / **Squaremap** (map rendering).

Configuration lives in `plugins/BeaconShield/`, with each expansion keeping its own files under `plugins/BeaconShield/expansions/<expansion-name>/`.

## Developers

BeaconShield exposes a public API and events, available through JitPack — see [Core → API](core/README.md#api). Add-ons like the Raid Expansion are built entirely on it.

## Support

Questions or issues? Discord: **@cykvta** · [GitHub](https://github.com/cykvta/BeaconShield)
