package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Deepthink extends BaseCard {
    public static final String ID = makeID("Deepthink");

    private static final int COST = 2;
    private static final int ANALYSIS = 2;
    private static final int ANALYSIS_UPG = 2;
    private static final int MAGIC = 2;

    public Deepthink() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));

        int finalMagic = MAGIC;
        if (HoolheyakDifficultyHelper.currentDifficulty  == HoolheyakDifficultyHelper.DifficultyLevel.HARD)
            finalMagic = 1;
        if (HoolheyakDifficultyHelper.currentDifficulty  == HoolheyakDifficultyHelper.DifficultyLevel.EASY)
            finalMagic = 3;

        setMagic(finalMagic);
        setCustomVar("ANALYSIS", ANALYSIS, ANALYSIS_UPG);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, customVar("ANALYSIS")), customVar("ANALYSIS")));
        addToBot(new ApplyPowerAction(p, p, new EruditionPower(p, magicNumber), magicNumber));
        addToBot(new ApplyPowerAction(p, p, new MeanderPower(p, magicNumber), magicNumber));
    }
}