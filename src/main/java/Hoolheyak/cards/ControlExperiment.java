package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.WeightlessPower; // 或者是 GravityPower，取决于你代码里的名字
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.*;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import java.util.ArrayList;

public class ControlExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("ControlExperiment");
    private static final int COST = 1;
    // 打9，升级后打12
    private static final int DAMAGE = 9;
    private static final int UPGRADE_PLUS_DMG = 3;

    public ControlExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        // 不再需要虚弱/易伤，所以把 MagicNumber 去掉了，让代码更干净
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 变量1：将一张随机相位置入你的抽牌堆，并抽2张牌
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            AbstractCard phaseCard = PhaseManager.getRandomPhaseCard();
            addToBot(new MakeTempCardInDrawPileAction(phaseCard, 1, true, true));
            addToBot(new DrawCardAction(p, 2));
        }));

        // 变量2：将一张随机相位置入你的弃牌堆，并把你的弃牌堆洗入你的抽牌堆
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            AbstractCard phaseCard = PhaseManager.getRandomPhaseCard();

            // 1. 先把卡放进弃牌堆
            addToBot(new MakeTempCardInDiscardAction(phaseCard, 1));

            // 2. 手写一个动作，把弃牌堆洗入抽牌堆
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    int discardSize = p.discardPile.size();
                    if (discardSize > 0) {
                        for (int i = 0; i < discardSize; i++) {
                            // 将弃牌堆的卡移入抽牌堆，true 表示随机位置（即洗牌效果）
                            p.discardPile.moveToDeck(p.discardPile.getBottomCard(), true);
                        }
                    }
                    this.isDone = true;
                }
            });
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
        // 注意看这里，如果你不想让它被当成 AutoTrigger 打出，传 false，或者保留你的 true 都可以
        addToBot(new VariableAction(this, getVariableChoices(p, m), true));
    }
}