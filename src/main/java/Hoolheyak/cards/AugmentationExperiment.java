package Hoolheyak.cards;

import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.MakeTempCardInHandAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import java.util.ArrayList;

public class AugmentationExperiment extends BaseCard implements IVariableCard {
    public static final String ID = makeID("AugmentationExperiment");
    private static final int COST = 1;
    private static final int DAMAGE = 5;
    private static final int UPGRADE_PLUS_DMG = 3;
    private static final int MAGIC = 1;
    private static final int ANALYSIS = 3;

    public AugmentationExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC);
        setCustomVar("ANALYSIS", ANALYSIS);
    }

    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 A：获得 1 层解析 (使用 magicNumber)
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber), this.magicNumber));
        }));

        // 选项 B：将一张本卡置入手牌，并获得 2 层解析
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            // 1. 生成副本并强制重置费用
            AbstractCard copy = this.makeStatEquivalentCopy();
            copy.cost = COST;                  // 强制设为基础费用 (1费)
            copy.costForTurn = COST;           // 重置本回合费用
            copy.isCostModified = false;       // 清除费用被修改的变色标记
            copy.isCostModifiedForTurn = false;
            copy.freeToPlayOnce = false;       // 关键防暴毙：防止原卡带有的“本回合打出免费”属性被复制

            // 2. 将修改后的纯净副本印入手牌
            addToBot(new MakeTempCardInHandAction(copy, 1));

            // 3. 获得解析层数
            addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, customVar("ANALYSIS")), customVar("ANALYSIS")));
        }));

        return choices;
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, this.damage, this.damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_LIGHT));

        if (!this.dontTriggerOnUseCard) {
            addToBot(new VariableAction(this, getVariableChoices(p, m, false), true));
        }
    }
}