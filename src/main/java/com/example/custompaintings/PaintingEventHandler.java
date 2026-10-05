package com.example.custompaintings;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.List;

public class PaintingEventHandler {

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        // Перехватываем появление сущности картины в мире
        if (event.getEntity() instanceof EntityPainting) {
            EntityPainting painting = (EntityPainting) event.getEntity();
            
            // Нам нужно понять, какой игрок её повесил. Ищем ближайшего игрока в радиусе 6 блоков
            AxisAlignedBB searchArea = new AxisAlignedBB(painting.posX - 6, painting.posY - 6, painting.posZ - 6,
                                                         painting.posX + 6, painting.posY + 6, painting.posZ + 6);
            List<EntityPlayer> players = event.getWorld().getEntitiesWithinAABB(EntityPlayer.class, searchArea);
            
            for (EntityPlayer player : players) {
                // Проверяем, держит ли этот игрок картину в главной руке
                ItemStack heldItem = player.getHeldItemMainhand();
                
                if (!heldItem.isEmpty() && heldItem.getItem() == net.minecraft.init.Items.PAINTING && heldItem.hasDisplayName()) {
                    String customName = heldItem.getDisplayName().toLowerCase().trim();
                    
                    // Если имя совпадает с файлом из папки конфига
                    if (CustomPaintingsMod.CUSTOM_FILES.containsKey(customName)) {
                        for (EntityPainting.EnumArt motive : EntityPainting.EnumArt.values()) {
                            if (motive.title.equals(customName)) {
                                try {
                                    // Принудительно меняем холст картины внутри сущности
                                    // "field_75692_b" — это SRG-имя приватного поля "art" в Minecraft 1.12.2
                                    ObfuscationReflectionHelper.setPrivateValue(EntityPainting.class, painting, motive, "art", "field_75692_b");
                                    painting.art = motive;
                                    
                                    CustomPaintingsMod.logger.info("Успешно подменен холст для картины на: " + customName);
                                    return; // Нашли нужного игрока и подменили, выходим
                                } catch (Exception e) {
                                    CustomPaintingsMod.logger.error("Не удалось подменить холст через рефлексию", e);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
