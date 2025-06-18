package spireMapOverhaul.zones.beastslair;

import basemod.BaseMod;
import basemod.abstracts.CustomSavable;
import basemod.eventUtil.EventUtils;
import basemod.patches.com.megacrit.cardcrawl.events.AbstractEvent.AdditionalEventParameters;
import com.badlogic.gdx.graphics.Color;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.events.AbstractEvent;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.map.MapRoomNode;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.random.Random;
import com.megacrit.cardcrawl.rewards.RewardItem;
import spireMapOverhaul.abstracts.AbstractZone;
import spireMapOverhaul.zoneInterfaces.CombatModifyingZone;
import spireMapOverhaul.zoneInterfaces.EncounterModifyingZone;
import spireMapOverhaul.zoneInterfaces.RewardModifyingZone;
import spireMapOverhaul.zones.gravewoodGrove.powers.DeadBranchPower;
import spireMapOverhaul.zones.invasion.powers.FixedTextDrawPower;
import spireMapOverhaul.zones.thieveshideout.rooms.ForcedEventRoom;

import java.util.ArrayList;

import static spireMapOverhaul.SpireAnniversary6Mod.makeID;
import static spireMapOverhaul.util.Wiz.adp;
import static spireMapOverhaul.util.Wiz.atb;

public class BeastsLairZone extends AbstractZone implements CombatModifyingZone, EncounterModifyingZone, RewardModifyingZone {
    public static final String ID = "BeastsLair";
    public static final String SaveID = makeID("BeastsLairBosses");
    public static ArrayList<String> bossList = new ArrayList<>();

    public BeastsLairZone() {
        super(ID, Icons.MONSTER);
        this.width = 1;
        this.maxWidth = 1;
        this.height = 1;
        this.maxHeight = 1;
        iconTexture = ImageMaster.MAP_NODE_ELITE;

    }

    public static void clearBossList() {
        bossList.clear();
    }

    @Override
    public void atPreBattle() {
        atb(new ApplyPowerAction(adp(), null, new VulnerablePower(adp(), 99, false)));
        atb(new ApplyPowerAction(adp(), null, new WeakPower(adp(), 99, false)));
    }

    public static void initializeSaveFields() {
        BaseMod.addSaveField(SaveID, new CustomSavable<ArrayList<String>>() {
            @Override
            public ArrayList<String> onSave() {
                return BeastsLairZone.bossList;
            }

            @Override
            public void onLoad(ArrayList<String> strings) {
                if (strings != null) {
                    BeastsLairZone.bossList = strings;
                }
            }
        });
    }

    @Override
    protected boolean canIncludeTreasureRow() {
        return false;
    }

    @Override
    protected boolean canIncludeFinalCampfireRow() {
        return false;
    }

    @Override
    protected boolean canIncludeEarlyRows() {
        return false;
    }

    @Override
    public void manualRoomPlacement(Random rng) {
        for (MapRoomNode node : nodes) {
            node.setRoom(new ForcedEventRoom(() -> {
                AbstractEvent eventInfo = EventUtils.getEvent(BeastsLairEvent.ID);
                BeastsLairEvent event = new BeastsLairEvent();
                AdditionalEventParameters.additionalParameters.set(event, AdditionalEventParameters.additionalParameters.get(eventInfo));
                return event;
            }));
        }
    }

    @Override
    public int changeRareCardRewardChance(int rareCardRewardChance) {
        return 50;
    }

    @Override
    public int changeUncommonCardRewardChance(int uncommonCardRewardChance) {
        return 50;
    }

    @Override
    public ArrayList<RewardItem> getAdditionalRewards() {
        ArrayList<RewardItem> rewards = new ArrayList<>();
        rewards.add(new RewardItem(AbstractDungeon.returnRandomRelic(AbstractDungeon.returnRandomRelicTier())));

        return rewards;
    }

    @Override
    public AbstractZone copy() {
        return new BeastsLairZone();
    }

    @Override
    public Color getColor() {
        return Color.RED.cpy();
    }

    @Override
    public boolean canSpawn() {
        return AbstractDungeon.actNum >= 2 && !bossList.isEmpty();
    }

    @SpirePatch2(clz = AbstractDungeon.class, method = "dungeonTransitionSetup")
    public static class DungeonTransitionSetupPatch {
        public static void Prefix() {
            bossList.clear();
            bossList.addAll(AbstractDungeon.bossList);
        }
    }


}
