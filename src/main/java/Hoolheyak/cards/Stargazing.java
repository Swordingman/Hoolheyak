package Hoolheyak.cards;

import Hoolheyak.actions.StargazingAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.EmptyDeckShuffleAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Stargazing extends BaseCard implements IVariableCard {
    public static final String ID = makeID("Stargazing");

    private static final int COST = 0;
    private static final int MAGIC = 3; // 基础看3张
    private static final int UPGRADE_PLUS_MAGIC = 2; // 升级看5张

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
        // 如果抽牌堆为空但弃牌堆有牌，先洗牌
        if (p.drawPile.isEmpty() && !p.discardPile.isEmpty()) {
            addToBot(new EmptyDeckShuffleAction());
        }

        // 呼叫观星 Action：看 magicNumber 张，最多选 3 张
        addToBot(new StargazingAction(this, this.magicNumber, this.magicNumber));
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        return new ArrayList<>();
    }

    @Override
    public boolean canBeAutoTriggered() {
        return false; // 严禁被自动触发
    }
}