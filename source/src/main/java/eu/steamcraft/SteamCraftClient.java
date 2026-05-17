package eu.steamcraft;

import eu.steamcraft.air.AirHealthMapClient;
import eu.steamcraft.air.AirHudClient;
import eu.steamcraft.air.AirVisualizationClient;
import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.custom.FridgeBlock;
import eu.steamcraft.energy.EnergyHelper;
import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.item.ModItems;
import eu.steamcraft.item.PortableScannerItem;
import eu.steamcraft.screen.BatteryScreen;
import eu.steamcraft.screen.FoodPouchScreen;
import eu.steamcraft.screen.FridgeScreen;
import eu.steamcraft.screen.ModScreenHandlers;
import eu.steamcraft.screen.TreeTapScreen;
import eu.steamcraft.sound.FridgeHumSoundInstance;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import team.reborn.energy.api.base.SimpleEnergyItem;

import java.util.HashMap;
import java.util.Map;

public class SteamCraftClient implements ClientModInitializer {

    // Tracks active fridge hum sounds by block position
    private static final Map<BlockPos, FridgeHumSoundInstance> activeFridgeSounds = new HashMap<>();
    private static int fridgeSoundScanTimer = 0;
    private static final int FRIDGE_SCAN_INTERVAL = 20; // ticks
    private static final int FRIDGE_SCAN_RADIUS   = 16;

    @Override
    public void onInitializeClient() {
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ELECTRIC_TORCH,
            net.minecraft.client.render.RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.ELECTRIC_TORCH_WALl,
            net.minecraft.client.render.RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.UNLIT_TORCH,
            net.minecraft.client.render.RenderLayer.getCutout()
        );
        BlockRenderLayerMap.INSTANCE.putBlock(
            ModBlocks.UNLIT_WALL_TORCH,
            net.minecraft.client.render.RenderLayer.getCutout()
        );

        HudRenderCallback.EVENT.register((DrawContext ctx, RenderTickCounter tickCounter) -> {
            MinecraftClient client = MinecraftClient.getInstance();
            PlayerEntity player = client.player;
            if (player == null) return;

            if (hasHazmatHelmet(player) || hasScannerActive(player)) {
                drawAirQualityHud(ctx);
            }
        });

        registerSoilColors();
        AirVisualizationClient.register();
        AirHudClient.register();
        AirHealthMapClient.register();
        HandledScreens.register(ModScreenHandlers.TREE_TAP, TreeTapScreen::new);
        HandledScreens.register(ModScreenHandlers.BATTERY, BatteryScreen::new);
        HandledScreens.register(ModScreenHandlers.FOOD_POUCH, FoodPouchScreen::new);
        HandledScreens.register(ModScreenHandlers.FRIDGE, FridgeScreen::new);
        registerEnergyTooltip();
        registerFoodVisuals();
        registerFridgeSoundManager();
    }

    private static void registerFridgeSoundManager() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) {
                // Stop all fridge sounds when leaving a world
                activeFridgeSounds.values().forEach(s -> client.getSoundManager().stop(s));
                activeFridgeSounds.clear();
                fridgeSoundScanTimer = 0;
                return;
            }

            // Remove sounds for fridges that turned off or were broken
            activeFridgeSounds.entrySet().removeIf(entry -> {
                FridgeHumSoundInstance sound = entry.getValue();
                if (sound.isDone()) return true;
                BlockState state = client.world.getBlockState(entry.getKey());
                if (!state.isOf(ModBlocks.FRIDGE) || !state.get(FridgeBlock.POWERED)) {
                    client.getSoundManager().stop(sound);
                    return true;
                }
                return false;
            });

            // Periodically scan for newly powered fridges in range
            if (++fridgeSoundScanTimer < FRIDGE_SCAN_INTERVAL) return;
            fridgeSoundScanTimer = 0;

            BlockPos playerPos = client.player.getBlockPos();
            for (BlockPos pos : BlockPos.iterateOutwards(playerPos, FRIDGE_SCAN_RADIUS, FRIDGE_SCAN_RADIUS, FRIDGE_SCAN_RADIUS)) {
                BlockState state = client.world.getBlockState(pos);
                if (state.isOf(ModBlocks.FRIDGE) && state.get(FridgeBlock.POWERED)) {
                    BlockPos immutable = pos.toImmutable();
                    if (!activeFridgeSounds.containsKey(immutable)) {
                        FridgeHumSoundInstance sound = new FridgeHumSoundInstance(immutable);
                        client.getSoundManager().play(sound);
                        activeFridgeSounds.put(immutable, sound);
                    }
                }
            }
        });
    }

    private static void registerSoilColors() {
        // Tint top + sides using the biome grass colormap, matching surrounding grass blocks.
        ColorProviderRegistry.BLOCK.register(
                (state, world, pos, tintIndex) -> world != null && pos != null
                        ? BiomeColors.getGrassColor(world, pos)
                        : 0x79C05A,
                ModBlocks.RICH_SOIL, ModBlocks.POISONED_SOIL);

        ColorProviderRegistry.ITEM.register(
                (stack, tintIndex) -> 0x79C05A,
                ModBlocks.RICH_SOIL.asItem(), ModBlocks.POISONED_SOIL.asItem());
    }

    private static void registerEnergyTooltip() {
        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            long energy = stack.getItem() instanceof SimpleEnergyItem energyItem
                    ? energyItem.getStoredEnergy(stack)
                    : 0;

            if (stack.getItem() instanceof eu.steamcraft.item.BatteryBlockItem bat) {
                lines.add(Text.literal("Energy: " + energy + " / " + bat.getMaxEnergy() + " FE")
                        .formatted(Formatting.AQUA));
                if (net.minecraft.client.gui.screen.Screen.hasShiftDown()) {
                    lines.add(Text.literal("Solar: " + bat.getSolarChargeRate() + " FE/t")
                            .formatted(Formatting.YELLOW));
                } else {
                    lines.add(Text.literal("Hold SHIFT for details").formatted(Formatting.DARK_GRAY));
                }
                return;
            }

            // Electric torch — show energy only when it carries a charge
            if (stack.getItem() == ModBlocks.ELECTRIC_TORCH_ITEM && energy > 0) {
                lines.add(Text.literal("Energy: " + energy + " / " + EnergyHelper.TORCH_MAX_ENERGY + " FE")
                        .formatted(Formatting.AQUA));
                return;
            }

            if (stack.getItem() instanceof PortableScannerItem) {
                boolean enabled = EnergyHelper.isEnabled(stack);
                lines.add(Text.literal("Energy: " + energy + " / " + EnergyHelper.SCANNER_MAX_ENERGY + " FE")
                        .formatted(Formatting.AQUA));
                lines.add(Text.literal("Status: " + (enabled ? "ON" : "OFF"))
                        .formatted(enabled ? Formatting.GREEN : Formatting.GRAY));
                lines.add(Text.literal("Shift+Right Click to toggle").formatted(Formatting.DARK_GRAY));
            }
        });
    }

    private static void registerFoodVisuals() {
        for (Item item : Registries.ITEM) {
            if (!FoodExpirySystem.isTrackedFood(item)) {
                continue;
            }

            ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
                if (tintIndex != 0) {
                    return 0xFFFFFFFF;
                }

                Long createdDay = FoodExpirySystem.getCreatedDay(stack);
                MinecraftClient client = MinecraftClient.getInstance();
                if (createdDay == null || client.world == null) {
                    return 0xFFFFFFFF;
                }

                long currentDay = FoodExpirySystem.getCurrentDay(client.world);
                float freshness = FoodExpirySystem.getFreshness(
                        createdDay,
                        FoodExpirySystem.getMaxAgeDays(stack.getItem()),
                        currentDay
                );
                return FoodExpirySystem.computeTint(freshness);
            }, item);
        }

        ItemTooltipCallback.EVENT.register((stack, context, type, lines) -> {
            if (!FoodExpirySystem.isTrackedFood(stack.getItem())) {
                return;
            }

            Long createdDay = FoodExpirySystem.getCreatedDay(stack);
            MinecraftClient client = MinecraftClient.getInstance();
            if (createdDay == null || client.world == null) {
                return;
            }

            long currentDay = FoodExpirySystem.getCurrentDay(client.world);
            long expiresIn = (createdDay + FoodExpirySystem.getMaxAgeDays(stack.getItem())) - currentDay;

            if (expiresIn > 0) {
                String suffix = expiresIn == 1 ? "" : "s";
                lines.add(Text.literal("Expires in " + expiresIn + " day" + suffix).formatted(Formatting.GRAY));
            } else {
                lines.add(Text.literal("Expired").formatted(Formatting.RED));
            }
        });
    }

    private static boolean hasHazmatHelmet(PlayerEntity player) {
        ItemStack helmet = player.getEquippedStack(EquipmentSlot.HEAD);
        return helmet.isOf(ModItems.HAZMAT_ARMOR_HELMET);
    }

    private static boolean hasScannerActive(PlayerEntity player) {
        ItemStack main = player.getMainHandStack();
        ItemStack off  = player.getOffHandStack();
        return (main.getItem() instanceof PortableScannerItem && EnergyHelper.isEnabled(main))
            || (off.getItem()  instanceof PortableScannerItem && EnergyHelper.isEnabled(off));
    }
    private static final Identifier AIR_BG     = Identifier.of("steamcraft", "textures/gui/air_overlay_bg.png");
    private static final Identifier AIR_CO2    = Identifier.of("steamcraft", "textures/gui/air_co2_line.png");
    private static final Identifier AIR_OXYGEN = Identifier.of("steamcraft", "textures/gui/air_oxigen_line.png");
    private static final Identifier AIR_TOXINS = Identifier.of("steamcraft", "textures/gui/air_toxin_line.png");

    // Background frame: 81×9, colored bar: 77×5 (offset 2px each side)
    private static final int BG_W       = 81;
    private static final int BG_H       = 9;
    private static final int BAR_W      = 77;
    private static final int BAR_H      = 5;
    private static final int BAR_INSET  = 2;
    private static final int ROW_GAP    = 2;
    private static final int HUD_MARGIN = 4;

    private static void drawAirQualityHud(DrawContext ctx) {
        int totalH = 3 * BG_H + 2 * ROW_GAP;
        int x = HUD_MARGIN;
        int y = ctx.getScaledWindowHeight() - totalH - 16;

        drawAirBar(ctx, x, y,                       AIR_CO2,    AirHudClient.co2);
        drawAirBar(ctx, x, y + BG_H + ROW_GAP,      AIR_OXYGEN, AirHudClient.oxygen);
        drawAirBar(ctx, x, y + 2 * (BG_H + ROW_GAP), AIR_TOXINS, AirHudClient.toxins);
    }

    private static void drawAirBar(DrawContext ctx, int x, int y, Identifier lineTexture, float value) {
        ctx.drawTexture(AIR_BG, x, y, 0, 0, BG_W, BG_H, BG_W, BG_H);

        int fillW = Math.round(net.minecraft.util.math.MathHelper.clamp(value / 100f, 0f, 1f) * BAR_W);
        if (fillW > 0) {
            ctx.drawTexture(lineTexture, x + BAR_INSET, y + BAR_INSET, 0, 0, fillW, BAR_H, BAR_W, BAR_H);
        }
    }
}
