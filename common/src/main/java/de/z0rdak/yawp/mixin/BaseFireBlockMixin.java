package de.z0rdak.yawp.mixin;

import de.z0rdak.yawp.api.events.flag.FlagCheckRequest;
import de.z0rdak.yawp.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static de.z0rdak.yawp.api.FlagEvaluator.processCheck;
import static de.z0rdak.yawp.core.flag.RegionFlag.FIRE_IGNITE;

@Mixin(BaseFireBlock.class)
public abstract class BaseFireBlockMixin {

    /**
     * fire-ignite
     * Prevents new fire from ever being created within a protected region.
     * Nearly every ignition source (flint and steel, fire charge, fireballs,
     * lightning strikes and fire spreading in from neighbours) resolves the fire
     * block state to place via {@link BaseFireBlock#getState}. Returning air here
     * means the subsequent block placement never yields a fire block.
     */
    @Inject(method = "getState(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), cancellable = true)
    private static void onGetFireState(BlockGetter getter, BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        if (getter instanceof ServerLevel world) {
            FlagCheckRequest checkEvent = new FlagCheckRequest(pos, FIRE_IGNITE, world.dimension());
            if (Services.FLAG_EVENT_DISPATCHER.post(checkEvent)) {
                return;
            }
            processCheck(checkEvent, denyResult -> {
                cir.setReturnValue(Blocks.AIR.defaultBlockState());
            });
        }
    }
}
