package net.splatcraft.component;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.splatcraft.block.InkPassableBlock;
import net.splatcraft.inkcolor.InkColor;
import net.splatcraft.inkcolor.InkColors;

import static net.splatcraft.util.SplatcraftConstants.*;

public class PlayerDataComponent implements Component, AutoSyncedComponent {
    private final PlayerEntity player;

    /**
     * Defines a player's ink color.
     */
    private InkColor inkColor = InkColors.DYE_WHITE;

    /**
     * Defines whether the player is in squid form.
     */
    private boolean squid = false;

    /**
     * Defines whether the player is submerged in ink.
     */
    private boolean submerged = false;

    @SuppressWarnings("unused")
    public PlayerDataComponent(PlayerEntity player) {
        this.player = player;
    }

    public static PlayerDataComponent get(PlayerEntity player) {
        return SplatcraftComponents.PLAYER_DATA.get(player);
    }

    @Override
    public boolean shouldSyncWith(ServerPlayerEntity player) {
        return true;
    }

    public void sync() {
        SplatcraftComponents.PLAYER_DATA.sync(this.player);
    }

    public InkColor getInkColor() {
        return this.inkColor;
    }

    public boolean setInkColor(InkColor inkColor) {
        if (this.inkColor.equals(inkColor)) return false;
        this.inkColor = inkColor;
        this.sync();
        return true;
    }

    public boolean isSquid() {
        return this.squid;
    }

    public boolean setSquid(boolean squid) {
        if (this.squid == squid) return false;
        this.squid = squid;

        this.player.calculateDimensions();

        if (squid) {
            this.player.setSprinting(false);
        } else {
            // teleport up if inside block
            BlockPos pos = this.player.getBlockPos();
            BlockState state = this.player.world.getBlockState(pos);
            if (state.getBlock() instanceof InkPassableBlock) {
                VoxelShape shape = state.getCollisionShape(this.player.world, pos);
                double maxY = shape.getMax(Direction.Axis.Y);
                if (maxY < 1.0d) {
                    double y = pos.getY() + maxY;
                    if (y > this.player.getY()) this.player.setPosition(this.player.getX(), y, this.player.getZ());
                }
            }
        }

        this.sync();
        return true;
    }

    public boolean isSubmerged() {
        return this.submerged;
    }

    public boolean setSubmerged(boolean submerged) {
        if (this.submerged == submerged) return false;
        this.submerged = submerged;

        this.player.calculateDimensions();

        this.sync();
        return true;
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        if (this.inkColor == null) this.inkColor = InkColors.DYE_WHITE;
        tag.putString(NBT_INK_COLOR, this.inkColor.toString());

        tag.putBoolean(NBT_IS_SQUID, this.squid);
        tag.putBoolean(NBT_IS_SUBMERGED, this.submerged);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.setInkColor(InkColor.fromString(tag.getString(NBT_INK_COLOR)));
        this.setSquid(tag.getBoolean(NBT_IS_SQUID));
        this.setSubmerged(tag.getBoolean(NBT_IS_SUBMERGED));
    }
}
