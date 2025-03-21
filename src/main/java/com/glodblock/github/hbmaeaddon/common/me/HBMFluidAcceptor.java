package com.glodblock.github.hbmaeaddon.common.me;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;

import com.glodblock.github.hbmaeaddon.api.IHBMFluidAcceptorHost;
import com.glodblock.github.hbmaeaddon.util.HBMFluidBridge;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.util.Compat;

import api.hbm.fluidmk2.IFluidConnectorMK2;
import api.hbm.fluidmk2.IFluidStandardReceiverMK2;
import appeng.api.config.Actionable;
import appeng.api.networking.GridFlags;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;
import appeng.util.item.AEFluidStack;

public class HBMFluidAcceptor implements IFluidStandardReceiverMK2 {

    private final AENetworkProxy gridProxy;
    private final IHBMFluidAcceptorHost iHost;
    private final BaseActionSource mySource;

    public HBMFluidAcceptor(AENetworkProxy networkProxy, IHBMFluidAcceptorHost ih) {
        this.gridProxy = networkProxy;
        this.gridProxy.setFlags(GridFlags.REQUIRE_CHANNEL);
        this.iHost = ih;
        this.mySource = new MachineSource(this.iHost);
    }

    public void subscribe() {
        // formerly subscribeToAllAround from IFluidUser
        TileEntity te = this.iHost.getTileEntity();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.trySubscribe(Fluids.NONE, te.getWorldObj(), te.xCoord + dir.offsetX, te.yCoord + dir.offsetY, te.zCoord + dir.offsetZ, dir);
        }
    }

    @Override
    // formerly trySubscribe from IFluidConnector
    public void trySubscribe(FluidType type, World world, int x, int y, int z, ForgeDirection dir) {
        var te = Compat.getTileStandard(world, x, y, z);
        if (te instanceof TileEntityPipeBaseNT con) {
            var pipeType = con.getType();
            GenNode node = UniNodespace.getNode(world, x, y, z, pipeType.getNetworkProvider());
            if (node != null && node.net != null) {
                node.net.addReceiver(this.iHost.getTileEntity());
            }
        }
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if (type != Fluids.NONE && this.gridProxy.isActive()) {
            return Long.MAX_VALUE;
        }
        return 0;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        var fluid = HBMFluidBridge.get(type);
        if (fluid != null) {
            var storage = this.getFluidGrid();
            if (storage != null) {
                var stack = AEFluidStack.create(new FluidStack(fluid, 1));
                stack.setStackSize(amount);
                var left = storage.injectItems(stack, Actionable.MODULATE, this.mySource);
                if (left == null) {
                    return 0;
                } else {
                    return left.getStackSize();
                }
            }
        }
        return amount;
    }

    private IMEMonitor<IAEFluidStack> getFluidGrid() {
        try {
            return this.gridProxy.getGrid().<IStorageGrid>getCache(IStorageGrid.class).getFluidInventory();
        } catch (GridAccessException e) {
            return null;
        }
    }

    // Don't use this
    @Override
    public FluidTank[] getReceivingTanks() {
        return new FluidTank[0];
    }

    // Don't use this
    @Override
    public FluidTank[] getAllTanks() {
        return new FluidTank[0];
    }

    @Override
    public boolean isLoaded() {
        return this.iHost.isLoaded();
    }

}
