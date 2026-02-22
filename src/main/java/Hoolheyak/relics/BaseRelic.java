package Hoolheyak.relics;

import basemod.abstracts.CustomRelic;
import basemod.helpers.RelicType;
import Hoolheyak.util.GeneralUtils;
import Hoolheyak.util.TextureLoader;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.RelicStrings;

import static Hoolheyak.HoolheyakMod.relicPath;

public abstract class BaseRelic extends CustomRelic {
    public AbstractCard.CardColor pool = null; // 遗物所属的卡牌颜色池，用于区分遗物的来源（例如：红色、绿色、蓝色、紫色）
    public RelicType relicType = RelicType.SHARED; // 遗物的类型，默认为共享（SHARED）
    protected String imageName; // 遗物的图片文件名

    // 用于角色专属遗物的构造函数
    public BaseRelic(String id, String imageName, AbstractCard.CardColor pool, RelicTier tier, LandingSound sfx) {
        this(id, imageName, tier, sfx); // 调用下面的构造函数，并设置遗物的颜色池

        setPool(pool); // 设置遗物的颜色池
    }

    // 用于没有指定图片名称的遗物的构造函数（会从ID中推断图片名）
    public BaseRelic(String id, RelicTier tier, LandingSound sfx) {
        this(id, GeneralUtils.removePrefix(id), tier, sfx); // 从ID中移除前缀作为图片名，并调用下面的构造函数
    }

    // 主要的构造函数
    // 如果要使用基础游戏中的遗物图片，只需传入基础游戏遗物的图片文件名，而不是ID。
    // 例如："calendar.png"
    public BaseRelic(String id, String imageName, RelicTier tier, LandingSound sfx) {
        super(testStrings(id), notPng(imageName) ? "" : imageName, tier, sfx); // 调用父类构造函数，加载本地化字符串和图片名

        this.imageName = imageName; // 保存图片文件名
        if (notPng(imageName)) { // 如果传入的图片名不是.png结尾，则认为是未指定后缀，需要加载纹理
            loadTexture(); // 加载遗物纹理
        }
    }

    // 加载遗物的纹理图片
    protected void loadTexture() {
        this.img = TextureLoader.getTextureNull(relicPath(imageName + ".png"), true); // 加载遗物主图
        if (img != null) { // 如果主图加载成功
            outlineImg = TextureLoader.getTextureNull(relicPath(imageName + "Outline.png"), true); // 加载遗物轮廓图
            if (outlineImg == null) // 如果轮廓图未加载成功，则使用主图作为轮廓图
                outlineImg = img;
        }
        else { // 如果主图加载失败，则使用默认的“Derp Rock”图片
            ImageMaster.loadRelicImg("Derp Rock", "derpRock.png"); // 加载默认图片
            this.img = ImageMaster.getRelicImg("Derp Rock"); // 设置主图
            this.outlineImg = ImageMaster.getRelicOutlineImg("Derp Rock"); // 设置轮廓图
        }
    }

    // 加载遗物的放大版图片（用于遗物详情页）
    @Override
    public void loadLargeImg() {
        if (notPng(imageName)) { // 如果图片名不是.png结尾，则尝试加载自定义的大图
            if (largeImg == null) { // 如果大图尚未加载
                this.largeImg = ImageMaster.loadImage(relicPath("large/" + imageName + ".png")); // 加载大图
            }
        }
        else { // 如果图片名是.png结尾，则调用父类方法加载大图
            super.loadLargeImg();
        }
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }

    // 设置遗物的颜色池（用于区分遗物来源）
    private void setPool(AbstractCard.CardColor pool) {
        switch (pool) { // 基础游戏中的颜色池处理方式不同
            case RED:
                relicType = RelicType.RED; // 设置为红色遗物
                break;
            case GREEN:
                relicType = RelicType.GREEN; // 设置为绿色遗物
                break;
            case BLUE:
                relicType = RelicType.BLUE; // 设置为蓝色遗物
                break;
            case PURPLE:
                relicType = RelicType.PURPLE; // 设置为紫色遗物
                break;
            default:
                this.pool = pool; // 如果不是基础游戏颜色，则直接设置颜色池
                break;
        }
    }

    /**
     * 检查遗物的本地化字符串是否设置正确，并在未正确设置时给出更准确的错误提示。
     * @param ID 遗物的ID
     * @return 遗物的ID，以便在调用super构造函数时使用
     */
    private static String testStrings(String ID) {
        RelicStrings text = CardCrawlGame.languagePack.getRelicStrings(ID); // 获取遗物的本地化字符串
        if (text == null) { // 如果本地化字符串为空，则抛出异常
            throw new RuntimeException("The \"" + ID + "\" relic does not have associated text. Make sure " +
                    "there's no issue with the RelicStrings.json file, and that the ID in the json file matches the " +
                    "relic's ID. It should look like \"${modID}:" + GeneralUtils.removePrefix(ID) + "\".");
        }
        return ID; // 返回遗物ID
    }

    // 检查文件名是否以.png结尾
    private static boolean notPng(String name) {
        return !name.endsWith(".png");
    }
}