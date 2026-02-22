package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.InheritedMemoriesPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class InheritedMemories extends BaseCard {
    public static final String ID = makeID("InheritedMemories");

    private static final int COST = 1;

    public InheritedMemories() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
        setInnate(false, true); // 升级后获得固有
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new InheritedMemoriesPower(p, 1), 1));
    }
}