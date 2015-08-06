package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CommunityBuffTable extends XmlDocumentParser
{
    private final Map<Integer, BBSGroupBuffStat> _groupBuff = new HashMap<>();
    private final Map<Integer, Map<Integer, Integer>> _skill = new HashMap<>();

    protected static CommunityBuffTable _instance;
    
    private CommunityBuffTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CommunityBuffTable getInstance()
    {
        return _instance == null ? _instance = new CommunityBuffTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _groupBuff.clear();
        _skill.clear();
        parseFile(FilePath.COMMUNITY_BUFFS);
        _log.log(Level.INFO, "Community Buff Table: Loaded " + _groupBuff.size() + " group buff.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("group"))
            {
                int idGroup = Integer.parseInt(element.getAttributeValue("id"));
                int priceGroup = Integer.parseInt(element.getAttributeValue("price"));
                String nameGroup = element.getAttributeValue("name");
                if(!_groupBuff.containsKey(idGroup))
                {
                    _groupBuff.put(idGroup, new BBSGroupBuffStat(priceGroup, nameGroup));
                    _skill.put(idGroup, new HashMap<>());
                }
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("buff"))
                    {
                        int idSkill = Integer.parseInt(element1.getAttributeValue("id"));
                        int lvlSkill = Integer.parseInt(element1.getAttributeValue("level"));
                        if(_skill.containsKey(idGroup))
                        {
                            _skill.get(idGroup).put(idSkill, lvlSkill);
                        }
                        else
                        {
                            _log.log(Level.WARN, "Community Buff Table: no search group id " + idGroup + " for skill id " + idSkill);
                        }
                    }
                }
            }
        }
    }

    public int getPriceGroup(int id)
    {
        if(_groupBuff.containsKey(id))
        {
            return _groupBuff.get(id).getPrice();
        }
        return -1;
    }

    public String getNameGroup(int id)
    {
        if(_groupBuff.containsKey(id))
        {
            return _groupBuff.get(id).getName();
        }
        return null;
    }

    public int getBBSGroupForBuf(int id, int lvl)
    {
        for(Map.Entry<Integer, Map<Integer, Integer>> entry : _skill.entrySet())
        {
            int key = entry.getKey();
            Map<Integer, Integer> skills = entry.getValue();
            if(skills.containsKey(id))
            {
                if(skills.get(id) == lvl)
                {
                    return key;
                }
            }
        }
        return 0;
    }

    public boolean isBBSSaveBuf(int id, int lvl)
    {
        return getBBSGroupForBuf(id, lvl) > 0;
    }

    public Map<Integer, BBSGroupBuffStat> getBBSGroups()
    {
        return _groupBuff;
    }

    public Map<Integer, Integer> getBBSBuffsForGoup(int id)
    {
        return _skill.get(id);
    }

    public class BBSGroupBuffStat
    {
        private int _prise;
        private String _nameGroup;

        private BBSGroupBuffStat(int prise, String nameGroup)
        {
            _prise = prise;
            _nameGroup = nameGroup;
        }

        public int getPrice()
        {
            return _prise;
        }

        public String getName()
        {
            return _nameGroup;
        }
    }
}