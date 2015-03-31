package com.empcraft.schem;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.World;

import com.intellectualcrafters.plot.PlotSquared;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.bukkit.BukkitUtil;
import com.intellectualcrafters.plot.util.bukkit.UUIDHandler;

public class SchemPlotManager extends PlotManager {

    /**
     * Custom implementation
     */
    @Override
    public PlotId getPlotIdAbs(final PlotWorld plotworld, int x, final int y, int z) {
        final SchemPlotWorld spw = ((SchemPlotWorld) plotworld);
        // get plot size
        final int size = spw.WIDTH;
        // calulating how many shifts need to be done
        int dx, dz;

        if (x < 0) {
            x++;
            dx = x / size;
            dx--;
            x += ((-dx) * size);
        } else {
            dx = x / size;
        }
        if (z < 0) {
            z++;
            dz = z / size;
            dz--;
            z += ((-dz) * size);
        } else {
            dz = z / size;
        }
        // reducing to first plot
        final int rx = (x) % size;
        final int rz = (z) % size;
        // checking if road (return null if so)
        if (spw.GENERATOR_PLOT_LOCS.contains(new PlotLoc((short) rx, (short) rz))) {
            return new PlotId(dx + 1, dz + 1);
        }
        return null;
    }

    /**
     * Some complex stuff for traversing mega plots (return getPlotIdAbs if you
     * do not support mega plots)
     */
    @Override
    public PlotId getPlotId(final PlotWorld plotworld, int x, final int y, int z) {
        final SchemPlotWorld spw = ((SchemPlotWorld) plotworld);
        if (plotworld == null) {
            return null;
        }

        final int size = spw.WIDTH;

        int dx, dz;

        if (x < 0) {
            dx = (x + 1) / size;
            dx--;
            x += ((-dx) * size);
        } else {
            dx = x / size;
        }
        if (z < 0) {
            dz = (z + 1) / size;
            dz--;
            z += ((-dz) * size);
        } else {
            dz = z / size;
        }

        final int rx = (x) % size;
        final int rz = (z) % size;

        final PlotId id = new PlotId(dx + 1, dz + 1);
        final Plot plot = PlotSquared.getPlots(plotworld.worldname).get(id);
        if (plot != null) {
            if (plot.settings.isMerged()) {
                if (spw.GENERATOR_PLOT_LOCS.contains(new PlotLoc((short) rx, (short) rz))) {
                    return MainUtil.getBottomPlot(plot).id;
                }

                if (plot.settings.getMerged(1)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) rx, (short) rz))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }

                    if (plot.settings.getMerged(2)) {
                        if (spw.GENERATOR_PLOT_LOCS_SE.contains(new PlotLoc((short) rx, (short) rz))) {
                            return MainUtil.getBottomPlot(plot).id;
                        }
                    }
                }
                if (plot.settings.getMerged(2)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) rz, (short) rx))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }
                }
                if (plot.settings.getMerged(3)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) (spw.MAX_X - rx), (short) (spw.MAX_Z - rz)))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }
                    if (plot.settings.getMerged(0)) {
                        if (spw.GENERATOR_PLOT_LOCS_SE.contains(new PlotLoc((short) (spw.MAX_X - rx), (short) (spw.MAX_Z - rz)))) {
                            return MainUtil.getBottomPlot(plot).id;
                        }
                    }
                }
                if (plot.settings.getMerged(0)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) (spw.MAX_Z - rz), (short) (spw.MAX_X - rx)))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }
                }
            } else {
                if (spw.GENERATOR_PLOT_LOCS.contains(new PlotLoc((short) rx, (short) rz))) {
                    return id;
                }
            }
        } else {
            if (spw.GENERATOR_PLOT_LOCS.contains(new PlotLoc((short) rx, (short) rz))) {
                return id;
            }
        }

        return null;

    }

    @Override
    public boolean claimPlot(final PlotWorld plotworld, final Plot plot) {
        return true;
    }

    @Override
    public boolean clearPlot(final PlotWorld plotworld, final Plot plot, final boolean isDelete, final Runnable whenDone) {
        final World world = BukkitUtil.getWorld(plot.world);
        final SchemPlotWorld spw = ((SchemPlotWorld) PlotSquared.getPlotWorld(plot.world));
        final com.intellectualcrafters.plot.object.Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final int botX = pos1.getX();
        final int botZ = pos1.getZ();
        for (final PlotLoc loc : spw.GENERATOR_PLOT_LOCS) {
            for (int by = 0; by < world.getMaxHeight(); by++) {
                BukkitUtil.setBlock(world, botX + loc.x, by, botZ + loc.z, 0, (byte) 0);
            }
        }
        for (final BlockWrapper loc : spw.GENERATOR_SCHEMATIC.values()) {
            final PlotLoc plotloc = new PlotLoc(loc.x, loc.z);
            if (spw.GENERATOR_PLOT_LOCS.contains(plotloc)) {
                BukkitUtil.setBlock(world, loc.x + botX, loc.y + spw.PLOT_HEIGHT, loc.z + botZ, loc.id, loc.data);
            }
        }

        TaskManager.runTask(whenDone);

        return true;
    }

    @Override
    public boolean finishPlotMerge(final PlotWorld arg0, final ArrayList<PlotId> arg1) {
        return false;
    }

    @Override
    public boolean finishPlotUnlink(final PlotWorld arg0, final ArrayList<PlotId> arg1) {
        return false;
    }

    @Override
    public String[] getPlotComponents(final PlotWorld arg0, final PlotId arg1) {
        return new String[] {};
    }

    @Override
    public com.intellectualcrafters.plot.object.Location getSignLoc(final PlotWorld plotworld, final Plot plot) {
        final SchemPlotWorld spw = (SchemPlotWorld) plotworld;
        final com.intellectualcrafters.plot.object.Location bot = MainUtil.getPlotBottomLoc(plotworld.worldname, plot.id).add(1, 0, 1);
        return bot.add(spw.SIGN_LOC.x, spw.SIGN_LOC.y - 1, spw.SIGN_LOC.z);
    }

    @Override
    public boolean setBiome(final Plot plot, final int biome) {
        final int bottomX = MainUtil.getPlotBottomLoc(plot.world, plot.id).getX() - 1;
        final int topX = MainUtil.getPlotTopLoc(plot.world, plot.id).getX() + 1;
        final int bottomZ = MainUtil.getPlotBottomLoc(plot.world, plot.id).getZ() - 1;
        final int topZ = MainUtil.getPlotTopLoc(plot.world, plot.id).getZ() + 1;
        final int size = ((topX - bottomX) + 1) * ((topZ - bottomZ) + 1);
        final int[] xb = new int[size];
        final int[] zb = new int[size];
        final int[] biomes = new int[size];
        int index = 0;
        for (int x = bottomX; x <= topX; x++) {
            for (int z = bottomZ; z <= topZ; z++) {
                xb[index] = x;
                zb[index] = z;
                biomes[index] = biome;
                index++;
            }
        }
        BlockManager.setBiomes(plot.world, xb, zb, biomes);
        return true;
    }

    @Override
    public boolean setComponent(final PlotWorld arg0, final PlotId arg1, final String arg2, final PlotBlock[] arg3) {
        return true;
    }

    @Override
    public boolean startPlotMerge(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final SchemPlotWorld spw = (SchemPlotWorld) plotworld;
        final String world = plotworld.worldname;

        final PlotId start = plotIds.get(0);
        final PlotId end = plotIds.get(plotIds.size() - 1);

        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {

                final PlotId plotid = new PlotId(x, y);

                final int px = plotid.x;
                final int pz = plotid.y;

                final int absX = (px - 1) * spw.WIDTH;
                final int absZ = (pz - 1) * spw.LENGTH;

                final Plot plot = PlotSquared.getPlots(world).get(plotid);

                // check if less than MAX

                //                System.out.print("P: "+px+";"+pz);

                final World worldObj = BukkitUtil.getWorld(plotworld.worldname);

                if ((x < end.x) && (y < end.y)) {

                    //                    System.out.print("MERGE SE");

                    if (!plot.settings.getMerged(1) || !plot.settings.getMerged(2)) {
                        for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_SE.values()) {
                            BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, blockwrapper.id, blockwrapper.data);
                        }
                    }
                }

                if (x < end.x) {
                    if (start.y == end.y) {
                        //                        System.out.print("MERGE EAST");

                        if (!plot.settings.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E2.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else if (y == start.y) {

                        //                            System.out.print("MERGE E1");

                        if (!plot.settings.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else if (y == end.y) {
                        if (!plot.settings.getMerged(1)) {

                            //                            System.out.print("MERGE E1 2");

                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, (absZ + spw.MAX_Z) - blockwrapper.z, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else {

                        //                        System.out.print("MERGE E0");

                        if (!plot.settings.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E0.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    }
                }
                if (y < end.y) {
                    if (start.x == end.x) {

                        //                        System.out.print("MERGE SOUTH");

                        if (!plot.settings.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E2.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else if (x == start.x) {

                        //                        System.out.print("MERGE S1");

                        if (!plot.settings.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else if (x == end.x) {

                        //                        System.out.print("MERGE S1 2");

                        if (!plot.settings.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                BukkitUtil.setBlock(worldObj, (absX + spw.MAX_X) - blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    } else {

                        //                        System.out.print("MERGE S0");

                        if (!plot.settings.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E0.values()) {
                                BukkitUtil.setBlock(worldObj, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, blockwrapper.id, blockwrapper.data);
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean startPlotUnlink(final PlotWorld plotworld, final ArrayList<PlotId> plotIds) {
        final SchemPlotWorld spw = (SchemPlotWorld) plotworld;
        final String world = plotworld.worldname;
        final Plot plot = PlotSquared.getPlots(world).get(plotIds.get(0));
        if (plot != null) {
            if (plot.hasOwner()) {
                final UUID owner = plot.owner;
                final PlotPlayer player = UUIDHandler.getPlayer(owner);
                if (player != null) {
                    MainUtil.sendMessage(player, "&cPlot unlinking is not fully implemented");
                }
            }
        }

        final int maxY = BukkitUtil.getMaxHeight(world);
        final World worldObj = BukkitUtil.getWorld(world);

        final PlotId start = plotIds.get(0);
        final PlotId end = plotIds.get(plotIds.size() - 1);

        for (int x = start.x; x <= end.x; x++) {
            for (int y = start.y; y <= end.y; y++) {

                final PlotId plotid = new PlotId(x, y);

                final int px = plotid.x;
                final int pz = plotid.y;

                final int absX = (px - 1) * spw.WIDTH;
                final int absZ = (pz - 1) * spw.LENGTH;

                for (int bx = 0; bx < spw.WIDTH; bx++) {
                    for (int bz = 0; bz < spw.LENGTH; bz++) {
                        final PlotLoc loc = new PlotLoc((short) bx, (short) bz);
                        if (!spw.GENERATOR_PLOT_LOCS.contains(loc)) {
                            for (int by = 0; by < maxY; by++) {

                                int valz = bz;
                                if (valz == (spw.LENGTH - 1)) {
                                    valz = -1;
                                    BukkitUtil.setBlock(worldObj, absX + bx, by, absZ + valz, 0, (byte) 0);
                                }

                                int valx = bx;
                                if (valx == (spw.LENGTH - 1)) {
                                    valx = -1;
                                    BukkitUtil.setBlock(worldObj, absX + valx, by, absZ + bz, 0, (byte) 0);
                                }

                                BukkitUtil.setBlock(worldObj, absX + bx, by, absZ + bz, 0, (byte) 0);
                            }
                        }
                    }
                }

                for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC.values()) {

                    final PlotLoc loc = new PlotLoc(blockwrapper.x, blockwrapper.z);

                    if (!spw.GENERATOR_PLOT_LOCS.contains(loc)) {
                        final PlotBlock plotblock = new PlotBlock(blockwrapper.id, blockwrapper.data);
                        int valz = blockwrapper.z;
                        if (valz == (spw.LENGTH - 1)) {
                            valz = -1;
                            BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + valz, plotblock.id, plotblock.data);
                        }

                        int valx = blockwrapper.x;
                        if (valx == (spw.LENGTH - 1)) {
                            valx = -1;
                            BukkitUtil.setBlock(worldObj, absX + valx, blockwrapper.y, absZ + blockwrapper.z, plotblock.id, plotblock.data);
                        }
                        BukkitUtil.setBlock(worldObj, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, plotblock.id, plotblock.data);
                    }
                }
            }
        }

        return true;
    }

    @Override
    public boolean unclaimPlot(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public boolean createRoadEast(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public boolean createRoadSouth(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public boolean createRoadSouthEast(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public com.intellectualcrafters.plot.object.Location getPlotBottomLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SchemPlotWorld spw = ((SchemPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = ((px - 1) * spw.WIDTH) - 1;
        final int z = ((pz - 1) * spw.LENGTH) - 1;

        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, x, 1, z);
    }

    @Override
    public com.intellectualcrafters.plot.object.Location getPlotTopLocAbs(final PlotWorld plotworld, final PlotId plotid) {
        final SchemPlotWorld spw = ((SchemPlotWorld) plotworld);

        final int px = plotid.x;
        final int pz = plotid.y;

        final int x = ((px - 1) * spw.WIDTH) + spw.MAX_X;
        final int z = ((pz - 1) * spw.LENGTH) + spw.MAX_Z;

        return new com.intellectualcrafters.plot.object.Location(plotworld.worldname, x, 1, z);
    }

    @Override
    public boolean removeRoadEast(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public boolean removeRoadSouth(final PlotWorld arg0, final Plot arg1) {
        return false;
    }

    @Override
    public boolean removeRoadSouthEast(final PlotWorld arg0, final Plot arg1) {
        return false;
    }
}
