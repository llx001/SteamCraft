package eu.steamcraft.sound;

import eu.steamcraft.block.ModBlocks;
import eu.steamcraft.block.custom.FridgeBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class FridgeHumSoundInstance extends MovingSoundInstance {
    private final BlockPos pos;

    public FridgeHumSoundInstance(BlockPos pos) {
        super(ModSounds.FRIDGE_HUM, SoundCategory.BLOCKS, SoundInstance.createRandom());
        this.pos = pos.toImmutable();
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.6f;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    @Override
    public void tick() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) {
            this.setDone();
            return;
        }
        BlockState state = client.world.getBlockState(this.pos);
        if (!state.isOf(ModBlocks.FRIDGE) || !state.get(FridgeBlock.POWERED)) {
            this.setDone();
        }
    }
}

