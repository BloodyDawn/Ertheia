package dwo.gameserver.instancemanager;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.instancemanager.castle.CastleManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.instancemanager.fort.FortManager;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.teleport.TeleportWhereType;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.model.world.residence.castle.Castle;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import dwo.gameserver.model.world.residence.clanhall.type.ClanHallSiegable;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.zone.L2MapRegion;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.model.world.zone.type.L2ClanHallZone;
import dwo.gameserver.model.world.zone.type.L2RespawnZone;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapRegionManager extends XmlDocumentParser
{
    private static final String defaultRespawn = "talking_island_town";

    private static final Map<String, L2MapRegion> _regions = new HashMap<>();

    private MapRegionManager()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static MapRegionManager getInstance()
    {
        return SingletonHolder._instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _regions.clear();
        parseDirectory(FilePath.MAP_REGION_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _regions.size() + " map regions.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        String name, town;
        int locId, castle, bbs;

        for(Element element : rootElement.getChildren())
        {
            final String name1 = element.getName();
            if(name1.equalsIgnoreCase("region"))
            {
                name = element.getAttributeValue("name");
                town = element.getAttributeValue("town");
                locId = Integer.parseInt(element.getAttributeValue("locId"));
                castle = Integer.parseInt(element.getAttributeValue("castle"));
                bbs = Integer.parseInt(element.getAttributeValue("bbs"));

                L2MapRegion region = new L2MapRegion(name, town, locId, castle, bbs);
                for(Element element1 : element.getChildren())
                {
                    final String name2 = element1.getName();
                    if(name2.equalsIgnoreCase("respawnPoint"))
                    {
                        int spawnX = Integer.parseInt(element1.getAttributeValue("X"));
                        int spawnY = Integer.parseInt(element1.getAttributeValue("Y"));
                        int spawnZ = Integer.parseInt(element1.getAttributeValue("Z"));

                        boolean other = Boolean.parseBoolean(element1.getAttributeValue("isOther"));
                        boolean chaotic = Boolean.parseBoolean(element1.getAttributeValue("isChaotic"));
                        boolean banish = Boolean.parseBoolean(element1.getAttributeValue("isBanish"));

                        if(other)
                        {
                            region.addOtherSpawn(spawnX, spawnY, spawnZ);
                        }
                        else if(chaotic)
                        {
                            region.addChaoticSpawn(spawnX, spawnY, spawnZ);
                        }
                        else if(banish)
                        {
                            region.addBanishSpawn(spawnX, spawnY, spawnZ);
                        }
                        else
                        {
                            region.addSpawn(spawnX, spawnY, spawnZ);
                        }
                    }
                    else if(name2.equalsIgnoreCase("map"))
                    {
                        region.addMap(Integer.parseInt(element1.getAttributeValue("X")),
                                Integer.parseInt(element1.getAttributeValue("Y")));
                    }
                    else if(name2.equalsIgnoreCase("banned"))
                    {
                        region.addBannedRace(element1.getAttributeValue("race"), element1.getAttributeValue("point"));
                    }
                }
                _regions.put(name, region);
            }
        }
    }

    public L2MapRegion getMapRegion(int locX, int locY)
    {
        for(L2MapRegion region : _regions.values())
        {
            if(region.isZoneInRegion(getMapRegionX(locX), getMapRegionY(locY)))
            {
                return region;
            }
        }
        return null;
    }

    public int getMapRegionLocId(int locX, int locY)
    {
        L2MapRegion region = getMapRegion(locX, locY);
        if(region != null)
        {
            return region.getLocId();
        }
        return 0;
    }

    public L2MapRegion getMapRegion(Location loc)
    {
        return getMapRegion(loc.getX(), loc.getY());
    }

    /**
     * @param regionName название региона
     * @return регион карты, если существует, если нет - то null
     */
    public L2MapRegion getMapRegionByName(String regionName)
    {
        return _regions.get(regionName);
    }

    public int getMapRegionLocId(L2Object obj)
    {
        return getMapRegionLocId(obj.getX(), obj.getY());
    }

    public int getMapRegionX(int posX)
    {
        return (posX >> 15) + 9 + 11;// + centerTileX;
    }

    public int getMapRegionY(int posY)
    {
        return (posY >> 15) + 10 + 8;// + centerTileX;
    }

    /**
     * Get town name by character position
     * @return String
     */
    public String getClosestTownName(Location loc)
    {
        L2MapRegion region = getMapRegion(loc);

        if(region == null)
        {
            return "Aden Castle Town";
        }

        return region.getTownName();
    }

    public int getAreaCastle(L2Character activeChar)
    {
        L2MapRegion region = getMapRegion(activeChar.getLoc());

        if(region == null)
        {
            return 0;
        }

        return region.getCastle();
    }

    public Location getTeleToLocation(L2Character activeChar, TeleportWhereType teleportWhere)
    {
        Location loc;

        if(activeChar instanceof L2PcInstance)
        {
            L2PcInstance player = (L2PcInstance) activeChar;

            Castle castle = null;
            Fort fort = null;
            ClanHall clanhall = null;

            if(player.getClan() != null && !player.isFlyingMounted() && !player.isFlying()) // flying players in gracia cant use teleports to aden continent
            {
                switch(teleportWhere)
                {
                    case CLANHALL: // If teleport to clan hall
                        clanhall = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
                        if(clanhall != null)
                        {
                            L2ClanHallZone zone = clanhall.getZone();
                            if(zone != null && !player.isFlyingMounted())
                            {
                                if(player.hasBadReputation())
                                {
                                    return zone.getChaoticSpawnLoc();
                                }
                                return zone.getSpawnLoc();
                            }
                        }
                        break;
                    case CASTLE: // If teleport to castle
                        castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
                        // Otherwise check if player is on castle or fortress ground and player's clan is defender
                        if(castle == null)
                        {
                            castle = CastleManager.getInstance().getCastle(player);
                            if(!(castle != null && castle.getSiege().isInProgress() && castle.getSiege().getDefenderClan(player.getClan()) != null))
                            {
                                castle = null;
                            }
                        }

                        if(castle != null && castle.getCastleId() > 0)
                        {
                            if(player.hasBadReputation())
                            {
                                return castle.getCastleZone().getChaoticSpawnLoc();
                            }
                            return castle.getCastleZone().getSpawnLoc();
                        }
                        break;
                    case FORTRESS: // If teleport to fortress
                        fort = FortManager.getInstance().getFortByOwner(player.getClan());
                        // Otherwise check if player is on castle or fortress ground and player's clan is defender
                        if(fort == null)
                        {
                            fort = FortManager.getInstance().getFort(player);
                            if(!(fort != null && fort.getSiege().isInProgress() && fort.getOwnerClan().equals(player.getClan())))
                            {
                                fort = null;
                            }
                        }

                        if(fort != null && fort.getFortId() > 0)
                        {
                            if(player.hasBadReputation())
                            {
                                return fort.getFortZone().getChaoticSpawnLoc();
                            }
                            return fort.getFortZone().getSpawnLoc();
                        }
                        break;
                    case SIEGE_FLAG: // If teleport to SiegeHQ
                        castle = CastleManager.getInstance().getCastle(player);
                        fort = FortManager.getInstance().getFort(player);
                        clanhall = ClanHallManager.getInstance().getNearbyAbstractHall(activeChar.getX(), activeChar.getY(), 10000);
                        if(castle != null)
                        {
                            if(castle.getSiege().isInProgress())
                            {
                                // Check if player's clan is attacker
                                List<L2Npc> flags = castle.getSiege().getFlag(player.getClan());
                                if(flags != null && !flags.isEmpty())
                                {
                                    // Spawn to flag - Need more work to get player to the nearest flag
                                    L2Npc flag = flags.get(0);
                                    return new Location(flag.getX(), flag.getY(), flag.getZ());
                                }
                            }
                        }
                        else if(fort != null)
                        {
                            if(fort.getSiege().isInProgress())
                            {
                                // Check if player's clan is attacker
                                List<L2Npc> flags = fort.getSiege().getFlag(player.getClan());
                                if(flags != null && !flags.isEmpty())
                                {
                                    // Spawn to flag - Need more work to get player to the nearest flag
                                    L2Npc flag = flags.get(0);
                                    return new Location(flag.getX(), flag.getY(), flag.getZ());
                                }
                            }
                        }
                        else if(clanhall != null && clanhall.isSiegableHall())
                        {
                            ClanHallSiegable sHall = (ClanHallSiegable) clanhall;
                            List<L2Npc> flags = sHall.getSiege().getFlag(player.getClan());
                            if(flags != null && !flags.isEmpty())
                            {
                                L2Npc flag = flags.get(0);
                                return new Location(flag.getX(), flag.getY(), flag.getZ());
                            }
                        }
                        break;
                }
            }
            else if(teleportWhere == TeleportWhereType.CASTLE_BANISH)
            {
                castle = CastleManager.getInstance().getCastle(player);
                if(castle != null)
                {
                    return castle.getCastleZone().getBanishSpawnLoc();
                }
            }
            else if(teleportWhere == TeleportWhereType.FORTRESS_BANISH)
            {
                fort = FortManager.getInstance().getFort(activeChar);
                if(fort != null)
                {
                    return fort.getFortZone().getBanishSpawnLoc();
                }
            }
            else if(teleportWhere == TeleportWhereType.CLANHALL_BANISH)
            {
                clanhall = ClanHallManager.getInstance().getClanHall(activeChar);
                if(clanhall != null)
                {
                    return clanhall.getZone().getBanishSpawnLoc();
                }
            }

            //Badreputation player land out of city
            if(player.hasBadReputation())
            {
                try
                {
                    L2RespawnZone zone = ZoneManager.getInstance().getZone(player, L2RespawnZone.class);
                    if(zone != null)
                    {
                        return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getChaoticSpawnLoc();
                    }
                    return getMapRegion(activeChar.getLoc()).getChaoticSpawnLoc();
                }
                catch(Exception e)
                {
                    if(player.isFlyingMounted()) // prevent flying players to teleport outside of gracia
                    {
                        return _regions.get("union_base_of_kserth").getChaoticSpawnLoc();
                    }
                    return _regions.get(defaultRespawn).getChaoticSpawnLoc();
                }
            }

            //Checking if needed to be respawned in "far" town from the castle;
            castle = CastleManager.getInstance().getCastle(player);
            if(castle != null)
            {
                if(castle.getSiege().isInProgress())
                {
                    // Check if player's clan is participating
                    if(castle.getSiege().checkIsDefender(player.getClan()) || castle.getSiege().checkIsAttacker(player.getClan()))
                    {
                        return castle.getCastleZone().getOtherSpawnLoc();
                    }
                }
            }

            // Checking if in an instance
            if(player.getInstanceId() > 0)
            {
                Instance inst = InstanceManager.getInstance().getInstance(player.getInstanceId());
                if(inst != null)
                {
                    loc = inst.getReturnLoc();
                    if(loc != null && loc.getX() != 0 && loc.getX() != 0 && loc.getZ() != 0)
                    {
                        return loc;
                    }
                }
            }
        }

        // Get the nearest town
        try
        {
            L2RespawnZone zone = ZoneManager.getInstance().getZone(activeChar, L2RespawnZone.class);
            if(zone != null)
            {
                return getRestartRegion(activeChar, zone.getRespawnPoint((L2PcInstance) activeChar)).getSpawnLoc();
            }
            return getMapRegion(activeChar.getLoc()).getSpawnLoc();
        }
        catch(Exception e)
        {
            // port to the Talking Island if no closest town found
            return _regions.get(defaultRespawn).getSpawnLoc();
        }
    }

    public L2MapRegion getRestartRegion(L2Character activeChar, String point)
    {
        try
        {
            L2PcInstance player = (L2PcInstance) activeChar;
            L2MapRegion region = _regions.get(point);

            if(region.getBannedRace().containsKey(player.getRace()))
            {
                getRestartRegion(player, region.getBannedRace().get(player.getRace()));
            }
            return region;
        }
        catch(Exception e)
        {
            return _regions.get(defaultRespawn);
        }
    }

    private static class SingletonHolder
    {
        protected static final MapRegionManager _instance = new MapRegionManager();
    }
}