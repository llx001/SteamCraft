package eu.steamcraft;

import eu.steamcraft.air.AirChunkEvents;
import eu.steamcraft.air.AirQualitySystem;
import eu.steamcraft.air.PlantAirGameRules;
import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.entity.ModBlockEntities;
import eu.steamcraft.block.entity.custom.FridgeBlockEntity;
import eu.steamcraft.command.AirDebugCommand;
import eu.steamcraft.command.EnergyDebugCommand;
import eu.steamcraft.command.FoodDebugCommand;
import eu.steamcraft.command.SteamCraftCommand;
import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.item.ModItemGroups;
import eu.steamcraft.item.ModItems;
import eu.steamcraft.item.FoodPouchItem;
import eu.steamcraft.item.PortableScannerItem;
import eu.steamcraft.network.AirHealthMapPayload;
import eu.steamcraft.network.AirHudPayload;
import eu.steamcraft.network.AirVisualizePayload;
import eu.steamcraft.screen.ModScreenHandlers;
import eu.steamcraft.sound.ModSounds;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import team.reborn.energy.api.EnergyStorage;

public class SteamCraft implements ModInitializer {
	public static final String MOD_ID = "steamcraft";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {

        AirChunkEvents.register();
        PlantAirGameRules.register();
        ModScreenHandlers.registerScreenHandlers();
						PayloadTypeRegistry.playS2C().register(AirVisualizePayload.ID, AirVisualizePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AirHudPayload.ID, AirHudPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(AirHealthMapPayload.ID, AirHealthMapPayload.CODEC);
        // FIX: Register command via the Fabric callback to get the dispatcher
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            SteamCraftCommand.register(dispatcher);
        });

		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if (world.isClient()) {
				return ActionResult.PASS;
			}

			BlockEntity blockEntity = world.getBlockEntity(hitResult.getBlockPos());
      if (blockEntity instanceof Inventory inventory && !(blockEntity instanceof FridgeBlockEntity)) {
				FoodExpirySystem.stampInventory(inventory, FoodExpirySystem.getCurrentDay(world));
			}

			return ActionResult.PASS;
		});

        ModItemGroups.registerItemGroups();

        ModBlocks.registerBlocks();
        ModBlockEntities.registerAllBlockEntities();
        ModItems.registerItems();
        ModSounds.registerSounds();

        // Register Team Reborn Energy storage
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.BATTERY_BE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.ADVANCED_BATTERY_BE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.ELECTRIC_TORCH_BE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.AIR_SCRUBBER_BE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.FRIDGE_BE
        );
        EnergyStorage.SIDED.registerForBlockEntity(
                (be, direction) -> be.getEnergyStorage(),
                ModBlockEntities.ENVIRONMENTAL_MONITOR_BE
        );

        ServerTickEvents.END_WORLD_TICK.register(AirQualitySystem::tick);
        ServerTickEvents.END_WORLD_TICK.register(PortableScannerItem::tickWorld);
        ServerTickEvents.END_WORLD_TICK.register(FoodPouchItem::tickWorld);
        // Convert expired food in player inventories to Rotten Food every 2 seconds.
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (world.getTime() % 40 != 0) return;
            long currentDay = FoodExpirySystem.getCurrentDay(world);
            for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
                net.minecraft.entity.player.PlayerInventory inv = player.getInventory();
                for (int i = 0; i < inv.size(); i++) {
                    ItemStack stack = inv.getStack(i);
                    if (stack.isEmpty()) continue;
                    if (!FoodExpirySystem.isTrackedFood(stack.getItem())) continue;
                    if (FoodExpirySystem.getCreatedDay(stack) == null) continue;
                    if (FoodExpirySystem.getExpiresInDays(stack, currentDay) < 0) {
                        inv.setStack(i, new ItemStack(ModItems.ROTTEN_FOOD, stack.getCount()));
                    }
                }
            }
        });

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            if (!source.isBuiltin()) return;
            var id = key.getValue();
            if (!id.getNamespace().equals("minecraft")) return;
            String path = id.getPath();
            if (!path.equals("entities/cow") && !path.equals("entities/pig")
                    && !path.equals("entities/sheep") && !path.equals("entities/chicken")
                    && !path.equals("entities/rabbit")) return;
            tableBuilder.pool(LootPool.builder()
                    .rolls(ConstantLootNumberProvider.create(1))
                    .with(ItemEntry.builder(ModItems.BIOMASS))
                    .conditionally(RandomChanceLootCondition.builder(0.5f))
                    .build());
        });


	}

}