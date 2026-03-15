package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class ColdEye extends BaseCard {
    public static final String ID = makeID("ColdEye");

    private static final int COST = 1;
    private static final int BLOCK = 12;
    private static final int UPGRADE_PLUS_BLOCK = 3;
    private static final int MAGIC = 3; // 解析
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public ColdEye() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));

        int finalMagic = MAGIC;
        int finalBlock = BLOCK;
        if (HoolheyakDifficultyHelper.currentDifficulty == HoolheyakDifficultyHelper.DifficultyLevel.HARD) {
            finalMagic = 2;
            finalBlock = 11;
        }
        setBlock(finalBlock, UPGRADE_PLUS_BLOCK);
        setMagic(finalMagic, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        boolean hasDealtDamage = false;

        for (AbstractCard c : AbstractDungeon.actionManager.cardsPlayedThisTurn) {
            if (c.type == CardType.ATTACK) {
                hasDealtDamage = true;
                break;
            }
        }

        if (hasDealtDamage) {
            // 如果受到过伤害，获得解析
            addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, magicNumber), magicNumber));
        } else {
            // 如果没有，获得格挡
            addToBot(new GainBlockAction(p, p, block));
        }
    }
}