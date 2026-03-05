package Hoolheyak.patches;

import Hoolheyak.character.FriendlyManifold;
import Hoolheyak.relics.PureWaterSpriteAssist;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;

public class PlayerDamageInterceptPatch {

    @SpirePatch(clz = AbstractPlayer.class, method = "damage")
    public static class RedirectDamagePatch {
        @SpirePrefixPatch
        public static SpireReturn<Void> Prefix(AbstractPlayer __instance, DamageInfo info) {
            // 如果伤害大于 0，且分身存在且存活
            if (info.output > 0 && PureWaterSpriteAssist.activeManifold != null && !PureWaterSpriteAssist.activeManifold.isDead) {

                FriendlyManifold manifold = PureWaterSpriteAssist.activeManifold;

                // 1. 让分身强行承受全部伤害（即使超出它的血量上限，它的 damage 方法里会处理死亡逻辑）
                manifold.damage(new DamageInfo(info.owner, info.output, info.type));

                // 2. 【补充遗物变灰逻辑】如果分身在这次攻击中死了，找到玩家身上的那个遗物并让它变灰
                if (manifold.isDead && __instance.hasRelic(PureWaterSpriteAssist.ID)) {
                    __instance.getRelic(PureWaterSpriteAssist.ID).grayscale = true;
                }

                // 3. 核心：直接 Return！强制中断玩家的受击代码
                // 怪物打出来的 999 伤害就像打在棉花上，玩家不掉血、不掉格挡、连受击动画都不播！
                return SpireReturn.Return();
            }

            // 如果分身不在了（或已死），玩家正常挨打
            return SpireReturn.Continue();
        }
    }
}