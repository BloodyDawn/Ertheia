package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.instancemanager.DayNightSpawnManager;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import javolution.util.FastMap;
import javolution.util.FastSet;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class SpawnTable extends XmlDocumentParser
{
    private static final Map<Integer, Set<L2Spawn>> _spawnTable = new FastMap<Integer, Set<L2Spawn>>().shared();

    protected static SpawnTable instance;

    private SpawnTable()
    {
        if(!Config.ALT_DEV_NO_SPAWNS)
        {
            try {
                load();
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static SpawnTable getInstance()
    {
        return instance == null ? instance = new SpawnTable() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _spawnTable.clear();
        parseDirectory(FilePath.STATIC_SPAWN_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _spawnTable.size() + " spawn's.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("spawns"))
            {
                for(Element element1 : element.getChildren())
                {
                    L2NpcTemplate npcTemplate;
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("npc"))
                    {
                        int npcId = Integer.parseInt(element1.getAttributeValue("id"));
                        npcTemplate = NpcTable.getInstance().getTemplate(npcId);

                        // Проверяем, чтобы в спауне не было мусора
                        switch(npcTemplate.getType())
                        {
                            case "L2RaidBoss":
                            case "L2SiegeGuard":
                            case "L2Pet":
                            case "L2BabyPet":
                                _log.log(Level.WARN, getClass().getSimpleName() + ": WARNING! Npc with templateType " + npcTemplate.getType() + " (NpcId:" + npcId + ") detected in spawnlist!");
                                continue;
                        }

                        for(Element element2 : element1.getChildren())
                        {
                            L2Spawn spawnDat;
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("spawn"))
                            {
                                try
                                {
                                    spawnDat = new L2Spawn(npcTemplate);
                                    spawnDat.setLocx(Integer.parseInt(element2.getAttributeValue("x")));
                                    spawnDat.setLocy(Integer.parseInt(element2.getAttributeValue("y")));
                                    spawnDat.setLocz(Integer.parseInt(element2.getAttributeValue("z")));
                                    spawnDat.setHeading(Integer.parseInt(element2.getAttributeValue("heading")));
                                    spawnDat.setAmount(1);
                                    if(element2.getAttributeValue("respawn") != null)
                                    {
                                        spawnDat.setRespawnDelay(Integer.parseInt(element2.getAttributeValue("respawn")));
                                    }

                                    // Проверяем время суток спауна
                                    if(element2.getAttributeValue("periodOfDay") == null)
                                    {
                                        spawnDat.init();
                                    }
                                    else
                                    {
                                        int periodOfDay = Integer.parseInt(element2.getAttributeValue("periodOfDay"));
                                        if(periodOfDay == 1)
                                        {
                                            DayNightSpawnManager.getInstance().addDayCreature(spawnDat);
                                        }
                                        else if(periodOfDay == 2)
                                        {
                                            DayNightSpawnManager.getInstance().addNightCreature(spawnDat);
                                        }
                                    }
                                    addNewSpawn(spawnDat);
                                }
                                catch(Exception e)
                                {
                                    _log.log(Level.ERROR, getClass().getSimpleName() + ": NpcTemplate with type " + npcTemplate.getType() + " didn't finded in declarations!");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void reload()
    {
        for(Set<L2Spawn> spawn : _spawnTable.values())
        {
            for(L2Spawn spawnDat : spawn)
            {
                spawnDat.stopRespawn();
            }
        }
        try 
        {
            load();
        } 
        catch (JDOMException | IOException e) 
        {
            e.printStackTrace();
        }
    }

    /**
     * Add a spawn to the spawn set if present, otherwise add a spawn set and add the spawn to the newly created spawn set.
     * @param spawn the NPC spawn to add
     */
    public void addNewSpawn(L2Spawn spawn)
    {
        if(!_spawnTable.containsKey(spawn.getNpcId()))
        {
            _spawnTable.put(spawn.getNpcId(), new FastSet<L2Spawn>().shared());
        }
        _spawnTable.get(spawn.getNpcId()).add(spawn);
    }

    /**
     * Remove a spawn from the spawn set, if the spawn set is empty, remove it as well.
     * @param spawn the NPC spawn to remove
     * @return {@code true} if the spawn was successfully removed, {@code false} otherwise
     */
    public boolean deleteSpawn(L2Spawn spawn)
    {
        if(_spawnTable.containsKey(spawn.getNpcId()))
        {
            Set<L2Spawn> set = _spawnTable.get(spawn.getNpcId());
            if(set == null)
            {
                return false;
            }

            boolean removed = set.remove(spawn);
            if(set.isEmpty())
            {
                _spawnTable.remove(spawn.getNpcId());
            }
            return removed;
        }
        return false;
    }

    /**
     * Get the spawns for the NPC Id.
     * @param npcId the NPC Id
     * @return the spawn set for the given npcId
     */
    public Set<L2Spawn> getSpawns(int npcId)
    {
        return _spawnTable.containsKey(npcId) ? _spawnTable.get(npcId) : Collections.<L2Spawn>emptySet();
    }

    /**
     * Get the first NPC spawn.
     * @param npcId the NPC Id to search
     * @return the first not null spawn, if any
     */
    public L2Spawn getFirstSpawn(int npcId)
    {
        if(_spawnTable.containsKey(npcId))
        {
            for(L2Spawn spawn : _spawnTable.get(npcId))
            {
                if(spawn != null)
                {
                    return spawn;
                }
            }
        }
        return null;
    }
}