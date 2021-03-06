/*  
    Copyright (C) 2012 Thunderdark

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    
    Contributors:
    
    Matchlighter
    Thunderdark 

 */

package com.nekokittygames.mffs.common.tileentity;


import cofh.api.energy.IEnergyReceiver;
import com.nekokittygames.mffs.api.IPowerLinkItem;
import com.nekokittygames.mffs.common.Linkgrid;
import com.nekokittygames.mffs.common.ModularForceFieldSystem;
import com.nekokittygames.mffs.common.compat.TeslaCap;
import com.nekokittygames.mffs.common.container.ContainerForceEnergyExtractor;
import com.nekokittygames.mffs.common.item.ItemCapacitorUpgradeCapacity;
import com.nekokittygames.mffs.common.item.ItemExtractorUpgradeBooster;
import com.nekokittygames.mffs.common.item.ItemForcicium;
import com.nekokittygames.mffs.common.item.ItemForcicumCell;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import net.darkhax.tesla.capability.TeslaCapabilities;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;

import java.util.LinkedList;
import java.util.List;

@Optional.InterfaceList({	@Optional.Interface(modid = "CoFHAPI|energy",iface = "cofh.api.energy.IEnergyReceiver"),
							@Optional.Interface(modid = "IC2",iface = "ic2.api.energy.tile.IEnergySink"),
							@Optional.Interface(modid = "IC2",iface = "ic2.api.energy.tile.IEnergyEmitter")})
public class TileEntityExtractor extends TileEntityFEPoweredMachine implements
		IEnergySink, IEnergyReceiver {
	private ItemStack inventory[];

	private int workmode = 0;

	protected int WorkEnergy;
	protected int MaxWorkEnergy;
	private int ForceEnergybuffer;
	private int MaxForceEnergyBuffer;
	private int WorkCylce;
	private int workTicker;
	private int workdone;
	private int maxworkcylce;
	private int capacity;
	//private IPowerEmitter powerEmitter;
	private boolean addedToEnergyNet;

	private TeslaCap cap;

	public TileEntityExtractor() {
		inventory = new ItemStack[5];
		WorkEnergy = 0;
		MaxWorkEnergy = 4000;
		ForceEnergybuffer = 0;
		MaxForceEnergyBuffer = 1000000;
		WorkCylce = 0;
		workTicker = 20;
		maxworkcylce = 125;
		capacity = 0;
		addedToEnergyNet = false;
		if(Loader.isModLoaded("tesla"))
			cap=new TeslaCap(this);


	}



	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int Capacity) {
		if (this.capacity != Capacity) {
			this.capacity = Capacity;
		}
	}

	public int getMaxworkcylce() {
		return maxworkcylce;
	}

	public void setMaxworkcylce(int maxworkcylce) {
		this.maxworkcylce = maxworkcylce;
	}

	public int getWorkdone() {
		return workdone;
	}

	public void setWorkdone(int workdone) {
		if (this.workdone != workdone) {
			this.workdone = workdone;

		}
	}

	public int getWorkTicker() {
		return workTicker;
	}

	public void setWorkTicker(int workTicker) {
		this.workTicker = workTicker;
	}

	public int getMaxForceEnergyBuffer() {
		return MaxForceEnergyBuffer;
	}

	public void setMaxForceEnergyBuffer(int maxForceEnergyBuffer) {
		MaxForceEnergyBuffer = maxForceEnergyBuffer;
		this.markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		worldObj.markBlockRangeForRenderUpdate(pos,pos);
		//todo: make more packet friendly
	}

	public int getForceEnergybuffer() {
		return ForceEnergybuffer;
	}

	public void setForceEnergybuffer(int forceEnergybuffer) {
		ForceEnergybuffer = forceEnergybuffer;
		this.markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		worldObj.markBlockRangeForRenderUpdate(pos,pos);
	}

	public void setWorkCylce(int i) {
		if (this.WorkCylce != i) {
			this.WorkCylce = i;
		}
		this.markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		worldObj.markBlockRangeForRenderUpdate(pos,pos);
	}

	public int getWorkCylce() {
		return WorkCylce;
	}

	public int getWorkEnergy() {
		return WorkEnergy;
	}

	public void setWorkEnergy(int workEnergy) {
		WorkEnergy = workEnergy;
		this.markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		worldObj.markBlockRangeForRenderUpdate(pos,pos);
	}

	public int getMaxWorkEnergy() {
		return MaxWorkEnergy;
	}

	public void setMaxWorkEnergy(int maxWorkEnergy) {
		MaxWorkEnergy = maxWorkEnergy;
		this.markDirty();
		worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
		worldObj.markBlockRangeForRenderUpdate(pos,pos);
	}

	@Override
	public void dropPlugins() {
		for (int a = 0; a < this.inventory.length; a++) {
			dropplugins(a, this);
		}
	}



	public void checkslots(boolean init) {

		if (getStackInSlot(2) != null) {
			if (getStackInSlot(2).getItem() == ModularForceFieldSystem.MFFSitemupgradecapcap) {
				setMaxForceEnergyBuffer(1000000 + (getStackInSlot(2).stackSize * 100000));
			} else {
				setMaxForceEnergyBuffer(1000000);
			}
		} else {
			setMaxForceEnergyBuffer(1000000);
		}

		if (getStackInSlot(3) != null) {
			if (getStackInSlot(3).getItem() == ModularForceFieldSystem.MFFSitemupgradeexctractorboost) {
				setWorkTicker(20 - getStackInSlot(3).stackSize);
			} else {
				setWorkTicker(20);
			}
		} else {
			setWorkTicker(20);
		}

		if (getStackInSlot(4) != null) {
			if (getStackInSlot(4).getItem() == ModularForceFieldSystem.MFFSitemForcicumCell) {
				workmode = 1;
				setMaxWorkEnergy(200000);
			}
		} else {
			workmode = 0;
			setMaxWorkEnergy(4000);
		}
	}

	private boolean hasPowertoConvert() {
		if (WorkEnergy >= MaxWorkEnergy - 1) {
			setWorkEnergy(0);
			return true;
		}
		return false;
	}

	private boolean hasfreeForceEnergyStorage() {
		if (this.MaxForceEnergyBuffer > this.ForceEnergybuffer)
			return true;
		return false;
	}

	private boolean hasStufftoConvert() {
		if (WorkCylce > 0) {
			return true;
		} else {
			if (ModularForceFieldSystem.adventureMapMode) {
				setMaxworkcylce(ModularForceFieldSystem.ForciciumCellWorkCycle);
				setWorkCylce(getMaxworkcylce());
				return true;
			}

			if (getStackInSlot(0) != null) {
				if (getStackInSlot(0).getItem() == ModularForceFieldSystem.MFFSitemForcicium) {
					setMaxworkcylce(ModularForceFieldSystem.ForciciumWorkCycle);
					setWorkCylce(getMaxworkcylce());
					decrStackSize(0, 1);
					return true;
				}

				if (getStackInSlot(0).getItem() == ModularForceFieldSystem.MFFSitemForcicumCell) {
					if (((ItemForcicumCell) getStackInSlot(0).getItem())
							.useForcecium(1, getStackInSlot(0))) {
						setMaxworkcylce(ModularForceFieldSystem.ForciciumCellWorkCycle);
						setWorkCylce(getMaxworkcylce());
						return true;
					}
				}
			}
		}

		return false;
	}

	public void transferForceEnergy() {
		if (this.getForceEnergybuffer() > 0) {
			if (this.hasPowerSource()) {
				int PowerTransferrate = this.getMaximumPower() / 120;
				int freeAmount = this.getMaximumPower()
						- this.getAvailablePower();

				if (this.getForceEnergybuffer() > freeAmount) {
					if (freeAmount > PowerTransferrate) {
						emitPower(PowerTransferrate, false);
						this.setForceEnergybuffer(this.getForceEnergybuffer()
								- PowerTransferrate);

					} else {
						emitPower(freeAmount, false);
						this.setForceEnergybuffer(this.getForceEnergybuffer()
								- freeAmount);
					}
				} else {
					if (freeAmount > this.getForceEnergybuffer()) {
						emitPower(getForceEnergybuffer(), false);
						this.setForceEnergybuffer(this.getForceEnergybuffer()
								- getForceEnergybuffer());
					} else {
						emitPower(freeAmount, false);
						this.setForceEnergybuffer(this.getForceEnergybuffer()
								- freeAmount);
					}
				}
			}
		}

	}

	@Override
	public short getmaxSwitchModi() {
		return 3;
	}

	@Override
	public short getminSwitchModi() {
		return 1;
	}

	@Override
	public void update() {
		if (worldObj.isRemote == false) {

			if (init) {
				checkslots(true);
				if(addedToEnergyNet==false)
				{
					if(Loader.isModLoaded("IC2"))
						AddToIC2EnergyNet();
					else
						addedToEnergyNet=true;
				}

			}



			if (getSwitchModi() == 1)
				if (!getSwitchValue() && isRedstoneSignal())
					toggelSwitchValue();

			if (getSwitchModi() == 1)
				if (getSwitchValue() && !isRedstoneSignal())
					toggelSwitchValue();

			if (!isActive() && getSwitchValue())
				setActive(true);

			if (isActive() && !getSwitchValue())
				setActive(false);

			if (isActive()) {
				if (ModularForceFieldSystem.buildcraftFound)
					converMJtoWorkEnergy();
			}

			if (this.getTicker() >= getWorkTicker()) {
				checkslots(false);

				if (workmode == 0 && isActive()) {

					if (this.getWorkdone() != getWorkEnergy() * 100
							/ getMaxWorkEnergy())
						setWorkdone(getWorkEnergy() * 100 / getMaxWorkEnergy());

					if (getWorkdone() > 100) {
						setWorkdone(100);
					}

					if (this.getCapacity() != (getForceEnergybuffer() * 100)
							/ getMaxForceEnergyBuffer())
						setCapacity((getForceEnergybuffer() * 100)
								/ getMaxForceEnergyBuffer());

					if (this.hasfreeForceEnergyStorage()
							&& this.hasStufftoConvert()) {

						if (this.hasPowertoConvert()) {
							setWorkCylce(getWorkCylce() - 1);
							setForceEnergybuffer(getForceEnergybuffer()
									+ ModularForceFieldSystem.ExtractorPassForceEnergyGenerate);
						}
					}

					transferForceEnergy();

					this.setTicker((short) 0);
				}

				if (workmode == 1 && isActive()) {
					if (this.getWorkdone() != getWorkEnergy() * 100
							/ getMaxWorkEnergy())
						setWorkdone(getWorkEnergy() * 100 / getMaxWorkEnergy());

					if (((ItemForcicumCell) getStackInSlot(4).getItem())
							.getForceciumlevel(getStackInSlot(4)) < ((ItemForcicumCell) getStackInSlot(
							4).getItem()).getMaxForceciumlevel()) {

						if (this.hasPowertoConvert() && isActive()) {
							((ItemForcicumCell) getStackInSlot(4).getItem())
									.setForceciumlevel(
											getStackInSlot(4),
											((ItemForcicumCell) getStackInSlot(
													4).getItem())
													.getForceciumlevel(getStackInSlot(4)) + 1);
						}
					}

					this.setTicker((short) 0);
				}
			}

			this.setTicker((short) (this.getTicker() + 1));
		}
		super.update();
	}

	@Optional.Method(modid = "IC2")
	private void AddToIC2EnergyNet() {
		if (!worldObj.isRemote) {
			EnergyTileLoadEvent event = new EnergyTileLoadEvent(this);
			MinecraftForge.EVENT_BUS.post(event);
			addedToEnergyNet=true;
		}
	}

	@Override
	public Container getContainer(InventoryPlayer inventoryplayer) {
		return new ContainerForceEnergyExtractor(inventoryplayer.player, this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);


	}

	@Override
	public void readExtraNBT(NBTTagCompound nbttagcompound) {
		super.readExtraNBT(nbttagcompound);
		ForceEnergybuffer = nbttagcompound.getInteger("ForceEnergybuffer");
		WorkEnergy = nbttagcompound.getInteger("WorkEnergy");
		WorkCylce = nbttagcompound.getInteger("WorkCylce");

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
		inventory = new ItemStack[getSizeInventory()];
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist
					.getCompoundTagAt(i);
			byte byte0 = nbttagcompound1.getByte("Slot");
			if (byte0 >= 0 && byte0 < inventory.length) {
				inventory[byte0] = ItemStack
						.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	@Override
	public void writeExtraNBT(NBTTagCompound nbttagcompound) {
		super.writeExtraNBT(nbttagcompound);
		nbttagcompound.setInteger("WorkCylce", WorkCylce);
		nbttagcompound.setInteger("WorkEnergy", WorkEnergy);
		nbttagcompound.setInteger("ForceEnergybuffer", ForceEnergybuffer);

		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < inventory.length; i++) {
			if (inventory[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				inventory[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		nbttagcompound.setTag("Items", nbttaglist);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);


		return nbttagcompound;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inventory[i];
	}

	@Override
	public String getName() {
		return "Extractor";
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public int getSizeInventory() {
		return inventory.length;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inventory[i] = itemstack;
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		if (inventory[i] != null) {
			if (inventory[i].stackSize <= j) {
				ItemStack itemstack = inventory[i];
				inventory[i] = null;
				return itemstack;
			}
			ItemStack itemstack1 = inventory[i].splitStack(j);
			if (inventory[i].stackSize == 0) {
				inventory[i] = null;
			}
			return itemstack1;
		} else {
			return null;
		}
	}

	@Override
	public List<String> getFieldsforUpdate() {
		List<String> NetworkedFields = new LinkedList<String>();
		NetworkedFields.clear();

		NetworkedFields.addAll(super.getFieldsforUpdate());
		NetworkedFields.add("capacity");
		NetworkedFields.add("WorkCylce");
		NetworkedFields.add("WorkEnergy");
		NetworkedFields.add("workdone");

		return NetworkedFields;
	}

	@Override
	public boolean isItemValid(ItemStack par1ItemStack, int Slot) {
		switch (Slot) {
		case 0:
			if ((par1ItemStack.getItem() instanceof ItemForcicium || par1ItemStack
					.getItem() instanceof ItemForcicumCell)
					&& getStackInSlot(4) == null)
				return true;
			break;

		case 1:
			if (par1ItemStack.getItem() instanceof IPowerLinkItem)
				return true;
			break;

		case 2:
			if (par1ItemStack.getItem() instanceof ItemCapacitorUpgradeCapacity)
				return true;
			break;

		case 3:
			if (par1ItemStack.getItem() instanceof ItemExtractorUpgradeBooster)
				return true;
			break;

		case 4:
			if (par1ItemStack.getItem() instanceof ItemForcicumCell
					&& getStackInSlot(0) == null)
				return true;
			break;
		}
		return false;
	}

	@Override
	public int getSlotStackLimit(int Slot) {
		switch (Slot) {
		case 0: // Forcicium
			return 64;
		case 1: // Powerlink
			return 1;
		case 2: // Cap upgrade
			return 9;
		case 3: // Boost upgrade
			return 19;
		case 4: // Forcicium cell
			return 1;
		}
		return 1;
	}




	@Override
	public void invalidate() {


		Linkgrid.getWorldMap(worldObj).getExtractor().remove(getDeviceID());

		super.invalidate();
	}

	public void converMJtoWorkEnergy() {
		/*if (this.getWorkEnergy() < this.getMaxWorkEnergy()) {
			float use = powerProvider
					.useEnergy(1, (float) (this.getMaxWorkEnergy() - this
							.getWorkEnergy() / 2.5), true);

			if (getWorkEnergy() + (use * 2.5) > getMaxWorkEnergy()) {
				setWorkEnergy(getMaxWorkEnergy());
			} else {
				setWorkEnergy((int) (getWorkEnergy() + (use * 2.5)));
			}
		}*/
	}

	/*@Override
	public void setPowerProvider(IPowerProvider provider) {
		this.powerProvider = provider;
	}

	@Override
	public IPowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public void doWork() {
	}

	@Override
	public int powerRequest(ForgeDirection from) {
		double workEnergyinMJ = getWorkEnergy() / 2.5;
		double MaxWorkEnergyinMj = getMaxWorkEnergy() / 2.5;

		return (int) Math.round(MaxWorkEnergyinMj - workEnergyinMJ);
	}*/

	@Override
	public ItemStack getPowerLinkStack() {
		return this.getStackInSlot(getPowerlinkSlot());
	}

	@Override
	public int getPowerlinkSlot() {
		return 1;
	}

	@Override
	public TileEntityAdvSecurityStation getLinkedSecurityStation() {

		TileEntityCapacitor cap = Linkgrid.getWorldMap(worldObj).getCapacitor()
				.get(getPowerSourceID());
		if (cap != null) {
			TileEntityAdvSecurityStation sec = cap.getLinkedSecurityStation();
			if (sec != null) {
				return sec;
			}

		}
		return null;
	}



	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}


	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public boolean canConnectEnergy(EnumFacing from) {
		return true;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(EnumFacing from) {
		return 0;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(EnumFacing from) {
		return 9999;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		double freeSpace = (double)(getMaxWorkEnergy() - getWorkEnergy());
		if(freeSpace==0)
		{

		}
		if(freeSpace >= maxReceive) {
			if(!simulate)
				setWorkEnergy(getWorkEnergy() + (int)maxReceive);
			return maxReceive;
		}
		else {
			if(!simulate)
				setWorkEnergy(getMaxWorkEnergy());
			return (int)(freeSpace);
		}
	}

	@Override
	public int[] getSlotsForFace(EnumFacing side) {
		return new int[0];
	}

	@Override
	public boolean canInsertItem(int index, ItemStack itemStackIn, EnumFacing direction) {
		return false;
	}

	@Override
	public boolean canExtractItem(int index, ItemStack stack, EnumFacing direction) {
		return false;
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		ItemStack item=inventory[index];
		inventory[index]=null;
		this.markDirty();
		return item;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {

	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {

	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if(Loader.isModLoaded("tesla"))
			if (hasTeslaCapability(capability))
				return true;
		return super.hasCapability(capability, facing);
	}

	@Optional.Method(modid = "tesla")
	private boolean hasTeslaCapability(Capability<?> capability) {
		if(capability== TeslaCapabilities.CAPABILITY_CONSUMER)
			return true;
		return false;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if(Loader.isModLoaded("tesla"))
			if (hasTeslaCapability(capability))
				return (T) cap;
		return super.getCapability(capability, facing);
	}


	@Override
	@Optional.Method(modid = "IC2")
	public double getDemandedEnergy() {

		if(!this.isActive())
			return 0;
		return (double)(getMaxWorkEnergy() - getWorkEnergy());
	}

	@Override
	@Optional.Method(modid = "IC2")
	public int getSinkTier() {
		return 3;
	}

	@Override
	@Optional.Method(modid = "IC2")
	public double injectEnergy(EnumFacing enumFacing, double v, double v1) {
		double freeSpace = (double) (getMaxWorkEnergy() - getWorkEnergy());
		if(getDemandedEnergy()<=0)
			return v;
		if (freeSpace >= v) {
			setWorkEnergy(getWorkEnergy() + (int) v);
			return 0;
		} else {
			setWorkEnergy(getMaxWorkEnergy());
			return (int) (v-freeSpace);
		}

	}

	@Override
	@Optional.Method(modid = "IC2")
	public boolean acceptsEnergyFrom(IEnergyEmitter iEnergyEmitter, EnumFacing enumFacing) {
		return true;
	}
}
