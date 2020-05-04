package me.levidevs.lunarclientspoofer;

import cpw.mods.fml.relauncher.FMLCorePlugin;
import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import me.levidevs.lunarclientspoofer.transformer.NetHandlerTransformer;

/**
 * @author Levi Taylor
 * @since 04/05/2020
 * Represents the loader.
 */
@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LunarClientSpooferLoader extends FMLCorePlugin {

    @Override
    public String[] getASMTransformerClass() {
        // Return the original transformers too
        String[] original = super.getASMTransformerClass();
        String[] result = new String[original.length + 1];

        System.arraycopy(original, 0, result, 0, original.length);
        result[result.length - 1] = NetHandlerTransformer.class.getName();

        return result;
    }

    @Override
    public String getModContainerClass() {
        return LunarClientSpoofer.class.getName();
    }

}
