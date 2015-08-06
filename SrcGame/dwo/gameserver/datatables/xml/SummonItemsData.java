package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.skills.L2SummonItem;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SummonItemsData extends XmlDocumentParser
{
    private static final Map<Integer, L2SummonItem> _summonitems = new HashMap<>();

    protected static SummonItemsData instance;

    private SummonItemsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static SummonItemsData getInstance()
    {
        return instance == null ? instance = new SummonItemsData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _summonitems.clear();
        parseFile(FilePath.SUMMON_ITEMS_DATA);
        _log.log(Level.INFO, "SummonItemsData: Loaded " + _summonitems.size() + " Summon Items.");
    }

    @Override
    public void parseDocument(Element rootElement)
    {
        StatsSet set;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("item"))
            {
                set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }

                _summonitems.put(set.getInteger("id"), new L2SummonItem(set));
            }
        }
    }

    public L2SummonItem getSummonItem(int itemId)
    {
        return _summonitems.get(itemId);
    }
}