package com.froggyy277353.aeronet.registry;

import com.froggyy277353.aeronet.AeroNet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AeroNet.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AERONET =
            TABS.register("aeronet", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.aeronet"))
                    .icon(() -> new ItemStack(ModItems.SERVER_BLOCK.get()))
                    .displayItems((parameters, output) ->
                            ModItems.ITEMS.getEntries().forEach(item -> output.accept(item.get())))
                    .build());
}
