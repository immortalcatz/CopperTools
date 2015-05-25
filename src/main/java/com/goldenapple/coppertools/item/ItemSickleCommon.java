package com.goldenapple.coppertools.item;

import com.goldenapple.coppertools.CopperToolsCreativeTab;
import com.goldenapple.coppertools.util.OreHelper;
import com.goldenapple.coppertools.reference.Reference;
import com.google.common.collect.Sets;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.world.World;

import java.util.Set;

//Contains a lot of "stolen" code from CoFHCore https://github.com/CoFH/CoFHCore

public class ItemSickleCommon extends ItemTool {
    public static final Set<Material> effectiveMaterials = Sets.newHashSet(Material.leaves, Material.plants, Material.vine, Material.web);
    private static final Set<String> toolClasses = Sets.newHashSet("sickle");

    private String repairOre;
    private ItemStack repairItem;
    private boolean useObsidian; //used for textures

    public ItemSickleCommon(ToolMaterial material, String name, String matRepair, boolean useObsidian){
        super(2.0F, material, effectiveMaterials);
        repairOre = matRepair;
        if(CopperToolsCreativeTab.tabTools != null){
            setCreativeTab(CopperToolsCreativeTab.tabTools);
        }else{
            setCreativeTab(CreativeTabs.tabTools);
        }
        setUnlocalizedName(name);
        this.useObsidian = useObsidian;
    }

    public ItemSickleCommon(Item.ToolMaterial material, String name, ItemStack matRepair, boolean useObsidian){
        super(2.0F, material, effectiveMaterials);
        repairItem = matRepair;
        if(CopperToolsCreativeTab.tabTools != null){
            setCreativeTab(CopperToolsCreativeTab.tabTools);
        }else{
            setCreativeTab(CreativeTabs.tabTools);
        }
        setUnlocalizedName(name);
        this.useObsidian = useObsidian;
    }

    @Override
    public boolean canHarvestBlock(Block block, ItemStack stack) {

        return block == Blocks.web || block == Blocks.vine;
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {

        return toolClasses;
    }

    @Override
    public float func_150893_a(ItemStack stack, Block block) //Returns efficiency of mining given block
    {
        return effectiveMaterials.contains(block.getMaterial()) ? efficiencyOnProperMaterial : 1.0F;
    }

    @Override
    public boolean isItemTool(ItemStack stack){
        return true;
    }

    public void harvestBlock(World world, int x, int y, int z, EntityPlayer player){
        Block block = world.getBlock(x, y, z);
        if (block.getBlockHardness(world, x, y, z) < 0 || block.equals(Blocks.waterlily)) {
            return;
        }
        int bMeta = world.getBlockMetadata(x, y, z);

        if (block.canHarvestBlock(player, bMeta)) {
            block.harvestBlock(world, player, x, y, z, bMeta);
        }
        if (!world.isRemote && block.equals(Blocks.vine)) {
            block.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 1);
        }
        world.setBlockToAir(x, y, z);
    }

    public boolean harvest(World world, Block block, int x, int y, int z, EntityPlayer player){ //i'm not doing all of this in the onBlockDestroyed because of other sickles that handle damage differently
        boolean used = false;

        if(block.getMaterial().equals(Material.leaves)) { //Harvesting leaves in a 3x3x3 area
            for (int a = x - 1; a <= x + 1; a++) {
                for (int b = y - 1; b <= y + 1; b++){
                    for (int c = z - 1; c <= z + 1; c++) {
                        if (effectiveMaterials.contains(world.getBlock(a, b, c).getMaterial())) {
                            harvestBlock(world, a, b, c, player);
                            used = true;
                        }
                    }
                }
            }
        }else{ //Harvesting grass in a 3x1x3 area
            for (int a = x - 1; a <= x + 1; a++) {
                for (int c = z - 1; c <= z + 1; c++) {
                    if (effectiveMaterials.contains(world.getBlock(a, y, c).getMaterial())) {
                        harvestBlock(world, a, y, c, player);
                        used = true;
                    }
                }
            }
        }
        return used;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack itemStack, World world, Block block, int x, int y, int z, EntityLivingBase entity) {
        /* EntityPlayer player = (EntityPlayer) entity;
        boolean used = false;

        if(!block.getMaterial().equals(Material.leaves)) { //Harvesting plants in a 3x1x3 area
            for (int i = x - 1; i <= x + 1; i++) {
                for (int k = z - 1; k <= z + 1; k++) {
                    if (effectiveMaterials.contains(world.getBlock(i, y, k).getMaterial())) {
                        harvestBlock(world, i, y, k, player);
                        used = true;
                    }
                }
            }
        }else{ //Harvesting leaves in a 3x3x3 area
            for (int a = x - 1; a <= x + 1; a++) {
                for (int b = y - 1; b <= y + 1; b++){
                    for (int c = z - 1; c <= z + 1; c++) {
                        if (effectiveMaterials.contains(world.getBlock(a, b, c).getMaterial())) {
                            harvestBlock(world, a, b, c, player);
                            used = true;
                        }
                    }
                }
            }
        }

        if (used) {
            if(!player.capabilities.isCreativeMode) {
                stack.damageItem(1, entity);
            }
        }
        return used; */
        if(entity instanceof EntityPlayer) {
            if (harvest(world, block, x, y, z, (EntityPlayer) entity)) {
                if (!((EntityPlayer) entity).capabilities.isCreativeMode) {
                    itemStack.damageItem(1, entity);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean getIsRepairable(ItemStack tool, ItemStack item){
        if (repairOre != null){
            return OreHelper.isItemThisOre(item, repairOre);
        }else{
            return item.isItemEqual(repairItem);
        }
    }

    //The code below is taken from Pahimar's Let's Mod Reboot mod. https://github.com/pahimar/LetsModReboot

    @Override
    public String getUnlocalizedName()
    {
        return String.format("item.%s%s", Reference.MOD_ID.toLowerCase() + ":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        return String.format("item.%s%s", Reference.MOD_ID.toLowerCase() + ":", getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister){
        itemIcon = useObsidian ? iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1) + "_o") : iconRegister.registerIcon(this.getUnlocalizedName().substring(this.getUnlocalizedName().indexOf(".") + 1));
    }

    protected String getUnwrappedUnlocalizedName(String unlocalizedName) {
        return unlocalizedName.substring(unlocalizedName.indexOf(".") + 1);
    }
}