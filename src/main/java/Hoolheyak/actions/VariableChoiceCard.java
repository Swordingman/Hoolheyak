package Hoolheyak.actions;

import Hoolheyak.cards.BaseCard;
import basemod.AutoAdd;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

@AutoAdd.Ignore
public class VariableChoiceCard extends BaseCard {
    private Runnable effect;

    public VariableChoiceCard(AbstractCard source, String optionName, int index, String description, Runnable effect) {
        // 重点：ID 改用数字后缀（例如 Hoolheyak_CardID_Choice_0），防止特殊字符（α、β）弄崩底层系统
        super(
                source.cardID + "_Choice_" + index,
                -2,
                CardType.STATUS,
                CardTarget.NONE,
                CardRarity.SPECIAL,
                CardColor.COLORLESS,
                getImgPath(source)
        );

        // 直接使用传入的名字（不再拼接 source.name）
        this.name = optionName;
        this.rawDescription = description;
        this.initializeTitle();
        this.initializeDescription();

        this.effect = effect;
        this.dontTriggerOnUseCard = true;

        this.portrait = source.portrait;
        this.jokePortrait = source.jokePortrait;
        this.baseMagicNumber = this.magicNumber = source.magicNumber;
    }

    private static String getImgPath(AbstractCard source) {
        if (source instanceof BaseCard) {
            return ((BaseCard) source).textureImg;
        }
        return "HoolheyakResources/images/cards/Skill.png";
    }

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}

    public void onChoseThisOption() {
        if (this.effect != null) {
            this.effect.run();
        }

        if (com.megacrit.cardcrawl.dungeons.AbstractDungeon.player != null) {
            for (com.megacrit.cardcrawl.powers.AbstractPower p : com.megacrit.cardcrawl.dungeons.AbstractDungeon.player.powers) {
                if (p instanceof Hoolheyak.powers.ByproductPower) {
                    ((Hoolheyak.powers.ByproductPower) p).onVariableTriggered();
                } else if (p instanceof Hoolheyak.powers.ComplementaryExperimentPower) {
                    ((Hoolheyak.powers.ComplementaryExperimentPower) p).onVariableTriggered();
                }
            }
        }
    }

    @Override
    public void upgrade() {}
}