package Hoolheyak.patches;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.character.HoolheyakPresetHelper;
import Hoolheyak.character.HoolheyakSkinHelper;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;

// 你可以把这个类放在专门存放 Patch 的包里
public class CharacterSelectPatches {

    // 1. 合并后的 Update 注入逻辑
    @SpirePatch(
            clz = CharacterSelectScreen.class,
            method = "update"
    )
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void Postfix(CharacterSelectScreen __instance) {
            // 皮肤 UI 更新
            HoolheyakSkinHelper.update();

            // 预设 UI 更新 (如果还没做，就像这样注释掉)
            HoolheyakPresetHelper.update();

            // 难度选择 UI 更新
            HoolheyakDifficultyHelper.update();
        }
    }

    // 2. 合并后的 Render 注入逻辑
    @SpirePatch(
            clz = CharacterSelectScreen.class,
            method = "render"
    )
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void Postfix(CharacterSelectScreen __instance, SpriteBatch sb) {
            // 注意：Render 的顺序决定了图层关系。
            // 后执行的 render 会画在先执行的上面（覆盖在上面）。

            // 绘制皮肤 UI
            HoolheyakSkinHelper.render(sb);

            // 绘制预设 UI (同理，没做完可以先注释)
            HoolheyakPresetHelper.render(sb);

            // 绘制难度选择 UI
            HoolheyakDifficultyHelper.render(sb);
        }
    }
}