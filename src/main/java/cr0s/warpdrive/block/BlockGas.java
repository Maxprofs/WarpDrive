package cr0s.warpdrive.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.conf.WarpDriveConfig;

public class BlockGas extends Block {
	private IIcon[] iconBuffer;

	public BlockGas() {
		super(Material.fire);
		setHardness(0.0F);
		setCreativeTab(WarpDrive.warpdriveTab);
		setBlockName("warpdrive.blocks.Gas");
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean isAir(IBlockAccess var1, int var2, int var3, int var4) {
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World var1, int var2, int var3, int var4) {
		return null;
	}

	@Override
	public boolean isReplaceable(IBlockAccess var1, int var2, int var3, int var4) {
		return true;
	}

	@Override
	public boolean canPlaceBlockAt(World var1, int var2, int var3, int var4) {
		return true;
	}

	@Override
	public boolean canCollideCheck(int var1, boolean var2) {
		return false;
	}

	@Override
	public int getRenderBlockPass() {
		return 1; // transparency enabled
	}

	@Override
	public void registerBlockIcons(IIconRegister par1IconRegister) {
		iconBuffer = new IIcon[12];
		iconBuffer[0] = par1IconRegister.registerIcon("warpdrive:gasBlockBlue");
		iconBuffer[1] = par1IconRegister.registerIcon("warpdrive:gasBlockRed");
		iconBuffer[2] = par1IconRegister.registerIcon("warpdrive:gasBlockGreen");
		iconBuffer[3] = par1IconRegister.registerIcon("warpdrive:gasBlockYellow");
		iconBuffer[4] = par1IconRegister.registerIcon("warpdrive:gasBlockDark");
		iconBuffer[5] = par1IconRegister.registerIcon("warpdrive:gasBlockDarkness");
		iconBuffer[6] = par1IconRegister.registerIcon("warpdrive:gasBlockWhite");
		iconBuffer[7] = par1IconRegister.registerIcon("warpdrive:gasBlockMilk");
		iconBuffer[8] = par1IconRegister.registerIcon("warpdrive:gasBlockOrange");
		iconBuffer[9] = par1IconRegister.registerIcon("warpdrive:gasBlockSyren");
		iconBuffer[10] = par1IconRegister.registerIcon("warpdrive:gasBlockGray");
		iconBuffer[11] = par1IconRegister.registerIcon("warpdrive:gasBlockViolet");
	}

	@Override
	public IIcon getIcon(int side, int metadata) {
		return iconBuffer[metadata % iconBuffer.length];
	}

	@Override
	public int getMobilityFlag() {
		return 1;
	}

	@Override
	public Item getItemDropped(int var1, Random var2, int var3) {
		return null;
	}

	@Override
	public int quantityDropped(Random par1Random) {
		return 0;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		Block sideBlock = world.getBlock(x, y, z);
		if (sideBlock.isAssociatedBlock(this)) {
			return false;
		}
		return world.isAirBlock(x, y, z);
	}

	@Override
	public boolean isCollidable() {
		return false;
	}

	@Override
	public void onBlockAdded(World par1World, int par2, int par3, int par4) {
		// Gas blocks allow only in space
		if (par1World.provider.dimensionId != WarpDriveConfig.G_SPACE_DIMENSION_ID) {
			par1World.setBlockToAir(par2, par3, par4);
		}
	}
}