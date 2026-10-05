package com.example.custompaintings;

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class PaintingEventHandler {

    private String lastCustomName = "";
    private long lastClickTime = 0;

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getItemStack();
        // Запоминаем имя картины из руки в момент клика по блоку
        if (!heldItem.isEmpty() && heldItem.getItem() == net.minecraft.init.Items.PAINTING && heldItem.hasDisplayName()) {
            lastCustomName = heldItem.getDisplayName().toLowerCase().trim();
            lastClickTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityPainting) {
            EntityPainting painting = (EntityPainting) event.getEntity();
            
            // Если картина спавнится сразу после клика игрока переименованным предметом
            if (System.currentTimeMillis() - lastClickTime < 1000 && !lastCustomName.isEmpty()) {
                if (CustomPaintingsMod.CUSTOM_FILES.containsKey(lastCustomName)) {
                    // Привязываем ID этой сущности к имени текстуры
                    CustomPaintingsMod.PLACED_PAINTINGS.put(painting.getEntityId(), lastCustomName);
                    CustomPaintingsMod.logger.info("Картина c ID " + painting.getEntityId() + " привязана к текстуре " + lastCustomName);
                }
            }
        }
    }
}
