package Hoolheyak.powers;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.cards.ContingencyPlan;
import Hoolheyak.cards.phases.QuincunxCard;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.phases.QuincunxPower;
import Hoolheyak.powers.phases.SextilePower;
import Hoolheyak.util.CustomTags;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class MeanderPower extends BasePower {
    public static final String POWER_ID = HoolheyakMod.makeID("Meander");

    public MeanderPower(AbstractCreature owner, int amount) {
        super(POWER_ID, PowerType.BUFF, false, owner, amount);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {

        if (card.hasTag(CustomTags.HOOLHEYAK_VARIABLE)) {
            return;
        }

        boolean isTriggerType = (card.type == AbstractCard.CardType.SKILL);
        if (this.owner.hasPower(QuincunxPower.POWER_ID)) {
            isTriggerType = (card.type == AbstractCard.CardType.ATTACK); // 变成攻击触发逶迤
        }

        if (isTriggerType) {
            this.amount++;
            checkAndTrigger();
        }
    }

    @Override
    public void stackPower(int stackAmount) {
        super.stackPower(stackAmount);
        checkAndTrigger();
    }

    private void checkAndTrigger() {
        int threshold = TriggerKeywordAction.getThreshold(this.owner, TriggerKeywordAction.KeywordType.MEANDER);
        int multiplier = 1;

        if (this.owner.hasPower(KukulkanLegacyPower.POWER_ID)) {
            int legacyStacks = this.owner.getPower(KukulkanLegacyPower.POWER_ID).amount;
            multiplier = (int) Math.pow(2, legacyStacks);
        }

        while (this.amount >= threshold) {
            this.amount -= threshold;
            this.flash();

            int x = this.owner.hasPower(AnalysisPower.POWER_ID) ? this.owner.getPower(AnalysisPower.POWER_ID).amount : 0;

            int finalDamage = 3 * multiplier;
            int finalLift = multiplier;

            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    if (AbstractDungeon.player instanceof Hoolheyak) {
                        ((Hoolheyak) AbstractDungeon.player).animHelper.playMeanderStart();
                    }
                    this.isDone = true;
                }
            });

            for (int i = 0; i < x; i++) {
                addToBot(new AbstractGameAction() {
                    @Override
                    public void update() {
                        AbstractMonster target = AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
                        if (target != null && !target.isDeadOrEscaped()) {
                            addToTop(new ApplyPowerAction(target, owner, new LiftPower(target, owner, finalLift), finalLift));
                            addToTop(new DamageAction(target, new DamageInfo(owner, finalDamage, DamageInfo.DamageType.THORNS), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                        }
                        this.isDone = true;
                    }
                });
            }

            addToBot(new AbstractGameAction() {
                @Override
                public void update() {
                    if (AbstractDungeon.player instanceof Hoolheyak) {
                        ((Hoolheyak) AbstractDungeon.player).animHelper.playMeanderEnd();
                    }
                    this.isDone = true;
                }
            });

            if (AbstractDungeon.player.hasPower(ForbiddenKnowledgeMeaPower.POWER_ID)) {
                ForbiddenKnowledgeMeaPower power = (ForbiddenKnowledgeMeaPower) AbstractDungeon.player.getPower(ForbiddenKnowledgeMeaPower.POWER_ID);
                power.onTriggerMeander();
            }

            ContingencyPlan.returnFromDiscard(false);
            InheritedMemoriesPower.onTriggerKeyword(this.owner);
            CovenantDexterityPower.trigger(this.owner);
        }

        updateDescription();
    }

    @Override
    public void updateDescription() {
        int threshold = TriggerKeywordAction.getThreshold(this.owner, TriggerKeywordAction.KeywordType.MEANDER);
        this.description = DESCRIPTIONS[0] + threshold + DESCRIPTIONS[1] + this.amount + "/" + threshold + DESCRIPTIONS[2];
    }
}