package Hoolheyak.patches; // 请替换为你实际的包名

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

// 这里请替换为你实际用到重力能力和角色的 Import
import Hoolheyak.powers.GravityPower;
import Hoolheyak.character.Hoolheyak;

// 告诉编译器：我们要往 AbstractMonster 类的 useUniversalPreBattleAction 方法注入代码
@SpirePatch(
        clz = AbstractMonster.class,
        method = "useUniversalPreBattleAction"
)
public class GravityOnSpawnPatch {

    // Postfix 意味着我们的代码会在原版方法执行完毕后紧接着执行
    @SpirePostfixPatch
    public static void Postfix(AbstractMonster __instance) {
        // 1. 确保当前玩家是你的角色，千万别把重力污染到原版角色的存档里
        // 注意：Hoolheyak.Enums.PLAYER_CLASS 只是我的假设，请换成你注册角色时用的实际 Enum
        if (AbstractDungeon.player != null && AbstractDungeon.player.chosenClass == Hoolheyak.Meta.HOOLHEYAK) {

            // 2. 只要怪物活着，不管是开局生成的还是中途掉下来的，统统吃重力！
            if (!__instance.isDeadOrEscaped()) {
                AbstractDungeon.actionManager.addToBottom(
                        new ApplyPowerAction(__instance, AbstractDungeon.player, new GravityPower(__instance))
                );
            }

        }
    }
}