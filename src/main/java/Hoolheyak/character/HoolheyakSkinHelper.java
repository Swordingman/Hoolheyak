package Hoolheyak.character;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import basemod.ReflectionHacks;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;
import com.megacrit.cardcrawl.screens.charSelect.CharacterSelectScreen;
import java.util.ArrayList;
import Hoolheyak.HoolheyakMod;

import com.Hoolheyak.spine38.AnimationState;
import com.Hoolheyak.spine38.AnimationStateData;
import com.Hoolheyak.spine38.Skeleton;
import com.Hoolheyak.spine38.SkeletonData;
import com.Hoolheyak.spine38.SkeletonJson;
import com.Hoolheyak.spine38.SkeletonRenderer;

public class HoolheyakSkinHelper {

    // --- 数据结构 ---
    public static class SkinInfo {
        public final int nameIndex;
        public String atlas;
        public String json;
        public float scale;

        public SkinInfo(int nameIndex, String atlas, String json, float scale) {
            this.nameIndex = nameIndex;
            this.atlas = atlas;
            this.json = json;
            this.scale = scale;
        }

        public String getLocalizedName() {
            UIStrings uiStrings = CardCrawlGame.languagePack.getUIString("Hoolheyak:SkinName");
            return uiStrings.TEXT[this.nameIndex];
        }
    }

    // --- 皮肤列表 ---
    public static final SkinInfo[] SKINS = new SkinInfo[] {
            new SkinInfo(
                    0,
                    "Hoolheyak/char/hoolheyak/animation/1/char_4027_heyak.atlas",
                    "Hoolheyak/char/hoolheyak/animation/1/char_4027_heyak.json",
                    1.5f
            ),
            new SkinInfo(
                    1,
                    "Hoolheyak/char/hoolheyak/animation/2/char_4027_heyak_ambienceSynesthesia_4.atlas",
                    "Hoolheyak/char/hoolheyak/animation/2/char_4027_heyak_ambienceSynesthesia_4.json",
                    1.5f
            ),
            new SkinInfo(
                    2,
                    "Hoolheyak/char/hoolheyak/animation/3/char_4027_heyak_epoque_50.atlas",
                    "Hoolheyak/char/hoolheyak/animation/3/char_4027_heyak_epoque_50.json",
                    1.5f
            )
    };

    public static int currentSkinIndex = 0;

    // --- 资源字段 ---
    private static TextureAtlas currentAtlas; // 持有引用以便释放
    private static Skeleton previewSkeleton;
    private static AnimationState previewState;
    private static AnimationStateData previewStateData;
    private static SkeletonRenderer skeletonRenderer; // 修正类型

    // --- UI 控件 ---
    private static Hitbox skinLeftHb = new Hitbox(70f * Settings.scale, 70f * Settings.scale);
    private static Hitbox skinRightHb = new Hitbox(70f * Settings.scale, 70f * Settings.scale);

    public static SkinInfo getCurrentSkin() {
        return SKINS[currentSkinIndex];
    }

    public static boolean isHoolheyakSelected() {
        if (CardCrawlGame.mainMenuScreen == null || CardCrawlGame.mainMenuScreen.charSelectScreen == null) {
            return false;
        }
        ArrayList<CharacterOption> options = ReflectionHacks.getPrivate(
                CardCrawlGame.mainMenuScreen.charSelectScreen, CharacterSelectScreen.class, "options");

        for (CharacterOption o : options) {
            Object p = ReflectionHacks.getPrivate(o, CharacterOption.class, "c");
            if (p instanceof Hoolheyak && o.selected) {
                return true;
            }
        }
        return false;
    }

    public static void loadPreviewAnimation() {
        SkinInfo skin = getCurrentSkin();

        // 释放旧资源，防止显存泄漏
        if (currentAtlas != null) {
            currentAtlas.dispose();
        }

        currentAtlas = new TextureAtlas(Gdx.files.internal(skin.atlas));
        SkeletonJson json = new SkeletonJson(currentAtlas); // 使用 3.8 的 Loader
        json.setScale(Settings.renderScale / skin.scale); // 计算缩放

        SkeletonData data = json.readSkeletonData(Gdx.files.internal(skin.json));

        previewSkeleton = new Skeleton(data);
        previewSkeleton.setColor(Color.WHITE);
        previewStateData = new AnimationStateData(data);
        previewState = new AnimationState(previewStateData);
        previewState.setAnimation(0, "Idle", true);

        // 设置预览位置
        previewSkeleton.setPosition(Settings.WIDTH * 0.85f, Settings.HEIGHT * 0.65f);

        if (skeletonRenderer == null) {
            skeletonRenderer = new SkeletonRenderer(); // 修正实例化
            skeletonRenderer.setPremultipliedAlpha(true);
        }
    }

    public static void saveSkin() {
        try {
            if (HoolheyakMod.hoolheyakConfig != null) {
                HoolheyakMod.hoolheyakConfig.setInt("skinIndex", currentSkinIndex);
                HoolheyakMod.hoolheyakConfig.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void update() {
        if (!isHoolheyakSelected()) return;

        float centerX = Settings.WIDTH * 0.85f;
        float centerY = Settings.HEIGHT * 0.65f;

        skinLeftHb.move(centerX - 120f * Settings.scale, centerY);
        skinRightHb.move(centerX + 120f * Settings.scale, centerY);
        skinLeftHb.update();
        skinRightHb.update();

        if (InputHelper.justClickedLeft) {
            if (skinLeftHb.hovered || skinRightHb.hovered) {
                CardCrawlGame.sound.play("UI_CLICK_1");
                if (skinLeftHb.hovered) skinLeftHb.clickStarted = true;
                if (skinRightHb.hovered) skinRightHb.clickStarted = true;
            }
        }

        if (skinLeftHb.clicked || skinRightHb.clicked) {
            if (skinLeftHb.clicked) {
                currentSkinIndex--;
                if (currentSkinIndex < 0) currentSkinIndex = SKINS.length - 1;
            } else {
                currentSkinIndex++;
                if (currentSkinIndex >= SKINS.length) currentSkinIndex = 0;
            }
            skinLeftHb.clicked = false;
            skinRightHb.clicked = false;

            saveSkin();

            loadPreviewAnimation();
        }
    }

    public static void render(SpriteBatch sb) {
        if (!isHoolheyakSelected()) return;
        if (previewSkeleton == null) loadPreviewAnimation();

        // 1. UI (箭头和文字) 的基准坐标 —— 【保持不动】
        float uiCenterX = Settings.WIDTH * 0.85f;
        float uiCenterY = Settings.HEIGHT * 0.65f;

        // 2. 皮肤模型的偏移量 —— 【在这里修改数值来移动皮肤】
        // 正数向右/上，负数向左/下。一定要乘 Settings.scale 以适配分辨率
        float skinOffsetX = 0f * Settings.scale;    // 例如：向右移 50f 就填 50f
        float skinOffsetY = 25f * Settings.scale; // 例如：向下移 100f 就填 -100f
        // --- 绘制 UI (使用 uiCenter) ---

        // 绘制箭头 (位置不变)
        sb.setColor(skinLeftHb.hovered ? Color.LIGHT_GRAY : Color.WHITE);
        sb.draw(ImageMaster.CF_LEFT_ARROW, skinLeftHb.cX - 24f, skinLeftHb.cY - 24f, 24f, 24f, 48f, 48f, Settings.scale, Settings.scale, 0f, 0, 0, 48, 48, false, false);

        sb.setColor(skinRightHb.hovered ? Color.LIGHT_GRAY : Color.WHITE);
        sb.draw(ImageMaster.CF_RIGHT_ARROW, skinRightHb.cX - 24f, skinRightHb.cY - 24f, 24f, 24f, 48f, 48f, Settings.scale, Settings.scale, 0f, 0, 0, 48, 48, false, false);

        // 绘制文字 (位置不变)
        FontHelper.renderFontCentered(sb, FontHelper.buttonLabelFont, getCurrentSkin().getLocalizedName(), uiCenterX, uiCenterY, Settings.GOLD_COLOR);

        // --- 绘制 Spine 预览 (使用 uiCenter + Offset) ---

        float deltaTime = Gdx.graphics.getDeltaTime();
        previewState.update(deltaTime);
        previewState.apply(previewSkeleton);
        previewSkeleton.updateWorldTransform();

        // ▼▼▼ 关键修改：在这里应用偏移量 ▼▼▼
        previewSkeleton.setPosition(uiCenterX + skinOffsetX, uiCenterY + skinOffsetY);

        // 绘制逻辑
        sb.end();
        CardCrawlGame.psb.begin();
        skeletonRenderer.draw(CardCrawlGame.psb, previewSkeleton);
        CardCrawlGame.psb.end();
        sb.begin();
    }
}