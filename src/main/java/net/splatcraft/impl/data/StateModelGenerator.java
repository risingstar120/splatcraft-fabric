package net.splatcraft.impl.data;

import net.minecraft.block.Block;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.util.Identifier;
import net.moddingplayground.frame.api.toymaker.v0.generator.model.InheritingModelGen;
import net.moddingplayground.frame.api.toymaker.v0.generator.model.StateGen;
import net.moddingplayground.frame.api.toymaker.v0.generator.model.StateModelInfo;
import net.moddingplayground.frame.api.toymaker.v0.generator.model.block.AbstractStateModelGenerator;
import net.moddingplayground.frame.api.toymaker.v0.generator.model.block.VariantsStateGen;
import net.splatcraft.api.Splatcraft;

import static net.splatcraft.api.block.SplatcraftBlocks.*;

public class StateModelGenerator extends AbstractStateModelGenerator {
    private static final BlockHalf[] BLOCK_HALVES = BlockHalf.values();

    public StateModelGenerator() {
        super(Splatcraft.MOD_ID);
    }

    @Override
    public void generate() {
        this.add(CANVAS, b -> this.simple(name(b), cubeAllTinted(name(b))));

        this.add(INKED_BLOCK, b -> this.simple(name(b), cubeAllTinted(name(b))));
        this.add(GLOWING_INKED_BLOCK, b -> this.simple(name(b), cubeAllTinted(name(b))));

        this.add(GRATE_BLOCK);
        this.add(GRATE, this::grate);
        this.add(GRATE_RAMP, b -> this.facingHorizontalPredefined(name(b), 90));

        this.add(STAGE_BARRIER);
        this.add(STAGE_VOID);

        this.add(EMPTY_INKWELL, this::predefined);
        this.add(INKWELL, this::predefined);
    }

    public StateGen grate(Block block) {
        VariantsStateGen gen = VariantsStateGen.variants();

        for (BlockHalf half : BLOCK_HALVES) {
            String halfId = half.asString();
            String variant = "half=" + halfId;
            gen.variant(variant, StateModelInfo.create(name(block, "block/%s_" + halfId), sidedTrapdoor(block, half)));
        }

        return gen;
    }

    public StateGen facingHorizontalPredefined(Identifier name, int offset) {
        return VariantsStateGen.variants("facing=north", StateModelInfo.create(name).rotate(0, offset))
                               .variant("facing=east", StateModelInfo.create(name).rotate(0, 90 + offset))
                               .variant("facing=south", StateModelInfo.create(name).rotate(0, 180 + offset))
                               .variant("facing=west", StateModelInfo.create(name).rotate(0, 270 + offset));
    }

    public InheritingModelGen sidedTrapdoor(Block block, BlockHalf half) {
        Identifier texture = name(block);
        Identifier side = name(block, "block/%s_side");
        return new InheritingModelGen(new Identifier(Splatcraft.MOD_ID, "block/template_sided_trapdoor_" + half.asString()))
            .texture("texture", texture)
            .texture("side", side);
    }

    public static InheritingModelGen cubeAllTinted(Identifier texture) {
        return new InheritingModelGen(new Identifier(Splatcraft.MOD_ID, "block/cube_all_tinted"))
            .texture("all", texture);
    }
}