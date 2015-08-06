package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: GenCloud
 * Date: 24.03.2015
 * Team: La2Era Team
 */
public class ItemPriceData extends XmlDocumentParser
{
    private static final Map<Integer, Long> _itemPrices = new HashMap<>();

    private ItemPriceData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemPriceData getInstance()
    {
        return SingletonHolder._instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _itemPrices.clear();
        parseFile(FilePath.ITEM_PRICE_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _itemPrices.size() + " prices for item's.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for (Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if (name.equalsIgnoreCase("set"))
            {
                int itemId = Integer.parseInt(element.getAttributeValue("itemId"));
                long price = Long.parseLong(element.getAttributeValue("price"));
                _itemPrices.put(itemId, price);
            }
        }
    }

    public long getPrice(int itemId)
    {
        long price = 0;
        if (_itemPrices.containsKey(itemId))
        {
            price = _itemPrices.get(itemId);
        }
        return price;
    }

    private static class SingletonHolder
    {
        protected static final ItemPriceData _instance = new ItemPriceData();
    }
}