package com.empcraft.schem;

import java.util.Random;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.object.PlotPopulator;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;

public class SchemPop extends PlotPopulator {

    private int absX, absZ;

    private final SchemPlotWorld plotworld;

    public SchemPop(final SchemPlotWorld plotworld) {
        this.plotworld = plotworld;
    }

    @Override
    public void populate(World world, RegionWrapper region, PseudoRandom rand, int cx, int cz) {

        int relX, relZ;

        if (this.absX >= 0) {
            relX = this.absX % this.plotworld.WIDTH;
        } else {
            relX = this.absX % this.plotworld.WIDTH;
            if (relX != 0) {
                relX += this.plotworld.WIDTH;
            }
        }
        if (this.absZ >= 0) {
            relZ = this.absZ % this.plotworld.LENGTH;
        } else {
            relZ = this.absZ % this.plotworld.LENGTH;
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
                        setBlock(x, y + this.plotworld.PLOT_HEIGHT, z, block.data);
                    }
                }
            }
        }
    }

}
