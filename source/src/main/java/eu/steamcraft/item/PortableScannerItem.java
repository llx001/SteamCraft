package eu.steamcraft.item;

import eu.steamcraft.energy.EnergyHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import team.reborn.energy.api.base.SimpleEnergyItem;

public class PortableScannerItem extends Item implements SimpleEnergyItem {
    public static final int DRAIN_PER_TICK = 2;

    public PortableScannerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (!user.isSneaking()) return TypedActionResult.pass(stack);
        if (world.isClient()) return TypedActionResult.success(stack);

        boolean enabled = EnergyHelper.isEnabled(stack);
        if (!enabled && getStoredEnergy(stack) <= 0) {
            user.sendMessage(Text.literal("Scanner has no energy").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }

        EnergyHelper.setEnabled(stack, !enabled);
        user.sendMessage(
            Text.literal("Scanner " + (!enabled ? "enabled" : "disabled"))
                .formatted(!enabled ? Formatting.GREEN : Formatting.GRAY),
            true
        );
        return TypedActionResult.success(stack);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return EnergyHelper.isEnabled(stack);
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        return Math.round(13f * getStoredEnergy(stack) / EnergyHelper.SCANNER_MAX_ENERGY);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        return 0x4488FF;
    }

    public static void tickWorld(ServerWorld world) {
        for (ServerPlayerEntity player : world.getPlayers()) {
            drainStack(player, player.getMainHandStack());
            drainStack(player, player.getOffHandStack());
        }
    }

    private static void drainStack(ServerPlayerEntity player, ItemStack stack) {
        if (!(stack.getItem() instanceof PortableScannerItem scanner)) return;
        if (!EnergyHelper.isEnabled(stack)) return;

        long energy = scanner.getStoredEnergy(stack);
        if (energy <= 0) {
            EnergyHelper.setEnabled(stack, false);
            player.sendMessage(Text.literal("Scanner powered off: no energy").formatted(Formatting.RED), true);
            player.playerScreenHandler.sendContentUpdates();
            return;
        }

        scanner.setStoredEnergy(stack, Math.max(0, energy - DRAIN_PER_TICK));
        player.playerScreenHandler.sendContentUpdates();
    }

    @Override
    public long getEnergyCapacity(ItemStack stack) {
        return EnergyHelper.SCANNER_MAX_ENERGY;
    }

    @Override
    public long getEnergyMaxInput(ItemStack stack) {
        return EnergyHelper.SCANNER_MAX_ENERGY;
    }

    @Override
    public long getEnergyMaxOutput(ItemStack stack) {
        return 0;
    }
}
