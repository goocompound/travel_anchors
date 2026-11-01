# Travel Anchors

Based on [Travel Anchors](https://www.curseforge.com/minecraft/mc-mods/travel-anchors) by [LukeGrahamLandry](https://github.com/LukeGrahamLandry/travel_anchors), CastCrafter, MelanX, and noeppinoeppi. This [Minecraft 26.3 update](https://github.com/goocompound/travel_anchors) of the existing Fabric version is maintained by [goocompound](https://github.com/goocompound). Licensed under [Apache-2.0](LICENSE).

Brings EnderIO's Travel Anchors and Staff of Traveling to a standalone mod: blink through walls, teleport between named anchors, and build vertical elevators. Anchors can be disguised as other blocks.

Requires **Minecraft 26.3.x**, Fabric Loader **0.19.5+**, Fabric API for 26.3, and Java **25+**.

## Usage

- Shift-right-click with the staff to blink forward, including through walls.
- Hold the staff and right-click toward a named anchor to teleport to it. Anchors and their names are visible through walls within range; the selected name is gold.
- Stand on an anchor to see other anchors and teleport toward them with an empty hand.
- Right-click an anchor while not standing on it to rename it.
- With an empty main hand and a block in the offhand, right-click an anchor to disguise it.
- In elevator mode, jump or sneak while standing on an anchor to travel directly up or down.
- **Range** increases the staff's anchor range. **Teleportation** lets swords and digging tools act as a staff.

## Configuration

With **Mod Menu** installed, open **Mods → Staff of Traveling → Configure**. Changes apply when you save. You can edit world settings in single-player or while hosting LAN; on remote servers, the server controls them.

| Setting | Default | Effect |
| --- | --- | --- |
| `maxTeleportDistance` | `64` | Base anchor range in blocks |
| `shortTeleportDistance` | `7` | Free-blink range in blocks |
| `staffCooldownTicks` | `20` | Blink cooldown in ticks (20 = 1 second) |
| `rangeEnchantScaling` | `0.5` | Anchor range multiplier: `1 + level * scaling` |
| `maxAngle` | `30` | Targeting tolerance in degrees |
| `isElevatorMode` | `true` | Jump/sneak to travel vertically; otherwise, aim at an anchor |
| `nameplateScale` | `0.7` | Anchor label size multiplier |

For manual edits:

- **Personal settings:** `config/travelstaff-client.json5` in your Minecraft folder. Restart after editing.
- **World settings:** `serverconfig/travelstaff-synced.json5` inside the world folder. Run `/reload` after editing.

Pack makers can put world defaults in `defaultconfigs/travelstaff-synced.json5`.

Range uses the highest enchantment level in either hand and does not affect blinking. Anchor travel has no cooldown.

## Known limitations

- Blinking can misjudge clearance around slabs, stairs and other partial blocks.
- Disguised anchors can still collide as full blocks.
- Saved teleport destinations from 1.19.3 aren't imported automatically.

## Build

Install JDK 25, set `JAVA_HOME`, and run:

```powershell
.\gradlew.bat build
```

Linux/macOS use `./gradlew`. Install the jar from `build/libs/` without `-sources` in its name. The build runs unit tests and server GameTests. Use `runClient` to launch a development client.
