package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.fishing.L2FishingRod;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FishingRodsData extends XmlDocumentParser
{
    private static final Map<Integer, L2FishingRod> _fishingRods = new HashMap<>();

    protected static FishingRodsData _instance;

    private FishingRodsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static FishingRodsData getInstance()
    {
        return _instance == null ? _instance = new FishingRodsData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _fishingRods.clear();
        parseFile(FilePath.FISH_RODS_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _fishingRods.size() + " Fishing Rods.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("fishingRod"))
            {
                final StatsSet set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                L2FishingRod fishingRod = new L2FishingRod(set);
                _fishingRods.put(fishingRod.getFishingRodItemId(), fishingRod);
            }
        }
    }

    /**
     * @param itemId
     * @return A fishing Rod by Item Id
     */
    public L2FishingRod getFishingRod(int itemId)
    {
        return _fishingRods.get(itemId);
    }
}