package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.cards.phases.*; // 引入你写好的所有状态牌
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.watcher.ChooseOneAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import java.util.ArrayList;

public class Transit extends BaseCard {
    public static final String ID = makeID("Transit");

    private static final int COST = 2;
    private static final int UPGRADED_COST = 1;

    public Transit() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.NONE,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        setExhaust(true);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        ArrayList<AbstractCard> phaseChoices = new ArrayList<>();

        // 直接复用我们写好的状态牌！
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