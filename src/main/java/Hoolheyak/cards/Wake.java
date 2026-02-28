package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Wake extends BaseCard {
    public static final String ID = makeID("Wake");

    private static final int COST = 1;
    private static final int BLOCK = 5;
    private static final int BLOCK_UPG = 2;
    private static final int MAGIC = 1;

    public Wake() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.COMMON,
                CardTarget.SELF,
                COST
        ));
        setBlock(BLOCK, BLOCK_UPG);
        setMagic(MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new GainBlockAction(p, p, this.block));
        addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber), this.magicNumber));

        if (!this.dontTriggerOnUseCard && AbstractDungeon.actionManager.cardsPlayedThisCombat.size() >= 2) {
            AbstractCard lastCard = AbstractDungeon.actionManager.cardsPlayedThisCombat
                    .get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 2);

            if (lastCard.type == CardType.SKILL) {
                // 触发连击，调用你写的 RepeatAction 复制本体并再次打出
                addToBot(new RepeatAction(this, m, 1));
            }
        }
    }

    @Override
    public void triggerOnGlowCheck() {
        this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
        if (AbstractDungeon.actionManager.cardsPlayedThisCombat.size() >= 1) {
            AbstractCard lastCard = AbstractDungeon.actionManager.cardsPlayedThisCombat
                    .get(AbstractDungeon.actionManager.cardsPlayedThisCombat.size() - 1);
            if (lastCard.type == CardType.SKILL) {
                this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy();
            }
        }
    }
}