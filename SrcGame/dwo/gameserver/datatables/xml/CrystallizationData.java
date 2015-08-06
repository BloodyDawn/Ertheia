package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.holders.ItemChanceHolder;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.CrystalGrade;
import javolution.util.FastList;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * @author ANZO
 * Date: 05.03.12
 * Time: 21:47
 */

public class CrystallizationData extends XmlDocumentParser
{
    private static final Map<CrystalGrade, Map<Integer, FastList<ItemChanceHolder>>> data = new HashMap<>();

    protected static CrystallizationData _instance;

    private CrystallizationData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CrystallizationData getInstance()
    {
        return _instance == null ? _instance = new CrystallizationData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        data.clear();
        parseFile(FilePath.CRYSTALLIZATION_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + data.size() + " Crystal Type's crystallization data.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        CrystalGrade crystalGrade;
        int crystalCount;
        Map<Integer, FastList<ItemChanceHolder>> crystallizationData;
        FastList<ItemChanceHolder> itemsData;
        ItemChanceHolder crHolder;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("grade"))
            {
                crystalGrade = CrystalGrade.valueOf(element.getAttributeValue("type"));
                crystallizationData = new HashMap<>();
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("crystal"))
                    {
                        crystalCount = Integer.parseInt(element1.getAttributeValue("count"));
                        itemsData = new FastList<>();

                        // Добавляем шансовые материалы кристаллизации
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("item"))
                            {
                                crHolder = new ItemChanceHolder(Integer.parseInt(element2.getAttributeValue("id")), Integer.parseInt(element2.getAttributeValue("count")), Double.parseDouble(element2.getAttributeValue("chance")));
                                itemsData.add(crHolder);
                            }
                        }
                        crystallizationData.put(crystalCount, itemsData);
                    }
                }
                data.put(crystalGrade, crystallizationData);
            }
        }
    }

    /**
     * @param item инстанс предмета
     * @return список продуктов, на которые кристаллизируется указанный предмет
     */
    public FastList<ItemChanceHolder> getProductsForItem(L2ItemInstance item)
    {
        Map<Integer, FastList<ItemChanceHolder>> temp = data.get(item.getItem().getItemGradeRPlus());

        return temp.containsKey(item.getItem().getCrystalCount()) ? temp.get(item.getItem().getCrystalCount()) : null;
    }

    /**
     * @param item инстанс предмета
     * @return {@code true} если в таблице есть данные о кристаллизации предмета
     */
    public boolean isItemExistInTable(L2ItemInstance item)
    {
        if(!data.containsKey(item.getItem().getItemGradeRPlus()))
        {
            return false;
        }
        // берем getItemGradeRPlus т.к для R R95 R99 формула одна.
        return data.get(item.getItem().getItemGradeRPlus()).containsKey(item.getItem().getCrystalCount());
    }
}