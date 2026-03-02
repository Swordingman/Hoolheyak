package Hoolheyak.patches;

import Hoolheyak.relics.PureWaterSpriteAssist;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

@SpirePatch(clz = AbstractPlayer.class, method = "render")
public class CloneRenderPatch {
    @SpirePostfixPatch
    public static void Postfix(AbstractPlayer __instance, SpriteBatch sb) {
        // 如果在战斗中，且玩家有这个遗物
        if (AbstractDungeon.player != null && AbstractDungeon.getCurrRoom() != null && AbstractDungeon.getCurrRoom().phase == com.megacrit.cardcrawl.rooms.AbstractRoom.RoomPhase.COMBAT) {
            if (AbstractDungeon.player.hasRelic(PureWaterSpriteAssist.ID)) {
                PureWaterSpriteAssist relic = (PureWaterSpriteAssist) AbstractDungeon.player.getRelic(PureWaterSpriteAssist.ID);
                if (relic.manifold != null) {
                    relic.manifold.render(sb); // 把水分身画出来！
                }
            }
        }
    }
}