package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import com.megacrit.cardcrawl.core.AbstractCreature;

public class KukulkanLegacyPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("KukulkanLegacy");

    public KukulkanLegacyPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
        // 初始化时立刻更新一次文本
        updateDescription();
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        this.fontScale = 8.0f;
        updateDescription();
    }

    @Override
    public void updateDescription() {
        int increasePerStack = 3;
        if (HoolheyakDifficultyHelper.currentDifficulty == HoolheyakDifficultyHelper.DifficultyLevel.EASY) {
            increasePerStack = 2;
        }

        int totalIncrease = increasePerStack * this.amount;

        int multiplier = (int) Math.pow(2, this.amount);

        this.description = DESCRIPTIONS[0] + totalIncrease + DESCRIPTIONS[1] + multiplier + DESCRIPTIONS[2];
    }
}