package Hoolheyak.character;

import Hoolheyak.powers.EruditionPower;
import Hoolheyak.powers.MeanderPower;
import Hoolheyak.relics.Bibliotheca;
import basemod.BaseMod;
import basemod.abstracts.CustomEnergyOrb;
import basemod.abstracts.CustomPlayer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.cutscenes.CutscenePanel;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.CardHelper;
import com.megacrit.cardcrawl.helpers.CardLibrary;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.orbs.AbstractOrb;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.rooms.MonsterRoom;
import com.megacrit.cardcrawl.rooms.RestRoom;
import com.megacrit.cardcrawl.screens.CharSelectInfo;

// 引入你的卡牌和遗物
import Hoolheyak.cards.*;
import Hoolheyak.util.Sounds;

// --- 关键：引用 Shade 后的 Spine 3.8 包 ---
import com.Hoolheyak.spine38.AnimationState;
import com.Hoolheyak.spine38.AnimationStateData;
import com.Hoolheyak.spine38.Skeleton;
import com.Hoolheyak.spine38.SkeletonData;
import com.Hoolheyak.spine38.SkeletonJson;
import com.Hoolheyak.spine38.SkeletonRenderer;

import java.util.ArrayList;
import java.util.List;

import static Hoolheyak.HoolheyakMod.characterPath;
import static Hoolheyak.HoolheyakMod.makeID;
import Hoolheyak.HoolheyakMod;

public class Hoolheyak extends CustomPlayer {
    // 角色基础数值
    public static final int ENERGY_PER_TURN = 3;
    public static final int MAX_HP = 67;
    public static final int STARTING_GOLD = 99;
    public static final int CARD_DRAW = 5;
    public static final int ORB_SLOTS = 0;

    // 字符串
    private static final String ID = makeID("Hoolheyak");
    private static String[] getNames() { return CardCrawlGame.languagePack.getCharacterString(ID).NAMES; }
    private static String[] getText() { return CardCrawlGame.languagePack.getCharacterString(ID).TEXT; }

    // 图片资源
    private static final String SHOULDER_1 = characterPath("shoulder.png");
    private static final String SHOULDER_2 = characterPath("shoulder2.png");

    // 能量球纹理
    private static final String[] orbTextures = {
            characterPath("energyorb/layer1.png"), characterPath("energyorb/layer2.png"),
            characterPath("energyorb/layer3.png"), characterPath("energyorb/layer4.png"),
            characterPath("energyorb/layer5.png"), characterPath("energyorb/cover.png"),
            characterPath("energyorb/layer1d.png"), characterPath("energyorb/layer2d.png"),
            characterPath("energyorb/layer3d.png"), characterPath("energyorb/layer4d.png"),
            characterPath("energyorb/layer5d.png")
    };
    private static final float[] layerSpeeds = new float[] { -20.0F, 20.0F, -40.0F, 40.0F, 360.0F };

    // Spine 3.8
    protected TextureAtlas atlas38;
    protected Skeleton skeleton38;
    protected AnimationState state38;
    protected AnimationStateData stateData38;

    // 独立 Renderer（不要用 CardCrawlGame.psb）
    protected static final PolygonSpriteBatch psb = new PolygonSpriteBatch();
    protected static final SkeletonRenderer sr = new SkeletonRenderer();
    static {
        sr.setPremultipliedAlpha(true);
    }

    private HoolheyakAnimHelper animHelper;

    // --- Meta ---
    public static class Meta {
        @SpireEnum public static PlayerClass HOOLHEYAK;
        @SpireEnum(name = "HOOLHEYAK_COLOR") public static AbstractCard.CardColor CARD_COLOR;
        @SpireEnum(name = "HOOLHEYAK_COLOR") @SuppressWarnings("unused") public static CardLibrary.LibraryType LIBRARY_COLOR;

        private static final String CHAR_SELECT_BUTTON = "Hoolheyak/char/select.png";
        private static final String CHAR_SELECT_PORTRAIT = "Hoolheyak/char/hoolheyak.png";

        private static final String BG_ATTACK = characterPath("cardback/bg_attack.png");
        private static final String BG_ATTACK_P = characterPath("cardback/bg_attack_p.png");
        private static final String BG_SKILL = characterPath("cardback/bg_skill.png");
        private static final String BG_SKILL_P = characterPath("cardback/bg_skill_p.png");
        private static final String BG_POWER = characterPath("cardback/bg_power.png");
        private static final String BG_POWER_P = characterPath("cardback/bg_power_p.png");
        private static final String ENERGY_ORB = characterPath("cardback/energy_orb.png");
        private static final String ENERGY_ORB_P = characterPath("cardback/energy_orb_p.png");
        private static final String SMALL_ORB = characterPath("cardback/small_orb.png");

        private static final Color cardColor = new Color(71f/255f, 63f/255f, 34f/255f, 1f);

        public static void registerColor() {
            BaseMod.addColor(CARD_COLOR, cardColor,
                    BG_ATTACK, BG_SKILL, BG_POWER, ENERGY_ORB,
                    BG_ATTACK_P, BG_SKILL_P, BG_POWER_P, ENERGY_ORB_P,
                    SMALL_ORB);
        }

        public static void registerCharacter() {
            BaseMod.addCharacter(new Hoolheyak(), CHAR_SELECT_BUTTON, CHAR_SELECT_PORTRAIT, Meta.HOOLHEYAK);
        }
    }

    // --- 构造方法 ---
    public Hoolheyak() {
        super(getNames()[0], Meta.HOOLHEYAK,
                new CustomEnergyOrb(orbTextures, characterPath("energyorb/vfx.png"), layerSpeeds),
                null, null); // 传 null 给父类动画

        // 基础初始化
        initializeClass(null, SHOULDER_2, SHOULDER_1, null, getLoadout(),
                20.0F, -30.0F, 220.0F, 290.0F, new EnergyManager(ENERGY_PER_TURN));

        loadSpine();
    }

    // --- 核心绘制与更新逻辑 (Override) ---

    private void loadSpine() {
        HoolheyakSkinHelper.SkinInfo skin = HoolheyakSkinHelper.getCurrentSkin();

        atlas38 = new TextureAtlas(Gdx.files.internal(skin.atlas));
        SkeletonJson json = new SkeletonJson(atlas38);
        json.setScale(Settings.renderScale / skin.scale);

        SkeletonData data = json.readSkeletonData(Gdx.files.internal(skin.json));

        skeleton38 = new Skeleton(data);
        skeleton38.setColor(Color.WHITE);

        stateData38 = new AnimationStateData(data);
        state38 = new AnimationState(stateData38);

        stateData38.setDefaultMix(0.2f);

        stateData38.setMix("Idle", "Attack", 0.1f);
        stateData38.setMix("Attack", "Idle", 0.1f);
        stateData38.setMix("Idle", "Die", 0.1f);

        state38.setAnimation(0, "Idle", true);

        this.animHelper = new HoolheyakAnimHelper(state38, skeleton38.getData());
    }

    @Override
    public void render(SpriteBatch sb) {
        // 姿态（必画）
        this.stance.render(sb);

        // 战斗中：血条 + orb
        if ((AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT
                || AbstractDungeon.getCurrRoom() instanceof MonsterRoom)
                && !this.isDead) {

            renderHealth(sb);

            if (!this.orbs.isEmpty()) {
                for (AbstractOrb o : this.orbs) {
                    o.render(sb);
                }
            }
        }

        // 非休息房
        if (!(AbstractDungeon.getCurrRoom() instanceof RestRoom)) {
            renderPlayerImage(sb);
            this.hb.render(sb);
            this.healthHb.render(sb);
        } else {
            sb.setColor(Color.WHITE);
            renderShoulderImg(sb);
        }
    }

    @Override
    public void renderPlayerImage(SpriteBatch sb) {
        if (state38 == null || skeleton38 == null) return;

        if (animHelper != null) {
            animHelper.update();
        }

        // 更新 Spine
        state38.update(Gdx.graphics.getDeltaTime());
        state38.apply(skeleton38);
        skeleton38.updateWorldTransform();

        skeleton38.setPosition(
                this.drawX + this.animX,
                this.drawY + this.animY
        );
        skeleton38.setColor(this.tint.color);

        float absX = Math.abs(skeleton38.getScaleX());
        float absY = Math.abs(skeleton38.getScaleY());
        skeleton38.setScaleX(this.flipHorizontal ? -absX : absX);
        skeleton38.setScaleY(this.flipVertical ? -absY : absY);

        sb.end();
        psb.begin();
        sr.draw(psb, skeleton38);
        psb.end();
        sb.begin();
    }

    // --- 动画控制 ---

    @Override
    public void useFastAttackAnimation() {
        if (animHelper != null) {
            animHelper.playAttack();
        } else {
            if (state38 != null) state38.setAnimation(0, "Attack", false);
        }
    }

    @Override
    public void playDeathAnimation() {
        if (state38 == null) return;
        state38.setAnimation(0, "Die", false);
    }

    @Override
    public void onVictory() {
        super.onVictory();

        if (this.animHelper != null) {
            this.animHelper.reset();
        }
    }

    // --- 常规配置 ---

    @Override
    public ArrayList<String> getStartingDeck() {
        ArrayList<String> retVal = new ArrayList<>();
        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Strike.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(Defend.ID);
        retVal.add(FeatherStrike.ID);
        retVal.add(Linger.ID);
        return retVal;
    }

    @Override
    public ArrayList<String> getStartingRelics() {
        ArrayList<String> retVal = new ArrayList<>();
        retVal.add(Bibliotheca.ID);
        return retVal;
    }

    @Override
    public AbstractCard getStartCardForEvent() {
        return new FeatherStrike();
    }

    @Override
    public void applyStartOfCombatLogic() {
        super.applyStartOfCombatLogic();

        AbstractDungeon.actionManager.addToBottom(
                new ApplyPowerAction(this, this,
                        new EruditionPower(this, 0), 0)
        );

        AbstractDungeon.actionManager.addToBottom(
                new ApplyPowerAction(this, this,
                        new MeanderPower(this, 0), 0)
        );
    }

    @Override
    public CharSelectInfo getLoadout() {
        int hp = MAX_HP;

        return new CharSelectInfo(getNames()[0], getText()[0],
                hp, hp, ORB_SLOTS, STARTING_GOLD, CARD_DRAW, this,
                getStartingRelics(), getStartingDeck(), false);
    }

    @Override
    public AbstractPlayer newInstance() {
        return new Hoolheyak();
    }

    @Override
    public int getAscensionMaxHPLoss() { return 4; }

    @Override
    public AbstractGameAction.AttackEffect[] getSpireHeartSlashEffect() {
        return new AbstractGameAction.AttackEffect[] {
                AbstractGameAction.AttackEffect.SLASH_DIAGONAL,
                AbstractGameAction.AttackEffect.SLASH_HORIZONTAL,
                AbstractGameAction.AttackEffect.SLASH_VERTICAL
        };
    }

    @Override
    public List<CutscenePanel> getCutscenePanels() {
        List<CutscenePanel> panels = new ArrayList<>();
        panels.add(new CutscenePanel("Hoolheyak/images/scenes/penance.jpg", "ATTACK_HEAVY"));
        panels.add(new CutscenePanel("Hoolheyak/images/scenes/vic1.png", "ATTACK_HEAVY"));
        panels.add(new CutscenePanel("Hoolheyak/images/scenes/vic2.png"));
        panels.add(new CutscenePanel("Hoolheyak/images/scenes/vic3.png"));
        return panels;
    }

    private final Color hoolheyakThemeColor = CardHelper.getColor(68, 146, 144);
    private final Color hoolheyakEffectColor = new Color(145f/255f, 175f/255f, 155f/255f, 1f);

    @Override public Color getCardRenderColor() { return hoolheyakThemeColor; }
    @Override public Color getCardTrailColor() { return hoolheyakEffectColor; }
    @Override public Color getSlashAttackColor() { return hoolheyakEffectColor; }
    @Override public BitmapFont getEnergyNumFont() { return FontHelper.energyNumFontRed; }

    // 新增一个方法，用于读取当前配置并返回对应的音频Key
    public static String getSelectedVoiceKey() {
        int lang = 0;
        if (HoolheyakMod.hoolheyakConfig != null) {
            lang = HoolheyakMod.hoolheyakConfig.getInt("voiceLang");
        }

        switch (lang) {
            case 0: return Sounds.CN;
            case 1: return Sounds.EN;
            case 2: return Sounds.JP;
            case 3: return Sounds.KR;
            default: return Sounds.CN;
        }
    }

    // 替换原来的选择界面特效方法
    @Override
    public void doCharSelectScreenSelectEffect() {
        CardCrawlGame.sound.playA(getSelectedVoiceKey(), 0.0F);
        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.MED, ScreenShake.ShakeDur.SHORT, false);
    }

    // 替换原来的自定义模式按钮音效方法
    @Override
    public String getCustomModeCharacterButtonSoundKey() {
        return getSelectedVoiceKey();
    }

    @Override public String getLocalizedCharacterName() { return getNames()[0]; }
    @Override public String getTitle(PlayerClass playerClass) { return getNames()[1]; }
    @Override public String getSpireHeartText() { return getText()[1]; }
    @Override public String getVampireText() { return getText()[2]; }
    @Override public AbstractCard.CardColor getCardColor() { return Meta.CARD_COLOR; }
}