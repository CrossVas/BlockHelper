package de.thexxturboxx.blockhelper.integration;

import de.thexxturboxx.blockhelper.BlockHelperCommonProxy;
import de.thexxturboxx.blockhelper.api.BlockHelperBlockState;
import de.thexxturboxx.blockhelper.api.BlockHelperInfoProvider;
import de.thexxturboxx.blockhelper.api.InfoHolder;
import de.thexxturboxx.blockhelper.i18n.I18n;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.src.Block;
import net.minecraft.src.BlockCrops;
import net.minecraft.src.BlockStem;
import net.minecraft.src.ItemStack;
import net.minecraft.src.mod_BlockHelper;

public class VanillaIntegration extends BlockHelperInfoProvider {

    @Override
    public void addInformation(BlockHelperBlockState state, InfoHolder info) {
        if (isCrop(state.block)) {
            double max_stage = getMaxStage(state.block, state.id);
            int grow = (int) ((state.meta / max_stage) * 100);
            String toShow;
            if (grow >= 100) {
                toShow = I18n.format("mature");
            } else {
                toShow = grow + "%";
            }
            info.add(I18n.format("growth_state_format", toShow));
        }

        if (state.id == Block.redstoneWire.blockID) {
            info.add(I18n.format("strength_format", state.meta));
        }

        if (state.id == Block.lever.blockID) {
            String leverState = I18n.format(state.meta >= 8 ? "on" : "off");
            info.add(I18n.format("state_format", leverState));
        }

        if (state.id == Block.redstoneRepeaterIdle.blockID || state.id == Block.redstoneRepeaterActive.blockID) {
            info.add(I18n.format("delay", ((state.meta & 0xc) >> 2) + 1));
        }
    }

    @Override
    public String getName(BlockHelperBlockState state) {
        if (state.block instanceof BlockStem) {
            Block drop = getDeclaredField(BlockStem.class, state.block, "a");
            return drop.translateBlockName();
        }

        if (state.id == Block.pistonExtension.blockID) {
            return I18n.format("piston_head");
        }

        if (state.id == Block.pistonMoving.blockID) {
            return I18n.format("moving_piston");
        }

        if (state.id == Block.redstoneRepeaterIdle.blockID || state.id == Block.redstoneRepeaterActive.blockID) {
            return I18n.format("redstone_repeater");
        }

        if (state.id == Block.stairSingle.blockID) {
            return mod_BlockHelper.getItemDisplayName(new ItemStack(state.id, 1, state.meta & ~0x8));
        }

        if (state.id == Block.stairDouble.blockID) {
            return mod_BlockHelper.getItemDisplayName(new ItemStack(Block.stairSingle, 1, state.meta));
        }

        if (state.id == Block.field_35289_bm.blockID) {
            switch (state.meta) {
            case 0:
                return I18n.format("stone_monster_egg");
            case 1:
                return I18n.format("cobblestone_monster_egg");
            case 2:
                return I18n.format("stone_brick_monster_egg");
            case 3:
                return I18n.format("mossy_stone_brick_monster_egg");
            case 4:
                return I18n.format("cracked_stone_brick_monster_egg");
            case 5:
                return I18n.format("chiseled_stone_brick_monster_egg");
            }
        }

        return super.getName(state);
    }

    @Override
    public boolean isEnabled() {
        return BlockHelperCommonProxy.vanillaIntegration;
    }

    private double getMaxStage(Block b, int id) {
        try {
            if (b instanceof BlockCrops) {
                return 7;
            } else if (b instanceof BlockStem) {
                return 7;
            } else {
                for (Field field : b.getClass().getFields()) {
                    if (containsIgnoreCase(field.getName(), "max")
                            && containsIgnoreCase(field.getName(), "stage")) {
                        field.setAccessible(true);
                        return field.getInt(Block.blocksList[id]);
                    }
                }
                for (Field field : b.getClass().getDeclaredFields()) {
                    if (containsIgnoreCase(field.getName(), "max")
                            && containsIgnoreCase(field.getName(), "stage")) {
                        field.setAccessible(true);
                        return field.getInt(Block.blocksList[id]);
                    }
                }
            }
        } catch (Throwable ignored) {
        }
        return 7;
    }

    private boolean isCrop(Block b) {
        boolean crop = b instanceof BlockCrops
                || b instanceof BlockStem;
        if (!crop) {
            try {
                for (Method method : b.getClass().getDeclaredMethods()) {
                    String name = method.getName();
                    if (name.equals("getGrowthRate")) {
                        return true;
                    }
                    if (name.equals("getGrowthModifier")) {
                        return true;
                    }
                }
            } catch (Throwable ignored) {
            }
        }
        return crop;
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null)
            return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }

}
