//package dwo.gameserver.datatables.xml;
//
//import dwo.config.FilePath;
//import dwo.gameserver.engine.documentengine.XmlDocumentParser;
//import dwo.gameserver.model.actor.instance.L2PcInstance;
//import dwo.gameserver.model.items.ItemTable;
//import dwo.gameserver.model.items.base.L2Item;
//import dwo.gameserver.model.items.primeshop.PrimeShopGroup;
//import dwo.gameserver.model.items.primeshop.PrimeShopItem;
//import dwo.gameserver.model.skills.stats.StatsSet;
//import dwo.gameserver.network.game.serverpackets.packet.primeshop.ExBR_ProductInfo;
//import org.jdom2.JDOMException;
//import org.w3c.dom.NamedNodeMap;
//import org.w3c.dom.Node;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//
//TODO: переписать и заюзать
//public class PrimeShopTable extends XmlDocumentParser
//{
//    private final Map<Integer, PrimeShopGroup> _primeItems = new LinkedHashMap<>();
//
//    private PrimeShopTable()
//    {
//        try {
//            load();
//        } catch (JDOMException | IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    public void load() throws JDOMException, IOException {
//        _primeItems.clear();
//        parseFile(FilePath.PRIME_DATA);
//
//        if (_primeItems.size() > 0)
//        {
//            _log.info(getClass().getSimpleName() + ": Loaded " + _primeItems.size() + " items");
//        }
//        else
//        {
//            _log.info(getClass().getSimpleName() + ": System is disabled.");
//        }
//    }
//
//    @Override
//    protected void parseDocument()
//    {
//        for (Node n = getCurrentDocument().getFirstChild(); n != null; n = n.getNextSibling())
//        {
//            if ("list".equalsIgnoreCase(n.getNodeName()))
//            {
//                NamedNodeMap at = n.getAttributes();
//                Node attribute = at.getNamedItem("enabled");
//                if ((attribute != null) && Boolean.parseBoolean(attribute.getNodeValue()))
//                {
//                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
//                    {
//                        if ("item".equalsIgnoreCase(d.getNodeName()))
//                        {
//                            NamedNodeMap attrs = d.getAttributes();
//                            Node att;
//                            StatsSet set = new StatsSet();
//                            for (int i = 0; i < attrs.getLength(); i++)
//                            {
//                                att = attrs.item(i);
//                                set.set(att.getNodeName(), att.getNodeValue());
//                            }
//
//                            List<PrimeShopItem> items = new ArrayList<>();
//                            for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
//                            {
//                                if ("item".equalsIgnoreCase(b.getNodeName()))
//                                {
//                                    attrs = b.getAttributes();
//
//                                    final int itemId = parseInteger(attrs, "itemId");
//                                    final int count = parseInteger(attrs, "count");
//
//                                    final L2Item item = ItemTable.getInstance().getTemplate(itemId);
//                                    if (item == null)
//                                    {
//                                        _log.warn(getClass().getSimpleName() + ": Item template null for itemId: " + itemId + " brId: " + set.getInteger("id"));
//                                        return;
//                                    }
//
//                                    items.add(new PrimeShopItem(itemId, count, item.getWeight(), item.isTradeable() ? 1 : 0));
//                                }
//                            }
//
//                            _primeItems.put(set.getInteger("id"), new PrimeShopGroup(set, items));
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    public void showProductInfo(L2PcInstance player, int brId)
//    {
//        final PrimeShopGroup item = _primeItems.get(brId);
//
//        if ((player == null) || (item == null))
//        {
//            return;
//        }
//
//        player.sendPacket(new ExBR_ProductInfo(item));
//    }
//
//    public PrimeShopGroup getItem(int brId)
//    {
//        return _primeItems.get(brId);
//    }
//
//    public Map<Integer, PrimeShopGroup> getPrimeItems()
//    {
//        return _primeItems;
//    }
//
//    public static PrimeShopTable getInstance()
//    {
//        return SingletonHolder._instance;
//    }
//
//    private static class SingletonHolder
//    {
//        protected static final PrimeShopTable _instance = new PrimeShopTable();
//    }
//}
