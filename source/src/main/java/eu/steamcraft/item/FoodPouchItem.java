package eu.steamcraft.item;

import eu.steamcraft.food.FoodExpirySystem;
import eu.steamcraft.screen.FoodPouchScreenHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.registry.RegistryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FoodPouchItem extends Item {
    private static final int SLOT_COUNT = 9;
    private static final Map<RegistryKey<World>, Long> LAST_SWEEP_DAY = new HashMap<>();

    public FoodPouchItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (world.isClient()) {
            return TypedActionResult.success(stack);
        }

        if (!(user instanceof ServerPlayerEntity serverPlayer)) {
            return TypedActionResult.success(stack);
        }

        serverPlayer.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInventory, player) -> new FoodPouchScreenHandler(syncId, playerInventory, hand),
                stack.getName()
        ));
        return TypedActionResult.success(stack);
    }

    public static void tickWorld(ServerWorld world) {
        long currentDay = FoodExpirySystem.getCurrentDay(world);
        RegistryKey<World> key = world.getRegistryKey();
        Long lastDay = LAST_SWEEP_DAY.get(key);
        if (lastDay != null && lastDay == currentDay) {
            return;
        }
        LAST_SWEEP_DAY.put(key, currentDay);

        for (ServerPlayerEntity player : world.getPlayers()) {
            boolean playerChanged = false;
            Inventory playerInventory = player.getInventory();

            for (int i = 0; i < playerInventory.size(); i++) {
                ItemStack stack = playerInventory.getStack(i);
                if (!(stack.getItem() instanceof FoodPouchItem)) {
                    continue;
                }

                ContainerComponent container = stack.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
                List<ItemStack> contents = new ArrayList<>(SLOT_COUNT);
                container.stream().forEach(entry -> contents.add(entry.copy()));
                while (contents.size() < SLOT_COUNT) {
                    contents.add(ItemStack.EMPTY);
                }

                boolean pouchChanged = false;
                for (int slot = 0; slot < SLOT_COUNT; slot++) {
                    ItemStack entry = contents.get(slot);
                    if (entry.isEmpty() || !FoodExpirySystem.isTrackedFood(entry.getItem())) {
                        continue;
                    }

                    FoodExpirySystem.applyPreservation(entry, 0.5, currentDay);
                    pouchChanged = true;
                }

                if (pouchChanged) {
                    stack.set(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(contents));
                    playerChanged = true;
                }
            }

            if (playerChanged) {
                player.playerScreenHandler.sendContentUpdates();
            }
        }
    }
}

