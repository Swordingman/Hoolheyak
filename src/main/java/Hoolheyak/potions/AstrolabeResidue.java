package Hoolheyak.potions;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.cards.*; // 假设你的相位卡都在这个包里
import Hoolheyak.cards.phases.*;
import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

import java.util.ArrayList;

public class AstrolabeResidue extends BasePotion {
    public static final String POTION_ID = HoolheyakMod.makeID("AstrolabeResidue");

    public AstrolabeResidue() {
        super(
                POTION_ID,
                1, // 基础效力为 1
                PotionRarity.RARE, // 稀有药水
                PotionSize.SPHERE, // 药水瓶的形状，比如圆球形
                Color.GOLD,
                null,
                null
        );
        this.isThrown = false;       // 自己喝的，不需要丢
        this.targetRequired = false; // 不需要目标
    }

    @Override
    public String getDescription() {
        // 药水描述："选择并触发 #bX 个 #y相位 。"
        return DESCRIPTIONS[0] + this.potency + DESCRIPTIONS[1];
    }

    @Override
    public void use(AbstractCreature target) {
        // 根据药水的效力，将选择界面的 Action 放入队列
        // 如果有树皮遗物，potency 会变成 2，就会连续弹出两次选择界面
        for (int i = 0; i < this.potency; i++) {
            ArrayList<AbstractCard> phaseChoices = new ArrayList<>();

            // 塞入所有的相位卡
            phaseChoices.add(new ConjunctionCard());
            phaseChoices.add(new QuincunxCard());
            phaseChoices.add(new SextileCard());
            phaseChoices.add(new TrineCard());
            phaseChoices.add(new SquareCard());
            phaseChoices.add(new OppositionCard());

            // 呼叫 6 选 1 界面
            addToBot(new ChooseOneAction(phaseChoices));
        }
    }
}