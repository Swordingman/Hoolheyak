package Hoolheyak.cards;

import Hoolheyak.actions.RepeatAction;
import Hoolheyak.actions.TriggerKeywordAction;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.util.CardStats;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

import static com.megacrit.cardcrawl.dungeons.AbstractDungeon.player;

public class Wingblade extends BaseCard {
    public static final String ID = makeID("Wingblade");

    private static final int COST = 1;
    private static final int DAMAGE = 9;
    private static final int UPGRADE_PLUS_DMG = 3;

    public Wingblade() {
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
    public void use(AbstractPlayer p, AbstractMonster m) {
        // 1. 造成基础伤害
        addToBot(new DamageAction(m, new DamageInfo(p, damage, damageTypeForTurn), AbstractGameAction.AttackEffect.SLASH_DIAGONAL));

        // 2. 检查博览触发条件，并确保当前卡牌不是被 RepeatAction 复制出来的临时卡（防止无限循环）
        if (!this.purgeOnUse && p.hasPower(EruditionPower.POWER_ID)) {
            int currentThreshold = TriggerKeywordAction.getThreshold(p, TriggerKeywordAction.KeywordType.ERUDITION);
            if (p.getPower(EruditionPower.POWER_ID).amount >= currentThreshold) {
                // 满足条件，将自身再次打出 1 次
                addToBot(new RepeatAction(this, m, 1));
            }
        }
    }

    @Override
    public void triggerOnGlowCheck() {
        this.glowColor = AbstractCard.BLUE_BORDER_GLOW_COLOR.cpy();
        if (player.hasPower(EruditionPower.POWER_ID)) {
            int currentThreshold = TriggerKeywordAction.getThreshold(player, TriggerKeywordAction.KeywordType.ERUDITION);
            if (player.getPower(EruditionPower.POWER_ID).amount >= currentThreshold) {
                this.glowColor = AbstractCard.GOLD_BORDER_GLOW_COLOR.cpy();
            }
        }
    }
}