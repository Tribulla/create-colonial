# Create Colonial

A Minecraft mod that integrates Create 0.5.x with MineColonies, enabling better building automation and Create contraption support.

> **⚠️ Important Version Notice**: This mod is designed for **Create 0.5.x only**. It does **NOT** work with Create 0.6 or newer versions, as Create 0.6 introduced significant API changes that are incompatible with this mod.

## Features

### 1. Create Block Blueprint Support
- **Save Create contraptions in blueprints**: When scanning a building with Create blocks (belts, gearboxes, mechanical arms, etc.), all Create-specific data is preserved
- **Proper restoration**: Builders can correctly rebuild Create contraptions with all their NBT data intact
- **Kinetic network support**: Create blocks maintain their rotational speed and stress values when rebuilt

### 2. Clipboard Resource Integration
- **Click with Create's Clipboard on Builder's Hut**: Right-click a Builder's Hut while holding Create's Clipboard item to get a list of all required resources
- **Uses existing Create item**: No need to craft anything special - just use the Clipboard from Create mod
- **Resource summary**: Automatically displays all materials needed for active work orders
- **Inventory management**: Helps you prepare the exact resources your builders need

### 3. Builder AI Extensions
- **Smart placement**: Builders understand Create blocks and place them correctly
- **Validation**: Automatic verification that Create blocks are properly placed and functional
- **Priority handling**: Create blocks are placed in the correct order for proper assembly

## Installation

1. Download and install Minecraft Forge 1.20.1 (version 47.2.0 or higher)
2. Install the required dependencies:
   - **Create 0.5.x** (tested with 0.5.1.j) - **DOES NOT WORK WITH 0.6+**
   - MineColonies 1.1.600 or higher
   - Structurize 1.0.700 or higher
3. Download Create Colonial mod
4. Place all mod files in your `mods` folder
5. Launch Minecraft

## Usage

### Saving Buildings with Create Blocks

1. Build your structure with Create contraptions
2. Use the MineColonies scan tool to scan the building
3. Create blocks will automatically be saved with all their configuration
4. Save the blueprint as normal

### Using the Clipboard Feature

1. Get a Clipboard from Create mod (craft with Gold Sheet + Andesite Alloy)
2. Hold the Clipboard in your hand
3. Right-click on a Builder's Hut
4. A list of required resources will appear in chat
5. The resources are now "on your clipboard" for easy reference

### Building from Blueprints

1. Place a blueprint with Create blocks as you normally would
2. Assign a builder to construct itS
3. The builder will place Create blocks with all their saved data
4. Create contraptions will work immediately once completed

## Technical Details

### Saved Create Block Data

The mod saves the following data for Create blocks:
- Full NBT data for complete restoration
- Kinetic properties (speed, stress)
- Block-specific configurations (arm targets, filter settings, etc.)
- Position-relative data for multi-block structures

### Compatibility

- **Minecraft**: 1.20.1
- **Forge**: 47.2.0+
- **Create**: 0.5.1 - 0.5.x (NOT compatible with 0.6+)
- **MineColonies**: 1.1.600+
- **Structurize**: 1.0.700+

## Version Compatibility Warning

**Create 0.6 Incompatibility**: Create 0.6+ introduced major internal API changes including:
- Complete refactor of kinetic block systems
- Changes to clipboard item handling
- Modified NBT data structures
- Different block entity architectures

Create 0.6.x support will come at some point in the future but currently the plan is to add features to create 0.5.x.

## Bulding from Source

```bash
git clone https://github.com/yourusername/create_colonial.git
cd create_colonial
gradlew build
```

The built jar will be in `build/libs/`

## Known Issues

- Very large Create contraptions may have some delay in kinetic network synchronization after building
- Mechanical arms may need to be reconfigured if their targets were moved
- Only compatible with Create 0.5.x - does not work with Create 0.6+

## Contributing

Contributions are welcome! Please:
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

This mod is licensed under the MIT License. See LICENSE.txt for details.

## Credits

- **Create Mod**: by simibubi and contributors
- **MineColonies**: by the LDT Team
- **Structurize**: by the LDT Team

## Support

For issues and feature requests, please use the GitHub issue tracker:
https://github.com/yourusername/create_colonial/issues

---

**Note**: This is an integration mod and requires both Create 0.5.x and MineColonies to be installed to function.
