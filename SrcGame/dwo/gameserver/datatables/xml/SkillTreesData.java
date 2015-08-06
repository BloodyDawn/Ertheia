package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SkillHolder;
import dwo.gameserver.model.player.base.*;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.model.skills.L2SkillLearn;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.proptypes.AcquireSkillType;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.residence.castle.CastleSide;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.packet.acquire.ExAcquirableSkillListByClass;
import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.w3c.dom.Node;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * L2GOD Team
 * User: Bacek, Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class SkillTreesData extends XmlDocumentParser
{
    private static final Map<ClassId, Map<Integer, L2SkillLearn>> _classSkillTrees = new HashMap<>();
    private static final Map<ClassId, Map<Integer, L2SkillLearn>> _transferSkillTrees = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _collectSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _fishingSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _pledgeSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _subClassSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _dualClassSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _subPledgeSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _transformSkillTree = new HashMap<>();
    private static final Map<Integer, List<Integer>> _replaceableSkillTree = new HashMap<>();
    private static final Map<Integer, Map<Integer, Boolean>> _replaceableSkillTreeCache = new HashMap<>();
    private static final Map<Race, List<L2SkillLearn>> _racePassiveSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _raceActiveSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _raceActiveSkillSubTree = new HashMap<>();
    private static final Map<Integer, List<Integer>> _awakeUndeleteSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _gameMasterSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _gameMasterAuraSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _nobleSkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _heroSkillTree = new HashMap<>();

    private static final Map<Integer, L2SkillLearn> _abilitySkillTree = new HashMap<>();
    private static final Map<Integer, L2SkillLearn> _alchemySkillTree = new HashMap<>();

    //TODO: Unhardcode?
    //Checker, sorted arrays of hash codes
    private TIntObjectHashMap<int[]> _skillsByClassIdHashCodes; //Occupation skills
    private TIntObjectHashMap<int[]> _skillsByRaceHashCodes; // race-specific transformations
    private int[] _allSkillsHashCodes; // fishing, collection and all races transformations
    private boolean _loading = true;

    protected static SkillTreesData instance;

    private SkillTreesData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static SkillTreesData getInstance()
    {
        return instance == null ? instance = new SkillTreesData() : instance;
    }

    public void reload()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void load() throws JDOMException, IOException {
        _loading = true;
        _classSkillTrees.clear();
        _collectSkillTree.clear();
        _fishingSkillTree.clear();
        _pledgeSkillTree.clear();
        _subClassSkillTree.clear();
        _dualClassSkillTree.clear();
        _subPledgeSkillTree.clear();
        _transferSkillTrees.clear();
        _transformSkillTree.clear();
        _replaceableSkillTree.clear();
        _replaceableSkillTreeCache.clear();
        _racePassiveSkillTree.clear();
        _awakeUndeleteSkillTree.clear();
        _nobleSkillTree.clear();
        _heroSkillTree.clear();
        _gameMasterSkillTree.clear();
        _gameMasterAuraSkillTree.clear();

        _abilitySkillTree.clear();
        _alchemySkillTree.clear();

        //Load files.
        _loading = parseDirectory(FilePath.SKILL_TREES_DATA);

        // Generate check arrays.
        generateCheckArrays();

        // Logs a report with skill trees info.
        report();
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        String attribute;
        String type;
        int cId = -1;
        Race raceId = null;
        ClassId classId = null;

        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("skillTree"))
            {
                Map<Integer, L2SkillLearn> classSkillTree = new HashMap<>();
                Map<Integer, L2SkillLearn> trasferSkillTree = new HashMap<>();
                List<L2SkillLearn> raceSkills = new ArrayList<>();

                attribute = element.getAttributeValue("type");
                if(attribute == null)
                {
                    _log.log(Level.WARN, getClass().getSimpleName() + ": Skill Tree without type!");
                    continue;
                }
                type = attribute;

                attribute = element.getAttributeValue("classId");
                if(attribute != null)
                {
                    cId = Integer.parseInt(attribute);
                    if(cId != -1)
                    {
                        classId = ClassId.values()[cId];
                    }
                }

                attribute = element.getAttributeValue("raceId");
                if(attribute != null)
                {
                    raceId = Race.values()[Integer.parseInt(attribute)];
                }

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("skill"))
                    {
                        StatsSet learnSkillSet = new StatsSet();

                        int skillId, skillLvl;

                        // Check skill name is set
                        if(element1.getAttributeValue("name") == null)
                        {
                            _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing skill name, skipping!");
                            continue;
                        }

                        // Check ID/Level info is set
                        if(element1.getAttributeValue("skillId") == null || element1.getAttributeValue("skillLvl") == null)
                        {
                            _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing skill ID or level, skipping!");
                            continue;
                        }

                        // Check ID/Level info filled correctly
                        try
                        {
                            skillId = Integer.parseInt(element1.getAttributeValue("skillId"));
                            skillLvl = Integer.parseInt(element1.getAttributeValue("skillLvl"));
                        }
                        catch(Exception e)
                        {
                            _log.log(Level.ERROR, "Failed to parse skill ID or level!", e);
                            continue;
                        }

                        final List<Attribute> attributes = element1.getAttributes();
                        for(final Attribute attribute1 : attributes)
                        {
                            learnSkillSet.set(attribute1.getName(), attribute1.getValue());
                        }

                        L2SkillLearn skillLearn = new L2SkillLearn(learnSkillSet);
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            switch(name2)
                            {
                                case "item":
                                    skillLearn.addRequiredItem(new ItemHolder(Integer.parseInt(element2.getAttributeValue("id")),
                                            Integer.parseInt(element2.getAttributeValue("count"))));
                                    break;
                                case "preRequisiteSkill":
                                    skillLearn.addPreReqSkill(new SkillHolder(Integer.parseInt(element2.getAttributeValue("id")),
                                            Integer.parseInt(element2.getAttributeValue("lvl"))));
                                    break;
                                case "race":
                                    skillLearn.addRace(Race.valueOf(element2.getText()));
                                    break;
                                case "residenceId":
                                    skillLearn.addResidenceId(Integer.valueOf(element2.getText()));
                                    break;
                                case "socialClass":
                                    skillLearn.setSocialClass(Enum.valueOf(SocialClass.class, element2.getText()));
                                    break;
                                case "subClassConditions":
                                    skillLearn.addSubclassConditions(Integer.parseInt(element2.getAttributeValue("slot")),
                                            Integer.parseInt(element2.getAttributeValue("lvl")));
                                    break;
                                case "castleSide":
                                    skillLearn.addCastleSide(Enum.valueOf(CastleSide.class, element2.getText()));
                                    break;
                                case "replaceable":
                                    addReplaceableSkill(cId, skillId, Integer.parseInt(element2.getAttributeValue("id")));
                                    break;
                                case "subClass":
                                    skillLearn.addsubClassType(element2.getAttributeValue("type"));
                                    break;
                            }
                        }

                        int skillHashCode = SkillTable.getSkillHashCode(skillId, skillLvl);
                        switch(type)
                        {
                            case "classSkillTree":
                                classSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "transferSkillTree":
                                trasferSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "collectSkillTree":
                                _collectSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "fishingSkillTree":
                                _fishingSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "pledgeSkillTree":
                                _pledgeSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "subClassSkillTree":
                                _subClassSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "dualClassSkillTree":
                                _dualClassSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "subPledgeSkillTree":
                                _subPledgeSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "transformSkillTree":
                                _transformSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "racePassiveSkillTree":
                                raceSkills.add(skillLearn);
                                break;
                            case "raceActiveSkillTree":
                                _raceActiveSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "raceActiveSkillSubTree":
                                _raceActiveSkillSubTree.put(skillHashCode, skillLearn);
                                break;
                            case "awakenSaveTree":
                                List<Integer> undelete;

                                undelete = _awakeUndeleteSkillTree.containsKey(cId) ? _awakeUndeleteSkillTree.get(cId) : new ArrayList<>();

                                // Does not allow duplicates
                                if(!undelete.contains(skillId))
                                {
                                    undelete.add(skillId);
                                }

                                _awakeUndeleteSkillTree.put(cId, undelete);
                                break;
                            case "gameMasterSkillTree":
                                _gameMasterSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "gameMasterAuraSkillTree":
                                _gameMasterAuraSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "nobleSkillTree":
                                _nobleSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "heroSkillTree":
                                _heroSkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "abilitySkillTree":
                                _abilitySkillTree.put(skillHashCode, skillLearn);
                                break;
                            case "alchemySkillTree":
                                _alchemySkillTree.put(skillHashCode, skillLearn);
                                break;
                        }
                    }
                }

                switch(type)
                {
                    case "classSkillTree":
                        if(_classSkillTrees.get(classId) == null)
                        {
                            _classSkillTrees.put(classId, classSkillTree);
                        }
                        else
                        {
                            _classSkillTrees.get(classId).putAll(classSkillTree);
                        }
                        break;
                    case "transferSkillTree":
                        _transferSkillTrees.put(classId, trasferSkillTree);
                        break;
                    case "racePassiveSkillTree":
                        if(_racePassiveSkillTree.get(raceId) == null)
                        {
                            _racePassiveSkillTree.put(raceId, raceSkills);
                        }
                        else
                        {
                            List<L2SkillLearn> list = _racePassiveSkillTree.get(raceId);
                            list.addAll(raceSkills);
                            _racePassiveSkillTree.put(raceId, list);
                        }
                        break;
                }
            }
        }
        generateCheckArrays();
    }

    /**
     * This displays Sub-Class Skill List to the player.
     *
     * @param player the active character.
     */
    public static void showSubClassSkillList(L2PcInstance player, boolean isDual)
    {
        List<L2SkillLearn> subClassSkills = getInstance().getAvailableSubClassSkills(player, isDual);
        ExAcquirableSkillListByClass asl = new ExAcquirableSkillListByClass(isDual ? AcquireSkillType.Dual : AcquireSkillType.SubClass);

        int count = 0;

        for(L2SkillLearn s : subClassSkills)
        {
            L2Skill sk = SkillTable.getInstance().getInfo(s.getSkillId(), s.getSkillLevel());

            if(sk != null)
            {
                count++;
                asl.addSkill(s.getSkillId(), s.getSkillLevel(), s.getSkillLevel(), 0, 1);
            }
        }
        if(count > 0)
        {
            player.sendPacket(asl);
        }
        else
        {
            player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN);
        }
    }

    /**
     * Wrapper for class skill trees.
     * @return the {@code _classSkillTrees}, if it's null allocate a new map and returns it.
     */
    private Map<ClassId, Map<Integer, L2SkillLearn>> getClassSkillTrees()
    {
        return _classSkillTrees;
    }

    /**
     * Method to get the complete skill tree for a given class id.<br>
     * Includes all parent skill trees.
     * @param classId the class skill tree ID.
     * @return the complete Class Skill Tree including skill trees from parent class for a given {@code classId}.
     */
    public Map<Integer, L2SkillLearn> getCompleteClassSkillTree(ClassId classId)
    {
        Map<Integer, L2SkillLearn> skillTree = new HashMap<>();
        if(classId == null)
        {
            return skillTree;
        }

        // Reparse skill trees for virtual awaken classes
        int oldClassId = classId.getId();
        classId = ClassId.getClassId(classId.getId());
        if(classId != null && _classSkillTrees.containsKey(classId))
        {
            skillTree.putAll(_classSkillTrees.get(classId));
        }
        classId = ClassId.getClassId(oldClassId).getParent();

        while(classId != null && _classSkillTrees.containsKey(classId))
        {
            skillTree.putAll(_classSkillTrees.get(classId));
            ClassInfo classInfo = ClassTemplateTable.getInstance().getClass(classId);
            classId = classInfo != null ? classInfo.getParentClassId() : null;
        }
        return skillTree;
    }

    /**
     * @param classId the transfer skill tree ID.
     * @return the complete Transfer Skill Tree for a given {@code classId}.
     */
    public Map<Integer, L2SkillLearn> getTransferSkillTree(ClassId classId)
    {
        while(true)
        {
            if(classId.level() == 4)
            {
                return null;
            }

            //If new classes are implemented over 3rd class, we use a recursive call.
            if(classId.level() == 3)
            {
                classId = classId.getParent();
                continue;
            }
            return _transferSkillTrees.get(classId);
        }
    }

    /**
     * @return the complete Collect Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getCollectSkillTree()
    {
        return _collectSkillTree;
    }

    /**
     * @return the complete Fishing Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getFishingSkillTree()
    {
        return _fishingSkillTree;
    }

    /**
     * @return the complete Pledge Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getPledgeSkillTree()
    {
        return _pledgeSkillTree;
    }

    /**
     * @return the complete Sub-Class Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getSubClassSkillTree()
    {
        return _subClassSkillTree;
    }

    /**
     * @return the complete Sub-Pledge Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getSubPledgeSkillTree()
    {
        return _subPledgeSkillTree;
    }

    /**
     * @return the complete Transform Skill Tree.
     */
    public Map<Integer, L2SkillLearn> getTransformSkillTree()
    {
        return _transformSkillTree;
    }

    /**
     * @return Ability Skill Tree
     */
    public Map<Integer, L2SkillLearn> getAbilitySkillTree()
    {
        return _abilitySkillTree;
    }

    /**
     * @return Alchemy Skill Tree
     */
    public Map<Integer, L2SkillLearn> getAlchemySkillTree()
    {
        return _alchemySkillTree;
    }

    /**
     * @return the complete Noble Skill Tree.
     */
    public Map<Integer, L2Skill> getNobleSkillTree()
    {
        Map<Integer, L2Skill> tree = new HashMap<>();
        SkillTable st = SkillTable.getInstance();
        for(Entry<Integer, L2SkillLearn> e : _nobleSkillTree.entrySet())
        {
            tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
        }
        return tree;
    }

    /**
     * @return the complete Hero Skill Tree.
     */
    public Map<Integer, L2Skill> getHeroSkillTree()
    {
        Map<Integer, L2Skill> tree = new HashMap<>();
        SkillTable st = SkillTable.getInstance();
        for(Entry<Integer, L2SkillLearn> e : _heroSkillTree.entrySet())
        {
            tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
        }
        return tree;
    }

    public void giveHeroSkills(L2PcInstance player)
    {
        for(L2Skill skill : getInstance().getHeroSkillTree().values())
        {
            player.addSkill(skill, false);
        }
    }

    public void removeHeroSkills(L2PcInstance player)
    {
        getInstance().getHeroSkillTree().values().forEach(player::removeSkill);
    }

    /**
     * @return the complete Game Master Skill Tree.
     */
    public Map<Integer, L2Skill> getGMSkillTree()
    {
        Map<Integer, L2Skill> tree = new HashMap<>();
        SkillTable st = SkillTable.getInstance();
        for(Entry<Integer, L2SkillLearn> e : _gameMasterSkillTree.entrySet())
        {
            tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
        }
        return tree;
    }

    /**
     * @return the complete Game Master Aura Skill Tree.
     */
    public Map<Integer, L2Skill> getGMAuraSkillTree()
    {
        Map<Integer, L2Skill> tree = new HashMap<>();
        SkillTable st = SkillTable.getInstance();
        for(Entry<Integer, L2SkillLearn> e : _gameMasterAuraSkillTree.entrySet())
        {
            tree.put(e.getKey(), st.getInfo(e.getValue().getSkillId(), e.getValue().getSkillLevel()));
        }
        return tree;
    }

    public List<L2SkillLearn> getNextLevelSkills(L2PcInstance player)
    {
        Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(player.getClassId());
        return skills.values().stream().filter(skill -> !skill.isAutoGet() && !skill.isLearnedByFS() && skill.getMinLevel() > player.getLevel()).collect(Collectors.toList());
    }

    /**
     * @param player the learning skill player.
     * @param classId the learning skill class ID.
     * @param includeByFs if {@code true} skills from Forgotten Scroll will be included.
     * @param includeAutoGet if {@code true} Auto-Get skills will be included.
     * @param allSkills allSkills?
     * @return allSkillsall available skills for a given {@code player}, {@code classId}, {@code includeByFs} and {@code includeAutoGet}.
     */
    public FastList<L2SkillLearn> getAvailableSkills(L2PcInstance player, ClassId classId, boolean includeByFs, boolean includeAutoGet, boolean allSkills)
    {
        FastList<L2SkillLearn> result = new FastList<>();
        Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(classId);

        List<Integer> ignoreSkillList = getAwakenDeleteSkills(player);
        Map<Integer, Boolean> ignoreSkills = new HashMap<>();

        for(Integer skill : ignoreSkillList)
        {
            ignoreSkills.put(skill, true);
        }

        if(skills.isEmpty())
        {
            //The Skill Tree for this class is undefined.
            _log.log(Level.WARN, getClass().getSimpleName() + ": Skilltree for class " + classId + " is not defined!");
            return result;
        }

        for(L2SkillLearn skill : skills.values())
        {
            if(skill.checkSubClassTypes(player) && ((includeAutoGet && skill.isAutoGet() || skill.isLearnedByNpc() || includeByFs && skill.isLearnedByFS() || !skill.isAutoGet() && !skill.isLearnedByFS()) && player.getLevel() >= skill.getMinLevel() || allSkills && !skill.isAutoGet()))
            {
                boolean knownSkill = false;
                L2Skill playerSkill = player.getSkills().get(skill.getSkillId());
                if(playerSkill != null)
                {
                    if(playerSkill.getId() == skill.getSkillId())
                    {
                        // Получаем список скилов заменяемых при изучении скила
                        for(SkillHolder sk : skill.getPrequisiteSkills())
                        {
                            ignoreSkills.put(sk.getSkillId(), true);
                        }

                        if(playerSkill.getLevel() == skill.getSkillLevel() - 1)
                        {
                            if(ignoreSkills.containsKey(skill.getSkillId()))
                            {
                                knownSkill = true;
                            }

                            if(!knownSkill)
                            {
                                result.add(skill); // Новый уровень скила.
                            }
                        }
                        knownSkill = true;
                    }
                }

                // Если скилл не учился ранее и является новым
                if(!knownSkill && skill.getSkillLevel() == 1)
                {
                    if(ignoreSkills.containsKey(skill.getSkillId()))
                    {
                        knownSkill = true;
                    }

                    if(!knownSkill)
                    {
                        result.add(skill);
                    }
                }
            }
        }

        return result;
    }

    //Проверяем есть ли новые скилы
    public boolean hasNewSkillsByLevel(L2PcInstance player)
    {
        int one_skills_size = 0;
        int two_skills_size = 0;
        for(L2SkillLearn temp : getCompleteClassSkillTree(player.getClassId()).values())
        {
            if(player.getLevel() - 1 < temp.getMinLevel())
            {
                one_skills_size++;
            }

            if(player.getLevel() < temp.getMinLevel())
            {
                two_skills_size++;
            }
        }
        return one_skills_size != two_skills_size;
    }

    /**
     * @param player the player requesting the Auto-Get skills.
     * @return all the available Auto-Get skills for a given {@code player}.
     */
    public List<L2SkillLearn> getAvailableAutoGetSkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();
        Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(player.getClassId());

        if(skills.isEmpty())
        {
            //The Skill Tree for this class is undefined, so we return an empty list.
            _log.log(Level.WARN, getClass().getSimpleName() + ": Skill Tree for this classId(" + player.getClassId() + ") is not defined!");
            return new ArrayList<>();
        }

        Race race = player.getRace();
        Collection<L2Skill> allSkills = player.getAllSkills();
        Map<Integer, L2Skill> oldSkills = new HashMap<>();
        List<Integer> undelete = getAwakenUndeleteSkills(player);
        List<Integer> deletable = getAwakenDeleteSkills(player);
        List<Integer> replaceables = getAllReplaceableSkills(player);

        for(L2Skill skill : allSkills)
        {
            oldSkills.put(skill.getId(), skill);
        }

        for(L2SkillLearn skill : skills.values())
        {
            if(!skill.checkSubClassTypes(player) && !skill.getRaces().isEmpty() && !skill.getRaces().contains(race))
            {
                continue;
            }

            if(!skill.isAutoGet())
            {
                continue;
            }

            if(player.isAwakened() && (deletable.contains(skill.getSkillId()) || replaceables.contains(skill.getSkillId()) || skill.getMinLevel() < 2 && !undelete.contains(skill.getSkillId())))
            {
                continue;
            }

            if(player.getLevel() >= skill.getMinLevel())
            {
                if(oldSkills.containsKey(skill.getSkillId()))
                {
                    if(oldSkills.get(skill.getSkillId()).getLevel() < skill.getSkillLevel())
                    {
                        result.add(skill);
                    }
                }
                else
                {
                    result.add(skill);
                }
            }
        }

        if(player.isAwakened())
        {
            getRacePassiveSkills(player.getRace()).stream().filter(temp -> temp.isAutoGet() && player.getLevel() >= temp.getMinLevel()).forEach(temp -> {
                if(oldSkills.containsKey(temp.getSkillId()))
                {
                    if(oldSkills.get(temp.getSkillId()).getLevel() < temp.getSkillLevel())
                    {
                        result.add(temp);
                    }
                }
                else
                {
                    result.add(temp);
                }
            });
        }
        return result;
    }

    /**
     * Dwarvens will get additional dwarven only fishing skills.
     * @param player
     * @return all the available Fishing skills for a given {@code player}.
     */
    public List<L2SkillLearn> getAvailableFishingSkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();

        Race playerRace = player.getRace();
        for(L2SkillLearn skill : _fishingSkillTree.values())
        {
            // If skill is Race specific and the player's race isn't allowed, skip it.
            if(!skill.getRaces().isEmpty() && !skill.getRaces().contains(playerRace))
            {
                continue;
            }

            if(skill.isLearnedByNpc() && player.getLevel() >= skill.getMinLevel())
            {
                L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
                if(oldSkill != null)
                {
                    if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                    {
                        result.add(skill);
                    }
                }
                else if(skill.getSkillLevel() == 1)
                {
                    result.add(skill);
                }
            }
        }

        return result;
    }

    /**
     * Используется на Континенте Грация
     * @param player игрок, запрашивающий список доступных скилов Сбора
     * @return все доступные для изучения скилы Сбора {@code player}.
     */
    public List<L2SkillLearn> getAvailableCollectSkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();
        for(L2SkillLearn skill : _collectSkillTree.values())
        {
            L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
            if(oldSkill != null)
            {
                if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                {
                    result.add(skill);
                }
            }
            else if(skill.getSkillLevel() == 1)
            {
                result.add(skill);
            }
        }
        return result;
    }

    /**
     * @param player игрок, запрашивающий список
     * @return все возможные Кросс-умения для игрока {@code player}.
     */
    public List<L2SkillLearn> getAvailableTransferSkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();

        if(player.isAwakened())
        {
            return result;
        }

        ClassId classId = player.getClassId();

        // Если игрок завершил смену 3-ей или 4-ой профессии, берем его родительский класс
        if(classId.level() == 3)
        {
            classId = classId.getParent();
        }

        if(!_transferSkillTrees.containsKey(classId))
        {
            return result;
        }

        // Если игрок не изучал текущее умение, добавляем его в список.
        result.addAll(_transferSkillTrees.get(classId).values().stream().filter(skill -> player.getKnownSkill(skill.getSkillId()) == null).collect(Collectors.toList()));
        return result;
    }

    /**
     * @param player игрок, запрашивающий список
     * @return все возможные Кросс-умения для игрока {@code player}.
     */
    public List<Integer> getAvailableTransferSkillsList(L2PcInstance player)
    {
        List<Integer> result = new ArrayList<>();

        ClassId classId = player.getClassId();

        // Если игрок завершил смену 3-ей или 4-ой профессии, берем его родительский класс
        if(classId.level() == 3)
        {
            classId = classId.getParent();
        }
        // Да,да,да уебищество, но пока по-другому никак - паренты от 4 ых проф сейчас 3-и - они то-же нужны :)
        else if(classId.level() == 4)
        {
            classId = classId.getParent().getParent();
        }

        if(!_transferSkillTrees.containsKey(classId))
        {
            return result;
        }

        result.addAll(_transferSkillTrees.get(classId).values().stream().map(L2SkillLearn::getSkillId).collect(Collectors.toList()));

        return result;
    }

    /**
     * Some transformations are not available for some races.
     * @param player transformation skill learning player.
     * @return all the available Transformation skills for a given {@code player}.
     */
    public List<L2SkillLearn> getAvailableTransformSkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();
        Map<Integer, L2SkillLearn> skills = _transformSkillTree;

        if(skills == null)
        {
            //The Skill Tree for Transformation skills is undefined.
            _log.log(Level.WARN, getClass().getSimpleName() + ": No Transform skills defined!");
            return new ArrayList<>();
        }

        Race race = player.getRace();
        skills.values().stream().filter(skill -> player.getLevel() >= skill.getMinLevel() && (skill.getRaces().isEmpty() || skill.getRaces().contains(race))).forEach(skill -> {
            L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
            if(oldSkill != null)
            {
                if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                {
                    result.add(skill);
                }
            }
            else if(skill.getSkillLevel() == 1)
            {
                result.add(skill);
            }
        });

        return result;
    }

    /**
     * @param clan the pledge skill learning clan.
     * @return all the available Pledge skills for a given {@code clan}.
     */
    public List<L2SkillLearn> getAvailablePledgeSkills(L2Clan clan)
    {
        List<L2SkillLearn> result = new ArrayList<>();
        _pledgeSkillTree.values().stream().filter(skill -> !skill.isResidencialSkill() && clan.getLevel() >= skill.getMinLevel()).forEach(skill -> {
            L2Skill oldSkill = clan.getSkills().get(skill.getSkillId());
            if(oldSkill != null)
            {
                if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                {
                    result.add(skill);
                }
            }
            else if(skill.getSkillLevel() == 1)
            {
                result.add(skill);
            }
        });
        return result;
    }

    /**
     * @param clan the sub-pledge skill learning clan.
     * @return all the available Sub-Pledge skills for a given {@code clan}.
     */
    public List<L2SkillLearn> getAvailableSubPledgeSkills(L2Clan clan)
    {
        return _subPledgeSkillTree.values().stream().filter(skill -> clan.getLevel() >= skill.getMinLevel() && clan.isLearnableSubSkill(skill.getSkillId(), skill.getSkillLevel())).collect(Collectors.toList());
    }

    /**
     * @param player the sub-class skill learning player.
     * @return all the available Sub-Class skills for a given {@code player}.
     */
    public List<L2SkillLearn> getAvailableSubClassSkills(L2PcInstance player, boolean isDual)
    {
        List<L2SkillLearn> result = new ArrayList<>();

        for(L2SkillLearn skill : isDual ? _dualClassSkillTree.values() : _subClassSkillTree.values())
        {
            if(player.getLevel() >= skill.getMinLevel())
            {
                List<L2SkillLearn.SubClassData> subClassConds = null;
                for(SubClass subClass : player.getSubClasses().values())
                {
                    subClassConds = skill.getSubClassConditions();
                    if(!subClassConds.isEmpty() && subClass.getClassIndex() <= subClassConds.size() && subClass.getClassIndex() == subClassConds.get(subClass.getClassIndex() - 1).getSlot() && subClassConds.get(subClass.getClassIndex() - 1).getLvl() <= subClass.getLevel())
                    {
                        L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
                        if(oldSkill != null)
                        {
                            if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                            {
                                result.add(skill);
                            }
                        }
                        else if(skill.getSkillLevel() == 1)
                        {
                            result.add(skill);
                        }
                    }
                }
            }
        }

        return result;
    }

    public List<L2SkillLearn> getAvailableAlchemySkills(L2PcInstance player)
    {
        List<L2SkillLearn> result = new ArrayList<>();

        Race playerRace = player.getRace();
        for(L2SkillLearn skill : _alchemySkillTree.values())
        {
            if(!skill.getRaces().isEmpty() && !skill.getRaces().contains(playerRace))
            {
                continue;
            }

            if(skill.isLearnedByNpc() && player.getLevel() >= skill.getMinLevel())
            {
                L2Skill oldSkill = player.getSkills().get(skill.getSkillId());
                if(oldSkill != null)
                {
                    if(oldSkill.getLevel() == skill.getSkillLevel() - 1)
                    {
                        result.add(skill);
                    }
                }
                else if(skill.getSkillLevel() == 1)
                {
                    result.add(skill);
                }
            }
        }

        return result;
    }

    /***
     * @return список всех сертификационнных скиллов
     */
    public Collection<L2SkillLearn> getAllSubClassSkills()
    {
        return _subClassSkillTree.values();
    }

    /***
     * @return список всех сертификационнных скиллов Дуала
     */
    public Collection<L2SkillLearn> getAllDualClassSkills()
    {
        return _dualClassSkillTree.values();
    }

    /**
     * @param residenceId the id of the Castle, Fort, Territory.
     * @return all the available Residential skills for a given {@code residenceId}.
     */
    public List<L2SkillLearn> getAvailableResidentialSkills(int residenceId, CastleSide side)
    {
        return _pledgeSkillTree.values().stream().filter(skill -> skill.isResidencialSkill() && skill.getResidenceIds().contains(residenceId) && skill.isCastleSide(side)).collect(Collectors.toList());
    }

    /**
     * @param id the transformation skill ID.
     * @param lvl the transformation skill level.
     * @return the transform skill from the Transform Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getTransformSkill(int id, int lvl)
    {
        return _transformSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the ability skill Id
     * @param lvl the ability skill level
     * @return the ability skill from the Ability Skill Tree for a given {@code id} and {@code lvl}
     */
    public L2SkillLearn getAbilitySkill(int id, int lvl)
    {
        return _abilitySkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the alchemy skill Id
     * @param lvl the alchemy skill level
     * @return the alchemy skill from the Alchemy Skill Tree for a given {@code id} and {@code lvl}
     */
    public L2SkillLearn getAlchemySkill(int id, int lvl)
    {
        return _alchemySkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the class skill ID.
     * @param lvl the class skill level.
     * @param classId the class skill tree ID.
     * @return the class skill from the Class Skill Trees for a given {@code classId}, {@code id} and {@code lvl}.
     */
    public L2SkillLearn getClassSkill(int id, int lvl, ClassId classId)
    {
        Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(classId);

        return skills.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the fishing skill ID.
     * @param lvl the fishing skill level.
     * @return Fishing skill from the Fishing Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getFishingSkill(int id, int lvl)
    {
        return _fishingSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the pledge skill ID.
     * @param lvl the pledge skill level.
     * @return the pledge skill from the Pledge Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getPledgeSkill(int id, int lvl)
    {
        return _pledgeSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the sub-pledge skill ID.
     * @param lvl the sub-pledge skill level.
     * @return the sub-pledge skill from the Sub-Pledge Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getSubPledgeSkill(int id, int lvl)
    {
        return _subPledgeSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the transfer skill ID.
     * @param lvl the transfer skill level.
     * @param classId the transfer skill tree ID.
     * @return the transfer skill from the Transfer Skill Trees for a given {@code classId}, {@code id} and {@code lvl}.
     */
    public L2SkillLearn getTransferSkill(int id, int lvl, ClassId classId)
    {
        if(classId.level() == 4)
        {
            return null;
        }

        if(classId.getParent() != null)
        {
            ClassId parentId = classId.getParent();
            if(_transferSkillTrees.get(parentId) != null)
            {
                return _transferSkillTrees.get(parentId).get(SkillTable.getSkillHashCode(id, lvl));
            }
        }
        return null;
    }

    /**
     * @param id the sub-class skill ID.
     * @param lvl the sub-class skill level.
     * @return the sub-class skill from the Sub-Class Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getSubClassSkill(int id, int lvl)
    {
        return _subClassSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    public L2SkillLearn getDualClassSkill(int id, int lvl)
    {
        return _dualClassSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param id the collect skill ID.
     * @param lvl the collect skill level.
     * @return the collect skill from the Collect Skill Tree for a given {@code id} and {@code lvl}.
     */
    public L2SkillLearn getCollectSkill(int id, int lvl)
    {
        return _collectSkillTree.get(SkillTable.getSkillHashCode(id, lvl));
    }

    /**
     * @param player the player that requires the minimum level.
     * @param skillTree the skill tree to search the minimum get level.
     * @return the minimum level for a new skill for a given {@code player} and {@code skillTree}.
     */
    public int getMinLevelForNewSkill(L2PcInstance player, Map<Integer, L2SkillLearn> skillTree)
    {
        int minLevel = 0;
        if(skillTree.isEmpty())
        {
            _log.log(Level.WARN, getClass().getSimpleName() + ": SkillTree is not defined for getMinLevelForNewSkill!");
        }
        else
        {
            for(L2SkillLearn s : skillTree.values())
            {
                if(s.isLearnedByNpc() && player.getLevel() < s.getMinLevel())
                {
                    if(minLevel == 0 || minLevel > s.getMinLevel())
                    {
                        minLevel = s.getMinLevel();
                    }
                }
            }
        }
        return minLevel;
    }

    /**
     * @param skillId the Id of the skill to check.
     * @param skillLevel the level of the skill to check, if it's -1 only Id will be checked.
     * @return {@code true} if the skill is present in the Hero Skill Tree, {@code false} otherwise.
     */
    public boolean isHeroSkill(int skillId, int skillLevel)
    {
        if(_heroSkillTree.containsKey(SkillTable.getSkillHashCode(skillId, skillLevel)))
        {
            return true;
        }

        for(L2SkillLearn skill : _heroSkillTree.values())
        {
            if(skill.getSkillId() == skillId && skillLevel == -1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param skillId skillId the Id of the skill to check.
     * @param skillLevel skillLevel the level of the skill to check, if it's -1 only Id will be checked.
     * @return {@code true} if the skill is present in the Game Master Skill Trees, {@code false} otherwise.
     */
    public boolean isGMSkill(int skillId, int skillLevel)
    {
        FastMap<Integer, L2SkillLearn> gmSkills = new FastMap<>();
        gmSkills.putAll(_gameMasterSkillTree);
        gmSkills.putAll(_gameMasterAuraSkillTree);
        if(gmSkills.containsKey(SkillTable.getSkillHashCode(skillId, skillLevel)))
        {
            return true;
        }

        for(L2SkillLearn skill : gmSkills.values())
        {
            if(skill.getSkillId() == skillId && skillLevel == -1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param gmchar the player to add the Game Master skills.
     * @param auraSkills if {@code true} it will add "GM Aura" skills, else will add the "GM regular" skills.
     */
    public void addGmSkills(L2PcInstance gmchar, boolean auraSkills)
    {
        Collection<L2SkillLearn> skills = auraSkills ? _gameMasterAuraSkillTree.values() : _gameMasterSkillTree.values();
        SkillTable st = SkillTable.getInstance();
        for(L2SkillLearn sl : skills)
        {
            gmchar.addSkill(st.getInfo(sl.getSkillId(), sl.getSkillLevel()), false); // Don't Save GM skills to database
        }
    }

    /**
     * Create and store hash values for skills for easy and fast checks.
     */
    private void generateCheckArrays()
    {
        int i;
        int[] array;

        //Class specific skills:
        Map<Integer, L2SkillLearn> tempMap;
        TIntObjectHashMap<int[]> result = new TIntObjectHashMap<>(_classSkillTrees.keySet().size());
        for(ClassId cls : _classSkillTrees.keySet())
        {
            i = 0;
            tempMap = getCompleteClassSkillTree(cls);
            array = new int[tempMap.size()];
            for(int h : tempMap.keySet())
            {
                array[i++] = h;
            }
            Arrays.sort(array);
            result.put(cls.ordinal(), array);
        }
        _skillsByClassIdHashCodes = result;

        //Race specific skills from Fishing and Transformation skill trees.
        FastList<Integer> list = FastList.newInstance();
        result = new TIntObjectHashMap<>(Race.values().length);
        for(Race r : Race.values())
        {
            list.addAll(_fishingSkillTree.values().stream().filter(s -> s.getRaces().contains(r)).map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

            list.addAll(_transformSkillTree.values().stream().filter(s -> s.getRaces().contains(r)).map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

            for(List<L2SkillLearn> sk : _racePassiveSkillTree.values())
            {
                list.addAll(sk.stream().filter(s -> s.getRaces().contains(r)).map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));
            }

            i = 0;
            array = new int[list.size()];
            for(int s : list)
            {
                array[i++] = s;
            }
            Arrays.sort(array);
            result.put(r.ordinal(), array);
            list.clear();
        }
        _skillsByRaceHashCodes = result;

        //Skills available for all classes and races
        list.addAll(_fishingSkillTree.values().stream().filter(s -> s.getRaces().isEmpty()).map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_transformSkillTree.values().stream().filter(s -> s.getRaces().isEmpty()).map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_collectSkillTree.values().stream().map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_raceActiveSkillTree.values().stream().map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_raceActiveSkillSubTree.values().stream().map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_abilitySkillTree.values().stream().map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));

        list.addAll(_alchemySkillTree.values().stream().map(s -> SkillTable.getSkillHashCode(s.getSkillId(), s.getSkillLevel())).collect(Collectors.toList()));
        i = 0;
        array = new int[list.size()];
        for(int s : list)
        {
            array[i++] = s;
        }
        Arrays.sort(array);
        _allSkillsHashCodes = array;

        FastList.recycle(list);
    }

    /**
     * Проверяет, соответствует ли скилл классу заданного игрока.
     * Скиллы Гейммастера исключаются из этой проверки для ГМ'ов.
     * @param player игрок, у которого проверяем скилл.
     * @param skill скилл, который проверяем.
     * @return {@code true} является ли скилл валидным для игрока.
     */
    public boolean isSkillAllowed(L2PcInstance player, L2Skill skill)
    {
        if(skill.isExcludedFromCheck())
        {
            return true;
        }

        if(player.isGM() && skill.isGMSkill())
        {
            return true;
        }

        // Если скилы в данный момент в перезагрузке, отменяем проверку
        if(_loading)
        {
            return true;
        }

        int maxLvl = SkillTable.getInstance().getMaxLevel(skill.getId());
        int hashCode = SkillTable.getSkillHashCode(skill.getId(), Math.min(skill.getLevel(), maxLvl));

        if(Arrays.binarySearch(_skillsByClassIdHashCodes.get(player.getClassId().ordinal()), hashCode) >= 0)
        {
            return true;
        }

        if(Arrays.binarySearch(_skillsByRaceHashCodes.get(player.getRace().ordinal()), hashCode) >= 0)
        {
            return true;
        }

        if(Arrays.binarySearch(_allSkillsHashCodes, hashCode) >= 0)
        {
            return true;
        }

        // Исключаем скиллы трансформации из проверки на валидность
        // TODO: Загнать в скилы трансформации isExcludedFromCheck() = true
        // TODO: return false;
        return getTransferSkill(skill.getId(), Math.min(skill.getLevel(), maxLvl), player.getClassId()) != null;
    }

    /**
     * Returns that skills that could be removed from character skill list on awakening.
     *
     * @param player Current player.
     * @return
     */
    public FastList<Integer> getAwakenDeleteSkills(L2PcInstance player)
    {
        FastList<Integer> ignoreSkills = new FastList<>();

        if(player.isAwakened())
        {
            Map<Integer, L2SkillLearn> skills = getCompleteClassSkillTree(player.getClassId().getParent());
            List<Integer> replaceables = getAllReplaceableSkills(player);
            List<Integer> undelete = getAwakenUndeleteSkills(player);
            if(replaceables != null)
            {
                skills.values().stream().filter(skill -> !replaceables.contains(skill.getSkillId()) && !ignoreSkills.contains(skill.getSkillId()) && !undelete.contains(skill.getSkillId())).forEach(skill -> ignoreSkills.add(skill.getSkillId()));
            }
        }
        return ignoreSkills;
    }

    /**
     * Returns set of skills that should be kept after character awakening.
     *
     * @param player Current player.
     * @return
     */
    public List<Integer> getAwakenUndeleteSkills(L2PcInstance player)
    {
        ClassId classId = player.getClassId();
        if(classId.level() < 3)
        {
            return new ArrayList<>();
        }
        if(classId.level() > 3)
        {
            classId = classId.getParent();
        }

        return _awakeUndeleteSkillTree.get(classId.getId()) != null ? _awakeUndeleteSkillTree.get(classId.getId()) : new ArrayList<>();
    }

    /**
     * Adds new replacement linkage for replacerSkill.
     * That so skill with ID replacedSkill will be replaced by skill with ID replacerSkill.
     *
     * @param replacerSkill The skill that will be kept.
     * @param replacedSkill The skill that will be replaced.
     */
    public void addReplaceableSkill(int classId, int replacerSkill, int replacedSkill)
    {
        List<Integer> replacements;
        replacements = _replaceableSkillTree.containsKey(replacerSkill) ? _replaceableSkillTree.get(replacerSkill) : new ArrayList<>(1);

        if(!replacements.contains(replacedSkill))
        {
            replacements.add(replacedSkill);
        }
        Map<Integer, Boolean> _cache = _replaceableSkillTreeCache.get(classId);
        if(_cache == null)
        {
            _cache = new HashMap<>(1);
        }
        _cache.put(replacedSkill, true);
        _replaceableSkillTreeCache.put(classId, _cache);
        _replaceableSkillTree.put(replacerSkill, replacements);
    }

    public List<Integer> getAllReplaceableSkills(L2PcInstance player)
    {
        List<Integer> replaceables = player.getAllSkills().stream().filter(skill -> _replaceableSkillTreeCache.containsKey(player.getClassId().getId()) && _replaceableSkillTreeCache.get(player.getClassId().getId()).containsKey(skill.getId())).map(L2Skill::getId).collect(Collectors.toList());
        return replaceables;
    }

    /**
     * Gets all replaceable skills for given skill ID.
     * @param skillId Skill ID.
     * @return
     */
    public List<Integer> getAllReplaceableSkills(int skillId)
    {
        return _replaceableSkillTree.get(skillId);
    }

    /**
     * Gets all replaceable skills for given skill.
     * @param skill Replacer skill.
     * @return
     */
    public List<Integer> getAllReplaceableSkills(L2SkillLearn skill)
    {
        return _replaceableSkillTree.get(skill.getSkillId());
    }

    /**
     * @param race ид расы
     * @return расовые пассивные скилы (учатся автоматически)
     */
    private List<L2SkillLearn> getRacePassiveSkills(Race race)
    {
        return _racePassiveSkillTree.get(race);
    }

    /**
     * @param forSubClass {@code true} если деревео нужно для саб-класса (разные предметы длы изучения)
     * @return расовые активные скилы (учатся у НПЦ)
     */
    public Map<Integer, L2SkillLearn> getRaceActiveSkills(boolean forSubClass)
    {
        return forSubClass ? _raceActiveSkillSubTree : _raceActiveSkillTree;
    }

    private void report()
    {
        int classSkillTreeCount = 0;
        for(Entry<ClassId, Map<Integer, L2SkillLearn>> classIdMapEntry1 : _classSkillTrees.entrySet())
        {
            classSkillTreeCount += classIdMapEntry1.getValue().size();
        }

        int trasferSkillTreeCount = 0;
        for(Entry<ClassId, Map<Integer, L2SkillLearn>> classIdMapEntry : _transferSkillTrees.entrySet())
        {
            trasferSkillTreeCount += classIdMapEntry.getValue().size();
        }

        int fishingDwarvenSkillCount = 0;
        for(L2SkillLearn fishSkill : _fishingSkillTree.values())
        {
            if(fishSkill.getRaces().contains(Race.Dwarf))
            {
                fishingDwarvenSkillCount++;
            }
        }

        int residentialSkillCount = 0;
        for(L2SkillLearn pledgeSkill : _pledgeSkillTree.values())
        {
            if(pledgeSkill.isResidencialSkill())
            {
                residentialSkillCount++;
            }
        }

        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + classSkillTreeCount + "  Class Skills for " + _classSkillTrees.size() + " Class Skill Trees.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _subClassSkillTree.size() + " Sub-Class Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + trasferSkillTreeCount + " Transfer Skills for " + _transferSkillTrees.size() + " Transfer Skill Trees.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _fishingSkillTree.size() + " Fishing Skills, " + fishingDwarvenSkillCount + " Dwarven only Fishing Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _collectSkillTree.size() + " Collect Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _pledgeSkillTree.size() + " Pledge Skills, " + (_pledgeSkillTree.size() - residentialSkillCount) + " for Pledge and " + residentialSkillCount + " Residential.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _subPledgeSkillTree.size() + " Sub-Pledge Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _transformSkillTree.size() + " Transform Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _racePassiveSkillTree.size() + " Race Skill collection.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _raceActiveSkillTree.size() + " Race Active Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _raceActiveSkillSubTree.size() + " Race Active(Subclass) Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _replaceableSkillTree.size() + " Awaken Replaceable Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _awakeUndeleteSkillTree.size() + " Awaken Undelete Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _nobleSkillTree.size() + " Noble Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _heroSkillTree.size() + " Hero Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _gameMasterSkillTree.size() + " Game Master Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _gameMasterAuraSkillTree.size() + " Game Master Aura Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _abilitySkillTree.size() + " Ability Skills.");
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _alchemySkillTree.size() + " Alchemy Skills.");
    }

    /**
     * Gives a set of GM skills.
     *
     * @param player Active player.
     * @param type Skill set type. Single-target or aura skills.
     */
    public void giveGmSkills(L2PcInstance player, GmSkillType type)
    {
        if(!player.isGM())
        {
            return;
        }

        Map<Integer, L2SkillLearn> skills;
        switch(type)
        {
            case AURA:
                skills = _gameMasterAuraSkillTree;
                break;
            case TARGET:
            default:
                skills = _gameMasterSkillTree;
                break;
        }
        for(L2SkillLearn learn : skills.values())
        {
            player.addSkill(SkillTable.getInstance().getInfo(learn.getSkillId(), learn.getSkillLevel()), false);
        }
    }

    /**
     * Removes GM all skills from player.
     * @param player Active player.
     */
    public void removeGmSkills(L2PcInstance player)
    {
        for(L2SkillLearn learn : _gameMasterSkillTree.values())
        {
            player.removeSkill(learn.getSkillId());
        }
        for(L2SkillLearn learn : _gameMasterAuraSkillTree.values())
        {
            player.removeSkill(learn.getSkillId());
        }
    }

    public enum GmSkillType
    {
        TARGET,
        AURA
    }
}