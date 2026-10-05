package com.example.custompaintings;

import net.minecraft.entity.item.EntityPainting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        if (files != null && files.length > 0) {
            try {
                // Получаем все существующие значения EnumArt
                EntityPainting.EnumArt[] oldValues = EntityPainting.EnumArt.values();
                List<EntityPainting.EnumArt> newValuesList = new ArrayList<>(Arrays.asList(oldValues));

                // Ищем скрытый конструктор EnumArt в рантайме PojavLauncher
                Constructor<?> targetConstructor = null;
                for (Constructor<?> c : EntityPainting.EnumArt.class.getDeclaredConstructors()) {
                    if (c.getParameterCount() == 5) { // Имя константы, порядковый номер, название картины, ширина, высота
                        c.setAccessible(true);
                        targetConstructor = c;
                        break;
                    }
                }

                if (targetConstructor == null) {
                    throw new NoSuchMethodException("Не найден подходящий конструктор EnumArt");
                }

                int nextOrdinal = oldValues.length;

                for (File file : files) {
                    String fileName = file.getName();
                    String motiveName = fileName.substring(0, fileName.lastIndexOf('.')).toLowerCase();
                    
                    BufferedImage image = ImageIO.read(file);
                    if (image != null) {
                        int widthInBlocks = (int) Math.ceil((double) image.getWidth() / 16.0);
                        int heightInBlocks = (int) Math.ceil((double) image.getHeight() / 16.0);

                        // Создаем экземпляр Enum вручную, передавая точные аргументы
                        EntityPainting.EnumArt customMotive = (EntityPainting.EnumArt) targetConstructor.newInstance(
                                motiveName.toUpperCase(), 
                                nextOrdinal++, 
                                motiveName, 
                                widthInBlocks * 16, 
                                heightInBlocks * 16
                        );

                        newValuesList.add(customMotive);
                        CUSTOM_FILES.put(motiveName, file);
                        logger.info("Успешно подготовлена картина: " + motiveName);
                    }
                }

                // Перезаписываем внутреннее поле $VALUES массива EnumArt через рефлексию
                for (Field field : EntityPainting.EnumArt.class.getDeclaredFields()) {
                    if (field.getName().equals("$VALUES") || field.getType().isArray() && field.getType().getComponentType() == EntityPainting.EnumArt.class) {
                        field.setAccessible(true);
                        
                        // Убираем модификатор final у поля
                        Field modifiersField = Field.class.getDeclaredField("modifiers");
                        modifiersField.setAccessible(true);
                        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
                        
                        EntityPainting.EnumArt[] newValuesArray = newValuesList.toArray(new EntityPainting.EnumArt[0]);
                        field.set(null, newValuesArray);
                        logger.info("Массив картинок EnumArt успешно расширен на мобильном клиенте!");
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Критическая ошибка при динамической инжекции картин", e);
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
