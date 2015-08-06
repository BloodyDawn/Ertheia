package dwo.gameserver.engine.documentengine.items;

import dwo.gameserver.engine.documentengine.XmlDocumentBase;
import dwo.gameserver.model.items.base.L2Armor;
import dwo.gameserver.model.items.base.L2EtcItem;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.L2Weapon;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * User: GenCloud
 * Date: 17.01.2015
 * Team: DWO
 */
public class XmlDocumentItemClient extends XmlDocumentBase
{
    public List<L2Item> items = new ArrayList<>();

    public XmlDocumentItemClient(File file)
    {
        super(file);
    }

    @Override
    protected int getCurrentId()
    {
        return 0;
    }

    @Override
    protected void parseDocument(Document doc)
    {
        for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if("list".equalsIgnoreCase(n.getNodeName()))
            {
                for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
                {
                    if("item".equalsIgnoreCase(d.getNodeName()))
                    {
                        parseItem(d);
                    }
                }
            }
            else if("item".equalsIgnoreCase(n.getNodeName()))
            {
                parseItem(n);
            }
        }
    }

    @Override
    protected StatsSet getStatsSet()
    {
        return null;
    }

    @Override
    protected String getTableValue(String name)
    {
        return null;
    }

    @Override
    protected String getTableValue(String name, int idx)
    {
        return null;
    }

    private void parseItem(Node n)
    {
        StatsSet set = new StatsSet();
        Node first = n.getFirstChild();
        for(n = first; n != null; n = n.getNextSibling())
        {
            if("set".equalsIgnoreCase(n.getNodeName()))
            {
                parseBeanSet(n, set, 1);
            }
        }
        L2Item item = null;
        switch(set.getString("type"))
        {
            case "Weapon":
                item = new L2Weapon(set);
                break;
            case "Armor":
                item = new L2Armor(set);
                break;
            case "EtcItem":
                item = new L2EtcItem(set);
                break;
        }
        items.add(item);
    }

    public List<L2Item> getItems()
    {
        return items;
    }
}
