package dwo.gameserver.datatables.xml;

/**
 * L2GOD Team
 * @author Unafraid
 * User: Keiichi
 * Date: 18.11.2011
 * Time: 20:35:27
 */

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.EnchantItem;
import dwo.gameserver.model.items.EnchantScroll;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantItemData extends XmlDocumentParser
{
    private static final Map<Integer, EnchantScroll> _scrolls = new HashMap<>();
    private static final Map<Integer, EnchantItem> _supports = new HashMap<>();

    protected static EnchantItemData _instance;

    public EnchantItemData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static EnchantItemData getInstance()
    {
        return _instance == null ? _instance = new EnchantItemData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _scrolls.clear();
        _supports.clear();
        parseFile(FilePath.ENCHANT_ITEM_TABLE);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _scrolls.size() + " Enchant Scrolls.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _supports.size() + " Support Items.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        StatsSet set;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("enchant"))
            {
                set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }

                List<Integer> items = new ArrayList<>();

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("item"))
                    {
                        items.add(Integer.parseInt(element1.getAttributeValue("id")));
                    }
                }
                EnchantScroll item = new EnchantScroll(set, items);
                _scrolls.put(item.getScrollId(), item);
            }
            else if(name.equalsIgnoreCase("support"))
            {
                set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }

                List<Integer> items = new ArrayList<>();

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("item"))
                    {
                        items.add(Integer.parseInt(element1.getAttributeValue("id")));
                    }
                }
                EnchantItem item = new EnchantItem(set, items);
                _supports.put(item.getScrollId(), item);
            }
        }
    }

    /**
     * @param scroll
     * @return enchant template for scroll
     */
    public EnchantScroll getEnchantScroll(L2ItemInstance scroll)
    {
        return _scrolls.get(scroll.getItemId());
    }

    /**
     * @param item
     * @return enchant template for support item
     */
    public EnchantItem getSupportItem(L2ItemInstance item)
    {
        return _supports.get(item.getItemId());
    }
}