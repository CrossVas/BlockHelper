package de.thexxturboxx.blockhelper.integration;

import de.thexxturboxx.blockhelper.api.BlockHelperModSupport;

public final class IntegrationRegistrar {

    private IntegrationRegistrar() {
        throw new UnsupportedOperationException();
    }

    public static void init() {
        BlockHelperModSupport.registerBlockProvider(new AdvMachinesIntegration());
        BlockHelperModSupport.registerBlockProvider(new BuildcraftIntegration());
        BlockHelperModSupport.registerItemStackFixer(new BuildcraftIntegration());
        BlockHelperModSupport.registerModFixer(new BuildcraftIntegration());
        BlockHelperModSupport.registerBlockProvider(new CChunksIntegration());
        BlockHelperModSupport.registerModFixer(new CChunksIntegration());
        BlockHelperModSupport.registerItemStackFixer(new FactorizationIntegration());
        BlockHelperModSupport.registerModFixer(new FactorizationIntegration());
        BlockHelperModSupport.registerBlockProvider(new ForestryIntegration());
        BlockHelperModSupport.registerBlockProvider(new ForgeIntegration());
        BlockHelperModSupport.registerBlockProvider(new FSIntegration());
        BlockHelperModSupport.registerItemStackFixer(new GregTechIntegration());
        BlockHelperModSupport.registerBlockProvider(new Ic2Integration());
        BlockHelperModSupport.registerItemStackFixer(new Ic2Integration());
        BlockHelperModSupport.registerBlockProvider(new MeteorsIntegration());
        BlockHelperModSupport.registerItemStackFixer(new MeteorsIntegration());
        BlockHelperModSupport.registerItemStackFixer(new PamIntegration());
        BlockHelperModSupport.registerItemStackFixer(new TEIntegration());
        BlockHelperModSupport.registerBlockProvider(new VanillaIntegration());
        BlockHelperModSupport.registerNameFixer(new VanillaIntegration());
    }

}
