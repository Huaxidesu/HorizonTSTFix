package dev.dsmnh.horizon.tstfix;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * 空矿辞补注册模组。
 * <p>
 * GTNH 2.9.0 中，格雷科技在 preInit 阶段通过 {@code GTModHandler.getModItem} 注册
 * {@code craftingBlastFurnace}，此时 Et Futurum 的高炉方块尚未进入注册表，取到 null，
 * 矿辞条目因此「存在但为空」。依赖该矿辞的合成表全部无法匹配。
 * <p>
 * 更严重的后果出现在 Twist Space Technology 一侧：该模组在 LoadComplete 阶段遍历
 * Avaritia 的极限合成表并转换为格雷配方，空矿辞会被原样交给
 * {@code GTRecipeBuilder.itemInputs}，格雷在开启 {@code crashOnNullRecipeInput} 时
 * 直接抛出 {@code IllegalArgumentException}，服务端启动中断。
 * <p>
 * 本模组在 {@link FMLPostInitializationEvent} 阶段处理：此时全部模组的 preInit 与 init
 * 均已结束，方块注册表完整；该阶段又早于任何模组的 LoadComplete，因此补注册结果对
 * 极限合成表转换可见。处理过程不依赖模组加载顺序。
 */
@Mod(
    modid = Tags.MODID,
    name = Tags.MODNAME,
    version = Tags.VERSION,
    dependencies = "after:etfuturum;after:gregtech",
    acceptedMinecraftVersions = "[1.7.10]")
public class HorizonTSTFix {

    public static final Logger LOG = LogManager.getLogger(Tags.MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.load(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        OreDictRepair.run();
    }
}
