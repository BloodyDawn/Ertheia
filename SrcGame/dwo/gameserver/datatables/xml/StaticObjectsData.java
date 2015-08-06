package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2StaticObjectInstance;
import dwo.gameserver.model.actor.templates.L2CharTemplate;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StaticObjectsData extends XmlDocumentParser
{
    private static final Map<Integer, L2StaticObjectInstance> _staticObjects = new HashMap<>();

    protected static StaticObjectsData instance;

    private StaticObjectsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static StaticObjectsData getInstance()
    {
        return instance == null ? instance = new StaticObjectsData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        if(!_staticObjects.isEmpty())
        {
            for(L2StaticObjectInstance object : _staticObjects.values())
            {
                object.getLocationController().delete();
            }
        }
        _staticObjects.clear();
        parseFile(FilePath.STATIC_OBJECTS_DATA);
        _log.info("StaticObjectsData: Loaded " + _staticObjects.size() + " StaticObject Templates.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        StatsSet set;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("object"))
            {
                set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                addObject(set);
            }
        }
    }

    /**
     * Initialize an static object based on the stats set and add it to the map.
     * @param set the stats set to add.
     */
    private void addObject(StatsSet set)
    {
        L2StaticObjectInstance obj = new L2StaticObjectInstance(IdFactory.getInstance().getNextId(), new L2CharTemplate(new StatsSet()), set.getInteger("id"));
        obj.setType(set.getInteger("type", 0));
        obj.setName(set.getString("name"));
        obj.setMap(set.getString("texture", "none"), set.getInteger("map_x", 0), set.getInteger("map_y", 0));
        obj.getLocationController().spawn(set.getInteger("x"), set.getInteger("y"), set.getInteger("z"));
        _staticObjects.put(obj.getObjectId(), obj);
    }

    /**
     * @return a collection of static objects.
     */
    public Collection<L2StaticObjectInstance> getStaticObjects()
    {
        return _staticObjects.values();
    }
}