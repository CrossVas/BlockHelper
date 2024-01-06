package de.thexxturboxx.blockhelper;

import de.thexxturboxx.blockhelper.integration.nei.ModIdentifier;
import net.minecraft.src.KeyBinding;
import net.minecraft.src.ModLoader;
import net.minecraft.src.mod_BlockHelper;
import org.lwjgl.input.Keyboard;

public class BlockHelperClientProxy extends BlockHelperCommonProxy {

    public static double size;
    public static double sizeInv;
    public static int background;
    public static int gradient1;
    public static int gradient2;
    public static boolean fixerNotify;
    public static boolean showItemId;
    public static boolean showHarvest;
    public static boolean showBreakProg;
    public static boolean showMod;
    public static boolean showBlock;
    public static boolean shouldHideFromDebug;
    public static KeyBinding showHide;

    @Override
    public void load(mod_BlockHelper instance) {
        super.load(instance);
        mod_BlockHelper.isClient = true;
        ModLoader.setInGameHook(instance, true, false);
        ModIdentifier.load();
        // Use deprecated config methods here to support older Forge versions
        size = Double.parseDouble(cfg.getOrCreateProperty("General", "Size", "1.0").value);
        background = parseUnsignedInt(cfg.getOrCreateProperty("General", "BackgroundColor", "cc100010").value, 16);
        gradient1 = parseUnsignedInt(cfg.getOrCreateProperty("General", "BorderColor1", "cc5000ff").value, 16);
        gradient2 = parseUnsignedInt(cfg.getOrCreateProperty("General", "BorderColor2", "cc28007f").value, 16);
        fixerNotify = cfg.getOrCreateBooleanProperty("General", "NotifyAboutFixers", true).getBoolean(true);
        showItemId = cfg.getOrCreateBooleanProperty("General", "ShowItemID", true).getBoolean(true);
        showHarvest = cfg.getOrCreateBooleanProperty("General", "ShowHarvestability", true).getBoolean(true);
        showBreakProg = cfg.getOrCreateBooleanProperty("General", "ShowBreakProgression", true).getBoolean(true);
        showMod = cfg.getOrCreateBooleanProperty("General", "ShowMod", true).getBoolean(true);
        showBlock = cfg.getOrCreateBooleanProperty("General", "ShowBlockInHud", true).getBoolean(true);
        shouldHideFromDebug = cfg.getOrCreateBooleanProperty("General", "ShouldHideFromDebug", true).getBoolean(true);
        cfg.save();
        sizeInv = 1 / size;
        showHide = new KeyBinding("blockhelper.key_show_hide", Keyboard.KEY_NUMPAD0);
        ModLoader.registerKey(instance, showHide, false);
    }

}
