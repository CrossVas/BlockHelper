package de.thexxturboxx.blockhelper;

import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import de.thexxturboxx.blockhelper.api.BlockHelperInfoProvider;
import de.thexxturboxx.blockhelper.api.BlockHelperModSupport;
import factorization.common.TileEntityCommon;
import ic2.core.Ic2Items;
import inficraft.microblocks.core.api.multipart.ICoverSystem;
import inficraft.microblocks.core.api.multipart.IMultipartTile;
import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.IMob;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.src.BaseMod;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import org.lwjgl.opengl.GL11;

import static de.thexxturboxx.blockhelper.BlockHelperClientProxy.size;
import static de.thexxturboxx.blockhelper.BlockHelperClientProxy.sizeInv;

public class mod_BlockHelper extends BaseMod implements IPacketHandler {

    private static final String PACKAGE = "de.thexxturboxx.blockhelper.";
    private static final String MOD_ID = "BlockHelper";
    static final String NAME = "Block Helper";
    static final String VERSION = "0.9";
    static final String CHANNEL = "BlockHelperInfo";

    public static final MopType[] MOP_TYPES = MopType.values();

    public static boolean isClient;

    private boolean isHidden = false;

    @SidedProxy(clientSide = PACKAGE + "BlockHelperClientProxy", serverSide = PACKAGE + "BlockHelperCommonProxy")
    public static BlockHelperCommonProxy proxy;

    public static String getModId() {
        return MOD_ID;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getVersion() {
        return VERSION;
    }

    @Override
    public void load() {
        proxy.load(this);
    }

    @Override
    public boolean onTickInGame(float time, Minecraft mc) {
        try {
            GL11.glScaled(size, size, size);
            BlockHelperUpdater.notifyUpdater(mc);
            if (mc.theWorld.isRemote) {
                updateKeyState();
                if (mc.currentScreen != null || isHidden)
                    return true;
                int i = isLookingAtBlock(mc);
                if (i == 0)
                    return true;
                MovingObjectPosition mop = mc.objectMouseOver;
                ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                DataOutputStream os = new DataOutputStream(buffer);
                try {
                    if (MOP_TYPES[i] == MopType.ENTITY) {
                        PacketCoder.encode(os, new PacketInfo(mc.theWorld.provider.dimensionId, mop, MOP_TYPES[i],
                                mop.entityHit.entityId));
                    } else {
                        PacketCoder.encode(os,
                                new PacketInfo(mc.theWorld.provider.dimensionId, mop, MOP_TYPES[i]));
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                byte[] fieldData = buffer.toByteArray();
                Packet250CustomPayload packet = new Packet250CustomPayload();
                packet.channel = CHANNEL;
                packet.data = fieldData;
                packet.length = fieldData.length;
                PacketDispatcher.sendPacketToServer(packet);
                switch (i) {
                case 1:
                    int meta = mc.theWorld.getBlockMetadata(mop.blockX, mop.blockY, mop.blockZ);
                    int id = mc.theWorld.getBlockId(mop.blockX, mop.blockY, mop.blockZ);
                    ItemStack is = new ItemStack(Block.blocksList[id], 1, meta);
                    TileEntity te = mc.theWorld.getBlockTileEntity(mop.blockX, mop.blockY, mop.blockZ);
                    String itemId = is.itemID + ":" + is.getItemDamage();
                    String ct = null;
                    if (te != null) {
                        if (iof(te, "thermalexpansion.transport.tileentity.TileConduitLiquid")) {
                            is.setItemDamage(4096);
                        } else if (iof(te, "ic2.core.block.wiring.TileEntityCable")) {
                            is = new ItemStack(Item.itemsList[Ic2Items.copperCableItem.itemID], 1, meta);
                        } else if (iof(te, "factorization.common.TileEntityCommon")) {
                            ct = "Factorization";
                            is.setItemDamage(((TileEntityCommon) te).getFactoryType().md);
                        } else if (iof(te, "codechicken.chunkloader.TileChunkLoaderBase")) {
                            ct = "ChickenChunks";
                        } else if (iof(te, "buildcraft.transport.TileGenericPipe")) {
                            TileGenericPipe pipe = (TileGenericPipe) te;
                            ct = "BuildCraft";
                            if (pipe.pipe != null && pipe.initialized) {
                                is = new ItemStack(Item.itemsList[pipe.pipe.itemID], te.blockMetadata);
                            }
                        } else if (iof(te, "inficraft.microblocks.core.api.multipart.IMultipartTile")) {
                            IMultipartTile te1 = (IMultipartTile) te;
                            if (mop.subHit >= 0) {
                                is = te1.pickPart(mop, mop.subHit);
                            } else {
                                ICoverSystem ci = te1.getCoverSystem();
                                is = ci == null ? is : ci.pickPart(mop, -1 - mop.subHit);
                            }
                            ct = "InfiMicroblocks";
                        }
                    }
                    Block b = Block.blocksList[id];
                    if (b != null) {
                        if (iof(b, "net.meteor.common.BlockMeteorShieldTorch")) {
                            is = new ItemStack(b.idDropped(0, null, 0), 1, meta);
                        }
                    }
                    if (is.getItem() == null)
                        return true;
                    if (ct == null) {
                        if (is.getItem().getCreativeTab() != null) {
                            if (is.getItem().getCreativeTab().getTabIndex() < 12) {
                                ct = "Minecraft";
                            } else {
                                ct = is.getItem().getCreativeTab().getTranslatedTabLabel();
                            }
                        } else {
                            ct = "Unknown";
                        }
                    }

                    infos.clear();
                    String name = BlockHelperModSupport.getName(b, te, id, meta);
                    name = name == null ? "" : name;
                    if (name.isEmpty()) {
                        try {
                            name = is.getDisplayName();
                            if (name.isEmpty())
                                throw new IllegalArgumentException();
                        } catch (Throwable e) {
                            try {
                                name = new ItemStack(b).getDisplayName();
                                if (name.isEmpty())
                                    throw new IllegalArgumentException();
                            } catch (Throwable e1) {
                                try {
                                    if (b != null) {
                                        name = new ItemStack(Item.itemsList[b.idDropped(meta, new Random(), 0)], 1,
                                                b.damageDropped(meta)).getDisplayName();
                                    }
                                    if (name.isEmpty())
                                        throw new IllegalArgumentException();
                                } catch (Throwable e2) {
                                    try {
                                        if (b != null) {
                                            ItemStack s = b.getPickBlock(mop, mc.theWorld,
                                                    mop.blockX, mop.blockY, mop.blockZ);
                                            name = s.getItem().getItemDisplayName(s);
                                        }
                                        if (name.isEmpty())
                                            throw new IllegalArgumentException();
                                    } catch (Throwable e3) {
                                        if (b != null) {
                                            name = b.translateBlockName();
                                        } else {
                                            name = "Please report this!";
                                        }
                                    }
                                }
                            }
                        }
                    }

                    String harvest = "Please report this!";
                    boolean harvestable = false;
                    if (b != null) {
                        float hardness = b.getBlockHardness(proxy.getWorld(), mop.blockX, mop.blockY, mop.blockZ);
                        if (hardness == -1.0F || hardness == -1.0D || hardness == -1) {
                            harvest = "Unbreakable";
                        } else if (b.canHarvestBlock(proxy.getPlayer(), meta)) {
                            harvestable = true;
                            harvest = "Currently harvestable";
                        } else {
                            harvest = "Currently not harvestable";
                        }
                    }
                    addInfo(name);
                    addInfo(itemId);
                    addInfo("§o" + ct.replaceAll("§.", ""), 0x000000ff);
                    addAdditionalInfo(packetInfos);
                    addInfo((harvestable ? "§a✔" : "§4✘") + " §r" + harvest);
                    int x = drawBox(mc);
                    drawInfo(x, mc);
                    break;
                case 2:
                    Entity e = mop.entityHit;
                    infos.clear();
                    String nameEntity = e.getEntityName();
                    if (e instanceof IMob) {
                        nameEntity = "§4" + nameEntity;
                    }
                    addInfo(nameEntity);
                    addAdditionalInfo(packetInfos);
                    x = drawBox(mc);
                    drawInfo(x, mc);
                    break;
                default:
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            GL11.glScaled(sizeInv, sizeInv, sizeInv);
        }
        return true;
    }

    private void updateKeyState() {
        if (BlockHelperClientProxy.showHide.isPressed()) {
            isHidden = !isHidden;
        }
    }

    private int getStringMid(int x, String s, Minecraft mc) {
        return x - mc.fontRenderer.getStringWidth(s) / 2;
    }

    private int isLookingAtBlock(Minecraft mc) {
        MovingObjectPosition mop = mc.objectMouseOver;
        if (mop == null)
            return 0;
        switch (mop.typeOfHit) {
        case ENTITY:
            return 2;
        case TILE:
            Material b = mc.theWorld.getBlockMaterial(mop.blockX, mop.blockY, mop.blockZ);
            if (b != null)
                return 1;
            else
                return 0;
        default:
            return 0;
        }
    }

    private static class FormatString {
        private final String str;
        private final int color;

        FormatString(String str, int color) {
            this.str = str;
            this.color = color;
        }
    }

    private static List<String> packetInfos = new ArrayList<String>();
    private static final List<FormatString> infos = new ArrayList<FormatString>();

    private void addAdditionalInfo(List<String> info) {
        for (String s : info) {
            addInfo("§7" + s);
        }
    }

    private void addInfo(String info) {
        addInfo(info, 0xffffffff);
    }

    private void addInfo(String info, int color) {
        if (info != null && !info.equals("")) {
            infos.add(new FormatString(info, color));
        }
    }

    private static final int PADDING = 12;
    private static final int dark = new Color(17, 2, 16).getRGB();
    private static final int light = new Color(52, 18, 102).getRGB();

    private void drawInfo(int x, Minecraft mc) {
        int currLine = PADDING;
        for (FormatString s : infos) {
            mc.fontRenderer.drawString(s.str, getStringMid(x, s.str, mc), currLine, s.color);
            currLine += mc.fontRenderer.FONT_HEIGHT;
        }
    }

    private int drawBox(Minecraft mc) {
        ScaledResolution res = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int width = (int) (res.getScaledWidth() * sizeInv);
        if (BlockHelperClientProxy.mode != 1) {
            int infoWidth = 0;
            int currLine = PADDING;
            for (FormatString s : infos) {
                infoWidth = Math.max(mc.fontRenderer.getStringWidth(s.str) + PADDING, infoWidth);
                currLine += mc.fontRenderer.FONT_HEIGHT;
            }
            int minusHalf = (width - infoWidth) / 2;
            int plusHalf = (width + infoWidth) / 2;
            Gui.drawRect(minusHalf + 2, 7, plusHalf - 2, currLine + 5, dark);
            Gui.drawRect(minusHalf + 1, 8, plusHalf - 1, currLine + 4, dark);
            Gui.drawRect(minusHalf + 2, 8, plusHalf - 2, currLine + 4, light);
            Gui.drawRect(minusHalf + 3, 9, plusHalf - 3, currLine + 3, dark);
        }
        return width / 2;
    }

    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packetGot, Player player) {
        try {
            if (packetGot.channel.equals(CHANNEL)) {
                ByteArrayInputStream isRaw = new ByteArrayInputStream(packetGot.data);
                DataInputStream is = new DataInputStream(isRaw);
                if (isClient && FMLCommonHandler.instance().getEffectiveSide().isClient()) {
                    try {
                        packetInfos = ((PacketClient) PacketCoder.decode(is)).data;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (FMLCommonHandler.instance().getEffectiveSide().isServer()) {
                    PacketInfo pi = null;
                    try {
                        pi = (PacketInfo) PacketCoder.decode(is);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (pi == null || pi.mop == null)
                        return;
                    World w = DimensionManager.getProvider(pi.dimId).worldObj;
                    if (pi.mt == MopType.ENTITY) {
                        Entity en = w.getEntityByID(pi.entityId);
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        DataOutputStream os = new DataOutputStream(buffer);
                        PacketClient pc = new PacketClient();
                        if (en != null) {
                            try {
                                pc.add(((EntityLiving) en).getHealth() + " ❤ / "
                                        + ((EntityLiving) en).getMaxHealth() + " ❤");
                                PacketCoder.encode(os, pc);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (Throwable e) {
                                try {
                                    PacketCoder.encode(os, pc);
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        } else {
                            try {
                                PacketCoder.encode(os, pc);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        byte[] fieldData = buffer.toByteArray();
                        Packet250CustomPayload packet = new Packet250CustomPayload();
                        packet.channel = CHANNEL;
                        packet.data = fieldData;
                        packet.length = fieldData.length;
                        PacketDispatcher.sendPacketToPlayer(packet, player);
                    } else if (pi.mt == MopType.BLOCK) {
                        TileEntity te = w.getBlockTileEntity(pi.mop.blockX, pi.mop.blockY, pi.mop.blockZ);
                        int id = w.getBlockId(pi.mop.blockX, pi.mop.blockY, pi.mop.blockZ);
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                        DataOutputStream os = new DataOutputStream(buffer);
                        PacketClient info = new PacketClient();
                        if (id > 0) {
                            int meta = w.getBlockMetadata(pi.mop.blockX, pi.mop.blockY, pi.mop.blockZ);
                            Block b = Block.blocksList[id];
                            BlockHelperModSupport.addInfo(info, b, id, meta, te);
                        }
                        try {
                            PacketCoder.encode(os, info);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        byte[] fieldData = buffer.toByteArray();
                        Packet250CustomPayload packet = new Packet250CustomPayload();
                        packet.channel = CHANNEL;
                        packet.data = fieldData;
                        packet.length = fieldData.length;
                        PacketDispatcher.sendPacketToPlayer(packet, player);
                    }
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    static boolean iof(Object obj, String clazz) {
        return BlockHelperInfoProvider.isLoadedAndInstanceOf(obj, clazz);
    }

}

