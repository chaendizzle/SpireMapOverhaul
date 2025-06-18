package spireMapOverhaul.util;

import basemod.DevConsole;
import basemod.devcommands.ConsoleCommand;
import spireMapOverhaul.SpireAnniversary6Mod;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class FilterBiomesCommand extends ConsoleCommand {

    final String ZONE_ID_ALL = "all";
    final String ZONE_ID_NONE = "none";

    public FilterBiomesCommand() {
        minExtraTokens = 1;
        simpleCheck = true;
    }

    @Override
    protected void execute(String[] tokens, int depth) {
        if (tokens.length != 2) {
            DevConsole.log("Specify the ID of the zone, or 'all', or 'none', to set the mod config to filter for.");
            return;
        }
        ArrayList<String> zoneIds = SpireAnniversary6Mod.unfilteredAllZones.stream().map(z->z.id).collect(Collectors.toCollection(ArrayList::new));
        if (tokens[1].equals(ZONE_ID_ALL))
        {
            for (String zid : zoneIds)
            {
                SpireAnniversary6Mod.setFilterConfig(zid, true);
            }
            return;
        }
        if (tokens[1].equals(ZONE_ID_NONE))
        {
            for (String zid : zoneIds)
            {
                SpireAnniversary6Mod.setFilterConfig(zid, false);
            }
            return;
        }
        if (zoneIds.contains(tokens[1]))
        {
            for (String zid : zoneIds)
            {
                SpireAnniversary6Mod.setFilterConfig(zid, false);
            }
            SpireAnniversary6Mod.setFilterConfig(tokens[1], true);
        }
        else
        {
            DevConsole.log("No matching zone id found");
        }
    }

    @Override
    protected ArrayList<String> extraOptions(String[] tokens, int depth) {
        ArrayList<String> zones = SpireAnniversary6Mod.unfilteredAllZones.stream().map(z->z.id).collect(Collectors.toCollection(ArrayList::new));
        zones.add(ZONE_ID_ALL);
        zones.add(ZONE_ID_NONE);
        return zones;
    }
}
