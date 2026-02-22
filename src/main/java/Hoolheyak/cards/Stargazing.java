package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.utility.ScryAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Stargazing extends BaseCard {
    public static final String ID = makeID("Stargazing");

    private static final int COST = 0;
    private static final int MAGIC = 3; // 预见 3
    private static final int UPGRADE_PLUS_MAGIC = 2; // 升级预见 5

    public Stargazing() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.UNCOMMON,
                CardTarget.NONE,
                COST
        ));
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 第一阶段：预见
        addToBot(new ScryAction(magicNumber));

        // 第二阶段：变量选择
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 α：弃掉选中的牌
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(createCardSelectAction(cardStrings.EXTENDED_DESCRIPTION[2], (selected) -> {
                for (AbstractCard c : selected) {
                    p.hand.moveToDiscardPile(c);
                    c.triggerOnManualDiscard();
                }
            }));
        }));

        // 选项 β：将选中的牌置于牌库底
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(createCardSelectAction(cardStrings.EXTENDED_DESCRIPTION[3], (selected) -> {
                for (AbstractCard c : selected) {
                    p.hand.moveToBottomOfDeck(c);
                }
            }));
        }));

        addToBot(new VariableAction(this, choices));
    }

    // 封装的选牌处理工具
    private AbstractGameAction createCardSelectAction(String msg, Consumer<ArrayList<AbstractCard>> callback) {
        return new AbstractGameAction() {
            private boolean opened = false;

            {
                this.actionType = ActionType.CARD_MANIPULATION;
                this.duration = Settings.ACTION_DUR_FAST;
            }

            @Override
            public void update() {
                if (!this.opened) {
                    if (AbstractDungeon.player.hand.isEmpty()) {
                        this.isDone = true;
                        return;
                    }
                    // 打开手牌选择界面，最多选3张，可以少选或不选 (anyNumber=true)
                    AbstractDungeon.handCardSelectScreen.open(msg, 3, true, true);
                    this.opened = true;
                    return;
                }
                if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
                    ArrayList<AbstractCard> selected = new ArrayList<>();
                    for (AbstractCard c : AbstractDungeon.handCardSelectScreen.selectedCards.group) {
                        selected.add(c);
                    }
                    callback.accept(selected);

                    AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
                    AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
                    this.isDone = true;
                }
            }
        };
    }
}