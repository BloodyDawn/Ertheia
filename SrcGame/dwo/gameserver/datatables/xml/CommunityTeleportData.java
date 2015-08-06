package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 13.08.12
 * Time: 8:54
 */

public class CommunityTeleportData extends XmlDocumentParser
{
    private final Map<Integer, TeleportList> _teleportLists = new HashMap<>();

    protected static CommunityTeleportData _instance;

    private CommunityTeleportData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CommunityTeleportData getInstance()
    {
        return _instance == null ? _instance = new CommunityTeleportData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _teleportLists.clear();
        parseFile(FilePath.COMMUNITY_TELEPORT);
        _log.log(Level.INFO, "CommunityTeleportData: Loaded " + _teleportLists.size() + " teleport locations.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        int[] base_item = new int[2];
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("config"))
            {
                String var = element.getAttributeValue("var");
                if(var.equals("base_price"))
                {
                    String item = element.getAttributeValue("val");
                    if(item != null)
                    {
                        String[] tempString = element.getAttributeValue("val").split(";");
                        base_item[0] = Integer.parseInt(tempString[0]);
                        base_item[1] = Integer.parseInt(tempString[1]);
                    }
                }
            }
            if(name.equalsIgnoreCase("point"))
            {
                int[] item = new int[2];
                int listId = Integer.parseInt(element.getAttributeValue("id"));
                TeleportList teleportList = new TeleportList(listId);
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("button"))
                    {
                        int id = Integer.parseInt(element1.getAttributeValue("id"));
                        String type = element1.getAttributeValue("type");
                        if(type.equalsIgnoreCase("TELEPORT"))
                        {
                            String loc = element1.getAttributeValue("loc");
                            String enName = element1.getAttributeValue("name_en");
                            String ruName = element1.getAttributeValue("name_ru");
                            String item_str = element1.getAttributeValue("item");
                            if(item_str != null)
                            {
                                String[] tempString = item_str.split(";");
                                item[0] = Integer.parseInt(tempString[0]);
                                item[1] = Integer.parseInt(tempString[1]);
                            }
                            else
                            {
                                item = base_item;
                            }
                            teleportList.addPoint(id, new TeleportPoint(new Location(loc), enName, ruName, item, false));
                        }
                        else if(type.equalsIgnoreCase("LINK"))
                        {
                            String enName = element1.getAttributeValue("name_en");
                            String ruName = element1.getAttributeValue("name_ru");
                            int link = Integer.parseInt(element1.getAttributeValue("link"));
                            teleportList.addPoint(id, new TeleportPoint(link, enName, ruName, true));
                        }
                    }
                }
                _teleportLists.put(listId, teleportList);
            }
        }
    }

    public TeleportList getTeleportLocationList(int listId)
    {
        if(_teleportLists.get(listId) == null)
        {
            _log.log(Level.WARN, "CommunityTeleportData: Not found teleport location for listId: " + listId);
            return null;
        }
        return _teleportLists.get(listId);
    }

    public TeleportPoint getTeleportPoint(int listId, int point)
    {

        TeleportList teleport = _teleportLists.get(listId);
        if(teleport == null || teleport.getPoint(point) == null)
        {
            _log.log(Level.WARN, "CommunityTeleportData: Not found teleport location for listId: " + listId + " point: " + point);
            return null;
        }
        if(teleport.getPoint(point).isLink())
        {
            return null;
        }
        return _teleportLists.get(listId).getPoint(point);
    }

    public static class TeleportList
    {
        private final int _id;
        private final Map<Integer, TeleportPoint> _points;

        public TeleportList(int id)
        {
            _id = id;
            _points = new HashMap<>();
        }

        public int getId()
        {
            return _id;
        }

        public void addPoint(int id, TeleportPoint point)
        {
            _points.put(id, point);
        }

        public Set<Integer> getPointsKeys()
        {
            return _points.keySet();
        }

        public int size()
        {
            return _points.size();
        }

        public TeleportPoint getPoint(int id)
        {
            return _points.get(id);
        }
    }

    public static class TeleportPoint
    {
        private final Location _loc;
        private final String _enName;
        private final String _ruName;
        private final int _itemId;
        private final int _itemCount;
        private final boolean _link;

        public TeleportPoint(Location loc, String enName, String ruName, int[] item, boolean link)
        {
            _loc = loc;
            _enName = enName;
            _ruName = ruName;
            _itemId = item[0];
            _itemCount = item[1];
            _link = link;
        }

        public TeleportPoint(int id, String enName, String ruName, boolean link)
        {
            _loc = null;
            _enName = enName;
            _ruName = ruName;
            _itemId = id;
            _itemCount = 0;
            _link = link;
        }

        public Location getLocation()
        {
            return _loc;
        }

        public boolean isLink()
        {
            return _link;
        }

        public String getName(L2PcInstance activeChar)
        {
            return activeChar.getLang().equals("en") ? _enName : _ruName;
        }

        public int getItemId()
        {
            return _itemId;
        }

        public int getItemCount()
        {
            return _itemCount;
        }
    }
}