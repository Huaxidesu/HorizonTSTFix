package dev.dsmnh.horizon.tstfix;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * 空矿辞的检查与补注册。
 * <p>
 * 补注册的对象由配置提供，逐条按 {@code 矿辞名=模组ID:注册名:metadata} 解析。
 * 解析成功后先确认矿辞为空，再从游戏注册表中取出对应物品注册进去；
 * 任一环节失败只记录日志并跳过该条，不影响其余条目。
 */
public final class OreDictRepair {

    /** 单次启动最多逐条打印的空矿辞数量，避免日志过长。 */
    private static final int LOG_LIMIT = 64;

    private OreDictRepair() {}

    public static void run() {
        if (ModConfig.logEmptyOreDict()) {
            logEmptyEntries();
        }

        int repaired = 0;
        for (String entry : ModConfig.repairs()) {
            if (repairOne(entry)) {
                repaired++;
            }
        }

        HorizonTSTFix.LOG.info("空矿辞补注册完成，本次补注册 {} 条。", repaired);
    }

    /**
     * 列出当前全部空矿辞，便于确认加载顺序问题的影响范围。
     */
    private static void logEmptyEntries() {
        List<String> empty = new ArrayList<>();
        for (String name : OreDictionary.getOreNames()) {
            if (OreDictionary.getOres(name)
                .isEmpty()) {
                empty.add(name);
            }
        }

        HorizonTSTFix.LOG.info("当前共有 {} 条空矿辞。", empty.size());
        int shown = Math.min(empty.size(), LOG_LIMIT);
        for (int i = 0; i < shown; i++) {
            HorizonTSTFix.LOG.warn("空矿辞：{}", empty.get(i));
        }
        if (empty.size() > shown) {
            HorizonTSTFix.LOG.warn("其余 {} 条空矿辞未逐条列出。", empty.size() - shown);
        }
    }

    /**
     * 处理单条补注册配置。
     *
     * @param entry 形如 {@code craftingBlastFurnace=etfuturum:blast_furnace:0} 的配置行
     * @return 本次是否实际写入矿辞
     */
    private static boolean repairOne(String entry) {
        String line = entry == null ? "" : entry.trim();
        if (line.isEmpty()) {
            return false;
        }

        int separator = line.indexOf('=');
        if (separator <= 0 || separator == line.length() - 1) {
            HorizonTSTFix.LOG.error("补注册配置格式错误，已跳过：{}", line);
            return false;
        }

        String oreName = line.substring(0, separator)
            .trim();
        String target = line.substring(separator + 1)
            .trim();
        String[] parts = target.split(":");
        if (parts.length < 2) {
            HorizonTSTFix.LOG.error("补注册目标格式错误，已跳过：{}", line);
            return false;
        }

        String modId = parts[0].trim();
        String registryName = parts[1].trim();
        int metadata = 0;
        if (parts.length >= 3) {
            try {
                metadata = Integer.parseInt(parts[2].trim());
            } catch (NumberFormatException e) {
                HorizonTSTFix.LOG.error("补注册目标 metadata 不是整数，按 0 处理：{}", line);
            }
        }

        if (!OreDictionary.getOres(oreName)
            .isEmpty()) {
            HorizonTSTFix.LOG.info("矿辞 {} 已有内容，无需补注册。", oreName);
            return false;
        }

        ItemStack stack = findStack(modId, registryName, metadata);
        if (stack == null) {
            HorizonTSTFix.LOG.error("注册表中找不到 {}:{}，矿辞 {} 仍为空。", modId, registryName, oreName);
            return false;
        }

        OreDictionary.registerOre(oreName, stack);
        HorizonTSTFix.LOG.info(
            "已补注册矿辞 {} -> {}:{}:{}，现有 {} 个物品。",
            oreName,
            modId,
            registryName,
            metadata,
            OreDictionary.getOres(oreName)
                .size());
        return true;
    }

    /**
     * 从游戏注册表取出物品。优先按物品查找，找不到时退回方块查找，
     * 以覆盖只注册了方块、未单独注册物品的情形。
     */
    private static ItemStack findStack(String modId, String registryName, int metadata) {
        ItemStack stack = GameRegistry.findItemStack(modId, registryName, 1);
        if (stack != null) {
            if (metadata != 0) {
                stack.setItemDamage(metadata);
            }
            return stack;
        }

        Block block = Block.getBlockFromName(modId + ":" + registryName);
        if (block != null) {
            return new ItemStack(block, 1, metadata);
        }
        return null;
    }
}
