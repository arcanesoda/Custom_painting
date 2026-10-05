package com.example.custompaintings;

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Mod(modid = CustomPaintingsMod.MODID, name = CustomPaintingsMod.NAME, version = CustomPaintingsMod.VERSION)
public class CustomPaintingsMod {
    public static final String MODID = "custompaintings";
    public static final String NAME = "Custom Paintings";
    public static final String VERSION = "1.0";

    public static Logger logger;
    public static File paintingsDir;
    public static final Map<String, File> CUSTOM_FILES = new HashMap<>();

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
                try {
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        int widthInBlocks = (int) Math.ceil((double) image.getWidth() / 16.0);
                        int heightInBlocks = (int) Math.ceil((double) image.getHeight() / 16.0);
                        
                        // Используем правильный EnumArt для версии 1.12.2
                        EnumHelper.addEnum(EntityPainting.EnumArt.class, motiveName.toUpperCase(),
                                new Class<?>[]{String.class, int.class, int.class},
                                motiveName, widthInBlocks * 16, heightInBlocks * 16);
                        
                        CUSTOM_FILES.put(motiveName, file);
                    }
                } catch (IOException e) {
                    logger.error("Ошибка загрузки: " + file.getName(), e);
                }
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
