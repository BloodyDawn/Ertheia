package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.shapeshift.ShapeShiftData;
import dwo.gameserver.model.items.shapeshift.ShapeShiftingWindowType;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 27.11.12
 * Time: 20:07
 */

public class ShapeShiftingItemsData extends XmlDocumentParser
{
    private static final HashMap<Integer, ShapeShiftData> _initialList = new HashMap<>();

    protected static ShapeShiftingItemsData instance;

    protected ShapeShiftingItemsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ShapeShiftingItemsData getInstance()
    {
        return instance == null ? instance = new ShapeShiftingItemsData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _initialList.clear();
        parseFile(FilePath.SHAPE_SHIFTING_ITEMS);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _initialList.size() + " ShapeShiftingItems data.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("type"))
            {
                ShapeShiftingWindowType shapeShiftingWindow = ShapeShiftingWindowType.valueOf(element.getAttributeValue("shapeShiftingWindow"));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("item"))
                    {
                        int itemId = Integer.parseInt(element1.getAttributeValue("id"));
                        _initialList.put(itemId, new ShapeShiftData(shapeShiftingWindow, element1));
                    }
                }
            }
        }
    }

    public ShapeShiftData getShapeShiftItem(int id)
    {
        return _initialList.get(id);
    }
}