package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate.DefaultAttributes;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate.DefaultAttributes.AttributeSet;
import dwo.gameserver.model.actor.templates.L2CharBaseTemplate.DefaultAttributes.DefenseSet;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassType;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.player.base.Sex;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * L2GOD Team
 * User: Yorie
 * Date: 16.06.12
 * Time: 17:55
 */

public class CharTemplateTable extends XmlDocumentParser
{
    private static final List<L2CharBaseTemplate> _templates = new ArrayList<>();

    protected static CharTemplateTable _instance;

    private CharTemplateTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CharTemplateTable getInstance()
    {
        return _instance == null ? _instance = new CharTemplateTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        parseDirectory(FilePath.CHAR_TEMPLATE_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + _templates.size() + " Class templates.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("template_data"))
            {
                Race race = Race.valueOf(element.getAttributeValue("race"));
                ClassType type = ClassType.valueOf(element.getAttributeValue("type"));

                L2CharBaseTemplate template = new L2CharBaseTemplate(race, type);

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("creation_data"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("start_points"))
                            {
                                parseCreationData(template, element2);
                            }
                        }
                    }
                    else if(name1.equalsIgnoreCase("stats_data"))
                    {
                        parseStatsData(template, element1);
                    }
                }

                _templates.add(template);
            }
        }
    }

    private void parseCreationData(L2CharBaseTemplate template, Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("point"))
            {
                int x = Integer.parseInt(element.getAttributeValue("x"));
                int y = Integer.parseInt(element.getAttributeValue("y"));
                int z = Integer.parseInt(element.getAttributeValue("z"));
                template.getCreationData().addStartPoint(x, y, z);
            }
        }
    }

    private void parseStatsData(L2CharBaseTemplate template, Element rootElement)
    {
        AttributeSet min = null;
        AttributeSet max = null;
        AttributeSet base = null;
        DefenseSet def = null;
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            switch(name)
            {
                case "min_attributes":
                case "max_attributes":
                case "base_attributes":
                    int _int = Integer.parseInt(element.getAttributeValue("int"));
                    int wit = Integer.parseInt(element.getAttributeValue("wit"));
                    int men = Integer.parseInt(element.getAttributeValue("men"));
                    int str = Integer.parseInt(element.getAttributeValue("str"));
                    int dex = Integer.parseInt(element.getAttributeValue("dex"));
                    int con = Integer.parseInt(element.getAttributeValue("con"));
                    int luc = Integer.parseInt(element.getAttributeValue("luc"));
                    int cha = Integer.parseInt(element.getAttributeValue("cha"));

                    switch(name)
                    {
                        case "min_attributes":
                            min = new AttributeSet(_int, wit, men, str, dex, con, luc, cha);
                            break;
                        case "max_attributes":
                            max = new AttributeSet(_int, wit, men, str, dex, con, luc, cha);
                            break;
                        case "base_attributes":
                            base = new AttributeSet(_int, wit, men, str, dex, con, luc, cha);
                            break;
                    }
                    break;
                case "base_defense":
                    // P.Def.
                    int chest = Integer.parseInt(element.getAttributeValue("chest"));
                    int legs = Integer.parseInt(element.getAttributeValue("legs"));
                    int helmet = Integer.parseInt(element.getAttributeValue("helmet"));
                    int boots = Integer.parseInt(element.getAttributeValue("boots"));
                    int gloves = Integer.parseInt(element.getAttributeValue("gloves"));
                    int underwear = Integer.parseInt(element.getAttributeValue("underwear"));
                    int cloak = Integer.parseInt(element.getAttributeValue("cloak"));

                    // M.Def.
                    int rEarring = Integer.parseInt(element.getAttributeValue("r_earring"));
                    int lEarring = Integer.parseInt(element.getAttributeValue("l_earring"));
                    int rRing = Integer.parseInt(element.getAttributeValue("r_ring"));
                    int lRing = Integer.parseInt(element.getAttributeValue("l_ring"));
                    int necklace = Integer.parseInt(element.getAttributeValue("necklace"));

                    def = new DefenseSet(chest, legs, helmet, boots, gloves, underwear, cloak, rRing, lRing, rEarring, lEarring, necklace);
                    break;
                case "base_stats":
                    parseBaseStats(template, element);
                    break;
            }
        }
        template.setDefaultAttributes(new DefaultAttributes(base, min, max, def));
    }

    private void parseBaseStats(L2CharBaseTemplate template, Element rootElement)
    {
        StatsSet baseSet = new StatsSet();
        StatsSet maleSet = new StatsSet();
        StatsSet femaleSet = new StatsSet();
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("stat_set"))
            {
                // Некоторые атрибуты свойственны только девушкам или только мужчинам
                int sex = -1;

                if(element.getAttributeValue("sex") != null)
                {
                    sex = element.getAttributeValue("sex").equalsIgnoreCase("male") ? Sex.MALE : Sex.FEMALE;
                }

                if(sex < 0)
                {
                    baseSet.set(element.getAttributeValue("name"), element.getAttributeValue("value"));
                }
                else if(sex == Sex.MALE)
                {
                    maleSet.set(element.getAttributeValue("name"), element.getAttributeValue("value"));
                }
                else if(sex == Sex.FEMALE)
                {
                    femaleSet.set(element.getAttributeValue("name"), element.getAttributeValue("value"));
                }
            }
            else if(name.equalsIgnoreCase("regen_lvl_data"))
            {
                parseRegenLevelData(template, element);
            }
        }

        template.setBaseStats(new L2CharBaseTemplate.BaseStats(baseSet, maleSet, femaleSet));
    }

    private void parseRegenLevelData(L2CharBaseTemplate template, Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("lvl_data"))
            {
                int level = Integer.parseInt(element.getAttributeValue("lvl"));
                double hpRegen = Double.parseDouble(element.getAttributeValue("hp"));
                double mpRegen = Double.parseDouble(element.getAttributeValue("mp"));
                double cpRegen = Double.parseDouble(element.getAttributeValue("cp"));
                template.getLevelData().set(L2CharBaseTemplate.LevelData.LevelDataType.HP_REGEN, level, hpRegen);
                template.getLevelData().set(L2CharBaseTemplate.LevelData.LevelDataType.MP_REGEN, level, mpRegen);
                template.getLevelData().set(L2CharBaseTemplate.LevelData.LevelDataType.CP_REGEN, level, cpRegen);
            }
        }
    }

    public L2CharBaseTemplate getTemplate(ClassId classId)
    {
        Race lookupRace = classId.getRace();
        ClassType lookupType = classId.isMage() ? ClassType.Mystic : ClassType.Fighter;

        for(L2CharBaseTemplate template : _templates)
        {
            if(template.getRace() == lookupRace && template.getClassType() == lookupType)
            {
                return template;
            }
        }

        return null;
    }
}