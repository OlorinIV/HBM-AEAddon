package com.glodblock.github.hbmaeaddon.common.tile;

import java.util.EnumSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkChannelsChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AECableType;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import com.glodblock.github.hbmaeaddon.common.me.HBMFluidExposerHyper;
import com.hbm.inventory.fluid.tank.FluidTank;
import io.netty.buffer.ByteBuf;

public class TileHBMFluidExposerHyper extends TileHBMFluidExposer {

    private final HBMFluidExposerHyper exposer = new HBMFluidExposerHyper(this.getProxy(), this);


    private static final int POWERED_FLAG = 1;
    private static final int CHANNEL_FLAG = 2;
    private static final int BOOTING_FLAG = 4;
    private int clientFlags = 0; // sent as byte.
    private boolean isLoaded = true;

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkChannelsChanged c) {
        super.stateChange(c);
    }

    @MENetworkEventSubscribe
    public void stateChange(final MENetworkPowerStatusChange c) {
        super.stateChange(c);
    }

    @Override
    public void gridChanged() {
        this.exposer.gridChanged();
    }

    @Override
    public void onReady() {
        super.onReady();
    }

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_TileInterface(final NBTTagCompound data) {
        this.exposer.writeToNBT(data);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_TileInterface(final NBTTagCompound data) {
        this.exposer.readFromNBT(data);
    }

    @Override
    public AECableType getCableConnectionType(final ForgeDirection dir) {
        return this.exposer.getCableConnectionType(dir);
    }

    @TileEvent(TileEventType.NETWORK_READ)
    public boolean readFromStream_HBMExposerHyper(final ByteBuf data) {
        int newState = data.readByte();
        if (newState != clientFlags) {
            clientFlags = newState;
            this.markForUpdate();
            return true;
        } else {
            return false;
        }
    }

    @TileEvent(TileEventType.NETWORK_WRITE)
    public void writeToStream_HBMExposerHyper(final ByteBuf data) {
        clientFlags = 0;
        try {
            if (this.getProxy().getEnergy().isNetworkPowered()) clientFlags |= POWERED_FLAG;
            if (this.getProxy().getNode().meetsChannelRequirements()) clientFlags |= CHANNEL_FLAG;
            if (this.getProxy().getPath().isNetworkBooting()) clientFlags |= BOOTING_FLAG;
        } catch (final GridAccessException e) {
            // meh
        }
        data.writeByte(clientFlags);
    }

    @Override
    public IMEMonitor<IAEItemStack> getItemInventory() {
        return this.exposer.getItemInventory();
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        return this.exposer.getFluidInventory();
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return this.exposer.getTickingRequest(node);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.exposer.tickingRequest(node, ticksSinceLastCall);
    }

    @Override
    public FluidTank[] getSendingTanks() {
        return this.exposer.getSendingTanks();
    }

    @Override
    public FluidTank[] getAllTanks() {
        return this.exposer.getAllTanks();
    }

    @Override
    public boolean isPowered() {
        return (clientFlags & POWERED_FLAG) == POWERED_FLAG;
    }

    @Override
    public boolean isActive() {
        return (clientFlags & CHANNEL_FLAG) == CHANNEL_FLAG;
    }

    @Override
    public boolean isBooting() {
        return (clientFlags & BOOTING_FLAG) == BOOTING_FLAG;
    }

    @Override
    public HBMFluidExposerHyper getExposer() {
        return this.exposer;
    }

    @Override
    public EnumSet<ForgeDirection> getTargets() {
        return super.getTargets();
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public boolean isLoaded() {
        return this.isLoaded;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        this.isLoaded = false;
    }
}
