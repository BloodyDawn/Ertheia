package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.base.L2Henna;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HennaTable extends XmlDocumentParser
{
    private static final Map<Integer, L2Henna> _henna = new HashMap<>();

    protected static HennaTable _instance;

    private HennaTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static HennaTable getInstance()
    {
        return _instance == null ? _instance = new HennaTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _henna.clear();
        parseFile(FilePath.HENNA_DATA);
        _log.log(Level.INFO, "HennaTable: Loaded " + _henna.size() + " henna emplates.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("dye"))
            {
                int id = Integer.parseInt(element.getAttributeValue("symbol_id"));
                StatsSet hennaDat = new StatsSet();
                hennaDat.set("symbol_id", id);
                hennaDat.set("dye", Integer.parseInt(element.getAttributeValue("dye_id")));
                hennaDat.set("price", Integer.parseInt(element.getAttributeValue("price")));
                hennaDat.set("amount", Integer.parseInt(element.getAttributeValue("count")));
                hennaDat.set("stat_INT", Integer.parseInt(element.getAttributeValue("INT")));
                hennaDat.set("stat_STR", Integer.parseInt(element.getAttributeValue("STR")));
                hennaDat.set("stat_CON", Integer.parseInt(element.getAttributeValue("CON")));
                hennaDat.set("stat_MEN", Integer.parseInt(element.getAttributeValue("MEN")));
                hennaDat.set("stat_DEX", Integer.parseInt(element.getAttributeValue("DEX")));
                hennaDat.set("stat_WIT", Integer.parseInt(element.getAttributeValue("WIT")));
                hennaDat.set("stat_LUC", Integer.parseInt(element.getAttributeValue("LUC")));
                hennaDat.set("stat_CHA", Integer.parseInt(element.getAttributeValue("CHA")));
                hennaDat.set("attributeSkillId", Integer.parseInt(element.getAttributeValue("attributeSkillId")));
                L2Henna template = new L2Henna(hennaDat);
                _henna.put(id, template);
            }
        }
    }

    public L2Henna getTemplate(int id)
    {
        return _henna.get(id);
    }
}