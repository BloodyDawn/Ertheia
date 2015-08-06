package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.holders.SpawnHolder;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DynamicSpawnData extends XmlDocumentParser
{
    private static final Map<String, SpawnsHolder> _holders = new HashMap<>();

    protected static DynamicSpawnData _instance;

    private DynamicSpawnData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static DynamicSpawnData getInstance()
    {
        return _instance == null ? _instance = new DynamicSpawnData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _holders.clear();
        parseDirectory(FilePath.DYNAMIC_SPAWN_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _holders.size() + " Spawn Holders");
    }

    @Override
    public void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("spawns"))
            {
                final String name0 = element.getAttributeValue("name");
                final SpawnsHolder holder = new SpawnsHolder(name0);
                for(Element element1 : element.getChildren())
                {
                    final String name1 =  element1.getName();
                    if(name1.equalsIgnoreCase("spawn"))
                    {
                        final StatsSet set = new StatsSet();
                        final List<Attribute> attributes = element1.getAttributes();
                        for(final Attribute attribute : attributes)
                        {
                            set.set(attribute.getName().toLowerCase(), attribute.getValue());
                        }
                        holder.addHolder(new SpawnHolder(set));
                    }
                }
                _holders.put(name0, holder);
            }
        }
    }

    public SpawnsHolder getSpawnsHolder(String name)
    {
        return _holders.get(name);
    }
}