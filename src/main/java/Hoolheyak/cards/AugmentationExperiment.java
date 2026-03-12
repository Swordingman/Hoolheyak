package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction; // 确保引入你的 RepeatAction
import Hoolheyak.actions.VariableAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.AnalysisPower;
import Hoolheyak.util.CardStats;
import Hoolheyak.util.IVariableCard;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
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
    private static final int UPGRADE_PLUS_MAGIC = 1;

    public AugmentationExperiment() {
        super(ID, new CardStats(Hoolheyak.Meta.CARD_COLOR, CardType.ATTACK, CardRarity.UNCOMMON, CardTarget.ENEMY, COST));
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
        setMagic(MAGIC, UPGRADE_PLUS_MAGIC);
    }

    // 2. 将选项逻辑剥离出来
    @Override
    public ArrayList<VariableAction.VariableChoice> getVariableChoices(AbstractPlayer p, AbstractMonster m, boolean isAutoTriggered) {
        ArrayList<VariableAction.VariableChoice> choices = new ArrayList<>();

        // 选项 A：使用自定义的 RepeatAction 额外打出
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[0], () -> {
            addToBot(new RepeatAction(this, m, this.magicNumber));
        }));

        // 选项 B：获得解析
        choices.add(new VariableAction.VariableChoice(cardStrings.EXTENDED_DESCRIPTION[1], () -> {
            addToBot(new ApplyPowerAction(p, p, new AnalysisPower(p, this.magicNumber), this.magicNumber));
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