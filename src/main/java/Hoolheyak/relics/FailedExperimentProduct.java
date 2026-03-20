package Hoolheyak.relics;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.HoolheyakMod;
import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.helpers.PowerTip;

public class FailedExperimentProduct extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("FailedExperimentProduct");

    public FailedExperimentProduct() {
        // 这里替换为你实际的贴图加载逻辑
        super(ID, "FailedExperimentProduct", RelicTier.SPECIAL, LandingSound.CLINK);
        this.counter = 1;
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        this.initializeTips();
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0] + this.counter + DESCRIPTIONS[1];
    }

    // 自定义的增加计数方法
    public void incrementCounter() {
        if (this.counter < 0) {
            this.counter = 0;
        }
        this.counter++;

        // 刷新遗物的描述，使其在UI上实时更新次数
        this.description = getUpdatedDescription();
        this.tips.clear();
        this.tips.add(new PowerTip(this.name, this.description));
        this.initializeTips();
    }
}