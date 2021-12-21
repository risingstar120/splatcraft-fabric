package net.splatcraft.mixin;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityTrackerUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.splatcraft.block.InkableBlock;
import net.splatcraft.entity.ItemEntityAccess;
import net.splatcraft.inkcolor.InkColor;
import net.splatcraft.inkcolor.Inkable;
import net.splatcraft.tag.SplatcraftBlockTags;
import net.splatcraft.world.SplatcraftGameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Inkable, ItemEntityAccess {
    @Shadow public abstract ItemStack getStack();

    private ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public InkColor getInkColor() {
        return Inkable.class.cast(this.getStack()).getInkColor();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean setInkColor(InkColor inkColor) {
        ItemStack stack = this.getStack();
        Inkable inkable = Inkable.class.cast(stack);

        if (inkColor.equals(inkable.getInkColor())) return false;
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof InkableBlock) {
            inkable.setInkColor(inkColor);

            // force sync because minecraft dumb
            if (!this.world.isClient) {
                for (ServerPlayerEntity player : PlayerLookup.tracking(this)) {
                    player.networkHandler.sendPacket(new EntityTrackerUpdateS2CPacket(this.getId(), this.dataTracker, true));
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public Text getTextForCommand() {
        return this.getDisplayName();
    }

    @Override
    public boolean isInkable() {
        ItemStack stack = this.getStack();
        return stack.getItem() instanceof BlockItem item && item.getBlock() instanceof InkableBlock;
    }

    // change ink color of stack if on inkable and configured
    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (this.world.getGameRules().getBoolean(SplatcraftGameRules.INKWELL_CHANGES_INK_COLOR) && this.isOnGround()) {
            if (this.isInkable()) {
                BlockEntity blockEntity = this.world.getBlockEntity(this.getLandingPos());
                if (blockEntity != null && SplatcraftBlockTags.INK_COLOR_CHANGERS.contains(blockEntity.getCachedState().getBlock())) {
                    if (blockEntity instanceof Inkable inkable) this.setInkColor(inkable.getInkColor());
                }
            }
        }
    }
}
