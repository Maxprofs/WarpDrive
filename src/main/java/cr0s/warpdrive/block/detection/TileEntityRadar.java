package cr0s.warpdrive.block.detection;

import java.util.ArrayList;
import java.util.Arrays;

import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Optional;
import cr0s.warpdrive.WarpDrive;
import cr0s.warpdrive.block.TileEntityAbstractEnergy;
import cr0s.warpdrive.block.movement.TileEntityShipCore;
import cr0s.warpdrive.conf.WarpDriveConfig;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.peripheral.IComputerAccess;

public class TileEntityRadar extends TileEntityAbstractEnergy {
	private ArrayList<TileEntityShipCore> results;

	private int scanRadius = 0;
	private int cooldownTime = 0;
	
	public TileEntityRadar() {
		super();
		peripheralName = "warpdriveRadar";
		methodsArray = new String[] {
				"scanRadius",
				"getResultsCount",
				"getResult",
				"getEnergyLevel",
				"pos"
			};
		CC_scripts = Arrays.asList("scan", "ping");
	}
	
	@Override
	public void updateEntity() {
		super.updateEntity();

		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return;
		}
		
		try {
			if (getBlockMetadata() == 2) {
				cooldownTime++;
				if (cooldownTime > (20 * ((scanRadius / 1000) + 1))) {
					results = WarpDrive.shipCores.searchWarpCoresInRadius(xCoord, yCoord, zCoord, scanRadius);
					// WarpDrive.debugPrint("" + this + " Scan found " + results.size() + " results in " + scanRadius + " radius...");
					worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
					cooldownTime = 0;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {
		super.writeToNBT(tag);
	}

	// OpenComputer callback methods
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] scanRadius(Context context, Arguments arguments) {
		return scanRadius(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getResultsCount(Context context, Arguments arguments) {
		if (results != null) {
			return new Integer[] { results.size() };
		}
		return new Integer[] { -1 };
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] getResult(Context context, Arguments arguments) {
		return getResult(argumentsOCtoCC(arguments));
	}
	
	@Callback
	@Optional.Method(modid = "OpenComputers")
	public Object[] pos(Context context, Arguments arguments) {
		return new Integer[] { xCoord, yCoord, zCoord };
	}
	
	private Object[] scanRadius(Object[] arguments) {
		// always clear results
		results = null;
		
		// validate parameters
		if (arguments.length != 1) {
			return new Boolean[] { false };
		}
		int radius;
		try {
			radius = toInt(arguments[0]);
		} catch(Exception e) {
           	return new Boolean[] { false };
        }
		if (radius <= 0 || radius > 10000) {
			scanRadius = 0;
			return new Boolean[] { false };
		}
		if (!consumeEnergy(Math.max(radius, 100) * Math.max(radius, 100), false)) {
			return new Boolean[] { false };
		}
		
		// Begin searching
		scanRadius = radius;
		cooldownTime = 0;
		if (getBlockMetadata() != 2) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 2, 1 + 2);
		}
		return new Boolean[] { true };
	}
	private Object[] getResult(Object[] arguments) {
		if (arguments.length == 1 && (results != null)) {
			int index;
			try {
				index = toInt(arguments[0]);
			} catch(Exception e) {
				return new Object[] { "FAIL", 0, 0, 0 };
			}
			if (index >= 0 && index < results.size()) {
				TileEntityShipCore res = results.get(index);
				if (res != null)
				{
					int yAddition = (res.getWorldObj().provider.dimensionId == WarpDriveConfig.G_SPACE_DIMENSION_ID) ? 256 : (res.getWorldObj().provider.dimensionId == WarpDriveConfig.G_HYPERSPACE_DIMENSION_ID) ? 512 : 0;
					return new Object[] { res.coreFrequency, res.xCoord, res.yCoord + yAddition, res.zCoord };
				}
			}
		}
		return new Object[] { "FAIL", 0, 0, 0 };
	}

	// ComputerCraft IPeripheral methods implementation
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void attach(IComputerAccess computer) {
		super.attach(computer);
		if (getBlockMetadata() == 0) {
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 1, 1 + 2);
		}
	}

	@Override
	@Optional.Method(modid = "ComputerCraft")
	public void detach(IComputerAccess computer) {
		super.detach(computer);
		// worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, 0, 1 + 2);
	}
	
	@Override
	@Optional.Method(modid = "ComputerCraft")
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments) {
	   	String methodName = methodsArray[method];
		if (methodName.equals("scanRadius")) {// scanRadius (radius)
			return scanRadius(arguments);

		} else if (methodName.equals("getResultsCount")) {
			if (results != null) {
				return new Integer[] { results.size() };
			}
			return new Integer[] { -1 };
			
		} else if (methodName.equals("getResult")) {
			return getResult(arguments);
			
		} else if (methodName.equals("getEnergyLevel")) {
			return getEnergyLevel();
				
		} else if (methodName.equals("pos")) {
			return new Integer[] { xCoord, yCoord, zCoord };
		}

		return null;
	}

	@Override
	public int getMaxEnergyStored() {
		return WarpDriveConfig.WR_MAX_ENERGY_VALUE;
	}

    @Override
    public boolean canInputEnergy(ForgeDirection from) {
    	return true;
    }
}
