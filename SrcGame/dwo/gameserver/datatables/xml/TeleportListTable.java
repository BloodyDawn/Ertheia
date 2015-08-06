package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.player.teleport.TeleportLocation;
import dwo.gameserver.model.world.zone.Location;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 09.12.11
 * Time: 14:28
 */

public class TeleportListTable extends XmlDocumentParser
{
    private static final Map<Integer, Map<Integer, TeleportLocation[]>> _lists = new HashMap<>();

    protected static TeleportListTable instance;

    private TeleportListTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static TeleportListTable getInstance()
    {
        return instance == null ? instance = new TeleportListTable() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _lists.clear();
        parseFile(FilePath.TELEPORT_DATA);
        _log.log(Level.INFO, "TeleportListTable: Loaded " + _lists.size() + " teleport instances.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("npc"))
            {
                Map<Integer, TeleportLocation[]> lists = new HashMap<>();
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("sublist"))
                    {
                        boolean isNoble;
                        FastList<TeleportLocation> targets = new FastList<>();
                        String[] locNode;
                        Location loc;

                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("target"))
                            {
                                locNode = element2.getAttributeValue("loc").split(" ");
                                loc = new Location(Integer.parseInt(locNode[0]), Integer.parseInt(locNode[1]), Integer.parseInt(locNode[2]));
                                String name3 = element2.getAttributeValue("fstring") == null ? "0" : element2.getAttributeValue("fstring");
                                int price = element2.getAttributeValue("price") == null ? 0 : Integer.parseInt(element2.getAttributeValue("price"));
                                int item = element2.getAttributeValue("item") == null ? PcInventory.ADENA_ID : Integer.parseInt(element2.getAttributeValue("item"));
                                isNoble = Boolean.parseBoolean(element2.getAttributeValue("isNoble"));

                                TeleportLocation t = new TeleportLocation(item, price, name3, loc, isNoble);
                                targets.add(t);
                            }
                        }
                        if(!targets.isEmpty())
                        {
                            lists.put(Integer.parseInt(element1.getAttributeValue("id")), targets.toArray(new TeleportLocation[targets.size()]));
                        }
                    }
                }
                if(!lists.isEmpty())
                {
                    _lists.put(Integer.parseInt(element.getAttributeValue("id")), lists);
                }
            }
        }
    }

    public TeleportLocation[] getTeleportLocationList(int npcId, int listId)
    {
        if(_lists.get(npcId) == null)
        {
            _log.log(Level.WARN, "TeleportListTable: Not found teleport location for npcId: " + npcId + ", listId: " + listId);
            return null;
        }
        return _lists.get(npcId).get(listId);
    }

    /**
     * @param npcId ID проверяемого НПЦ
     * @return существует-ли список телепортов для указанного ID НПЦ
     */
    public boolean containsTeleportData(int npcId)
    {
        return _lists.containsKey(npcId);
    }
}