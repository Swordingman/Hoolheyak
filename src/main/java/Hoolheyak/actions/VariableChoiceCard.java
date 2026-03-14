package Hoolheyak.actions;

import Hoolheyak.cards.BaseCard;
import basemod.AutoAdd;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.monsters.AbstractMonster;

@AutoAdd.Ignore
public class VariableChoiceCard extends BaseCard {
    // 👇 1. 新增变量：用来把构造函数的参数存下来，供 makeCopy 使用
    private AbstractCard sourceCard;
    private String optionName;
    private int index;
    private String originalDescription;
    private Runnable effect;

    public VariableChoiceCard(AbstractCard source, String optionName, int index, String description, Runnable effect) {
        super(
                source.cardID + "_Choice_" + index,
                -2,
                CardType.STATUS,
                CardTarget.NONE,
                CardRarity.SPECIAL,
                CardColor.COLORLESS,
                getImgPath(source)
        );

        // 👇 2. 赋值保存这些变量
        this.sourceCard = source;
        this.optionName = optionName;
        this.index = index;
        this.originalDescription = description;
        this.effect = effect;

        this.name = optionName;
        this.rawDescription = description;

        if (source instanceof BaseCard) {
            BaseCard baseSource = (BaseCard) source;

            // 1. 强行获取原卡的 customVars (注意这里的泛型类型已经改对)
            java.util.Map<String, basemod.abstracts.DynamicVariable> sourceVars =
                    basemod.ReflectionHacks.getPrivate(baseSource, BaseCard.class, "customVars");

            // 2. 如果原卡有自定义变量，就直接给当前这选项卡也克隆一份塞进去
            if (sourceVars != null) {
                basemod.ReflectionHacks.setPrivate(
                        this,
                        BaseCard.class,
                        "customVars",
                        new java.util.HashMap<>(sourceVars) // new 一份新的防止互相干扰
                );
            }
        }

        this.initializeTitle();
        this.initializeDescription();

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

    // 👇 3. 核心修复：重写 makeCopy 方法，告诉底层系统如何复制这张卡！
    @Override
    public AbstractCard makeCopy() {
        return new VariableChoiceCard(this.sourceCard, this.optionName, this.index, this.originalDescription, this.effect);
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