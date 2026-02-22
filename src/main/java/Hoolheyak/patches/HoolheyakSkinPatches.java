package Hoolheyak.patches;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import Hoolheyak.character.HoolheyakSkinHelper; // 1. 引入 Helper

public class HoolheyakSkinPatches {

    // 1. 注入更新逻辑
    @SpirePatch(
            clz = CharacterSelectScreen.class,
            method = "update"
    )
    public static class UpdateSkinPatch {
        @SpirePostfixPatch
        public static void Postfix(CharacterSelectScreen __instance) {
            // 2. 直接调用 Helper
            HoolheyakSkinHelper.update();
        }
    }

    // 2. 注入渲染逻辑
    @SpirePatch(
            clz = CharacterSelectScreen.class,
            method = "render"
    )
    public static class RenderSkinPatch {
        @SpirePostfixPatch
        public static void Postfix(CharacterSelectScreen __instance, SpriteBatch sb) {
            // 3. 直接调用 Helper
            HoolheyakSkinHelper.render(sb);
        }
    }
}