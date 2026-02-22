package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class InheritedMemoriesPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("InheritedMemoriesPower");

    public InheritedMemoriesPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 提供给“逶迤”和“博览”调用的静态方法
    public static void onTriggerKeyword(AbstractCreature player) {
        if (player.hasPower(POWER_ID)) {
            int amt = player.getPower(POWER_ID).amount;
            player.getPower(POWER_ID).flash();
            // 触发时获得解析
            com.megacrit.cardcrawl.dungeons.AbstractDungeon.actionManager.addToBottom(
                    new ApplyPowerAction(player, player, new AnalysisPower(player, amt), amt)
            );
        }
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1];
    }
}