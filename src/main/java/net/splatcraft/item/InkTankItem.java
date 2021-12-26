package net.splatcraft.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import net.splatcraft.entity.InkEntityAccess;
import net.splatcraft.inkcolor.Inkable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static net.splatcraft.util.SplatcraftConstants.NBT_CONTAINED_INK;
import static net.splatcraft.util.SplatcraftConstants.T_CONTAINED_INK;

public class InkTankItem extends Item implements Wearable {
    private final int capacity;

    public InkTankItem(int capacity, Settings settings) {
        super(settings);
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSER_BEHAVIOR);

        this.capacity = capacity;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public static int getContainedInk(ItemStack stack) {
        if (!(stack.getItem() instanceof InkTankItem item)) return 0;
        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains(NBT_CONTAINED_INK)) return nbt.getInt(NBT_CONTAINED_INK);
        return item.getCapacity();
    }

    public static ItemStack setContainedInk(ItemStack stack, int containedInk) {
        if (!(stack.getItem() instanceof InkTankItem)) return stack;
        stack.getOrCreateNbt().putInt(NBT_CONTAINED_INK, containedInk);
        return stack;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!entity.world.isClient && entity instanceof Inkable inkable) {
            Inkable.class.cast(stack).setInkColor(inkable.getInkColor());

            int containedInk = getContainedInk(stack);
            int capacity = this.getCapacity();

            if (containedInk == capacity) {
                NbtCompound nbt = stack.getNbt();
                if (nbt == null || !nbt.contains(NBT_CONTAINED_INK)) setContainedInk(stack, capacity);
            } else {
                if (!(entity instanceof PlayerEntity player) || player.getEquippedStack(EquipmentSlot.CHEST) == stack) {
                    InkEntityAccess access = ((InkEntityAccess) entity);
                    int nu = containedInk;

                    if (entity.age % 3 == 0) nu++;
                    if (access.isInSquidForm() && access.isOnInk()) nu += 2;

                    nu = Math.min(nu, capacity);
                    if (nu != containedInk) setContainedInk(stack, nu);
                }
            }
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        EquipmentSlot slot = MobEntity.getPreferredEquipmentSlot(stack);

        ItemStack equippedStack = player.getEquippedStack(slot);
        if (equippedStack.isEmpty()) {
            player.equipStack(slot, stack.copy());
            stack.setCount(0);
            if (!world.isClient) player.incrementStat(Stats.USED.getOrCreateStat(this));
            return TypedActionResult.success(stack, world.isClient);
        }

        return super.use(world, player, hand);
    }

    @Nullable
    @Override
    public SoundEvent getEquipSound() {
        return SoundEvents.ITEM_ARMOR_EQUIP_CHAIN;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        String key = super.getTranslationKey(stack);
        return getContainedInk(stack) == 0 ? "%s.empty".formatted(key) : key;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext ctx) {
        super.appendTooltip(stack, world, tooltip, ctx);
        tooltip.add(new TranslatableText(T_CONTAINED_INK, getContainedInk(stack), this.getCapacity()).formatted(Formatting.GRAY));
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (this.isIn(group)) stacks.add(setContainedInk(new ItemStack(this), 0));
    }
}