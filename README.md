# BeaconShield

BeaconShield is a Minecraft plugin that allows you to protect chunks using a custom beacon with unique functionalities. It provides additional features like upgrades and fuel management to enhance server control.

[![](https://img.shields.io/badge/Spigot-BeaconShield-ED8106?logo=spigotmc)](https://www.spigotmc.org/resources/123248/)

## Features

- Beacon-based area protection.
- Predefined upgrades for beacons.
- Fuel system to keep the beacon active.
- Integration with Vault for economy management.
- Compatibility with WorldGuard to prevent chunk protection in protected regions.
- Fully configurable language support.

## Installation

1. Download the `BeaconShield.jar` file.
2. Place it in the `plugins` folder of your server.
3. Restart the server.

## Commands

| Command                     | Description                                    | Permission            |
|-----------------|--------------------------------|----------------|
| `/bsd give <player>` | Gives or grants a protection beacon to a player. | `beaconshield.give` |
| `/bsd reload` | Reloads the plugin configuration. | `beaconshield.reload` |
| `/bsd upgrade <upgrade> <player>` | Provides an upgrade item to a player. | `beaconshield.upgrade` |
| `/bsd` or `/bsd help` | Displays the plugin configuration. | `beaconshield.command` |

### Available Upgrades

- `disable_pvp`
- `disable_drowning`
- `disable_fall_damage`
- `disable_mob_spawning`

## Permissions

- `beaconshield.bypass`: Bypass protection.
- `beaconshield.reload`: Reload the plugin configuration.
- `beaconshield.command`: Access to the `/bsd` or `/bsd help` command to view the configuration.
- `beaconshield.upgrade`: Provide upgrade items to players.
- `beaconshield.give`: Give protection beacons to players.

## Fuel System

If the beacon runs out of fuel, the protection will be disabled. Fuel items and burn times are configurable in the `config.yml` file.

## Configuration

Modify the `config.yml` file located in `plugins/BeaconShield/` to customize the plugin settings, including economy integration and language options. Use `/bsd reload` to apply changes without restarting the server.

