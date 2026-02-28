package Hoolheyak.cards;

import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.WardrobePower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class Wardrobe extends BaseCard {
    public static final String ID = makeID("Wardrobe");

    private static final int COST = 2;

    public Wardrobe() {
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.POWER,
                CardRarity.UNCOMMON,
                CardTarget.SELF,
                COST
        ));
    }

    @Override
    public void upgrade() {
        if (!this.upgraded) {
            upgradeName();
            upgradeBaseCost(1);

            // 切换为升级后的卡牌描述
            this.rawDescription = cardStrings.UPGRADE_DESCRIPTION;
            initializeDescription();
        }
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 传递升级状态：如果这张牌升级了，衍生的牌就变成 0 费
        boolean isZeroCost = this.upgraded;

        // 防呆设计：如果玩家打出了未升级的，又打出了升级的，确保 0 费效果能够覆盖生效
        if (p.hasPower(WardrobePower.POWER_ID)) {
            WardrobePower power = (WardrobePower) p.getPower(WardrobePower.POWER_ID);
            if (isZeroCost) {
                power.makeZeroCost = true;
                power.updateDescription(); // 刷新能力文本
            }
        }

        // 给予 1 层能力，每层代表每次触发给 1 张牌
        addToBot(new ApplyPowerAction(p, p, new WardrobePower(p, 1, isZeroCost), 1));
    }
}