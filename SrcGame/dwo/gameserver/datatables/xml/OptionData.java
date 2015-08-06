package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Bacek
 * Date: 09.03.13
 * Time: 11:22
 * TODO* что за хуйвола пздц 
 */
public class OptionData extends XmlDocumentParser
{
    private static final Map<Boolean, Map<Integer, Integer>> _lists = new HashMap<>();

    private OptionData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static OptionData getInstance()
    {
        return SingletonHolder._instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _lists.clear();
        parseDirectory(FilePath.OPTION_DATA);
        _log.log(Level.INFO, "OptionData: Loaded " + _lists.size() + " Minerals.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("option_data"))
            {
                int option_id = Integer.parseInt(element.getAttributeValue("id"));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("for") || name1.equalsIgnoreCase("triggers"))
                    {
								/*
										<for>
											<add order="0x60" stat="pEvasRate" value="1.058135" />
											<add order="0x60" stat="pAccCombat" value="0.957361" />
										</for>
								 */
                    }
                    else if(name1.equalsIgnoreCase("skills"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("skill"))
                            {
                                int id = Integer.parseInt(element.getAttributeValue("id"));
                                int level = Integer.parseInt(element.getAttributeValue("level"));

                                L2Skill skill = SkillTable.getInstance().getInfo(id, level);
                                if(skill != null)
                                {
                                    // Добовляем
                                }
                                else
                                {
                                    _log.log(Level.ERROR, "OptionData: Skill not found (" + id + ',' + level + ") for option data:" + option_id);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static class SingletonHolder
    {
        protected static final OptionData _instance = new OptionData();
    }
}
