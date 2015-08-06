package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.holders.FortFacilitySpawnHolder;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.residence.fort.Fort;
import dwo.gameserver.model.world.residence.fort.FortFacilityType;
import dwo.gameserver.model.world.residence.fort.FortSpawnType;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.13
 * Time: 18:25
 */

public class FortSpawnList extends XmlDocumentParser
{
    // Таблицы спауна
    protected Map<Integer, List<FortFacilitySpawnHolder>> _onSiegeStartSpawn = new HashMap<>();
    protected Map<Integer, List<FortFacilitySpawnHolder>> _alwaysSpawn = new HashMap<>();
    protected Map<Integer, List<FortFacilitySpawnHolder>> _onSiegeDespawn = new HashMap<>();
    protected Map<Integer, List<FortFacilitySpawnHolder>> _onSiegeDespawn10min = new HashMap<>();
    protected Map<Integer, List<FortFacilitySpawnHolder>> _onSiegeEndSpawn = new HashMap<>();

    // Таблицы поиска
    protected Map<Integer, Integer> _castleEnvoyTable = new HashMap<>();

    protected static FortSpawnList _instance;

    private FortSpawnList()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static FortSpawnList getInstance()
    {
        return _instance == null ? _instance = new FortSpawnList() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _onSiegeStartSpawn.clear();
        _alwaysSpawn.clear();
        _onSiegeDespawn.clear();
        _onSiegeDespawn10min.clear();
        _onSiegeEndSpawn.clear();
        parseFile(FilePath.FORT_SPAWNLIST);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _castleEnvoyTable.size() + " castle envoy NpcId's.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _onSiegeStartSpawn.size() + " fort siege spawn data's.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _alwaysSpawn.size() + " fort always spawn data's.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _onSiegeDespawn.size() + " fort on siege despawn data's.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _onSiegeDespawn10min.size() + " fort on siege despawn after 10 min data's.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _onSiegeEndSpawn.size() + " fort on siege end spawn data's.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("fort"))
            {
                final int fortId = Integer.parseInt(element.getAttributeValue("id"));
                List<FortFacilitySpawnHolder> onSiegeHolders = new ArrayList<>();
                List<FortFacilitySpawnHolder> alwaysHolders = new ArrayList<>();
                List<FortFacilitySpawnHolder> onSiegeDespawnHolders = new ArrayList<>();
                List<FortFacilitySpawnHolder> onSiegeDespawn10minHolders = new ArrayList<>();
                List<FortFacilitySpawnHolder> onSiegeEndHolders = new ArrayList<>();

                for(Element element1 : element.getChildren())
                {
                    FortSpawnType spawnType;
                    final String name1 =  element1.getName();
                    if(name1.equalsIgnoreCase("spawnType"))
                    {
                        spawnType = FortSpawnType.valueOf(element1.getAttributeValue("type"));

                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("npc"))
                            {
                                final int npcId = Integer.parseInt(element2.getAttributeValue("id"));
                                FortFacilityType facilityType = null;
                                int facilityLevel = 0;

                                if(element2.getAttributeValue("facilityType") != null)
                                {
                                    facilityType = FortFacilityType.values()[Integer.parseInt(element2.getAttributeValue("facilityType"))];
                                    facilityLevel = Integer.parseInt(element2.getAttributeValue("facilityLevel"));
                                }

                                if(spawnType == FortSpawnType.SPAWN_AFTER_SIEGE)
                                {
                                    _castleEnvoyTable.put(npcId, Integer.parseInt(element2.getAttributeValue("castleId")));
                                }

                                L2Spawn spawn;
                                L2NpcTemplate template;
                                List<L2Spawn> spawnList = new ArrayList<>();
                                for(Element element3 : element2.getChildren())
                                {
                                    final String name3 = element3.getName();
                                    if(name3.equalsIgnoreCase("spawn"))
                                    {
                                        int x = Integer.parseInt(element3.getAttributeValue("x"));
                                        int y = Integer.parseInt(element3.getAttributeValue("y"));
                                        int z = Integer.parseInt(element3.getAttributeValue("z"));
                                        int heading = Integer.parseInt(element3.getAttributeValue("heading"));
                                        int respawnDelay = 0;
                                        if(element3.getAttributeValue("respawnDelay") != null)
                                        {
                                            respawnDelay = Integer.parseInt(element3.getAttributeValue("respawnDelay"));
                                        }
                                        try
                                        {
                                            template = NpcTable.getInstance().getTemplate(npcId);
                                            spawn = new L2Spawn(template);
                                            spawn.setAmount(1);
                                            spawn.setLocx(x);
                                            spawn.setLocy(y);
                                            spawn.setLocz(z);
                                            spawn.setHeading(heading);
                                            spawn.setRespawnDelay(respawnDelay);
                                            spawn.setLocation(0);
                                            spawn.setRespawnDelay(60);
                                            spawnList.add(spawn);
                                        }
                                        catch(Exception e)
                                        {
                                            _log.log(Level.ERROR, getClass().getSimpleName() + ": Error while creating L2Spawn data for npcId : " + npcId + " , guard of fortId: " + fortId);
                                        }
                                    }
                                }
                                switch(spawnType)
                                {
                                    case SPAWN_ON_SIEGE:
                                        onSiegeHolders.add(new FortFacilitySpawnHolder(facilityType, facilityLevel, spawnList));
                                        break;
                                    case SPAWN_ALWAYS:
                                        alwaysHolders.add(new FortFacilitySpawnHolder(facilityType, facilityLevel, spawnList));
                                        break;
                                    case DESPAWN_ON_SIEGE:
                                        onSiegeDespawnHolders.add(new FortFacilitySpawnHolder(facilityType, facilityLevel, spawnList));
                                        break;
                                    case DESPAWN_ON_SIEGE_AFTER_10MIN:
                                        onSiegeDespawn10minHolders.add(new FortFacilitySpawnHolder(facilityType, facilityLevel, spawnList));
                                        break;
                                    case SPAWN_AFTER_SIEGE:
                                        onSiegeEndHolders.add(new FortFacilitySpawnHolder(facilityType, facilityLevel, spawnList));
                                        break;
                                }
                            }
                        }
                    }
                }
                _onSiegeStartSpawn.put(fortId, onSiegeHolders);
                _alwaysSpawn.put(fortId, alwaysHolders);
                _onSiegeDespawn.put(fortId, onSiegeDespawnHolders);
                _onSiegeDespawn10min.put(fortId, onSiegeDespawn10minHolders);
                _onSiegeEndSpawn.put(fortId, onSiegeEndHolders);
            }
        }
    }

    /***
     * @param fort форт, для которого нужно отдать список спауна гвардов
     * @param spawnType тип спауна, список которого требуется
     * @return список спауна гвардов в указанном форте
     */
    public List<FortFacilitySpawnHolder> getSpawnForFort(Fort fort, FortSpawnType spawnType)
    {
        switch(spawnType)
        {
            case SPAWN_ON_SIEGE:
                return _onSiegeStartSpawn.get(fort.getFortId());
            case SPAWN_ALWAYS:
                return _alwaysSpawn.get(fort.getFortId());
            case DESPAWN_ON_SIEGE:
                return _onSiegeDespawn.get(fort.getFortId());
            case DESPAWN_ON_SIEGE_AFTER_10MIN:
                return _onSiegeDespawn10min.get(fort.getFortId());
            case SPAWN_AFTER_SIEGE:
                return _onSiegeEndSpawn.get(fort.getFortId());
        }
        return null;
    }

    /***
     * @param npcId NpcId посланника замка
     * @return ID замка, к которому принадлежит посланник
     */
    public int getCastleForEnvoyId(int npcId)
    {
        return _castleEnvoyTable.get(npcId);
    }
}