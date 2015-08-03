package com.empcraft.schem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import org.bukkit.World;

import com.intellectualcrafters.configuration.ConfigurationSection;
import com.intellectualcrafters.plot.PS;
import com.intellectualcrafters.plot.commands.Template;
import com.intellectualcrafters.plot.object.FileBytes;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotBlock;
import com.intellectualcrafters.plot.object.PlotId;
import com.intellectualcrafters.plot.object.PlotManager;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.intellectualcrafters.plot.object.PlotWorld;
import com.intellectualcrafters.plot.util.BlockManager;
import com.intellectualcrafters.plot.util.MainUtil;
import com.intellectualcrafters.plot.util.SetBlockQueue;
import com.intellectualcrafters.plot.util.TaskManager;
import com.intellectualcrafters.plot.util.UUIDHandler;
import com.plotsquared.bukkit.util.BukkitUtil;

public class SchemPlotManager extends PlotManager {

    @Override
    public void exportTemplate(PlotWorld plotworld) throws IOException {
        ConfigurationSection config = PS.get().config.getConfigurationSection("worlds." + plotworld.worldname);
        final String schem1Str = config.getString("plot.schematic.default");
        final String schem2Str = config.getString("plot.schematic.merged_east");
        final String schem3Str = config.getString("plot.schematic.merged_southeast");
        final String schem4Str = config.getString("plot.schematic.merged_all");
        HashSet<FileBytes> files = new HashSet<>(Arrays.asList(new FileBytes("templates/" + "tmp-data.yml", Template.getBytes(plotworld))));
        String schemRoot = PS.get().IMP.getDirectory() + File.separator + "schematics" + File.separator;
        String newDir =  "schematics" + File.separator;
        try {
            File file1 = new File(schemRoot + schem1Str + ".schematic");
            if (file1.exists()) {
                files.add(new FileBytes(newDir + schem1Str + ".schematic", Files.readAllBytes(file1.toPath())));
            }
            
            File file2 = new File(schemRoot + schem2Str + ".schematic");
            if (file2.exists()) {
                files.add(new FileBytes(newDir + schem2Str + ".schematic", Files.readAllBytes(file2.toPath())));
            }
            
            File file3 = new File(schemRoot + schem3Str + ".schematic");
            if (file3.exists()) {
                files.add(new FileBytes(newDir + schem3Str + ".schematic", Files.readAllBytes(file3.toPath())));
            }
            
            File file4 = new File(schemRoot + schem4Str + ".schematic");
            if (file4.exists()) {
                files.add(new FileBytes(newDir + schem4Str + ".schematic", Files.readAllBytes(file4.toPath())));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        Template.zipAll(plotworld.worldname, files);
    }
    
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
        final Plot plot = PS.get().getPlots(plotworld.worldname).get(id);
        if (plot != null) {
            if (plot.isMerged()) {
                if (spw.GENERATOR_PLOT_LOCS.contains(new PlotLoc((short) rx, (short) rz))) {
                    return MainUtil.getBottomPlot(plot).id;
                }

                if (plot.getMerged(1)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) rx, (short) rz))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }

                    if (plot.getMerged(2)) {
                        if (spw.GENERATOR_PLOT_LOCS_SE.contains(new PlotLoc((short) rx, (short) rz))) {
                            return MainUtil.getBottomPlot(plot).id;
                        }
                    }
                }
                if (plot.getMerged(2)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) rz, (short) rx))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }
                }
                if (plot.getMerged(3)) {
                    if (spw.GENERATOR_PLOT_LOCS_E.contains(new PlotLoc((short) (spw.MAX_X - rx), (short) (spw.MAX_Z - rz)))) {
                        return MainUtil.getBottomPlot(plot).id;
                    }
                    if (plot.getMerged(0)) {
                        if (spw.GENERATOR_PLOT_LOCS_SE.contains(new PlotLoc((short) (spw.MAX_X - rx), (short) (spw.MAX_Z - rz)))) {
                            return MainUtil.getBottomPlot(plot).id;
                        }
                    }
                }
                if (plot.getMerged(0)) {
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
        final SchemPlotWorld spw = ((SchemPlotWorld) PS.get().getPlotWorld(plot.world));
        final com.intellectualcrafters.plot.object.Location pos1 = MainUtil.getPlotBottomLoc(plot.world, plot.id).add(1, 0, 1);
        final int botX = pos1.getX();
        final int botZ = pos1.getZ();
        for (final PlotLoc loc : spw.GENERATOR_PLOT_LOCS) {
            for (int by = 0; by < world.getMaxHeight(); by++) {
                SetBlockQueue.setBlock(plot.world, botX + loc.x, by, botZ + loc.z, 0);
            }
        }
        for (final BlockWrapper loc : spw.GENERATOR_SCHEMATIC.values()) {
            final PlotLoc plotloc = new PlotLoc(loc.x, loc.z);
            if (spw.GENERATOR_PLOT_LOCS.contains(plotloc)) {
                SetBlockQueue.setBlock(plot.world, loc.x + botX, loc.y + spw.PLOT_HEIGHT, loc.z + botZ, new PlotBlock(loc.id, loc.data));
            }
        }
        SetBlockQueue.addNotify(whenDone);
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

                final Plot plot = PS.get().getPlots(world).get(plotid);

                // check if less than MAX

                //                System.out.print("P: "+px+";"+pz);

                final World worldObj = BukkitUtil.getWorld(plotworld.worldname);

                if ((x < end.x) && (y < end.y)) {

                    //                    System.out.print("MERGE SE");

                    if (!plot.getMerged(1) || !plot.getMerged(2)) {
                        for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_SE.values()) {
                            SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, new PlotBlock(blockwrapper.id, blockwrapper.data));
                        }
                    }
                }

                if (x < end.x) {
                    if (start.y == end.y) {
                        //                        System.out.print("MERGE EAST");

                        if (!plot.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E2.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else if (y == start.y) {

                        //                            System.out.print("MERGE E1");

                        if (!plot.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else if (y == end.y) {
                        if (!plot.getMerged(1)) {

                            //                            System.out.print("MERGE E1 2");

                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, (absZ + spw.MAX_Z) - blockwrapper.z, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else {

                        //                        System.out.print("MERGE E0");

                        if (!plot.getMerged(1)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E0.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    }
                }
                if (y < end.y) {
                    if (start.x == end.x) {

                        //                        System.out.print("MERGE SOUTH");

                        if (!plot.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E2.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else if (x == start.x) {

                        //                        System.out.print("MERGE S1");

                        if (!plot.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else if (x == end.x) {

                        //                        System.out.print("MERGE S1 2");

                        if (!plot.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E1.values()) {
                                SetBlockQueue.setBlock(plot.world, (absX + spw.MAX_X) - blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, new PlotBlock(blockwrapper.id, blockwrapper.data));
                            }
                        }
                    } else {

                        //                        System.out.print("MERGE S0");

                        if (!plot.getMerged(2)) {
                            for (final BlockWrapper blockwrapper : spw.GENERATOR_SCHEMATIC_MERGED_E0.values()) {
                                SetBlockQueue.setBlock(plot.world, absX + blockwrapper.z, blockwrapper.y, absZ + blockwrapper.x, new PlotBlock(blockwrapper.id, blockwrapper.data));
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
        final Plot plot = PS.get().getPlots(world).get(plotIds.get(0));
        if (plot != null) {
            if (plot.hasOwner()) {
                final UUID owner = plot.owner;
                final PlotPlayer player = UUIDHandler.getPlayer(owner);
                if (player != null) {
                    MainUtil.sendMessage(player, "&cPlot unlinking is not fully implemented");
                }
            }
        }

        final int maxY = 255;

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
                                    SetBlockQueue.setBlock(plot.world, absX + bx, by, absZ + valz, 0);
                                }

                                int valx = bx;
                                if (valx == (spw.LENGTH - 1)) {
                                    valx = -1;
                                    SetBlockQueue.setBlock(plot.world, absX + valx, by, absZ + bz, 0);
                                }

                                SetBlockQueue.setBlock(plot.world, absX + bx, by, absZ + bz, 0);
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
                            SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + valz, plotblock);
                        }

                        int valx = blockwrapper.x;
                        if (valx == (spw.LENGTH - 1)) {
                            valx = -1;
                            SetBlockQueue.setBlock(plot.world, absX + valx, blockwrapper.y, absZ + blockwrapper.z, plotblock);
                        }
                        SetBlockQueue.setBlock(plot.world, absX + blockwrapper.x, blockwrapper.y, absZ + blockwrapper.z, plotblock);
                    }
                }
            }
        }

        return true;
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
