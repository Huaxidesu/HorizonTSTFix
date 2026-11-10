# HorizonTSTFix

补注册 GTNH 2.9.0 中因模组加载顺序缺失的矿辞条目，修复依赖这些矿辞的合成表，以及 Twist Space Technology 在启动阶段的崩溃。

## 背景

GTNH 2.9.0 的格雷科技在 `preInit` 阶段注册 `craftingBlastFurnace` 矿辞：

```java
GTOreDictUnificator.registerOre(
    OreDictNames.craftingBlastFurnace,
    GTModHandler.getModItem(EtFuturumRequiem.ID, "blast_furnace", 1));
```

此时 Et Futurum 的高炉方块可能尚未进入注册表，`getModItem` 返回 null，矿辞条目因此**存在但为空**。日志中对应：

```
getModItem call: object "blast_furnace" with mod id "etfuturum" has returned null
because the item was not found in the game registry
```

由此产生两类后果：

| 后果 | 说明 |
|---|---|
| 合成表失效 | 以 `craftingBlastFurnace` 为原料的配方（小型燃煤锅炉等）全部无法匹配 |
| 服务端无法启动 | Twist Space Technology 在 `LoadComplete` 阶段把 Avaritia 极限合成表转换为格雷配方，空矿辞被原样交给 `GTRecipeBuilder.itemInputs`，格雷在 `crashOnNullRecipeInput=true` 时抛出 `IllegalArgumentException`，启动中断 |

是否触发取决于模组加载顺序：加入或移除任意模组都可能改变无关模组之间的先后关系。

## 处理方式

在 `FMLPostInitializationEvent` 阶段检查并补注册配置中列出的矿辞。

该时点的两个性质决定了它不依赖加载顺序：

- 全部模组的 `preInit` 与 `init` 均已结束，方块注册表完整；
- 早于任何模组的 `LoadComplete`，补注册结果对极限合成表转换可见。

补注册只在目标矿辞当前为空时执行，已有内容的矿辞不会被改动。

## 配置

首次启动后生成 `config/HorizonTSTFix.cfg`：

```ini
general {
    # 是否在启动时列出全部空矿辞条目
    B:logEmptyOreDict=true

    # 需要补注册的矿辞，格式：矿辞名=模组ID:注册名:metadata
    S:repairs <
        craftingBlastFurnace=etfuturum:blast_furnace:0
     >
}
```

`logEmptyOreDict` 会把当前所有空矿辞逐条打印到日志（最多 64 条），用于确认加载顺序问题的影响范围。发现其它空矿辞时，按同样格式追加到 `repairs` 即可。

## 安装

| 端 | 是否必需 | 说明 |
|---|---|---|
| 服务端 | 是 | 修复合成表与服务端启动 |
| 客户端 | 建议 | NEI 的配方显示使用客户端自身的矿辞表，仅装服务端时客户端仍看不到相关配方 |

放入 `mods/` 即可，无前置模组。

## 构建

```bash
./gradlew build
```

产物位于 `build/libs/`。构建使用 GTNH 构建脚本，需要 JDK 17 以上，缺失的 JDK 由 Gradle 自动下载。

## 许可

[MIT](LICENSE)
