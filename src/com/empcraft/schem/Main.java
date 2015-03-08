package com.empcraft.schem;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    @Override
    public ChunkGenerator getDefaultWorldGenerator(final String worldname, final String id) {
        return new SchemGen();
    }

}
