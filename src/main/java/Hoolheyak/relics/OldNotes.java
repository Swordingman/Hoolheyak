package Hoolheyak.relics;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

public class OldNotes extends BaseRelic {
    public static final String ID = HoolheyakMod.makeID("OldNotes");

    private int attackDrawCount = 0;
    private int skillDrawCount = 0;

    public OldNotes() {
        super(ID, "OldNotes", Hoolheyak.Meta.CARD_COLOR, RelicTier.UNCOMMON, LandingSound.FLAT);
    }

    @Override
    public void onCardDraw(AbstractCard card) {
        if (card.type == AbstractCard.CardType.ATTACK) {
            attackDrawCount++;
            if (attackDrawCount >= 5) {
                attackDrawCount = 0;
                this.flash();
                addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new EruditionPower(AbstractDungeon.player, 1), 1));
            }
        } else if (card.type == AbstractCard.CardType.SKILL) {
            skillDrawCount++;
            if (skillDrawCount >= 5) {
                skillDrawCount = 0;
                this.flash();
                addToBot(new ApplyPowerAction(AbstractDungeon.player, AbstractDungeon.player, new MeanderPower(AbstractDungeon.player, 1), 1));
            }
        }
    }

    @Override
    public void onEquip() {
        attackDrawCount = 0;
        skillDrawCount = 0;
    }
}