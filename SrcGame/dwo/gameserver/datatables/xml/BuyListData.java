package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.player.L2TradeList;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static dwo.gameserver.model.player.L2TradeList.L2TradeItem;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 20.11.12
 * Time: 20:10
 */

public class BuyListData extends XmlDocumentParser
{
    private static final Map<Integer, Map<Integer, L2TradeList>> _lists = new HashMap<>();

    protected static BuyListData _instance;

    private BuyListData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static BuyListData getInstance()
    {
        return _instance == null ? _instance = new BuyListData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _lists.clear();
        parseFile(FilePath.BUYLIST_DATA);
        _log.log(Level.INFO, "Loaded " + _lists.size() + " NPCs with buylist's.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("tradelist"))
            {
                final int[] npcs = parseIntArray(element.getAttributeValue("npcs"));
                final Map<Integer, L2TradeList> tempTradeMap = new HashMap<>();

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("shop"))
                    {
                        final int shopId = Integer.parseInt(element1.getAttributeValue("id"));
                        L2TradeList list = new L2TradeList(shopId);
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("item"))
                            {
                                int itemId = Integer.parseInt(element2.getAttributeValue("id"));
                                L2TradeItem tradeItem = new L2TradeItem(itemId);
                                tradeItem.setPrice(ItemTable.getInstance().getTemplate(itemId).getReferencePrice());

                                if(element2.getAttributeValue("count") != null)
                                {
                                    int count = Integer.parseInt(element2.getAttributeValue("count"));
                                    tradeItem.setMaxCount(count);
                                    tradeItem.setCurrentCount(count);
                                }
                                if(element2.getAttributeValue("time") != null)
                                {
                                    tradeItem.setRestoreDelay(Integer.parseInt(element2.getAttributeValue("time")));
                                }
                                list.addItem(tradeItem);
                            }
                        }
                        if(!list.getItems().isEmpty())
                        {
                            tempTradeMap.put(shopId, list);
                        }
                    }
                }
                for(int npcId : npcs)
                {
                    if(_lists.containsKey(npcId))
                    {
                        _log.log(Level.ERROR, getClass().getSimpleName() + ": Shop for NpcId: " + npcId + " already exists in MAP!");
                    }
                    else
                    {
                        _lists.put(npcId, tempTradeMap);
                    }
                }
            }
        }
    }

    /***
     * @param npcId NpcId
     * @return список всех бай-листов к указанного NPC
     */
    public Collection<L2TradeList> getBuyLists(int npcId)
    {
        if(_lists.containsKey(npcId))
        {
            return _lists.get(npcId).values();
        }
        return null;
    }

    /***
     * @param npcId NpcId
     * @param shopId Id магазина у NPC
     * @return L2TradeList бай-лист по указанному shopId у NPC
     */
    public L2TradeList getBuyList(int npcId, int shopId)
    {
        if(_lists.containsKey(npcId))
        {
            Map<Integer, L2TradeList> temp = _lists.get(npcId);
            if(temp.containsKey(shopId))
            {
                return temp.get(shopId);
            }
        }
        return null;
    }

    /***
     * @param npcId NpcID проверяемого NPC
     * @return {@code true} если указанный NPC имеет байлисты
     */
//    public boolean isMerchanter(int npcId)
//    {
//        return _lists.containsKey(npcId);
//    }
}