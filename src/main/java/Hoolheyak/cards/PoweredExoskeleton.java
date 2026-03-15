package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.PlatedArmorPower;

public class PoweredExoskeleton extends BaseCard {
    public static final String ID = makeID("PoweredExoskeleton");

    private static final int COST = 1;
    private static final int MAGIC = 3; // 多重护甲层数
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public PoweredExoskeleton() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));

        int finalMagic = MAGIC;
        if (HoolheyakDifficultyHelper.currentDifficulty  == HoolheyakDifficultyHelper.DifficultyLevel.EASY)
            finalMagic = 4;
        
        setMagic(finalMagic, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new PlatedArmorPower(p, magicNumber), magicNumber));
    }
}