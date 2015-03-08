package com.empcraft.schem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.config.Configuration;
import com.intellectualcrafters.plot.config.ConfigurationNode;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BukkitSchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler;
import com.intellectualcrafters.plot.util.SchematicHandler.DataCollection;
import com.intellectualcrafters.plot.util.SchematicHandler.Dimension;
import com.intellectualcrafters.plot.util.SchematicHandler.Schematic;

public class SchemPlotWorld extends PlotWorld {

    public static SchematicHandler schematicHandler = new BukkitSchematicHandler();

    public int PLOT_HEIGHT;
    public int HEIGHT = 0;
    public static int PLOT_HEIGHT_DEFAULT = 0;

    public HashMap<BlockLoc, BlockWrapper> GENERATOR_SCHEMATIC = new HashMap<BlockLoc, BlockWrapper>();
    public HashSet<PlotLoc> GENERATOR_PLOT_LOCS = new HashSet<PlotLoc>();
    public HashSet<PlotLoc> GENERATOR_PLOT_LOCS_E = new HashSet<PlotLoc>();
    public HashSet<PlotLoc> GENERATOR_PLOT_LOCS_SE = new HashSet<PlotLoc>();

    public HashMap<BlockLoc, BlockWrapper> GENERATOR_SCHEMATIC_MERGED_E2 = new HashMap<BlockLoc, BlockWrapper>();
    public HashMap<BlockLoc, BlockWrapper> GENERATOR_SCHEMATIC_MERGED_E1 = new HashMap<BlockLoc, BlockWrapper>();
    public HashMap<BlockLoc, BlockWrapper> GENERATOR_SCHEMATIC_MERGED_E0 = new HashMap<BlockLoc, BlockWrapper>();
    public HashMap<BlockLoc, BlockWrapper> GENERATOR_SCHEMATIC_MERGED_SE = new HashMap<BlockLoc, BlockWrapper>();

    public BlockLoc SIGN_LOC = null;
    public static String GENERATOR_SCHEMATICS_DEFAULT = "null";

    public int WIDTH;
    public int LENGTH;
    public int MAX_X;
    public int MAX_Z;

    public SchemPlotWorld(final String worldname) {
        super(worldname);
    }

    @Override
    public void loadConfiguration(final ConfigurationSection config) {
        try {
            this.PLOT_HEIGHT = config.getInt("plot.height");
            final String schem1Str = config.getString("plot.schematic.default");
            final String schem2Str = config.getString("plot.schematic.merged_east");
            final String schem3Str = config.getString("plot.schematic.merged_southeast");
            final String schem4Str = config.getString("plot.schematic.merged_all");
            final Schematic schem1 = schematicHandler.getSchematic(schem1Str);
            final Schematic schem2 = schematicHandler.getSchematic(schem2Str);
            final Schematic schem3 = schematicHandler.getSchematic(schem3Str);
            final Schematic schem4 = schematicHandler.getSchematic(schem4Str);
            if (schem1 == null) {
                PlotSquared.log("&c[AdvPlots] Error loading schematic " + schem1Str + ". Please set a valid schematic in the settings.yml");
                return;
            }

            /*
             * CALCULATE MAIN DIFF
             */

            final Dimension demensions = schem1.getSchematicDimension();

            this.WIDTH = demensions.getX();
            this.LENGTH = demensions.getZ();
            final int height = demensions.getY();

            // Original schematic
            final DataCollection[] blocks1 = schem1.getBlockCollection();

            // Merge east schematic
            final DataCollection[] blocks2 = schem2.getBlockCollection();

            // Merge east and west schematic
            final DataCollection[] blocks3 = schem3.getBlockCollection();

            // Entirely merged schematic
            final DataCollection[] blocks4 = schem4.getBlockCollection();

            for (int x = 0; x < this.WIDTH; ++x) {
                for (int z = 0; z < this.LENGTH; ++z) {
                    final PlotLoc loc = new PlotLoc((short) x, (short) z);
                    this.GENERATOR_PLOT_LOCS.add(loc);
                    boolean contains = true;
                    for (int y = 0; y < height; y++) {
                        final int index = (y * this.WIDTH * this.LENGTH) + (z * this.WIDTH) + x;

                        final BlockLoc location = new BlockLoc((short) x, (short) y, (short) z);

                        final short id1 = blocks1[index].getBlock();
                        final short id4 = blocks4[index].getBlock();

                        final byte data1 = blocks1[index].getData();
                        final byte data4 = blocks4[index].getData();

                        if (id1 != 0) {

                            if ((this.SIGN_LOC == null) && ((id1 == 63) || (id1 == 68) || (id1 == 323))) {
                                this.SIGN_LOC = new BlockLoc((short) x, (short) y, (short) z);
                            }

                            if (this.HEIGHT < y) {
                                this.HEIGHT = y;
                            }
                            final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) z, id1, data1);
                            this.GENERATOR_SCHEMATIC.put(location, block);
                        }

                        if ((id1 != id4) || (data1 != data4)) {
                            contains = false;
                        }
                    }
                    if (contains) {
                        if (x > this.MAX_X) {
                            this.MAX_X = x;
                        }
                        if (z > this.MAX_Z) {
                            this.MAX_Z = z;
                        }
                    } else {
                        if (this.GENERATOR_PLOT_LOCS.contains(loc)) {
                            this.GENERATOR_PLOT_LOCS.remove(loc);
                        }
                    }
                }
            }

            // TODO below MAX && below MIN plot location

            for (int x = 0; x < this.WIDTH; ++x) {
                for (int z = 0; z < this.LENGTH; ++z) {
                    for (int y = 0; y < height; y++) {
                        final int index = (y * this.WIDTH * this.LENGTH) + (z * this.WIDTH) + x;

                        final BlockLoc location = new BlockLoc((short) x, (short) y, (short) z);

                        final PlotLoc plotloc = new PlotLoc((short) x, (short) z);

                        final short id1 = blocks1[index].getBlock();
                        final short id2 = blocks2[index].getBlock();
                        short id3 = blocks3[index].getBlock();
                        final short id4 = blocks4[index].getBlock();

                        final byte data1 = blocks1[index].getData();
                        final byte data2 = blocks2[index].getData();
                        byte data3 = blocks3[index].getData();
                        final byte data4 = blocks4[index].getData();
                        if ((id1 != id2) || (data1 != data2)) {
                            int valz = z;
                            if (valz == (this.LENGTH - 1)) {
                                valz = -1;
                            }
                            final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) valz, id2, data2);
                            this.GENERATOR_SCHEMATIC_MERGED_E2.put(location, block);

                            if (x <= this.MAX_X) {
                                final BlockLoc loc2 = new BlockLoc((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) z);
                                final BlockWrapper block2 = new BlockWrapper((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) valz, id2, data2);
                                this.GENERATOR_SCHEMATIC_MERGED_E2.put(loc2, block2);
                            }

                            if ((id2 == id4) && (data2 == data4)) {
                                this.GENERATOR_PLOT_LOCS_E.add(plotloc);
                            }
                        }
                        if ((id1 != id3) || (data1 != data3) || ((z == (this.LENGTH - 1)) && ((id2 != id1) || (data2 != data1)))) {

                            if (this.GENERATOR_PLOT_LOCS_E.contains(plotloc) || ((z == (this.LENGTH - 1)) && ((id2 != id1) || (data2 != data1)))) {
                                int valz = z;
                                if (valz == (this.LENGTH - 1)) {
                                    if ((z == (this.LENGTH - 1)) && ((id2 != id1) || (data2 != data1))) {
                                        final PlotLoc loc2 = new PlotLoc((short) z, (short) x);
                                        if (!this.GENERATOR_PLOT_LOCS_E.contains(loc2) && !this.GENERATOR_PLOT_LOCS_E.contains(plotloc)) {
                                            final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) z, id3, data3);
                                            this.GENERATOR_SCHEMATIC_MERGED_SE.put(location, block);
                                            this.GENERATOR_PLOT_LOCS_SE.add(plotloc);

                                            final BlockLoc location2 = new BlockLoc((short) z, (short) y, (short) x);
                                            final BlockWrapper block2 = new BlockWrapper((short) z, (short) y, (short) x, id3, data3);
                                            this.GENERATOR_SCHEMATIC_MERGED_SE.put(location2, block2);
                                        }
                                    }
                                    valz = -1;
                                    id3 = id2;
                                    data3 = data2;
                                }
                                final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) valz, id3, data3);
                                this.GENERATOR_SCHEMATIC_MERGED_E1.put(location, block);
                                if (x <= this.MAX_X) {
                                    final BlockLoc loc2 = new BlockLoc((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) z);
                                    final BlockWrapper block2 = new BlockWrapper((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) valz, id3, data3);
                                    this.GENERATOR_SCHEMATIC_MERGED_E1.put(loc2, block2);

                                }

                            } else {
                                final PlotLoc loc2 = new PlotLoc((short) z, (short) x);
                                if (!this.GENERATOR_PLOT_LOCS_E.contains(loc2)) {
                                    final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) z, id3, data3);
                                    this.GENERATOR_SCHEMATIC_MERGED_SE.put(location, block);
                                    this.GENERATOR_PLOT_LOCS_SE.add(plotloc);
                                }
                            }
                        } else if ((id2 != id1) || (data2 != data1)) {
                            final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) z, id3, data3);
                            this.GENERATOR_SCHEMATIC_MERGED_SE.put(location, block);
                            this.GENERATOR_PLOT_LOCS_SE.add(plotloc);

                            final BlockLoc location2 = new BlockLoc((short) z, (short) y, (short) x);

                            final PlotLoc plotloc2 = new PlotLoc((short) z, (short) x);
                            final BlockWrapper block2 = new BlockWrapper((short) z, (short) y, (short) x, id3, data3);
                            this.GENERATOR_SCHEMATIC_MERGED_SE.put(location2, block2);
                            this.GENERATOR_PLOT_LOCS_SE.add(plotloc2);

                        }
                        if ((id1 != id4) || (data1 != data4)) {
                            if (this.GENERATOR_PLOT_LOCS_E.contains(plotloc)) {

                                int valz = z;
                                if (valz == (this.LENGTH - 1)) {
                                    valz = -1;
                                }

                                final BlockWrapper block = new BlockWrapper((short) x, (short) y, (short) valz, id4, data4);
                                this.GENERATOR_SCHEMATIC_MERGED_E0.put(location, block);

                                if (x <= this.MAX_X) {
                                    final BlockLoc loc2 = new BlockLoc((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) z);
                                    final BlockWrapper block2 = new BlockWrapper((short) ((this.WIDTH + this.MAX_X) - x), (short) y, (short) valz, id4, data4);
                                    this.GENERATOR_SCHEMATIC_MERGED_E1.put(loc2, block2);
                                }
                            }
                        }

                    }
                }
            }
            final Iterator<Entry<BlockLoc, BlockWrapper>> iter = this.GENERATOR_SCHEMATIC_MERGED_SE.entrySet().iterator();
            while (iter.hasNext()) {
                final Entry<BlockLoc, BlockWrapper> entry = iter.next();
                final BlockLoc loc = entry.getKey();
                final PlotLoc loc2 = new PlotLoc(loc.z, loc.x);
                if (this.GENERATOR_PLOT_LOCS_E.contains(loc2)) {
                    if (this.GENERATOR_PLOT_LOCS_SE.contains(loc2)) {
                        this.GENERATOR_PLOT_LOCS_SE.remove(loc2);
                    }
                    iter.remove();
                }
            }
            if (this.SIGN_LOC == null) {
                this.SIGN_LOC = new BlockLoc((short) 0, (short) (this.HEIGHT), (short) -2);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public ConfigurationNode[] getSettingNodes() {
        return new ConfigurationNode[] { new ConfigurationNode("plot.height", SchemPlotWorld.PLOT_HEIGHT_DEFAULT, "Schematic paste height (use 0 if unsure)", Configuration.INTEGER, true), new ConfigurationNode("plot.schematic.default", SchemPlotWorld.GENERATOR_SCHEMATICS_DEFAULT, "Unmerged schematic file e.g. 'default'", Configuration.STRING, true), new ConfigurationNode("plot.schematic.merged_east", SchemPlotWorld.GENERATOR_SCHEMATICS_DEFAULT, "Merged schematic file e.g. 'east'", Configuration.STRING, true), new ConfigurationNode("plot.schematic.merged_southeast", SchemPlotWorld.GENERATOR_SCHEMATICS_DEFAULT, "Merged schematic file e.g. 'southeast'", Configuration.STRING, true), new ConfigurationNode("plot.schematic.merged_all", SchemPlotWorld.GENERATOR_SCHEMATICS_DEFAULT, "Merged schematic file e.g. 'all'", Configuration.STRING, true) };
    }

}
