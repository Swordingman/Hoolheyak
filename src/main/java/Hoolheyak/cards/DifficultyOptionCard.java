package Hoolheyak.cards;

import basemod.AutoAdd;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;
import Hoolheyak.character.HoolheyakDifficultyHelper;
import Hoolheyak.util.CardStats;

@AutoAdd.Ignore
public class DifficultyOptionCard extends BaseCard {

    // 我们可以给这几张卡定义一个固定的基础ID
    public static final String ID = HoolheyakMod.makeID("DifficultyOption");

    // 1. 创建一个静态方法，根据难度返回对应的图片路径
    // 必须是 static 的，因为要在 super() 里面调用，那时候 this 还不存在
    private static String getImagePath(HoolheyakDifficultyHelper.DifficultyLevel level) {
        switch (level) {
            case EASY:
                return "Hoolheyak/images/cards/skill/ContingencyPlan.png";
            case HARD:
                return "Hoolheyak/images/cards/skill/Stargazing.png";
            case NORMAL:
            default:
                return "Hoolheyak/images/cards/skill/Revision.png";
        }
    }

    public DifficultyOptionCard(HoolheyakDifficultyHelper.DifficultyLevel level) {
        // 2. 调用 BaseCard 的 3个参数的构造函数 (ID, Stats, ImagePath)
        // 这样 BaseCard 就不会去自动瞎找图了，直接用我们给的路径
        super(ID, new CardStats(
                Hoolheyak.Meta.CARD_COLOR,
                AbstractCard.CardType.SKILL,
                AbstractCard.CardRarity.SPECIAL,
                AbstractCard.CardTarget.NONE,
                -2
        ), getImagePath(level));

        // 3. 这里的逻辑保持不变，用于覆盖 ID 对应的文本
        String targetID;
        switch (level) {
            case EASY:
                targetID = HoolheyakMod.makeID("DiffEasy");
                break;
            case HARD:
                targetID = HoolheyakMod.makeID("DiffHard");
                break;
            default:
                targetID = HoolheyakMod.makeID("DiffNormal");
                break;
        }

        // 手动读取本地化文本并覆盖 (因为 super 读取的是 DifficultyOption 的占位符文本)
        CardStrings cardStrings = CardCrawlGame.languagePack.getCardStrings(targetID);
        this.name = cardStrings.NAME;
        this.rawDescription = cardStrings.DESCRIPTION;

        // 刷新显示的标题和描述
        this.initializeTitle();
        this.initializeDescription();
    }

    @Override
    public void upgrade() {}

    @Override
    public void use(AbstractPlayer p, AbstractMonster m) {}
}