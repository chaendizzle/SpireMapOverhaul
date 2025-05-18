package spireMapOverhaul.zones.beastslair;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.*;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.SpawnMonsterAction;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.city.Colosseum;
import com.megacrit.cardcrawl.helpers.MonsterHelper;
import com.megacrit.cardcrawl.localization.EventStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.monsters.exordium.*;
import com.megacrit.cardcrawl.powers.ArtifactPower;
import javassist.CtBehavior;
import spireMapOverhaul.SpireAnniversary6Mod;
import spireMapOverhaul.zones.beastslair.powers.FuryPower;

import java.util.ArrayList;
import java.util.Arrays;

// We extend the Colosseum event because ProceedButton.java specifically checks if an event is an instance of this type
// (or a few other types) in the logic for what happens when you click proceed. This is easier than a patch.
public class BeastsLairEvent extends Colosseum {
    public static final String ID = SpireAnniversary6Mod.makeID("BeastsLairEvent");
    private static final EventStrings eventStrings = CardCrawlGame.languagePack.getEventString(ID);
    private static final String NAME = eventStrings.NAME;
    private static final String[] DESCRIPTIONS = eventStrings.DESCRIPTIONS;
    private static final String[] OPTIONS = eventStrings.OPTIONS;
    public static final String IMG = SpireAnniversary6Mod.makeImagePath("events/eventpicture.png");

    private CurScreen screen;
    String encounter;

    public BeastsLairEvent() {
        super();
        this.imageEventText.clear();
        this.roomEventText.clear();
        this.title = NAME;
        this.body = DESCRIPTIONS[0];
        this.imageEventText.loadImage(IMG);
        type = EventType.IMAGE;
        this.noCardsInRewards = false;

        this.screen = CurScreen.INTRO;
        this.imageEventText.setDialogOption(OPTIONS[0]);
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
                        AbstractDungeon.getCurrRoom().monsters = MonsterHelper.getEncounter(encounter);
                        AbstractDungeon.getCurrRoom().rewards.clear();
                        AbstractDungeon.getCurrRoom().rewardAllowed = true;
                        //Adds all starting relics to the reward screen
                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractDungeon.returnRandomRelicTier());
                        AbstractDungeon.getCurrRoom().addRelicToRewards(AbstractDungeon.returnRandomRelicTier());
                        AbstractDungeon.getCurrRoom().addPotionToRewards();
                        AbstractDungeon.getCurrRoom().eliteTrigger = true;
                        this.enterCombatFromImage();
                        // boss buff
                        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
                            m.maxHealth = (int) (m.maxHealth * 1.25f);
                            m.currentHealth = (int) (m.currentHealth * 1.25f);
                            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new ArtifactPower(m, 2)));
                            AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new FuryPower(m, 10)));
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

    @SpirePatch2(clz = SpawnMonsterAction.class, method = "update")
    public static class SpawnMonsterActionPatch {
        @SpireInsertPatch(locator = BeastsLairEvent.SpawnMonsterActionPatch.Locator.class)
        public static void patch(SpawnMonsterAction __instance) {
            boolean used = ReflectionHacks.getPrivate(__instance, SpawnMonsterAction.class, "used");
            if (!used && AbstractDungeon.getCurrRoom().event instanceof BeastsLairEvent) {
                AbstractMonster m = ReflectionHacks.getPrivate(__instance, SpawnMonsterAction.class, "m");
                if (!isSlimebossSpawn(m)) {
                    m.maxHealth = (int) (m.maxHealth * 1.25f);
                    m.currentHealth = (int) (m.currentHealth * 1.25f);
                }
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new ArtifactPower(m, 2), 2));
                AbstractDungeon.actionManager.addToBottom(new ApplyPowerAction(m, m, new FuryPower(m, 10), 10));
            }
        }

        private static boolean isSlimebossSpawn(AbstractMonster m) {
            return (AbstractDungeon.lastCombatMetricKey.equals("Slime Boss") && slimes.contains(m.id));
        }

        private static final ArrayList<String> slimes = new ArrayList<>(Arrays.asList(SpikeSlime_L.ID, SpikeSlime_M.ID,
                SpikeSlime_S.ID, AcidSlime_L.ID, AcidSlime_M.ID, AcidSlime_S.ID));


        private static class Locator extends SpireInsertLocator {
            @Override
            public int[] Locate(CtBehavior ctMethodToPatch) throws Exception {
                Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractMonster.class, "showHealthBar");
                return LineFinder.findInOrder(ctMethodToPatch, finalMatcher);
            }
        }
    }
}
