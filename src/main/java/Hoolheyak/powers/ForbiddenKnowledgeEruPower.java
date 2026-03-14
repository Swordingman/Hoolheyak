package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;

// 处理“触发博览 -> 抽牌”的能力
public class ForbiddenKnowledgeEruPower extends BasePower {
    public static final String POWER_ID = "HoolheyakMod:ForbiddenKnowledgeEru";

    public ForbiddenKnowledgeEruPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    // 自定义方法：当博览被触发时调用
    public void onTriggerErudition() {
        this.flash();
        addToBot(new DrawCardAction(this.owner, this.amount));
    }

    @Override
    public void updateDescription() {
        this.description = DESCRIPTIONS[0] + this.amount + DESCRIPTIONS[1]; // 记得在本地化文件里改成：当你触发博览时，抽 #b 张牌。
    }
}

