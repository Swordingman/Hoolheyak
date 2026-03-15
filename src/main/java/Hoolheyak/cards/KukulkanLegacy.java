package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.powers.KukulkanLegacyPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class KukulkanLegacy extends BaseCard {
    public static final String ID = makeID("KukulkanLegacy");

    private static final int COST = 3;
    private static final int UPGRADED_COST = 2;

    public KukulkanLegacy() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.RARE,
                CardTarget.SELF,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);

        int magic = 3;
        if (HoolheyakDifficultyHelper.currentDifficulty == HoolheyakDifficultyHelper.DifficultyLevel.EASY) {
            magic = 2;
        }

        setMagic(magic);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new KukulkanLegacyPower(p, 1), 1));
    }
}