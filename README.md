# PUSENBI
# project-Pixel-Unknown-Sekai-Never-Be-In-Chibi
> A fast-paced pixel action RPG focused on party-based combat, fluid character switching, and satisfying combo chains.

PUSENBI is a long-term game development project built around one simple goal:

> **project long-term"**

The project begins as a Java OOP Workshop prototype and is designed to grow over time into a larger Unity-based action RPG with multiplayer and game infrastructure systems.

---

## 🎮 About the Game

PUSENBI is a **2D pixel-style action RPG** with a strong focus on fast, responsive combat and active party coordination.

Instead of controlling only one character at a time, the player fights together with party members who actively participate in combat.

Party members can:

- Fight alongside the player
- Use skills independently
- Support each other
- React to combat situations
- Participate in combo chains
- Continue attacks during character switching

The main idea is to make the party feel like a real combat team rather than passive followers.

---

## ⭐ Core Design Pillars

### 1. Combat First

Combat must feel fun from the first few minutes.

The prototype prioritizes:

- Responsive attacks
- Fast movement
- Clear impact feedback
- Smooth switching
- Satisfying combo chains

### 2. Living Party

Party members should feel active and useful.

They are not just characters waiting to be selected.

They can:

- Attack enemies
- Reposition themselves
- Use abilities
- Assist the player
- Create combo opportunities

### 3. Chain Switch Combat

Switching characters is part of the combo itself.

Instead of:

```text
Character A
    ↓
Switch
    ↓
Character B
```

PUSENBI aims for:

```text
Swordsman Combo
      ↓
Chain Switch
      ↓
Lancer Dash-In
      ↓
Enemy Launch
      ↓
Mage Skill
      ↓
Elemental Explosion
      ↓
Switch Back
      ↓
Combo Finisher
```

### 4. Easy to Understand, Hard to Master

The controls and combat loop should be easy to learn while allowing deeper mastery through timing, positioning, party synergy, and combo routing.

---

## ⚔️ Core Gameplay

The main combat loop is built around:

```text
Move
  ↓
Attack
  ↓
Skill
  ↓
Chain Switch
  ↓
Party Follow-up
  ↓
Element / Combo Interaction
  ↓
Finisher / Ultimate
```

The player should be able to move between characters smoothly while maintaining the flow of combat.

---

## 👥 Living Party System

The player directly controls one character while the rest of the party continues fighting.

Party AI may eventually support:

- Basic enemy targeting
- Automatic normal attacks
- Conditional skill usage
- Dodging dangerous attacks
- Supporting the active character
- Preparing combo opportunities
- Joining team skills

For the Workshop prototype, the AI scope will remain intentionally small and predictable.

---

## 🧙 Prototype Characters

The current prototype party consists of four character concepts.

### ⚔️ Swordsman

**Role:** Fast Melee / Combo Starter

A battle-worn adventurer focused on mobility, quick melee attacks, and initiating Chain Switch combos.

### 🗡️ Spear Fighter

**Role:** Reach / Crowd Control

Uses long-range melee attacks to control groups of enemies, interrupt movement, and create openings for follow-up attacks.

### 🔮 Mage

**Role:** Element / Area Damage

Uses elemental magic, area attacks, and combo setup abilities.

### 🏹 Archer / Rogue

**Role:** Ranged Damage / Mobility

A fast ranged character focused on positioning, movement, and attacking enemies from safer angles.

---

## 🎨 Art Direction

PUSENBI uses a:

> **Chibi Pixel Fantasy + Compact Action Sprite**

visual direction.

The style focuses on:

- Small readable characters
- Strong silhouettes
- Simple but recognizable designs
- Clear character color identities
- Large and satisfying combat effects
- Smooth animation
- Strong visual feedback

The visual philosophy is:

> **Simple Art, Premium Feedback.**

The project prioritizes combat readability and game feel over highly detailed character sprites.

---

## 🧩 Layered Character Animation

Character and weapon sprites are designed as separate visual layers.

```text
Character Layer
      +
Weapon Layer
      +
Combat VFX Layer
      =
Final Animation
```

Weapons such as:

- Sword
- Spear
- Staff
- Bow

can animate independently from the character.

This allows the project to create more flexible attack animations without redrawing the entire character and weapon together for every frame.

The character still has its own attack, dash, skill, and casting animation. The weapon layer is synchronized with those animations.

---

## 🖼️ Sprite Production Direction

The Workshop prototype will use a compact sprite standard.

Planned rules include:

```text
Character Size   : 32x32 or 48x48
Directions       : 4 directions for the prototype
Character Layer  : Separate
Weapon Layer     : Separate
VFX Layer        : Separate
Feet Anchor      : Consistent across all characters
Outline          : Consistent style
Palette          : Limited and readable
```

The first fully animated test character will be the Swordsman.

Initial animations:

- Idle
- Walk / Run
- Normal Attack
- Dash
- Hurt
- Switch-In

Additional animations will only be added when needed.

---

## 🏰 Prototype Setting

The Workshop prototype takes place inside a fantasy dungeon.

The initial level structure is intentionally small:

```text
Dungeon Entrance
      ↓
Combat Room
      ↓
Combat Room
      ↓
Elite Encounter
      ↓
Boss Arena
```

The focus is not on building a large world.

The goal is to create a small but highly polished combat experience.

---

## 👹 Enemy Design

The prototype will use a small number of enemy archetypes.

### Melee Enemy

Chases the player and attacks at close range.

### Ranged Enemy

Maintains distance and attacks using projectiles.

### Heavy Enemy

A slower and more durable enemy designed to disrupt player movement and positioning.

### Boss

A dedicated encounter designed around:

- Reading attack patterns
- Dodging
- Counterattacking
- Party coordination
- Chain Switch combos

The boss should be fun because of its interaction design, not simply because it has high HP.

---

## 💥 Game Feel

Combat feedback is one of the most important parts of PUSENBI.

The prototype aims to use:

- Hit sparks
- Slash effects
- Attack trails
- Knockback
- Hit stop
- Screen shake
- Sound effects
- Enemy hit reactions
- Skill effects
- Dash trails

The goal is for every successful attack to feel responsive and satisfying.

---

## 🔧 Technology

### Workshop Prototype

```text
Language       : Java
IDE            : IntelliJ IDEA
Game Framework : libGDX
Desktop Backend: LWJGL3
Rendering      : OpenGL through libGDX
Art Style      : Pixel Art
```

The Workshop version focuses heavily on Object-Oriented Programming principles.

> The final framework choice may be adjusted if the course project rules restrict external libraries or frameworks.

---

## 🧱 Software Architecture Goals

The project is also designed as an OOP learning project.

Core systems may include:

```text
Character
Weapon
Skill
Enemy
Party
CombatSystem
AnimationSystem
AISystem
GameState
```

The architecture will gradually incorporate:

- Encapsulation
- Inheritance
- Polymorphism
- Abstraction
- SOLID Principles
- Design Patterns

The goal is not to use patterns unnecessarily, but to use them when they solve real design problems.

---

## 🗂️ Planned Architecture Direction

A simplified conceptual structure:

```text
Character
 ├─ movement
 ├─ state
 ├─ stats
 ├─ animation
 └─ equippedWeapon

Weapon
 ├─ type
 ├─ range
 ├─ damage
 ├─ attackPattern
 └─ weaponAnimation

Skill
 ├─ cooldown
 ├─ damage
 ├─ element
 └─ effect

CombatSystem
 ├─ attack timing
 ├─ hit detection
 ├─ combo state
 └─ chain switch handling

Enemy
 ├─ stats
 ├─ state
 ├─ AI behavior
 └─ attack logic
```

---

## 🔁 Example Attack Flow

```text
Player Presses Attack
        ↓
Character Enters ATTACK State
        ↓
Character Animation Starts
        ↓
Weapon Layer Plays Matching Animation
        ↓
Hitbox Appears at Impact Frame
        ↓
VFX + Sound Trigger
        ↓
Enemy Takes Damage
        ↓
Combo / Switch Window Opens
```

---

## 🗺️ Development Roadmap

### Phase 0 — Pre-Production

- [x] Game vision
- [x] Core combat direction
- [x] Character concept direction
- [x] Four prototype character concepts
- [x] Pixel art direction
- [x] Weapon layer concept
- [ ] Finalize sprite standard
- [ ] Complete GDD v0.1
- [ ] Complete Character Bible v0.1
- [ ] Finalize prototype scope

### Phase 1 — Core Prototype

- [ ] Create libGDX project
- [ ] Player movement
- [ ] Basic attack
- [ ] Dash
- [ ] Collision
- [ ] Damage system
- [ ] Enemy prototype
- [ ] Basic animation playback

### Phase 2 — Combat Foundation

- [ ] Skill system
- [ ] Weapon animation layers
- [ ] Combat effects
- [ ] Hit reactions
- [ ] Knockback
- [ ] Sound feedback
- [ ] Combo timing

### Phase 3 — Party System

- [ ] Party AI
- [ ] Character switching
- [ ] Chain Switch
- [ ] Party follow-up attacks
- [ ] Combo chaining

### Phase 4 — Playable Dungeon

- [ ] Dungeon environment
- [ ] Melee enemy
- [ ] Ranged enemy
- [ ] Heavy enemy
- [ ] Boss fight

### Phase 5 — Polish

- [ ] UI
- [ ] Audio
- [ ] VFX
- [ ] Balance
- [ ] Bug fixing
- [ ] Performance testing
- [ ] Showcase build

---

## 🎓 OOP Workshop Milestones

The first prototype is being developed for a university Object-Oriented Programming project.

Important project milestones include:

```text
Design Document
      ↓
Core Logic Demo
      ↓
Playable Prototype
      ↓
Final Presentation
      ↓
Project Showcase
```

The Workshop prototype represents the first playable version of the larger PUSENBI project.

---

## 🎯 Prototype Scope

The Workshop prototype is intentionally limited.

### Must Have

- Responsive movement
- Normal attacks
- Dash
- Basic enemy AI
- Basic damage system
- Character switching
- Chain Switch prototype
- Party AI prototype
- One dungeon theme
- One boss encounter
- Combat VFX and sound

### Stretch Goals

- Fourth playable character
- Deeper elemental interactions
- Ultimate chains
- Additional enemy archetypes
- More advanced party behavior

### Not in Workshop Scope

- Open world
- Multiplayer
- Large quest system
- Large skill trees
- Complex crafting
- Large inventory system
- Full procedural world generation
- Large-scale story content

---

## 🚀 Long-Term Vision

The Java version is only the beginning.

Future versions may explore:

```text
Java Prototype
      ↓
Unity / C#
      ↓
Expanded Combat
      ↓
Procedural Systems
      ↓
Multiplayer
      ↓
Dedicated Game Servers
      ↓
Game Infrastructure
```

Long-term systems may include:

- Expanded party AI
- Deeper elemental interactions
- Procedural dungeon generation
- More characters and weapons
- Multiplayer
- Dedicated servers
- Matchmaking
- Backend services
- Cloud infrastructure

---

## 📦 Release Plan

The first playable public release is planned as:

> **PUSENBI Prototype v0.1**

Potential distribution:

- GitHub
- itch.io

The first release will focus on presenting the core combat experience rather than large amounts of content.

---

## 🎮 Controls

Controls will be finalized during development.

Example layout:

```text
WASD        Move
Left Click  Normal Attack
Right Click Skill
Space       Dash
Q / E       Switch Character
R           Ultimate
```

These controls are provisional and may change during playtesting.

---

## 📁 Planned Project Structure

```text
PUSENBI/
│
├── assets/
│   ├── characters/
│   ├── weapons/
│   ├── enemies/
│   ├── environments/
│   ├── effects/
│   ├── ui/
│   └── audio/
│
├── docs/
│   ├── GDD/
│   ├── UML/
│   ├── character-bible/
│   └── technical-design/
│
├── core/
│
├── lwjgl3/
│
└── README.md
```

---

## 📚 Documentation

Planned project documentation includes:

- Game Design Document
- Technical Design Document
- Character Bible
- Art Direction Guide
- UML Diagrams
- Combat Design
- Development Log

---

## 📸 Screenshots & Media

Coming soon.

Planned media:

- Character concept art
- Gameplay screenshots
- Chain Switch GIF
- Dungeon screenshots
- Boss fight footage

---

## 🙌 Credits

### Game Design & Development

PUSENBI Development Team

### Third-Party Assets

Third-party assets, music, sound effects, fonts, tools, and libraries used during development will be credited here according to their respective licenses.

---

## 📜 License

The source code and game assets may use different licenses.

Please refer to future license documentation before redistributing any part of the project.

Third-party assets remain under the licenses of their original creators.

---

## ❤️ Project Philosophy

PUSENBI is not being developed around the idea of creating the largest game possible.

The project focuses on creating a small number of systems that interact deeply and feel satisfying to use.

> **Combat first.**  
> **Party matters.**  
> **Switching is part of the combo.**  
> **Fun before content.**
