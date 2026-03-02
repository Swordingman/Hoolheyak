package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.actions.RepeatAction; // 替换为你的实际包路径
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

public class TimeMuseum extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("TimeMuseum");

    private boolean attackPlayed = false;
    private boolean skillPlayed = false;
    private boolean powerPlayed = false;

    public TimeMuseum() {
        super(ID, "TimeMuseum", Hoolheyak.Meta.CARD_COLOR, RelicTier.RARE, LandingSound.MAGICAL);
    }

    @Override
    public void atPreBattle() {
        attackPlayed = false;
        skillPlayed = false;
        powerPlayed = false;
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (!card.purgeOnUse && this.checkAndSetFirstTime(card.type)) {
            this.flash();

            // 安全转型 target
            AbstractMonster target = null;
            if (action.target instanceof AbstractMonster) {
                target = (AbstractMonster) action.target;
            }

            // 直接调用你写好的动作，复读1次
            addToBot(new RepeatAction(card, target, 1));
        }
    }

    private boolean checkAndSetFirstTime(AbstractCard.CardType type) {
        if (type == AbstractCard.CardType.ATTACK && !attackPlayed) {
            attackPlayed = true; return true;
        } else if (type == AbstractCard.CardType.SKILL && !skillPlayed) {
            skillPlayed = true; return true;
        } else if (type == AbstractCard.CardType.POWER && !powerPlayed) {
            powerPlayed = true; return true;
        }
        return false;
    }
}