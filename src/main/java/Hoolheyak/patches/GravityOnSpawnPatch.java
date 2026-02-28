package Hoolheyak.patches; // 请替换为你实际的包名

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

// 这里请替换为你实际用到重力能力和角色的 Import
import Hoolheyak.powers.GravityPower;
import Hoolheyak.character.Hoolheyak;

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

@SpirePatch(
        clz = AbstractCreature.class, // 【关键修复】：改为拦截父类 AbstractCreature
        method = "showHealthBar"
)
public class GravityOnSpawnPatch {

    @SpirePostfixPatch
    public static void Postfix(AbstractCreature __instance) {
        // 1. 确保亮血条的是怪物，而不是玩家自己
        if (__instance instanceof AbstractMonster) {

            // 2. 确保当前角色是你的角色
            if (AbstractDungeon.player != null && AbstractDungeon.player.chosenClass == Hoolheyak.Meta.HOOLHEYAK) {

                // 3. 确保怪物活着
                if (!__instance.isDeadOrEscaped()) {
                    // 使用 addToTop 强行插队挂上重力
                    AbstractDungeon.actionManager.addToTop(
                            new ApplyPowerAction(__instance, AbstractDungeon.player, new GravityPower(__instance))
                    );
                }
            }
        }
    }
}