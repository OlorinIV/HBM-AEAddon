package com.glodblock.github.hbmaeaddon.common.block;

import com.glodblock.github.hbmaeaddon.HBMAEAddon;
import com.glodblock.github.hbmaeaddon.common.tile.TileHBMFluidExposerHyper;

public class BlockHBMFluidExposerHyper extends BlockHBMFluidExposer {

    public BlockHBMFluidExposerHyper() {
        super();
        this.setBlockName(HBMAEAddon.MODID + ".hbm_fluid_exposer_hyper");
        this.setBlockTextureName(HBMAEAddon.MODID + ":" + "hbm_fluid_exposer_hyper");
        this.setTileEntity(TileHBMFluidExposerHyper.class);
    }
}
