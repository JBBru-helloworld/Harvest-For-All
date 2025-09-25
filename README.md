# 🌾 Harvest for All

### A 2D Pixel-Art Farming Simulation with Sustainable Agriculture Education

[![Scala](https://img.shields.io/badge/Scala-3.3.4-red.svg)](https://scala-lang.org/)
[![ScalaFX](https://img.shields.io/badge/ScalaFX-21.0.0-blue.svg)](https://scalafx.org/)
[![License](https://img.shields.io/badge/License-CC%20BY--NC--SA%204.0-orange.svg)](LICENSE)
[![SDG](https://img.shields.io/badge/UN%20SDG-2%20Zero%20Hunger-orange.svg)](https://sdgs.un.org/goals/goal2)

![Harvest for All Screenshot](<img width="2396" height="1578" alt="image" src="https://github.com/user-attachments/assets/d2a9d26f-f98d-42e5-9a30-860c6fc2807a" />
)

## 📖 Overview

**Harvest for All** is an educational 2D farming simulation game built with Scala 3 and ScalaFX that demonstrates advanced object-oriented programming principles while promoting sustainable agriculture education aligned with UN Sustainable Development Goal 2 (Zero Hunger).

Players manage virtual farms, cultivate diverse crops, trade in village markets, and learn sustainable farming practices through interactive gameplay mechanics and educational quizzes.

### 🎯 Key Features

- **🌱 Interactive Farming System**: Plant, water, and harvest 21+ crop varieties across 4 botanical categories
- **📈 Dynamic Market System**: Trade crops with fluctuating prices based on sustainability practices
- **🎓 Educational Components**: SDG 2-focused treasure chest quiz system with sustainable agriculture content
- **💾 Robust Save/Load System**: JSON serialization with auto-save and custom farm naming
- **🎮 Polished UI/UX**: Pixel-art graphics with intuitive controls and context-sensitive interactions
- **❤️ Life Management**: Health system that encourages crop consumption for self-sufficiency
- **🔄 Seasonal Mechanics**: Dynamic weather and growth cycles affecting crop development

## 🎬 Demo

[![Video Demonstration](https://img.youtube.com/vi/HfdJD3vehZk/0.jpg)](https://www.youtube.com/watch?v=HfdJD3vehZk)

_Click the image to watch the full gameplay demonstration_

## 🏗️ Technical Architecture

### Object-Oriented Design Principles

This project showcases advanced OOP concepts including:

- **3+ Level Inheritance Hierarchy**: Complete plant taxonomy (Plant → Cereal → Wheat)
- **Design Patterns**: Observer, Factory, Strategy, and Command patterns
- **Encapsulation**: Protected game state with controlled access methods
- **Polymorphism**: Context-sensitive input handling and plant behaviors
- **Traits & Mixins**: Reusable Observer pattern implementation
- **Generic Type Safety**: Scala 3 opaque types for coordinate system safety

### Project Structure

```
src/main/scala/harvestforall/
├── core/                    # Game state management & core logic
│   ├── GameState.scala      # Central state with Observer pattern
│   ├── Season.scala         # Seasonal mechanics enumeration
│   └── SaveData.scala       # JSON serialization DTOs
├── agriculture/             # Farm simulation logic
│   ├── Plants.scala         # 21-class inheritance hierarchy
│   └── Farm.scala           # Farm management with composition
├── game/                    # Game systems & mechanics
│   ├── systems/             # Core farming & hunger systems
│   ├── ui/                  # Dialogue, inventory, life systems
│   └── proximity/           # Location-based interactions
├── gui/                     # Complete UI implementation
│   ├── scenes/              # Game screens (menu, farm, market)
│   ├── managers/            # Asset & scene management
│   ├── components/          # Reusable UI components
│   └── utils/               # Styling & font utilities
├── graphics/                # Rendering & visual systems
│   ├── FarmTileManager.scala     # Tile-based world rendering
│   ├── FarmPlayer.scala          # Player animations
│   └── CropSpriteManager.scala   # 4-stage crop visualization
└── systems/                 # Infrastructure & persistence
    ├── SaveManager.scala         # Game state persistence
    └── AutoSaveManager.scala     # Automated saving system
```

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Scala 3.3.4**
- **SBT 1.x**

### Installation & Running

> **⚠️ Asset Requirements**: This project uses paid assets that are **NOT included** in the repository. You must obtain the crop sprites separately from [Seliel the Shaper](https://seliel-the-shaper.itch.io/) to run the complete game.

1. **Clone the repository**

   ```bash
   git clone https://github.com/JBBru-helloworld/Harvest-For-All.git
   cd Harvest-For-All
   ```

2. **Obtain Required Assets** (for full functionality)

   - Purchase "20.02a - Farming Crops #1 3.0" from [Seliel the Shaper](https://seliel-the-shaper.itch.io/)
   - Download free assets: [Blue Boy Adventure Pack](https://www.youtube.com/@RyiSnow), [Upheaval Font](https://www.dafont.com/upheaval.font)
   - Place assets in appropriate `src/main/resources/` directories

3. **Run the game**

   ```bash
   sbt run
   ```

4. **Alternative: Compile and run manually**
   ```bash
   sbt compile
   sbt "runMain harvestforall.HarvestForAllApp"
   ```

### Controls

| Key                 | Action                           |
| ------------------- | -------------------------------- |
| `WASD` / Arrow Keys | Move player / Navigate inventory |
| `SPACE`             | Interact (plant/water/harvest)   |
| `I`                 | Toggle inventory                 |
| `C`                 | Show controls dialogue           |
| `ENTER`             | Confirm actions / Eat crops      |
| `ESC`               | Pause game                       |
| Mouse Click         | UI interactions                  |

## 🎮 Gameplay Features

### 🌾 Farming System

- **21 Crop Varieties**: Wheat, corn, tomatoes, carrots, spinach, and more
- **4-Stage Growth**: Visual crop progression from seed to harvest
- **Seasonal Mechanics**: Crops affected by seasonal changes (5-day cycles)
- **Multi-Harvest Crops**: Advanced crops support multiple seasonal harvests
- **Sustainability Tracking**: Dynamic rating based on farming practices

### 🏪 Village Trading

- **Dynamic Pricing**: Crop values fluctuate based on sustainability rating
- **Village Satisfaction**: Relationship management affects trading outcomes
- **Economic Progression**: Currency system enables farm expansion

### 🎓 Educational Components

- **Treasure Chest Quizzes**: 5 different SDG 2-focused quiz sets
- **Progressive Learning**: Unlock premium seeds through knowledge demonstration
- **Real-World Applications**: Practical sustainable farming concepts
- **Detailed Explanations**: Learn from both correct and incorrect answers

### 💾 Save System

- **JSON Serialization**: Structured save data format
- **Auto-Save**: Configurable 5-minute intervals
- **Custom Naming**: Name your farms with default "My Farm"
- **Error Recovery**: Comprehensive handling for corrupted saves
- **Load Management**: Select, refresh, or delete saved farms

## 🏆 Educational Value

**Harvest for All** integrates learning objectives aligned with **UN SDG 2: Zero Hunger**:

- **Food Security**: Understanding crop diversity's role in nutrition
- **Sustainable Practices**: Soil health, water conservation, resource management
- **Economic Sustainability**: Agricultural decision-making and market dynamics
- **Environmental Impact**: Relationship between farming practices and stewardship

## 🛠️ Development Highlights

### Advanced Scala 3 Features

- **Opaque Types**: Type-safe coordinate systems preventing pixel/tile confusion
- **Enums**: Modern enumeration syntax for seasons and game states
- **Extension Methods**: Enhanced functionality without inheritance restrictions
- **Pattern Matching**: Comprehensive error handling and state management

### Performance Optimizations

- **Lazy Evaluation**: UI redraws only when necessary
- **Mutable Collections**: Optimized farm plot management
- **Batched Updates**: Reduced observer notifications
- **60 FPS Target**: Smooth gameplay even with hundreds of active crops

## 🎨 Assets & Attribution

### Graphics

- **Tile Graphics**: Blue Boy Adventure Graphics Pack by RyiSnow (Educational Use)

  - Source: [RyiSnow YouTube Channel](https://www.youtube.com/@RyiSnow)
  - License: Educational Use Permission
  - Status: ✅ Freely available

- **Crop Sprites**: "20.02a - Farming Crops #1 3.0" by Seliel the Shaper (Purchased License)
  - Source: [Seliel the Shaper Itch.io](https://seliel-the-shaper.itch.io/)
  - License: Commercial License (Purchased)
  - Status: ⚠️ **NOT INCLUDED** - Must be purchased separately

### Audio

- **Background Music**: "A Lonely Cherry Tree" by Pix (YouTube Audio Library - No Copyright)
  - Source: [Pix YouTube Channel](https://www.youtube.com/@Pixverses)
  - License: No Copyright (YouTube Audio Library)
  - Status: ✅ Freely available

### Fonts

- **UI Font**: Upheaval TrueType Font by Brian Kent (Freeware License)
  - Source: [DaFont - Upheaval](https://www.dafont.com/upheaval.font)
  - License: Freeware (Personal and Commercial Use)
  - Status: ✅ Freely available

### 📁 Asset Setup for Developers

If you want to run the complete game, place assets in the following structure:

```
src/main/resources/
├── graphics/
│   ├── tiles/           # Blue Boy Adventure Pack
│   ├── crops/           # 🚨 Paid assets - not included
│   └── player/          # Blue Boy Adventure Pack
├── audio/
│   └── background/      # YouTube Audio Library tracks
└── fonts/
    └── upheaval.ttf     # Free font download
```

## 🔧 Technical Requirements

### Dependencies

```scala
libraryDependencies ++= Seq(
  "org.scalafx" %% "scalafx" % "21.0.0-R32",
  "com.typesafe.play" %% "play-json" % "2.10.4", // JSON serialization
  "org.openjfx" % "javafx-*" % "21.0.4" // Platform-specific JavaFX
)
```

### Build Configuration

- **Scala Version**: 3.3.4
- **Main Class**: `harvestforall.HarvestForAllApp`
- **Target Platform**: Cross-platform (Windows, macOS, Linux)
- **JVM Target**: Java 21+

## 🐛 Known Issues & Future Enhancements

### Current Limitations

- **Single Player Only**: No multiplayer support (would require networking architecture)
- **Performance**: Large farms (500+ crops) may experience slowdowns
- **AI Complexity**: Simplified market and weather systems

### Planned Improvements

- **Multiplayer Support**: Shared farm management
- **Advanced AI**: Dynamic market simulation, realistic weather, pest systems
- **Performance**: Spatial partitioning for large farm optimization
- **Additional Crops**: Unlock system for remaining 16 crop varieties

## 📊 Development Statistics

- **Lines of Code**: ~3,000+ lines of Scala
- **Classes**: 50+ classes demonstrating OOP principles
- **Inheritance Levels**: 3+ level plant taxonomy
- **Design Patterns**: 4+ patterns implemented
- **Save Format**: JSON with comprehensive error handling
- **Development Time**: Full semester project

## 🤝 Contributing

This is an educational project developed for university coursework. While not actively seeking contributions, the codebase demonstrates:

- Clean architecture principles
- Comprehensive OOP implementation
- Educational game design
- Sustainable development integration

## 📄 License

This project is licensed under the **Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License (CC BY-NC-SA 4.0)**.

- **Source Code**: Available for educational and non-commercial use only
- **Commercial Use**: ❌ **Prohibited** - contact author for commercial licensing
- **Sharing**: ✅ Allowed with proper attribution
- **Modifications**: ✅ Allowed but must use same license
- **Paid Assets**: NOT included - must be obtained separately (see [LICENSE](LICENSE) for details)
- **Educational Use**: This project was developed for university coursework

See the full [LICENSE](LICENSE) file for complete terms and asset usage restrictions.

## 👨‍💻 Author

**Joshua Bonham** (JBBru-helloworld)

- GitHub: [@JBBru-helloworld](https://github.com/JBBru-helloworld)
- Project: [Harvest-For-All](https://github.com/sunwaydcis/final-project-JBBru-helloworld)

## 🌟 Acknowledgments

- **UN Sustainable Development Goals** for educational framework
- **ScalaFX Community** for comprehensive documentation
- **Scala 3 Team** for modern language features
- **Asset Creators** for high-quality educational resources

---

_"Bridging theory and practice through sustainable agriculture simulation"_

### 📈 Project Impact

This project demonstrates how advanced software engineering concepts can be made engaging and educational, creating meaningful connections between technical implementation and real-world sustainable development challenges.
