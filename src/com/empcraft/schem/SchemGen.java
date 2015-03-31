package com.empcraft.schem;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;

import com.intellectualcrafters.plot.object.PlotGenerator;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPopulator;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.object.PseudoRandom;
import com.intellectualcrafters.plot.object.RegionWrapper;

public class SchemGen extends PlotGenerator {

    short[][] result;
    private static PlotManager manager = null;
    SchemPlotWorld plotworld;

    @Override
    public PlotWorld getNewPlotWorld(final String world) {
        this.plotworld = new SchemPlotWorld(world);
        return this.plotworld;
    }

    @Override
    public PlotManager getPlotManager() {
        if (manager == null) {
            manager = new SchemPlotManager();
        }
        return manager;
    }

    @Override
    public short[][] generateExtBlockSections(final World world, final Random random, final int cx, final int cz, final BiomeGrid biomes) {
        final int height = this.plotworld.HEIGHT + 1;
        this.result = new short[Math.min(world.getMaxHeight(), ((height + 15) / 16) * 16) / 16][];

        try {

            final int absX = cx << 4;
            final int absZ = cz << 4;

            int relX, relZ;

            if (absX >= 0) {
                relX = absX % this.plotworld.WIDTH;
            } else {
                relX = absX % this.plotworld.WIDTH;
                if (relX != 0) {
                    relX += this.plotworld.WIDTH;
                }
            }
            if (absZ >= 0) {
                relZ = absZ % this.plotworld.LENGTH;
            } else {
                relZ = absZ % this.plotworld.LENGTH;
                if (relZ != 0) {
                    relZ += this.plotworld.LENGTH;
                }
            }
            final Biome biome = Biome.valueOf(this.plotworld.PLOT_BIOME);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biomes.setBiome(x, z, biome);
                    for (int y = 0; y < (this.plotworld.HEIGHT + 1); y++) {
                        final BlockLoc loc = new BlockLoc((short) ((x + relX) % this.plotworld.WIDTH), (short) y, (short) ((z + relZ) % this.plotworld.LENGTH));
                        final BlockWrapper block = this.plotworld.GENERATOR_SCHEMATIC.get(loc);
                        if (block != null) {
                            setBlock(this.result, x, y + this.plotworld.PLOT_HEIGHT, z, block.id);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
        return this.result;
    }

    private void setBlock(final short[][] result, final int x, final int y, final int z, final short blkid) {
        if (result[y >> 4] == null) {
            result[y >> 4] = new short[4096];
        }
        result[y >> 4][((y & 0xF) << 8) | (z << 4) | x] = blkid;
    }

    @Override
    public void init(final PlotWorld plotworld) {
        // TODO Auto-generated method stub
    }

    @Override
    public void generateChunk(World world, RegionWrapper region, PseudoRandom rand, int cx, int cz, BiomeGrid biomes) {
        try {

            final int absX = cx << 4;
            final int absZ = cz << 4;

            int relX, relZ;

            if (absX >= 0) {
                relX = absX % this.plotworld.WIDTH;
            } else {
                relX = absX % this.plotworld.WIDTH;
                if (relX != 0) {
                    relX += this.plotworld.WIDTH;
                }
            }
            if (absZ >= 0) {
                relZ = absZ % this.plotworld.LENGTH;
            } else {
                relZ = absZ % this.plotworld.LENGTH;
                if (relZ != 0) {
                    relZ += this.plotworld.LENGTH;
                }
            }
            final Biome biome = Biome.valueOf(this.plotworld.PLOT_BIOME);
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    biomes.setBiome(x, z, biome);
                    for (int y = 0; y < (this.plotworld.HEIGHT + 1); y++) {
                        final BlockLoc loc = new BlockLoc((short) ((x + relX) % this.plotworld.WIDTH), (short) y, (short) ((z + relZ) % this.plotworld.LENGTH));
                        final BlockWrapper block = this.plotworld.GENERATOR_SCHEMATIC.get(loc);
                        if (block != null) {
                            setBlock(this.result, x, y + this.plotworld.PLOT_HEIGHT, z, block.id);
                        }
                    }
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<PlotPopulator> getPopulators(String world) {
        return Arrays.asList((PlotPopulator) new SchemPop(this.plotworld));
    }

}
