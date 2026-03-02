package Hoolheyak.cards;

import Hoolheyak.actions.LiteratureRetrievalAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class LiteratureRetrieval extends BaseCard {
    public static final String ID = makeID("LiteratureRetrieval");

    private static final int COST = 1;

    public LiteratureRetrieval() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE, // 发现牌不需要目标
                COST
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 调用我们刚刚写好的专属 Action
        addToBot(new LiteratureRetrievalAction(this.upgraded));
    }
}