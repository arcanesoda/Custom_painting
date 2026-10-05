package com.example.custompaintings;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = CustomPaintingsMod.MODID, name = CustomPaintingsMod.NAME, version = CustomPaintingsMod.VERSION)
public class CustomPaintingsMod {
    public static final String MODID = "custompaintings";
    public static final String NAME = "Custom Paintings";
    public static final String VERSION = "1.0";

    public static Logger logger;
    public static File paintingsDir;
    
    // Храним список доступных имен файлов картинок
    public static final Map<String, File> CUSTOM_FILES = new HashMap<>();
    // Связка: ID сущности картины в мире -> имя кастомного файла текстуры
    public static final Map<Integer, String> PLACED_PAINTINGS = new HashMap<>();

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        paintingsDir = new File(event.getModConfigurationDirectory(), "custom_paintings");
        if (!paintingsDir.exists()) {
            paintingsDir.mkdirs();
        }

        File[] files = paintingsDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                String motiveName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();
                CUSTOM_FILES.put(motiveName, file);
                logger.info("Найден файл кастомной картины: " + motiveName);
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new PaintingEventHandler());
        if (net.minecraftforge.fml.common.FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(new ClientTextureLoader());
        }
    }
}
