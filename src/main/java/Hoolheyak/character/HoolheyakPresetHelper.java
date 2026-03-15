package Hoolheyak.character;


import Hoolheyak.cards.*;
import Hoolheyak.cards.phases.*;
import Hoolheyak.powers.phases.OppositionPower;
import Hoolheyak.relics.CocktailShaker;
import Hoolheyak.relics.FrenziedSundial;
import Hoolheyak.relics.StarMapProjection;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.PrismaticShard;
import com.megacrit.cardcrawl.screens.charSelect.CharacterOption;

import Hoolheyak.HoolheyakMod;
import Hoolheyak.character.Hoolheyak;

import java.util.HashMap;
import java.util.Map;

public class HoolheyakPresetHelper {
    public enum PresetLevel {
        // 格式：枚举名(索引, "本地化ID", "图片路径", new String[]{初始卡组}, new String[]{额外遗物})
        DEFAULT(0, "PreDefault", "Hoolheyak/images/cards/skill/Compose.png",
                new String[]{
                        Strike.ID,
                        Strike.ID,
                        Strike.ID,
                        Strike.ID,
                        Defend.ID,
                        Defend.ID,
                        Defend.ID,
                        Defend.ID,
                        FeatherStrike.ID,
                        Linger.ID
                },
                new String[]{}), // 默认没有额外遗物
        UNIVERSE(1, "PreUniverse", "Hoolheyak/images/cards/skill/ChimeraExperiment.png",
                new String[]{
                        TransitionalFlow.ID,
                        Linger.ID,
                        Wake.ID,
                        Revision.ID,
                        LiteratureRetrieval.ID,
                        BygoneWings.ID,
                        EntropyIncrease.ID,
                        Wardrobe.ID,
                        BorrowedWeapons.ID,
                        UniversalMapping.ID,
                        Archive.ID
                },
                new String[]{PrismaticShard.ID, CocktailShaker.ID}
                ),
        VARIABLE(2, "PreVariable", "Hoolheyak/images/cards/skill/Compose.png",
                new String[]{
                        ControlGroup.ID,
                        Compose.ID,
                        Archive.ID,
                        CrossExperiment.ID,
                        CrossExperiment.ID,
                        CrossExperiment.ID,
                        RecursiveExperiment.ID,
                        RedundantExperiment.ID,
                        Byproduct.ID,
                        ComplementaryExperiment.ID,
                        KnockInExperiment.ID,
                        ChimeraExperiment.ID,
                        AugmentationExperiment.ID,
                        ApoptosisExperiment.ID,
                        ControlExperiment.ID,
                        MutagenesisExperiment.ID,
                        FactorialExperiment.ID
                },
                new String[]{StarMapProjection.ID}
                ),
        PHASE(3, "PrePhase", "Hoolheyak/images/cards/skill/Transit.png",
                new String[]{
                        Transit.ID,
                        ConjunctionCard.ID,
                        QuincunxCard.ID,
                        SextileCard.ID,
                        TrineCard.ID,
                        SquareCard.ID,
                        OppositionCard.ID,
                        Stargazing.ID,
                        Strike.ID,
                        Strike.ID,
                        Strike.ID,
                        Strike.ID,
                        Defend.ID,
                        Defend.ID,
                        Defend.ID,
                        Defend.ID,
                        FeatherStrike.ID,
                        Linger.ID
                },
                new String[]{FrenziedSundial.ID}
                )
        ;
        public final int index;
        public final String stringId;
        public final String imagePath;
        public final String[] startingDeck;
        public final String[] extraRelics;
        // 枚举的构造函数
        PresetLevel(int index, String stringId, String imagePath, String[] startingDeck, String[] extraRelics) {
            this.index = index;
            this.stringId = stringId;
            this.imagePath = imagePath;
            this.startingDeck = startingDeck;
            this.extraRelics = extraRelics;
        }
    }

    public static PresetLevel currentPreset = PresetLevel.DEFAULT;

    private static Map<PresetLevel, AbstractCard> presetCards = new HashMap<>();

    // UI 控件
    private static Hitbox leftHb = new Hitbox(70.0f * Settings.scale, 70.0f * Settings.scale);
    private static Hitbox rightHb = new Hitbox(70.0f * Settings.scale, 70.0f * Settings.scale);

    public static void savePreset() {
        try {
            // 替换为霍尔海雅的 config
            HoolheyakMod.hoolheyakConfig.setInt("preset", currentPreset.index);
            HoolheyakMod.hoolheyakConfig.save();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void loadPreset() {
        try {
            if (HoolheyakMod.hoolheyakConfig != null) {
                int index = HoolheyakMod.hoolheyakConfig.getInt("preset");
                if (index < 0 || index >= PresetLevel.values().length) index = 0;
                currentPreset = PresetLevel.values()[index];
            }
        } catch (Exception e) { currentPreset = PresetLevel.DEFAULT; }

        initializeCards();
    }

    private static void initializeCards() {
        if (presetCards.isEmpty()) {
            // 如果你上面修改了枚举，这里也要跟着改
            presetCards.put(PresetLevel.DEFAULT, new PresetOptionCard(PresetLevel.DEFAULT));
            presetCards.put(PresetLevel.UNIVERSE, new PresetOptionCard(PresetLevel.UNIVERSE));
            presetCards.put(PresetLevel.VARIABLE, new PresetOptionCard(PresetLevel.VARIABLE));
            presetCards.put(PresetLevel.PHASE, new PresetOptionCard(PresetLevel.PHASE));
        }
    }

    // 判断是否选中了霍尔海雅
    private static boolean isHoolheyakSelected() {
        if (CardCrawlGame.mainMenuScreen == null || CardCrawlGame.mainMenuScreen.charSelectScreen == null) return false;
        for (CharacterOption o : CardCrawlGame.mainMenuScreen.charSelectScreen.options) {
            if (o.selected && o.c instanceof Hoolheyak) return true;
        }
        return false;
    }

    public static void update() {
        if (!isHoolheyakSelected()) return;
        initializeCards();

        // 预设卡牌的位置（在左边）
        float centerX = (Settings.WIDTH * 0.85f) - (550.0f * Settings.scale);
        float centerY = Settings.HEIGHT * 0.65f;
        float arrowOffset = 200.0f * Settings.scale;

        leftHb.move(centerX - arrowOffset, centerY);
        rightHb.move(centerX + arrowOffset, centerY);
        leftHb.update();
        rightHb.update();

        AbstractCard card = presetCards.get(currentPreset);
        if (card != null) {
            card.target_x = centerX;
            card.target_y = centerY;
            card.drawScale = 0.75f;
            card.update();
            card.updateHoverLogic();
        }

        if (InputHelper.justClickedLeft) {
            boolean changed = false;
            if (leftHb.hovered) {
                CardCrawlGame.sound.play("UI_CLICK_1");
                int newIndex = currentPreset.index - 1;
                if (newIndex < 0) newIndex = PresetLevel.values().length - 1;
                currentPreset = PresetLevel.values()[newIndex];
                changed = true;
            } else if (rightHb.hovered) {
                CardCrawlGame.sound.play("UI_CLICK_1");
                int newIndex = currentPreset.index + 1;
                if (newIndex >= PresetLevel.values().length) newIndex = 0;
                currentPreset = PresetLevel.values()[newIndex];
                changed = true;
            }

            if (changed) {
                savePreset();
                AbstractCard newCard = presetCards.get(currentPreset);
                if (newCard != null) {
                    newCard.current_x = centerX;
                    newCard.current_y = centerY;
                    newCard.target_x = centerX;
                    newCard.target_y = centerY;
                }
            }
        }
    }

    public static void render(SpriteBatch sb) {
        if (!isHoolheyakSelected()) return;

        AbstractCard card = presetCards.get(currentPreset);
        if (card != null) {
            card.render(sb);
        }

        Color cLeft = Settings.GOLD_COLOR.cpy();
        if (leftHb.hovered) cLeft = Color.WHITE.cpy();
        sb.setColor(cLeft);
        sb.draw(ImageMaster.CF_LEFT_ARROW, leftHb.cX - 24.0f, leftHb.cY - 24.0f, 24.0f, 24.0f, 48.0f, 48.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 48, 48, false, false);

        Color cRight = Settings.GOLD_COLOR.cpy();
        if (rightHb.hovered) cRight = Color.WHITE.cpy();
        sb.setColor(cRight);
        sb.draw(ImageMaster.CF_RIGHT_ARROW, rightHb.cX - 24.0f, rightHb.cY - 24.0f, 24.0f, 24.0f, 48.0f, 48.0f, Settings.scale, Settings.scale, 0.0f, 0, 0, 48, 48, false, false);

        leftHb.render(sb);
        rightHb.render(sb);
    }
}