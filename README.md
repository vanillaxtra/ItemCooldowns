# ItemCooldowns

![Version](https://img.shields.io/badge/version-3.0.0-blue.svg)
![Platform](https://img.shields.io/badge/Paper-1.21.11-purple.svg)
![Modrinth](https://img.shields.io/modrinth/dt/itemcooldowns?style=flat&logo=modrinth&label=Downloads&color=00AF5C)

Per-item cooldowns for **PvP, survival, and minigame servers** configured in-game with a GUI, as **rules**.

# 📋 Rule list & GUI

Every cooldown is a **rule**: one timer per item, smart matching, and optional filters (potion type, custom model data, item model).

* **Left-click** a rule → open the rule editor
* **Right-click** a rule → enable or disable (disabled rules will not apply)

![Rule list and GUI](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/rule-list-gui.png)![Enable and disable rules](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/rule-enable-disable.png)

---

# ⏱️ Edit cooldown time

* **Left-click** +1s · **Right-click** −1s
* **Shift + left-click** +5s · **Shift + right-click** −5s
* Drop a specific item in the center slot, or use a preset / group rule
* **Auto-saves** when you leave the editor (no save button)

![Edit cooldown duration](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/rule-editor-time.png)

---

# 📦 Presets

One-click presets to add common rules which i think are quite commonly needed:

Ender pearl, trident, wind charge, mace, chorus fruit, firework rocket, golden apple, enchanted golden apple, totem of undying, plus grouped **Spear**, **Turtle Master** (10s), and **Instant Health** (5s).

![Presets menu](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/presets.png)

---

# ⚔️ Combat & Showcase

Cooldowns apply on **real use** which i have tried to capture (if im missing any join the discord or open an issue on github):

| Item / action | When the timer starts |
|---------------|------------------------|
| **Mace** | Melee hit (attack) |
| **Wind charge** | Throw / use |
| **Trident** | Throw or **riptide** |
| **Totem of Undying** | On resurrection |
| **Spear** | **Lunge** only full charge lunge |
| Potions, pearls, bows, shield, elytra firework & everything else | Use / Interact event |

### Totem

![Totem cooldown](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/totem-cooldown.png )

### Trident (throw & riptide)

![Trident and riptide cooldown](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/trident-riptide-cooldown.png)

### Spear lunge

Grouped spear rules share one timer across all tiers. Includes protection against attribute-swap abuse around lunges.

![Spear lunge cooldown](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/spear-lunge-cooldown.png )

### Mace & wind charge

![Mace and wind charge cooldown](https://raw.githubusercontent.com/vanillaxtra/ItemCooldowns/refs/heads/master/assets/mace-wind-charge-cooldown.png )


---


## Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/itemcooldowns` | `itemcooldowns.admin` | Open the cooldown GUI |
| `/itemcooldowns reload` | `itemcooldowns.admin` | Reload config and messages |
| `/setcooldown <item> <seconds>` | `itemcooldowns.set` | Add or update a material-wide rule |
| `/getcooldown <item>` | `itemcooldowns.get` | Show configured time for a material |
| `/resetcooldown <item\|all>` | `itemcooldowns.reset` | Remove rule(s) from config |

No command aliases. Namespaced `itemcooldowns:*` suggestions are hidden from the client list.

---

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `itemcooldowns.admin` | OP | GUI and reload |
| `itemcooldowns.set` | OP | `/setcooldown` |
| `itemcooldowns.get` | OP | `/getcooldown` |
| `itemcooldowns.reset` | OP | `/resetcooldown` |

---

## Configuration

| File | Purpose |
|------|---------|
| `plugins/ItemCooldowns/config.yml` | Rule list (`rules:`), schema version |
| `plugins/ItemCooldowns/messages.yml` | GUI and command text (auto-updated by version) |

Rules are saved from the GUI; you can also edit YAML by hand. Use `/itemcooldowns reload` after manual edits.

---

## Contact

![Discord](https://img.shields.io/badge/Contact-Discord-5865F2?logo=discord&logoColor=white)
![GitHub Issues](https://img.shields.io/badge/Issues-GitHub-green?logo=github)

---

## Statistics

[![bStats: servers using InstantBrew](https://bstats.org/signatures/bukkit/itemcooldown3.svg)](https://bstats.org/plugin/bukkit/itemcooldown3/34985)

---
