package Hoolheyak.cards;

import basemod.AutoAdd;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakPresetHelper;
import Hoolheyak.util.CardStats;

@AutoAdd.Ignore
public class PresetOptionCard extends BaseCard {

    public static final String ID = HoolheyakMod.makeID("PresetOptionCard");

    public PresetOptionCard(HoolheyakPresetHelper.PresetLevel level) {
        // 直接读取传入预设的 imagePath
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                CardType.SKILL,
                CardRarity.SPECIAL,
                CardTarget.NONE,
                -2
        ), level.imagePath);

        // 直接读取传入预设的 stringId
        String targetID = HoolheyakMod.makeID(level.stringId);
        CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(targetID);
        this.name = cardStrings.NAME;
        this.rawDescription = cardStrings.DESCRIPTION;

        this.initializeTitle();
        this.initializeDescription();
    }

    @Override
    public void upgrade() {}

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}
}