package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.alchemy.AlchemyDataTemplate;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.util.Util;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;

/**
 * User: GenCloud
 * Date: 17.06.2015
 * Team: La2Era Team
 */
public class AlchemyDataTable extends XmlDocumentParser
{
    private static final TIntObjectMap<AlchemyDataTemplate> _datas = new TIntObjectHashMap<>();
    private static final Logger _log = LogManager.getLogger(AlchemyDataTable.class);

    protected static AlchemyDataTable _instance;

    private AlchemyDataTable()
    {
        try 
        {
            load();
        } 
        catch (JDOMException | IOException e) 
        {
            e.printStackTrace();
        }
    }
    
    public static AlchemyDataTable getInstance()
    {
        return _instance == null ? _instance = new AlchemyDataTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException 
    {
        _datas.clear();
        parseFile(FilePath.ALCHEMY_CONVERSION_DATA_FILE);
        _log.info("AlchemyDataTable: Loaded for alchemy conversion " + _datas.size() + " skill's count.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(final Element element : rootElement.getChildren()) 
        {
            final String name0 = element.getName();
            if (name0.equalsIgnoreCase("alchemy"))
            {
                final int skill_id = Integer.parseInt(element.getAttributeValue("skill_id")),
                        skill_level = Integer.parseInt(element.getAttributeValue("skill_level")),
                        success_rate = Integer.parseInt(element.getAttributeValue("success_rate"));

                final AlchemyDataTemplate data = new AlchemyDataTemplate(skill_id, skill_level, success_rate);

                for (final Element element_1 : element.getChildren()) 
                {
                    final String name = element_1.getName();
                    if (name.equalsIgnoreCase("ingridients")) 
                    {
                        for (final Element item : element_1.getChildren())
                        {
                            final int itemId = Integer.parseInt(item.getAttributeValue("id"));
                            final int itemCount = Integer.parseInt(item.getAttributeValue("count"));
                            data.addIngridient(new AlchemyDataTemplate.AlchemyItem(itemId, itemCount));
                        }
                    } 
                    else if (name.equalsIgnoreCase("products")) 
                    {
                        for (final Element group : element_1.getChildren())
                        {
                            final String name_group = group.getName();
                            if (name_group.equalsIgnoreCase("on_success"))
                            {
                                for (final Element item : group.getChildren())
                                {
                                    final int itemId2 = Integer.parseInt(item.getAttributeValue("id"));
                                    final int itemCount2 = Integer.parseInt(item.getAttributeValue("count"));
                                    data.addOnSuccessProduct(new AlchemyDataTemplate.AlchemyItem(itemId2, itemCount2));
                                }
                            } 
                            else if (name_group.equalsIgnoreCase("on_fail")) 
                            {
                                for (final Element item : group.getChildren())
                                {
                                    final int itemId2 = Integer.parseInt(item.getAttributeValue("id"));
                                    final int itemCount2 = Integer.parseInt(item.getAttributeValue("count"));
                                    data.addOnFailProduct(new AlchemyDataTemplate.AlchemyItem(itemId2, itemCount2));
                                }
                            }
                        }
                    }
                }
                addData(data);
            }
        }
    }

    public AlchemyDataTemplate getData(L2Skill skill) {
        return _datas.get(skill.hashCode());
    }
    
    private void addData(AlchemyDataTemplate data) {
        _datas.put(Util.generateHashCode(data.getSkillId(), data.getSkillLevel()), data);
    }
}
