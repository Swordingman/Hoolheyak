package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.powers.WeightlessPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import Hoolheyak.util.PhaseManager;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import java.util.ArrayList;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.ui.panels.EnergyPanel;

import java.util.ArrayList;

public class MutagenesisExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("MutagenesisExperiment");

    // 调整为1费，打15
    private static final int COST = 1;
    private static final int DAMAGE = 15;
    private static final int UPGRADE_PLUS_DMG = 5;

    private static final UIStrings energyNoticeUI = CardCrawlGame.languagePack.getUIString("Hoolheyak:EnergyNoticeUI");
    public static final String[] TEXT = energyNoticeUI.TEXT;

    public MutagenesisExperiment() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.COMMON,
                CardTarget.ENEMY,
                COST
                ));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 变量1：随机触发一个相位 (不耗费额外能量)
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            AbstractPower randomPhase = PhaseManager.getRandomPhase(p);
            PhaseManager.applyPhase(randomPhase);
        }));

        // 变量2：消耗一点能量，随机触发一个吉相相位
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            // 将逻辑包装在一个 Action 中，确保在战斗队列中按顺序结算
            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    // 判断玩家当前能量是否足够
                    if (EnergyPanel.getCurrentEnergy() >= 1) {
                        // 扣除1点能量
                        p.energy.use(1);
                        // 获取并施加吉相
                        AbstractPower goodPhase = PhaseManager.getRandomGoodPhase(p);
                        PhaseManager.applyPhase(goodPhase);
                    } else {
                        // 如果能量不足（比如通过某些手段0费打出这张卡且没费用了），可以给个提示
                        AbstractDungeon.effectList.add(new com.megacrit.cardcrawl.vfx.ThoughtBubble(
                                p.dialogX, p.dialogY, 3.0F,
                                TEXT[0],
                                true));
                    }
                    this.isDone = true;
                }
            });
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 造成伤害
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.FIRE));
        // 2. 触发变量选择
        addToBot(new VariableAction(this, getVariableChoices(p, m), false));
    }
}