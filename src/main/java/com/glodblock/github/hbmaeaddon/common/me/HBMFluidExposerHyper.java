package com.glodblock.github.hbmaeaddon.common.me;

import com.glodblock.github.hbmaeaddon.api.IHBMFluidExposerHost;
import com.glodblock.github.inventory.MEMonitorIFluidHandler;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.me.helpers.AENetworkProxy;

public class HBMFluidExposerHyper extends HBMFluidExposer {

    public HBMFluidExposerHyper(AENetworkProxy networkProxy, IHBMFluidExposerHost ih) {
        super(networkProxy, ih);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (!this.gridProxy.isActive()) {
            return TickRateModulation.SLEEP;
        } else {
            this.sendAround();
            this.updateStorage();
            return TickRateModulation.URGENT;
        }
    }

    @Override
    public IMEMonitor<IAEFluidStack> getFluidInventory() {
        if (this.hasConfig) {
            if (this.resetConfigCache) {
                this.resetConfigCache = false;
                return new ExposerHyperInventory(this);
            }
        }
        return this.fluids;
    }

    private static class ExposerHyperInventory extends MEMonitorIFluidHandler {

        ExposerHyperInventory(HBMFluidExposerHyper exposer) {
            super(exposer.storage);
        }
    }
}
