package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CharStartingItems extends XmlDocumentParser
{
    private static final Map<Integer, List<ItemHolder>> _initialEquipmentList = new HashMap<>();

    protected static CharStartingItems _instance;

    protected CharStartingItems()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CharStartingItems getInstance()
    {
        return _instance == null ? _instance = new CharStartingItems() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _initialEquipmentList.clear();
        parseFile(FilePath.CHAR_STARTING_ITEMS_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + _initialEquipmentList.size() + " initial Equipment data.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("equipment"))
            {
                parseEquipment(element);
            }
        }
    }

    /**
     * @param rootElement parse an initial equipment and add it to {@link #_initialEquipmentList}
     */
    private void parseEquipment(Element rootElement)
    {
        Integer classId = Integer.parseInt(rootElement.getAttributeValue("classId"));
        List<ItemHolder> equipList = new ArrayList<>();
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("item"))
            {
                final StatsSet set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                equipList.add(new ItemHolder(set));
            }
        }
        _initialEquipmentList.put(classId, equipList);
    }

    /**
     * @param cId the class Id for the required initial equipment.
     * @return the initial equipment for the given class Id.
     */
    public List<ItemHolder> getEquipmentList(int cId)
    {
        if(_initialEquipmentList.containsKey(cId))
        {
            List<ItemHolder> tempItemHolder = new ArrayList<>();
            tempItemHolder.addAll(_initialEquipmentList.get(cId));
            tempItemHolder.addAll(_initialEquipmentList.get(-1));
            return tempItemHolder;
        }
        else
        {
            return null;
        }
    }
}