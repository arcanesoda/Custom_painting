package com.example.custompaintings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class ClientTextureLoader {
    @SubscribeEvent
    public void onTextureStitch(TextureStitchEvent.Pre event) {
        for (Map.Entry<String, File> entry : CustomPaintingsMod.CUSTOM_FILES.entrySet()) {
            String motiveName = entry.getKey();
            File file = entry.getValue();
            try {
                BufferedImage image = ImageIO.read(file);
                if (image != null) {
                    DynamicTexture dynamicTexture = new DynamicTexture(image);
                    ResourceLocation location = new ResourceLocation("minecraft", "textures/painting/" + motiveName + ".png");
                    Minecraft.getMinecraft().getTextureManager().loadTexture(location, dynamicTexture);
                }
            } catch (IOException e) {
                CustomPaintingsMod.logger.error("Ошибка текстуры: " + motiveName, e);
            }
        }
    }
}
