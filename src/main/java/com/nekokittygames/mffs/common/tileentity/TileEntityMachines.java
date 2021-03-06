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
    Thunderdark - initial implementation
 */

package com.nekokittygames.mffs.common.tileentity;


import com.nekokittygames.mffs.api.IMFFS_Wrench;
import com.nekokittygames.mffs.api.ISwitchabel;
import com.nekokittygames.mffs.api.PointXYZ;
import com.nekokittygames.mffs.common.IModularProjector.Slots;
import com.nekokittygames.mffs.common.Linkgrid;
import com.nekokittygames.mffs.common.ModularForceFieldSystem;
import com.nekokittygames.mffs.common.SecurityHelper;
import com.nekokittygames.mffs.common.SecurityRight;
import com.nekokittygames.mffs.common.block.BlockMFFSBase;
import com.nekokittygames.mffs.common.item.ItemCardDataLink;
import com.nekokittygames.mffs.common.item.ItemCardPersonalID;
import com.nekokittygames.mffs.common.item.ItemCardPowerLink;
import com.nekokittygames.mffs.common.item.ItemCardSecurityLink;
import com.nekokittygames.mffs.network.INetworkHandlerEventListener;
import com.nekokittygames.mffs.network.INetworkHandlerListener;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.ForgeChunkManager.Type;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public abstract class TileEntityMachines extends TileEntity implements
		INetworkHandlerListener, INetworkHandlerEventListener, ISidedInventory,
		IMFFS_Wrench, ISwitchabel,ITickable {

	private boolean Active;
	private EnumFacing Side;
	private short ticker;
	protected boolean init;
	protected String DeviceName;
	protected int DeviceID;
	protected short SwitchModi;
	protected boolean SwitchValue;
	protected Random random = new Random();
	protected Ticket chunkTicket;

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
		return (oldState.getBlock()!=newSate.getBlock());
	}

	public TileEntityMachines() {
		Active = false;
		SwitchValue = false;
		init = true;
		Side = EnumFacing.UP;
		SwitchModi = 0; // 0:OFF 1: Redstone 2:Switch 3:CC
		ticker = 0;
		DeviceID = 0;
		DeviceName = "Please set Name";
	}

	public int getPercentageCapacity() {
		return 0;
	}

	public boolean hasPowerSource() {
		return false;
	}

	public abstract TileEntityAdvSecurityStation getLinkedSecurityStation();

	@Override
	public void onNetworkHandlerEvent(int key, String value) {

		switch (key) {
		case 0: // GUIchangeSwitchModi id: 0
			toogleSwitchModi();
			break;
		// DeviceName tipping Events id :10-12
		case 10:
			setDeviceName("");
			break;
		case 11:
			if (getDeviceName().length() <= 20)
				setDeviceName(getDeviceName() + value);
			break;
		case 12:
			if (getDeviceName().length() >= 1)
				setDeviceName(getDeviceName().substring(0,
						getDeviceName().length() - 1));
			break;

		}
		this.markDirty();
        worldObj.notifyBlockUpdate(pos, worldObj.getBlockState(pos), worldObj.getBlockState(pos), 2);
        worldObj.markBlockRangeForRenderUpdate(pos,pos);
	}

	@Override
	public List<String> getFieldsforUpdate() {
		List<String> NetworkedFields = new LinkedList<String>();
		NetworkedFields.clear();

		NetworkedFields.add("Active");
		NetworkedFields.add("Side");
		NetworkedFields.add("DeviceID");
		NetworkedFields.add("DeviceName");
		NetworkedFields.add("SwitchModi");
		NetworkedFields.add("SwitchValue");

		return NetworkedFields;
	}

	@Override
	public void onNetworkHandlerUpdate(String field) {

		worldObj.markBlockRangeForRenderUpdate(pos,pos);

	}


	@Override
	public void update() {

		if (!worldObj.isRemote)
			if (init)
				init();

		if (worldObj.isRemote)
			if (DeviceID == 0) {
				if (this.getTicker() >= 5 + random.nextInt(20)) {
					setTicker((short) 0);
				}
				setTicker((short) (getTicker() + 1));
			}
	}

	public void init() {

		DeviceID = Linkgrid.getWorldMap(worldObj).refreshID(this, DeviceID);
		if (ModularForceFieldSystem.enableChunkLoader)
			registerChunkLoading();
		init = false;
	}

	public short getmaxSwitchModi() {
		return 0;
	}

	public short getminSwitchModi() {
		return 0;
	}

	public void toogleSwitchModi() {

		if (getSwitchModi() == getmaxSwitchModi()) {
			SwitchModi = getminSwitchModi();
		} else {
			SwitchModi++;
		}

	}

	public boolean isRedstoneSignal() {
		// if(worldObj.isBlockGettingPowered(xCoord,yCoord, zCoord) ||
		if (worldObj.getStrongPower(pos) > 0
				|| worldObj.isBlockIndirectlyGettingPowered(pos)>0)
			return true;
		return false;
	}

	public short getSwitchModi() {
		if (SwitchModi < getminSwitchModi())
			SwitchModi = getminSwitchModi();
		return SwitchModi;
	}

	public boolean getSwitchValue() {
		return SwitchValue;
	}

	@Override
	public boolean isSwitchabel() {
		if (getSwitchModi() == 2)
			return true;
		return false;
	}

	@Override
	public void toggelSwitchValue() {
		SwitchValue = !SwitchValue;

	}

	public void setDeviceName(String DeviceName) {
		this.DeviceName = DeviceName;
	}

	public int getDeviceID() {
		return DeviceID;
	}

	public void setDeviceID(int i) {
		this.DeviceID = i;
	}

	public String getDeviceName() {
		return DeviceName;
	}

	public PointXYZ getMaschinePoint() {
		return new PointXYZ(pos, worldObj);
	}

	public abstract void dropPlugins();

	public void dropplugins(int slot, IInventory inventory) {

		if (worldObj.isRemote) {
			this.setInventorySlotContents(slot, null);
			return;
		}

		if (inventory.getStackInSlot(slot) != null) {
			if (inventory.getStackInSlot(slot).getItem() instanceof ItemCardSecurityLink
					|| inventory.getStackInSlot(slot).getItem() instanceof ItemCardPowerLink
					|| inventory.getStackInSlot(slot).getItem() instanceof ItemCardPersonalID
					|| inventory.getStackInSlot(slot).getItem() instanceof ItemCardDataLink) {
				worldObj.spawnEntityInWorld(new EntityItem(worldObj,
						this.pos.getX(), this.pos.getY(), this.pos.getZ(), new ItemStack(
								ModularForceFieldSystem.MFFSitemcardempty, 1)));
			} else {
				worldObj.spawnEntityInWorld(new EntityItem(worldObj,
						this.pos.getX(), this.pos.getY(), this.pos.getZ(), inventory
								.getStackInSlot(slot)));
			}

			inventory.setInventorySlotContents(slot, null);
			this.markDirty();
		}
	}


	@Override
	public void markDirty() {
		super.markDirty();
	}

	public abstract Container getContainer(InventoryPlayer inventoryplayer);

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound) {
		super.readFromNBT(nbttagcompound);
		readExtraNBT(nbttagcompound);
	}

	public void readExtraNBT(NBTTagCompound nbttagcompound) {
		Side = EnumFacing.values()[nbttagcompound.getInteger("side")];
		Active = nbttagcompound.getBoolean("active");
		SwitchValue = nbttagcompound.getBoolean("SwitchValue");
		DeviceID = nbttagcompound.getInteger("DeviceID");
		DeviceName = nbttagcompound.getString("DeviceName");
		SwitchModi = nbttagcompound.getShort("SwitchModi");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
		super.writeToNBT(nbttagcompound);

		writeExtraNBT(nbttagcompound);
		return nbttagcompound;
	}

	public void writeExtraNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setShort("SwitchModi", SwitchModi);
		nbttagcompound.setInteger("side", Side.getIndex());
		nbttagcompound.setBoolean("active", Active);
		nbttagcompound.setBoolean("SwitchValue", SwitchValue);
		nbttagcompound.setInteger("DeviceID", DeviceID);
		nbttagcompound.setString("DeviceName", DeviceName);
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		NBTTagCompound cmp=super.getUpdateTag();
		writeExtraNBT(cmp);
		return cmp;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag) {
		super.handleUpdateTag(tag);
		readExtraNBT(tag);
	}

	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {

		NBTTagCompound comp=new NBTTagCompound();
		writeExtraNBT(comp);
		SPacketUpdateTileEntity packetUpdateTileEntity=new SPacketUpdateTileEntity(pos,worldObj.getBlockState(pos).getBlock().getMetaFromState(worldObj.getBlockState(pos)),comp);
		return packetUpdateTileEntity;
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		readExtraNBT(pkt.getNbtCompound());
	}

	@Override
	public boolean wrenchCanManipulate(EntityPlayer entityPlayer, EnumFacing side) {
		if (!SecurityHelper.isAccessGranted(this, entityPlayer, worldObj,
				SecurityRight.EB)) {
			return false;
		}

		return true;
	}

	public short getTicker() {
		return ticker;
	}

	public void setTicker(short ticker) {
		this.ticker = ticker;
	}

	@Override
	public void setSide(EnumFacing i) {
		Side = i;
		worldObj.setBlockState(pos,worldObj.getBlockState(pos).withProperty(BlockMFFSBase.FACING,i));

	}

	public boolean isActive() {
		return Active;
	}

	public void setActive(boolean flag) {
		Active = flag;
		if(worldObj.getBlockState(pos).getBlock() instanceof BlockMFFSBase)
			worldObj.setBlockState(pos,worldObj.getBlockState(pos).withProperty(BlockMFFSBase.ACTIVE,flag));

	}

	@Override
	public EnumFacing getSide() {
		if(worldObj.getBlockState(pos).getBlock() instanceof BlockMFFSBase)
			return worldObj.getBlockState(pos).getValue(BlockMFFSBase.FACING);
		return EnumFacing.UP;
	}



	public void forceChunkLoading(Ticket ticket) {
		if (chunkTicket == null) {
			chunkTicket = ticket;
		}
		ChunkPos Chunk = new ChunkPos(pos.getX() >> 4,
				pos.getZ()>> 4);
		ForgeChunkManager.forceChunk(ticket, Chunk);
	}

	protected void registerChunkLoading() {
		if (chunkTicket == null) {
			chunkTicket = ForgeChunkManager.requestTicket(
					ModularForceFieldSystem.instance, worldObj, Type.NORMAL);
		}
		if (chunkTicket == null) {
			System.out
					.println("[ModularForceFieldSystem]no free Chunkloaders available");
			return;
		}

		chunkTicket.getModData().setInteger("MaschineX", pos.getX());
		chunkTicket.getModData().setInteger("MaschineY", pos.getY());
		chunkTicket.getModData().setInteger("MaschineZ", pos.getZ());
		ForgeChunkManager.forceChunk(chunkTicket, new ChunkPos(
				pos.getX() >> 4, pos.getZ()>> 4));

		forceChunkLoading(chunkTicket);
	}

	@Override
	public void invalidate() {
		ForgeChunkManager.releaseTicket(chunkTicket);
		super.invalidate();
	}

	public abstract boolean isItemValid(ItemStack par1ItemStack, int Slot);

	public abstract int getSlotStackLimit(int slt);

	@Override
	public void openInventory(EntityPlayer player) {

	}

	@Override
	public void closeInventory(EntityPlayer player) {

	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getTileEntity(pos) != this) {
			return false;
		} else {
			return entityplayer.getDistance(pos.getX(),pos.getY(),pos.getZ()) <= 64D;
		}
	}



	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	public int countItemsInSlot(Slots slt) {
		if (this.getStackInSlot(slt.slot) != null)
			return this.getStackInSlot(slt.slot).stackSize;
		return 0;
	}
}
