package com.empcraft.schem;

import org.bukkit.World;

import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;
import com.plotsquared.bukkit.generator.BukkitPlotPopulator;

public class SchemPop extends BukkitPlotPopulator {

    private final SchemPlotWorld plotworld;

    public SchemPop(final SchemPlotWorld plotworld) {
        this.plotworld = plotworld;
    }

    @Override
    public void populate(World world, RegionWrapper region, PseudoRandom rand, int cx, int cz) {
        int relX, relZ;
        if (this.X >= 0) {
            relX = this.X % this.plotworld.WIDTH;
        } else {
            relX = this.X % this.plotworld.WIDTH;
            if (relX != 0) {
                relX += this.plotworld.WIDTH;
            }
        }
        if (this.Z >= 0) {
            relZ = this.Z % this.plotworld.LENGTH;
        } else {
            relZ = this.Z % this.plotworld.LENGTH;
            if (relZ != 0) {
                relZ += this.plotworld.LENGTH;
            }
        }

        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < (this.plotworld.HEIGHT + 1); y++) {
                for (int z = 0; z < 16; z++) {
                    final BlockLoc loc = new BlockLoc((short) ((x + relX) % this.plotworld.WIDTH), (short) y, (short) ((z + relZ) % this.plotworld.LENGTH));
                    final BlockWrapper block = this.plotworld.GENERATOR_SCHEMATIC.get(loc);
                    if (block != null) {
                        setBlockAbs(x, y + this.plotworld.PLOT_HEIGHT, z, block.data);
                    }
                }
            }
        }
    }

}
