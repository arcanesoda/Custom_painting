package com.example.custompaintings;

import net.minecraft.entity.item.EntityPainting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class PaintingEventHandler {
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent.RightClickBlock event) {
        ItemStack heldItem = event.getItemStack();
        if (!heldItem.isEmpty() && heldItem.getItem() == net.minecraft.init.Items.PAINTING && heldItem.hasDisplayName()) {
            String customName = heldItem.getDisplayName().toLowerCase().trim();
            if (CustomPaintingsMod.CUSTOM_FILES.containsKey(customName)) {
                for (EntityPainting.Motive motive : EntityPainting.Motive.values()) {
                    if (motive.title.equals(customName)) {
                        event.getWorld().getMinecraftServer().addScheduledTask(() -> {
                            BlockPos pos = event.getPos();
                            java.util.List<EntityPainting> paintings = event.getWorld().getEntitiesWithinAABB(
                                    EntityPainting.class, new net.minecraft.util.math.AxisAlignedBB(pos).grow(2));
                            for (EntityPainting entityPainting : paintings) {
                                if (entityPainting.ticksExisted < 2) {
                                    try {
                                        ObfuscationReflectionHelper.setPrivateValue(EntityPainting.class, entityPainting, motive, "art", "field_75692_b");
                                        entityPainting.art = motive;
                                        break;
                                    } catch (Exception e) {
                                        CustomPaintingsMod.logger.error("Ошибка рефлексии", e);
                                    }
                                }
                            }
                        });
                        break;
                    }
                }
            }
        }
    }
}
