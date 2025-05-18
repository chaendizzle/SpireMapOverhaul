package spireMapOverhaul.zones.characterinfluence;

import basemod.BaseMod;
import basemod.ReflectionHacks;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.GenericEventDialog;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rewards.RewardItem;
import spireMapOverhaul.SpireAnniversary6Mod;

import java.util.ArrayList;

import static spireMapOverhaul.zones.characterinfluence.CharacterInfluenceZone.getCurrentZoneCharacter;

// We extend the Colosseum event because ProceedButton.java specifically checks if an event is an instance of this type
// (or a few other types) in the logic for what happens when you click proceed. This is easier than a patch.
public class CharacterInfluenceEvent extends Colosseum {
    public static final String ID = SpireAnniversary6Mod.makeID("CharacterVisit");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    public static final String IMG = SpireAnniversary6Mod.makeImagePath("events/ShadowyFigure.png");

    private CurScreen screen;

    public CharacterInfluenceEvent() {
        super();
        this.imageEventText.clear();
        this.roomEventText.clear();
        this.title = NAME;
        this.body = DESCRIPTIONS[0] + DESCRIPTIONS[2] + DESCRIPTIONS[1];
        this.imageEventText.loadImage(IMG);
        setImage();
        type = EventType.IMAGE;
        this.noCardsInRewards = false;

        this.screen = CurScreen.INTRO;
        this.imageEventText.setDialogOption(OPTIONS[0]);
        this.imageEventText.setDialogOption(OPTIONS[1]);
        if (getCurrentZoneCharacter() != null) {
            this.body = DESCRIPTIONS[0] + getCurrentZoneCharacter().title + DESCRIPTIONS[1];
        }
        this.imageEventText.updateBodyText(this.body);
    }

    @Override
    protected void buttonEffect(int buttonPressed) {
        switch(this.screen) {
            case INTRO:
                AbstractDungeon.getCurrRoom().rewardAllowed = false;
                switch (buttonPressed) {
                    case 0:
                        this.screen = CurScreen.LEAVE;
                        String encounter = AbstractDungeon.eliteMonsterList.remove(0);
                        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(encounter);
                        AbstractDungeon.getCurrRoom().rewards.clear();
                        AbstractDungeon.getCurrRoom().rewardAllowed = true;
                        //Adds all starting relics to the reward screen
                        ArrayList<String> relicStrings = new ArrayList<>();
                        if (getCurrentZoneCharacter() != null) {
                            relicStrings = getCurrentZoneCharacter().getStartingRelics();
                        }
                        for (String relicID : relicStrings) {
                            AbstractDungeon.getCurrRoom().rewards.add(new RewardItem(RelicLibrary.getRelic(relicID).makeCopy()));
                        }
                        AbstractDungeon.getCurrRoom().addPotionToRewards();
                        AbstractDungeon.getCurrRoom().eliteTrigger = true;
                        this.enterCombatFromImage();
                        // start damaged
                        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                            AbstractDungeon.actionManager.addToBottom(new LoseHPAction(m, m, m.currentHealth/4));
                        }
                        AbstractDungeon.lastCombatMetricKey = encounter;
                        break;
                    default:
                        this.openMap();
                        break;
                }
                break;
            default:
                this.openMap();
                break;
        }
    }

    @Override
    public void reopen() {
        if (this.screen != CurScreen.LEAVE) {
            AbstractDungeon.resetPlayer();
            AbstractDungeon.player.drawX = (float) Settings.WIDTH * 0.25F;
            AbstractDungeon.player.preBattlePrep();
            this.enterImageFromCombat();
            this.imageEventText.updateBodyText(DESCRIPTIONS[2]);
            this.imageEventText.updateDialogOption(0, OPTIONS[2]);
            this.imageEventText.setDialogOption(OPTIONS[3]);
        }
    }

    private enum CurScreen {
        INTRO,
        FIGHT,
        LEAVE,
        FLED,
        POST_COMBAT
    }

    // Sets the Event image to a cropped version of the character portrait.
    public void setImage() {

        Texture portraitTexture = null;

        // Finds the portrait
        if (getCurrentZoneCharacter() != null) {
            if (Gdx.files.internal("images/ui/charSelect/" + getCurrentZoneCharacter().getPortraitImageName()).exists()) {
                portraitTexture = ImageMaster.loadImage("images/ui/charSelect/" + getCurrentZoneCharacter().getPortraitImageName());
            } else if (Gdx.files.internal(BaseMod.playerPortraitMap.get(getCurrentZoneCharacter().chosenClass)).exists()) {
                portraitTexture = ImageMaster.loadImage((BaseMod.playerPortraitMap.get(getCurrentZoneCharacter().chosenClass)));
            }
        }

        if (portraitTexture == null) return;

        if (!portraitTexture.getTextureData().isPrepared())
            portraitTexture.getTextureData().prepare();

        Pixmap pixmap = new Pixmap(
                600,
                600,
                portraitTexture.getTextureData().getFormat()
        );

        pixmap.drawPixmap(
                portraitTexture.getTextureData().consumePixmap(),
                720,
                0,
                1200,
                portraitTexture.getHeight(),
                0,
                0,
                600,
                600
        );

        Texture newTexture = new Texture(pixmap);

        // Repeats what this.imageEventText.loadImage(imgUrl) does, with ReflectionHacks.

        if (ReflectionHacks.getPrivate(this.imageEventText, GenericEventDialog.class, "img") != null) {
            ((Texture)ReflectionHacks.getPrivate(this.imageEventText, GenericEventDialog.class, "img")).dispose();
            ReflectionHacks.setPrivate(this.imageEventText, GenericEventDialog.class, "img", null);
        }

        ReflectionHacks.setPrivate(this.imageEventText, GenericEventDialog.class, "img", newTexture);

        ReflectionHacks.setPrivateStatic(GenericEventDialog.class, "DIALOG_MSG_X", ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "DIALOG_MSG_X_IMAGE"));
        ReflectionHacks.setPrivateStatic(GenericEventDialog.class, "DIALOG_MSG_W", ReflectionHacks.getPrivateStatic(GenericEventDialog.class, "DIALOG_MSG_W_IMAGE"));

        pixmap.dispose();

    }
}
