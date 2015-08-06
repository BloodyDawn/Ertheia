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
 * Date: 23.06.2015
 * Team: La2Era Team
 */

public class BuffStackGroupData extends XmlDocumentParser
{
    private static final Map<String, String[]> locked_list = new HashMap<>();

    protected static BuffStackGroupData instance;

    private BuffStackGroupData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static BuffStackGroupData getInstance()
    {
        return instance == null ? instance = new BuffStackGroupData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        locked_list.clear();
        parseFile(FilePath.BUFF_STACK_GROUP_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + locked_list.size() + " group.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        String abnormal_type = null;
        String[] loc_type = new String[0];
        for (Element element : rootElement.getChildren()) 
        {
            final String name = element.getName();
            if (name.equalsIgnoreCase("abnormal_type"))
            {
                abnormal_type = element.getAttributeValue("type");
                
                for (Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if (name1.equalsIgnoreCase("locked_abnormals"))
                    {
                        loc_type = element1.getAttributeValue("type").split(",");
                    }
                }
            }
            locked_list.put(abnormal_type, loc_type);
        }
    }

    public String[] getLockedAbnormalsList(String locked_name)
    {
        return locked_list.get(locked_name);
    }
}
