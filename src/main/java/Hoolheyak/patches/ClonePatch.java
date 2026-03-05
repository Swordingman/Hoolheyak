package Hoolheyak.patches;

import Hoolheyak.relics.PureWaterSpriteAssist;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.rooms.AbstractRoom;

public class ClonePatch {

    // 转移到 AbstractRoom 的 update，绝对安全
    @SpirePatch(clz = AbstractRoom.class, method = "update")
    public static class UpdatePatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractRoom __instance) {
            if (__instance.phase == AbstractRoom.RoomPhase.COMBAT && PureWaterSpriteAssist.activeManifold != null) {
                PureWaterSpriteAssist.activeManifold.update();
            }
        }
    }

    // 转移到 AbstractRoom 的 render，并明确指定参数类型防呆
    @SpirePatch(clz = AbstractRoom.class, method = "render", paramtypez = {SpriteBatch.class})
    public static class RenderPatch {
        @SpirePostfixPatch
        public static void Postfix(AbstractRoom __instance, SpriteBatch sb) {
            if (__instance.phase == AbstractRoom.RoomPhase.COMBAT && PureWaterSpriteAssist.activeManifold != null) {
                PureWaterSpriteAssist.activeManifold.render(sb);
            }
        }
    }
}