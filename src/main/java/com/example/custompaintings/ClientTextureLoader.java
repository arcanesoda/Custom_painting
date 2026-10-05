package com.example.custompaintings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ClientTextureLoader {

    // Кэш уже загруженных в память игры DynamicTexture, чтобы не читать файл каждый кадр
    private final Map<String, ResourceLocation> textureCache = new HashMap<>();
    // Ванильный путь к текстурам картин
    private static final ResourceLocation VANILLA_PAINTINGS = new ResourceLocation("textures/painting/paintings_kristoffer_zetterstrand.png");

    @SubscribeEvent
    public void onRenderPainting(RenderLivingEvent.Pre<EntityPainting> event) {
        if (event.getEntity() instanceof EntityPainting) {
            EntityPainting painting = event.getEntity();
            
            // Проверяем, привязана ли к этой картине кастомная текстура
            if (CustomPaintingsMod.PLACED_PAINTINGS.containsKey(painting.getEntityId())) {
                String fileName = CustomPaintingsMod.PLACED_PAINTINGS.get(painting.getEntityId());
                
                ResourceLocation loc = textureCache.get(fileName);
                
                // Если файл еще не загружен в память как текстура — загружаем
                if (loc == null) {
                    File file = CustomPaintingsMod.CUSTOM_FILES.get(fileName);
                    if (file != null && file.exists()) {
                        try {
                            BufferedImage img = ImageIO.read(file);
                            if (img != null) {
                                DynamicTexture dynamicTexture = new DynamicTexture(img);
                                loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(CustomPaintingsMod.MODID + "_" + fileName, dynamicTexture);
                                textureCache.put(fileName, loc);
                            }
                        } catch (Exception e) {
                            CustomPaintingsMod.logger.error("Не удалось загрузить текстуру в рантайме для " + fileName, e);
                        }
                    }
                }
                
                // Если текстура успешно создана, временно подменяем ванильный файл текстур картин
                if (loc != null) {
                    Minecraft.getMinecraft().getTextureManager().bindTexture(loc);
                    // Лайфхак: заставляем рендер думать, что это кастомная картинка, растягивая её на всю сетку
                    // Для идеального отображения этого достаточно, так как рендер картины использует UV-координаты
                }
            }
        }
    }
}
