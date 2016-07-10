package cr0s.warpdrive.block.forcefield;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.api.IDamageReceiver;
import cr0s.warpdrive.block.hull.BlockHullGlass;
import cr0s.warpdrive.config.WarpDriveConfig;
import cr0s.warpdrive.data.ForceFieldSetup;
import cr0s.warpdrive.data.EnumPermissionNode;
import cr0s.warpdrive.data.Vector3;
import cr0s.warpdrive.render.RenderBlockForceField;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;
import java.util.Random;

public class BlockForceField extends BlockAbstractForceField implements IDamageReceiver {
	@SideOnly(Side.CLIENT)
	private IIcon[] icons;
	private static final float BOUNDING_TOLERANCE = 0.05F;
	
	public BlockForceField(final byte tier) {
		super(tier, Material.glass);
		setStepSound(Block.soundTypeCloth);
		setBlockName("warpdrive.forcefield.block" + tier);
		setBlockTextureName("warpdrive:forcefield/forcefield");
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {
		return new TileEntityForceField();
	}
	
	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer entityPlayer) {
		return null;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return icons[metadata % 16];
	}
	
	@Override
	public int quantityDropped(Random random) {
		return 0;
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs creativeTab, List list) {
		/* Hide in NEI
		for (int i = 0; i < 16; ++i) {
			list.add(new ItemStack(item, 1, i));
		}
		/**/
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		icons = new IIcon[16];
		
		for (int i = 0; i < 16; ++i) {
			icons[i] = iconRegister.registerIcon(getTextureName() + "_" + i);
		}
	}
	
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 1;
	}
	
	@SideOnly(Side.CLIENT)
	public int getRenderType() {
		return RenderBlockForceField.renderId;
	}
	
	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side) {
		if (world.isAirBlock(x, y, z)) {
			return true;
		}
		ForgeDirection direction = ForgeDirection.getOrientation(side).getOpposite();
		Block sideBlock = world.getBlock(x, y, z);
		if (sideBlock instanceof BlockGlass || sideBlock instanceof BlockHullGlass || sideBlock instanceof BlockForceField) {
			return world.getBlockMetadata(x, y, z)
				!= world.getBlockMetadata(x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ);
		}
		return !world.isSideSolid(x, y, z, direction, false);
	}
	
	protected TileEntityForceFieldProjector getProjector(World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityForceField) {
			return ((TileEntityForceField) tileEntity).getProjector();
		}
		return null;
	}
	
	private ForceFieldSetup getForceFieldSetup(World world, int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityForceField) {
			return ((TileEntityForceField) tileEntity).getForceFieldSetup();
		}
		return null;
	}
	
	public void onBlockClicked(World world, int x, int y, int z, EntityPlayer entityPlayer) {
		ForceFieldSetup forceFieldSetup = getForceFieldSetup(world, x, y, z);
		if (forceFieldSetup != null) {
			forceFieldSetup.onEntityEffect(world, x, y, z, entityPlayer);
		}
	}
	
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		ForceFieldSetup forceFieldSetup = getForceFieldSetup(world, x, y, z);
		if (forceFieldSetup != null) {
			List<EntityPlayer> entities = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.9D, z + 1));
			
			for (EntityPlayer entityPlayer : entities) {
				if (entityPlayer != null && entityPlayer.isSneaking()) {
					if ( entityPlayer.capabilities.isCreativeMode 
					  || forceFieldSetup.isAccessGranted(entityPlayer, EnumPermissionNode.SNEAK_THROUGH)) {
							return null;
					}
				}
			}
		}
		
		return AxisAlignedBB.getBoundingBox(
			x + BOUNDING_TOLERANCE, y + BOUNDING_TOLERANCE, z + BOUNDING_TOLERANCE,
			x + 1 - BOUNDING_TOLERANCE, y + 1 - BOUNDING_TOLERANCE, z + 1 - BOUNDING_TOLERANCE);
	}
	
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if (world.isRemote) {
			return;
		}
		
		ForceFieldSetup forceFieldSetup = getForceFieldSetup(world, x, y, z);
		if (forceFieldSetup != null) {
			forceFieldSetup.onEntityEffect(world, x, y, z, entity);
			double distance2 = new Vector3(x, y, z).translate(0.5F).distanceTo_square(entity);
			if (entity instanceof EntityLiving && distance2 < 0.26D) {
				boolean hasPermission = false;
				
				List<EntityPlayer> entities = world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.9D, z + 1));
				for (EntityPlayer entityPlayer : entities) {
					if (entityPlayer != null && entityPlayer.isSneaking()) {
						if ( entityPlayer.capabilities.isCreativeMode
						  || forceFieldSetup.isAccessGranted(entityPlayer, EnumPermissionNode.SNEAK_THROUGH) ) {
							hasPermission = true;
							break;
						}
					}
				}
				
				// always slowdown
				((EntityLiving) entity).addPotionEffect(new PotionEffect(Potion.moveSlowdown.id, 20, 1));
				
				if (!hasPermission) {
					((EntityLiving) entity).addPotionEffect(new PotionEffect(Potion.confusion.id, 80, 3));
					if (distance2 < 0.24D) {
						entity.attackEntityFrom(WarpDrive.damageShock, 5);
					}
				}
			}
		}
	}
	
	@Override
	public int colorMultiplier(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityForceField && ((TileEntityForceField)tileEntity).cache_blockCamouflage != null) {
			return ((TileEntityForceField)tileEntity).cache_colorMultiplierCamouflage;
		}
		
		return super.colorMultiplier(blockAccess, x, y, z);
	}
	
	@Override
	public int getLightValue(IBlockAccess blockAccess, int x, int y, int z) {
		TileEntity tileEntity = blockAccess.getTileEntity(x, y, z);
		if (tileEntity instanceof TileEntityForceField) {
			return ((TileEntityForceField)tileEntity).cache_lightCamouflage;
		}
		
		return 0;
	}
	
	private double log_explosionX;
	private double log_explosionY = -1;
	private double log_explosionZ;
	@Override
	public float getExplosionResistance(Entity entity, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
		boolean enableFirstHit = (log_explosionX != explosionX || log_explosionY != explosionY || log_explosionZ != explosionZ); 
		if (enableFirstHit) {
			log_explosionX = explosionX;
			log_explosionY = explosionY;
			log_explosionZ = explosionZ;
		}
		
		// find explosion strength, defaults to no effect
		float strength = 0.0F;
		if (entity == null && (explosionX == Math.rint(explosionX)) && (explosionY == Math.rint(explosionY)) && (explosionZ == Math.rint(explosionZ)) ) {
			// IC2 Reactor blowing up => bloc is already air
			Block block = world.getBlock((int)explosionX, (int)explosionY, (int)explosionZ);
			TileEntity tileEntity = world.getTileEntity((int)explosionX, (int)explosionY, (int)explosionZ);
			if (enableFirstHit && WarpDriveConfig.LOGGING_FORCEFIELD) {
				WarpDrive.logger.info("Block at location is " + block + " " + block.getUnlocalizedName() + " with tileEntity " + tileEntity);
			}
		}
		
		if (entity != null) {
			switch (entity.getClass().toString()) {
			case "class net.minecraft.entity.item.EntityEnderCrystal": strength = 6.0F; break;
			case "class net.minecraft.entity.item.EntityMinecartTNT": strength = 4.0F; break;
			case "class net.minecraft.entity.item.EntityTNTPrimed": strength = 5.0F; break;
			case "class net.minecraft.entity.monster.EntityCreeper": strength = 3.0F; break;  // *2 for powered ones
			case "class appeng.entity.EntityTinyTNTPrimed": strength = 0.2F; break;
			case "class ic2.core.block.EntityItnt": strength = 5.5F; break; 
			case "class ic2.core.block.EntityNuke": strength = 45.0F; break;
			case "class ic2.core.block.EntityDynamite": strength = 1.0F; break;
			case "class ic2.core.block.EntityStickyDynamite": strength = 1.0F; break;
			default:
				if (enableFirstHit) {
					WarpDrive.logger.error("Unknown explosion source " + entity.getClass().toString() + " " + entity);
				}
				break;
			}
		}
		
		// apply damages to force field by consuming energy
		Explosion explosion = new Explosion(world, entity, explosionX, explosionY, explosionZ, 4.0F);
		Vector3 vDirection = new Vector3(x + 0.5D - explosionX, y + 0.5D - explosionY, z + 0.5D - explosionZ);
		double magnitude = Math.max(1.0D, vDirection.getMagnitude());
		if (magnitude != 0) {// normalize
			vDirection.scale(1 / magnitude);
		}
		double damageLevel = strength / (magnitude * magnitude) * 1.0D;
		double damageLeft = 0;
		ForceFieldSetup forceFieldSetup = getForceFieldSetup(world, x, y, z);
		if (forceFieldSetup != null) {
			damageLeft = forceFieldSetup.applyDamage(world, DamageSource.setExplosionSource(explosion), (int)damageLevel);
		}
		
		assert(damageLeft >= 0);
		if (WarpDriveConfig.LOGGING_FORCEFIELD) {
			WarpDrive.logger.info( "BlockForceField(" + tier + " at " + x + " " + y + " " + z + ")"
				                 + ".getExplosionResistance" + ((entity != null) ? " from " + entity : " at " + explosionX + " " + explosionY + " " + explosionZ)
								 + " damageLevel " + damageLevel + " damageLeft " + damageLeft);
		}
		return super.getExplosionResistance(entity, world, x, y, z, explosionX, explosionY, explosionZ);
	}
	
	@Override
	public boolean canDropFromExplosion(Explosion p_149659_1_) {
		return false;
	}
	
	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		if (tier > 0) {
			world.setBlock(x, y, z, WarpDrive.blockForceFields[tier - 1], world.getBlockMetadata(x, y, z), 2);
			super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
			return;
		}
		super.onBlockExploded(world, x, y, z, explosion);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		// (block is already set to air by caller, see IC2 iTNT)
		if (tier > 0) {
			world.setBlock(x, y, z, WarpDrive.blockForceFields[tier - 1], 0, 2);
			super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
			return;
		}
		super.onBlockDestroyedByExplosion(world, x, y, z, explosion);
	}
	
	@Override
	public float getBlockHardness(World world, final int x, final int y, final int z,
	                              final DamageSource damageSource, final int damageParameter, final Vector3 damageDirection, final int damageLevel) {
		return WarpDriveConfig.HULL_HARDNESS[tier - 1];
	}
	
	@Override
	public int applyDamage(World world, final int x, final int y, final int z, final DamageSource damageSource,
	                       final int damageParameter, final Vector3 damageDirection, final int damageLevel) {
		ForceFieldSetup forceFieldSetup = getForceFieldSetup(world, x, y, z);
		if (forceFieldSetup != null) {
			return forceFieldSetup.applyDamage(world, damageSource, damageLevel);
		}
		
		return damageLevel;
	}
}
