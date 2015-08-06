package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.fishing.L2FishingMonster;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishingMonstersData extends XmlDocumentParser
{
    private static final Map<Integer, L2FishingMonster> _fishingMonstersData = new HashMap<>();

    protected static FishingMonstersData _instance;

    protected FishingMonstersData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static FishingMonstersData getInstance()
    {
        return _instance == null ? _instance = new FishingMonstersData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _fishingMonstersData.clear();
        parseFile(FilePath.FISH_MONSTERS_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _fishingMonstersData.size() + " Fishing Monsters.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("fishingMonster"))
            {
                final StatsSet set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                L2FishingMonster fishingMonster = new L2FishingMonster(set);
                _fishingMonstersData.put(fishingMonster.getFishingMonsterId(), fishingMonster);
            }
        }
    }

    /**
     * Gets the fishing monster.
     * @param lvl the fisherman level
     * @return a fishing monster given the fisherman level
     */
    public L2FishingMonster getFishingMonster(int lvl)
    {
        for(L2FishingMonster fishingMonster : _fishingMonstersData.values())
        {
            if(lvl >= fishingMonster.getUserMinLevel() && lvl <= fishingMonster.getUserMaxLevel())
            {
                return fishingMonster;
            }
        }
        return null;
    }

    /**
     * Gets the fishing monster by Id.
     * @param id the fishing monster Id
     * @return the fishing monster by Id
     */
    public L2FishingMonster getFishingMonsterById(int id)
    {
        if(_fishingMonstersData.containsKey(id))
        {
            return _fishingMonstersData.get(id);
        }
        return null;
    }
}