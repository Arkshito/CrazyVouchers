package com.badbones69.crazyvouchers.support;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.lang.reflect.Method;
import java.util.List;

public class NexoSupport {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    public static boolean isAvailable() {
        return Bukkit.getPluginManager().isPluginEnabled("Nexo");
    }

    public static @Nullable ItemStack buildItem(@NotNull final String nexoId,
                                                @NotNull final List<String> overrideLore,
                                                @NotNull final String overrideGlowing,
                                                final int overrideCustomModelData,
                                                final int amount) {
        try {
            final Plugin nexoPlugin = Bukkit.getPluginManager().getPlugin("Nexo");
            if (nexoPlugin == null) return null;

            final ClassLoader nexoClassLoader = nexoPlugin.getClass().getClassLoader();

            final Class<?> nexoItemsClass = Class.forName("com.nexomc.nexo.api.NexoItems", true, nexoClassLoader);
            final Class<?> itemBuilderClass = Class.forName("com.nexomc.nexo.items.ItemBuilder", true, nexoClassLoader);

            final Method itemFromId = nexoItemsClass.getMethod("itemFromId", String.class);
            final Object builder = itemFromId.invoke(null, nexoId);

            if (builder == null) return null;

            if (!overrideLore.isEmpty()) {
                final List<Component> components = overrideLore.stream()
                        .map(MM::deserialize)
                        .toList();
                final Method loreMethod = itemBuilderClass.getMethod("lore", List.class);
                loreMethod.invoke(builder, components);
            }

            switch (overrideGlowing.toLowerCase()) {
                case "add_glow", "true" -> {
                    final Method m = itemBuilderClass.getMethod("setEnchantmentGlintOverride", Boolean.class);
                    m.invoke(builder, true);
                }
                case "remove_glow", "false" -> {
                    final Method m = itemBuilderClass.getMethod("setEnchantmentGlintOverride", Boolean.class);
                    m.invoke(builder, false);
                }
                default -> {}
            }

            if (overrideCustomModelData != -1) {
                final Method cmdMethod = itemBuilderClass.getMethod("customModelData", int.class);
                cmdMethod.invoke(builder, overrideCustomModelData);
            }

            final Method buildMethod = itemBuilderClass.getMethod("build");
            final ItemStack item = (ItemStack) buildMethod.invoke(builder);

            if (item != null) item.setAmount(amount);

            return item;
        } catch (final Exception e) {
            Bukkit.getLogger().warning("[CrazyVouchers] Error building Nexo item '" + nexoId + "': " + e.getMessage());
            return null;
        }
    }
}
