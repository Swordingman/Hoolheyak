package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.RedundantExperimentPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class RedundantExperiment extends BaseCard {
    public static final String ID = makeID("RedundantExperiment");

    private static final int COST = 2;
    private static final int MAGIC = 1; // 选 1 张
    private static final int UPGRADE_PLUS_MAGIC = 1; // 升级选 2 张

    public RedundantExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.RARE,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new AbstractGameAction() {
            private boolean opened = false;

            {
                this.actionType = ActionType.CARD_MANIPULATION;
                this.duration = Settings.ACTION_DUR_FAST;
            }

            @Override
            public void update() {
                if (!this.opened) {
                    if (p.hand.isEmpty()) {
                        this.isDone = true;
                        return;
                    }
                    AbstractDungeon.handCardSelectScreen.open(cardStrings.EXTENDED_DESCRIPTION[0], magicNumber, false, false);
                    this.opened = true;
                    return;
                }

                if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                    ArrayList<AbstractCard> selectedCards = new ArrayList<>();
                    for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                        selectedCards.add(c);
                        p.hand.addToTop(c); // 放回手牌
                        c.superFlash(); // 闪光提示
                    }

                    if (!selectedCards.isEmpty()) {
                        // 赋予监控能力
                        addToBot(new ApplyPowerAction(p, p, new RedundantExperimentPower(p, selectedCards)));
                    }

                    AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                    AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                    this.isDone = true;
                }
            }
        });
    }
}