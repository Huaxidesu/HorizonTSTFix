package dev.dsmnh.horizon.tstfix;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

/**
 * 模组配置。
 * <p>
 * 配置项集中在 {@code general} 分类下：
 * <ul>
 * <li>{@code logEmptyOreDict}：是否在启动时列出全部空矿辞，用于排查同类问题。</li>
 * <li>{@code repairs}：需要补注册的矿辞列表，格式为 {@code 矿辞名=模组ID:注册名:metadata}。</li>
 * </ul>
 * 补注册仅在目标矿辞当前为空时执行，已存在内容的矿辞不会被改动。
 */
public final class ModConfig {

    private static final String CATEGORY = "general";

    private static final String[] DEFAULT_REPAIRS = new String[] { "craftingBlastFurnace=etfuturum:blast_furnace:0" };

    private static boolean logEmptyOreDict = true;
    private static String[] repairs = DEFAULT_REPAIRS;

    private ModConfig() {}

    public static void load(File file) {
        Configuration configuration = new Configuration(file);
        configuration.load();

        logEmptyOreDict = configuration
            .getBoolean("logEmptyOreDict", CATEGORY, true, "是否在启动时列出全部空矿辞条目。空矿辞通常意味着某个模组在注册阶段取到了尚未注册的物品。");

        repairs = configuration.getStringList(
            "repairs",
            CATEGORY,
            DEFAULT_REPAIRS,
            "需要补注册的矿辞，格式：矿辞名=模组ID:注册名:metadata。仅在该矿辞为空时注册，已有内容时不改动。");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    public static boolean logEmptyOreDict() {
        return logEmptyOreDict;
    }

    public static String[] repairs() {
        return repairs;
    }
}
