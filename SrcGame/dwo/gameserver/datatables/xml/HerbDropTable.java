package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.world.npc.drop.L2DropCategory;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HerbDropTable extends XmlDocumentParser
{
    private static final Map<Integer, FastList<L2DropCategory>> _herbGroups = new HashMap<>();

    protected static HerbDropTable _instance;

    private HerbDropTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static HerbDropTable getInstance()
    {
        return _instance == null ? _instance =  new HerbDropTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _herbGroups.clear();
        parseFile(FilePath.HERB_DROP);
        _log.log(Level.INFO, "HerbDropTable: Loaded " + _herbGroups.size() + " herbs drop groups.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        L2DropData dropDat;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("HerbDrop"))
            {
                int groupId = Integer.parseInt(element.getAttributeValue("groupId"));
                FastList<L2DropCategory> category;
                if(_herbGroups.containsKey(groupId))
                {
                    category = _herbGroups.get(groupId);
                }
                else
                {
                    category = new FastList<>();
                    _herbGroups.put(groupId, category);
                }

                dropDat = new L2DropData();

                dropDat.setItemId(Integer.parseInt(element.getAttributeValue("itemId")));
                dropDat.setMinDrop(Integer.parseInt(element.getAttributeValue("min")));
                dropDat.setMaxDrop(Integer.parseInt(element.getAttributeValue("max")));
                dropDat.setChance(Integer.parseInt(element.getAttributeValue("chance")));

                int categoryType = Integer.parseInt(element.getAttributeValue("category"));

                if(ItemTable.getInstance().getTemplate(dropDat.getItemId()) == null)
                {
                    _log.log(Level.WARN, "Herb Drop data for undefined item template! GroupId: " + groupId + " itemId: " + dropDat.getItemId());
                    continue;
                }

                boolean catExists = false;
                for(L2DropCategory cat : category)
                {
                    // if the category exists, add the drop to this category.
                    if(cat.getCategoryType() == categoryType)
                    {
                        cat.addDropData(dropDat, false);
                        catExists = true;
                        break;
                    }
                }
                // if the category doesn't exit, create it and add the drop
                if(!catExists)
                {
                    L2DropCategory cat = new L2DropCategory(categoryType);
                    cat.addDropData(dropDat, false);
                    category.add(cat);
                }
            }
        }
    }

    public FastList<L2DropCategory> getHerbDroplist(int groupId)
    {
        return _herbGroups.get(groupId);
    }
}