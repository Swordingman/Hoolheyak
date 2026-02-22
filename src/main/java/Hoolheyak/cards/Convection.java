package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Convection extends BaseCard {
    public static final String ID = makeID("Convection");

    private static final int COST = 4;
    private static final int UPGRADED_COST = 3;
    private static final int DAMAGE = 18;
    private static final int UPGRADE_PLUS_DMG = 4;

    public Convection() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.ATTACK,
                CardRarity.UNCOMMON,
                CardTarget.ENEMY,
                COST
        ));
        setCostUpgrade(UPGRADED_COST);
        setDamage(DAMAGE, UPGRADE_PLUS_DMG);
    }

    // 实时监听状态刷新，执行减费逻辑
    @Override
    public void applyPowers() {
        int attackCount = 0;
        // 统计本回合内打出的攻击牌数量
        for (AbstractCard c : AbstractDungeon.actionManager.cardsPlayedThisTurn) {
            if (c.type == CardType.ATTACK) {
                attackCount++;
            }
        }

        int newCost = Math.max(this.baseCost - attackCount, 0); // 确保费用不会低于 0

        // 如果费用有变化，则将其应用为本回合的临时费用
        if (this.costForTurn != newCost) {
            this.costForTurn = newCost;
            this.isCostModifiedForTurn = true;
        }

        super.applyPowers();
    }

    @Override
    public void triggerWhenDrawn() {
        super.triggerWhenDrawn();
        applyPowers(); // 刚抽到时也立即核算一次费用
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.BLUNT_HEAVY));
    }
}