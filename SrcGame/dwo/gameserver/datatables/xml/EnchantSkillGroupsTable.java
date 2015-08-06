package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.skills.L2EnchantSkillGroup;
import dwo.gameserver.model.skills.L2EnchantSkillGroup.EnchantSkillDetail;
import dwo.gameserver.model.skills.L2EnchantSkillLearn;
import dwo.gameserver.model.skills.base.L2Skill;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class EnchantSkillGroupsTable extends XmlDocumentParser
{
    public static final int TYPE_NORMAL_ENCHANT = 0;
    public static final int TYPE_SAFE_ENCHANT = 1;
    public static final int TYPE_UNTRAIN_ENCHANT = 2;
    public static final int TYPE_CHANGE_ENCHANT = 3;
    public static final int TYPE_PASS_TICKET = 4;

    public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
    public static final int SAFE_ENCHANT_COST_MULTIPLIER = 5;

    // Книги заточки для третьей профы
    public static final int NORMAL_ENCHANT_BOOK = 6622;
    public static final int SAFE_ENCHANT_BOOK = 9627;
    public static final int CHANGE_ENCHANT_BOOK = 9626;
    public static final int UNTRAIN_ENCHANT_BOOK = 9625;

    // Книги заточки для пробужденных персонажей
    public static final int AWAKED_NORMAL_ENCHANT_BOOK = 30297;
    public static final int AWAKED_SAFE_ENCHANT_BOOK = 30298;
    public static final int AWAKED_CHANGE_ENCHANT_BOOK = 30299;
    public static final int AWAKED_UNTRAIN_ENCHANT_BOOK = 30300;
    public static final int AWAKED_ENCHANT_PASS_TICKET = 37044;

    private final Map<Integer, L2EnchantSkillGroup> _enchantSkillGroups = new HashMap<>();
    private final Map<Integer, L2EnchantSkillLearn> _enchantSkillTrees = new HashMap<>();

    protected static EnchantSkillGroupsTable _instance;

    private EnchantSkillGroupsTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static EnchantSkillGroupsTable getInstance()
    {
        return _instance == null ? _instance = new EnchantSkillGroupsTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _enchantSkillGroups.clear();
        _enchantSkillTrees.clear();
        parseFile(FilePath.ENCHANT_SKILL_GROUPS_TABLE);
        _log.log(Level.INFO, "EnchantSkillGroupsTable: Loaded " + _enchantSkillGroups.size() + " groups.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name =  element.getName();
            if(name.equalsIgnoreCase("group"))
            {
                int id = Integer.parseInt(element.getAttributeValue("id"));
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("enchant"))
                    {
                        int lvl = Integer.parseInt(element1.getAttributeValue("level"));
                        int adena = Integer.parseInt(element1.getAttributeValue("adena"));
                        int exp = 0;
                        int sp = Integer.parseInt(element1.getAttributeValue("sp"));

                        String[] rates = element1.getAttributeValue("successRate").split(",");
                        int i = 0;
                        byte[] success = new byte[24];
                        for(String rate : rates)
                        {
                            success[i++] = Byte.parseByte(rate);
                        }

                        L2EnchantSkillGroup group = _enchantSkillGroups.get(id);
                        if(group == null)
                        {
                            group = new L2EnchantSkillGroup(id);
                            _enchantSkillGroups.put(id, group);
                        }

                        EnchantSkillDetail esd = new EnchantSkillDetail(lvl, adena, exp, sp, success);
                        group.addEnchantDetail(esd);
                    }
                }
            }
        }
    }

    public int addNewRouteForSkill(int skillId, int maxLvL, int route, int group)
    {
        L2EnchantSkillLearn enchantableSkill = _enchantSkillTrees.get(skillId);
        if(enchantableSkill == null)
        {
            enchantableSkill = new L2EnchantSkillLearn(skillId, maxLvL);
            _enchantSkillTrees.put(skillId, enchantableSkill);
        }
        if(_enchantSkillGroups.containsKey(group))
        {
            enchantableSkill.addNewEnchantRoute(route, group);

            return _enchantSkillGroups.get(group).getEnchantGroupDetails().size();
        }
        _log.log(Level.ERROR, "Error while loading generating enchant skill id: " + skillId + "; route: " + route + "; missing group: " + group);
        return 0;
    }

    public L2EnchantSkillLearn getSkillEnchantmentForSkill(L2Skill skill)
    {
        L2EnchantSkillLearn esl = getSkillEnchantmentBySkillId(skill.getId());
        // there is enchantment for this skill and we have the required level of it
        if(esl != null && skill.getLevel() >= esl.getBaseLevel())
        {
            return esl;
        }
        return null;
    }

    public L2EnchantSkillLearn getSkillEnchantmentBySkillId(int skillId)
    {
        return _enchantSkillTrees.get(skillId);
    }

    public int getEnchantSkillSpCost(L2Skill skill)
    {
        L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
        if(enchantSkillLearn != null)
        {

            EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
            if(esd != null)
            {
                return esd.getSpCost();
            }
        }

        return Integer.MAX_VALUE;
    }

    public int getEnchantSkillAdenaCost(L2Skill skill)
    {
        L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
        if(enchantSkillLearn != null)
        {
            EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
            if(esd != null)
            {
                return esd.getAdenaCost();
            }
        }

        return Integer.MAX_VALUE;
    }

    public byte getEnchantSkillRate(L2PcInstance player, L2Skill skill)
    {
        L2EnchantSkillLearn enchantSkillLearn = _enchantSkillTrees.get(skill.getId());
        if(enchantSkillLearn != null)
        {
            EnchantSkillDetail esd = enchantSkillLearn.getEnchantSkillDetail(skill.getLevel());
            if(esd != null)
            {
                return esd.getRate(player);
            }
        }

        return 0;
    }

    public L2EnchantSkillGroup getEnchantSkillGroupById(int id)
    {
        return _enchantSkillGroups.get(id);
    }
}