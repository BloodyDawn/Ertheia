package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2MonsterInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.skills.SkillTable;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.npc.L2MinionData;
import dwo.gameserver.model.world.npc.drop.L2DropData;
import dwo.gameserver.model.world.zone.Location;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * L2GOD Team
 * User: Bacek
 * Date: 22.09.12
 * Time: 13:27
 */

public class NpcTable extends XmlDocumentParser
{
    private static final Map<Integer, L2NpcTemplate> _npcs = new HashMap<>();
    private List<DropList> _drop = new ArrayList<>();
    private boolean custom;

    protected static NpcTable instance;

    private NpcTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static NpcTable getInstance()
    {
        return instance == null ? instance = new NpcTable() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _npcs.clear();
        _drop.clear();
        parseFile( new File( Config.DATAPACK_ROOT, "/data/stats/client/ClientNpc.xml" ) );
        parseDirectory(FilePath.NPC_STATS);

        try
        {
            custom = true;
            parseDirectory( FilePath.CUSTOM_NPC_STATS );
        }
        finally
        {
            custom = false;
        }

        for (final DropList dl : _drop)
        {
            for (final DropCategory cat : dl.getCategories())
            {
                getTemplate(dl.getId()).setDropCategoryChance(cat.getId(), (int) cat.getChance());

                for (final L2DropData item : cat.getItems())
                {
                    item.setChance(item.getChance() * 10000.0);
                    getTemplate(dl.getId()).addDropData(item, cat.getId());
                }
            }
        }
        _log.info(getClass().getSimpleName() + ": Loaded " + _npcs.size() + " NPC template(s) and " + _drop.size() + " drop(s) count.");
    }

    protected void parseDocument(Element rootElement)
    {
        StatsSet npcDat;
        List<L2Skill> npc_skills;
        List<L2MinionData> minions;
        List<ClassId> teachInfos;
        List<Location> telePositions;
        Map<String, Integer> doorList;
        for(Element element : rootElement.getChildren())
        {
            final String nameE = element.getName();
            if(nameE.equalsIgnoreCase("npc"))
            {
                npcDat = new StatsSet();

                // Default element resists
                npcDat.set("baseFireRes", 20);
                npcDat.set("baseWindRes", 20);
                npcDat.set("baseWaterRes", 20);
                npcDat.set("baseEarthRes", 20);
                npcDat.set("baseHolyRes", 20);
                npcDat.set("baseDarkRes", 20);

                npc_skills = new ArrayList<>();
                minions = new ArrayList<>();
                teachInfos = new ArrayList<>();
                telePositions = new ArrayList<>();
                doorList = new HashMap<>();

                int npcId = Integer.parseInt(element.getAttributeValue("id"));
                int templateId = element.getAttributeValue("displayId") == null ? npcId : Integer.parseInt(element.getAttributeValue("displayId"));
                String name = element.getAttributeValue("name");
                String title = element.getAttributeValue("title");
                String server_name = element.getAttributeValue("server_name") == null ? "" : element.getAttributeValue("server_name");

                npcDat.set("npcId", npcId);
                npcDat.set("idTemplate", templateId);
                npcDat.set("name", name);
                npcDat.set("title", title);
                npcDat.set("server_name", server_name);

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("skills"))
                    {
                        L2Skill npcSkill;
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }

                            int id = Integer.parseInt(element2.getAttributeValue("id"));
                            int level = Integer.parseInt(element2.getAttributeValue("level"));

                            // Для определения расы используется скилл 4416
                            if(id == L2Skill.SKILL_NPC_RACE)
                            {
                                npcDat.set("raceId", level);
                                continue;
                            }

                            npcSkill = SkillTable.getInstance().getInfo(id, level);

                            if(npcSkill == null)
                            {
                                continue;
                            }

                            npc_skills.add(npcSkill);
                        }
                    }
                    else if(name1.equalsIgnoreCase("minions"))
                    {
                        L2MinionData minionDat;
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }

                            minionDat = new L2MinionData();
                            minionDat.setMinionId(Integer.parseInt(element2.getAttributeValue("id")));
                            minionDat.setAmountMin(Integer.parseInt(element2.getAttributeValue("amount_min")));
                            minionDat.setAmountMax(Integer.parseInt(element2.getAttributeValue("amount_max")));

                            minions.add(minionDat);
                        }
                    }
                    else if(name1.equalsIgnoreCase("teach_classes"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }

                            teachInfos.add(ClassId.values()[Integer.parseInt(element2.getAttributeValue("id"))]);
                        }
                    }
                    else if(name1.equalsIgnoreCase("ai_params"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }

                            npcDat.set(element2.getAttributeValue("name").trim(), element2.getAttributeValue("value").trim());
                        }
                    }
                    else if(name1.equalsIgnoreCase("teleportPositions"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }
                            StatsSet teleStats = new StatsSet();
                            teleStats.set("x", Integer.parseInt(element2.getAttributeValue("x")));
                            teleStats.set("y", Integer.parseInt(element2.getAttributeValue("y")));
                            teleStats.set("z", Integer.parseInt(element2.getAttributeValue("z")));
                            teleStats.set("id", Integer.parseInt(element2.getAttributeValue("id")));
                            telePositions.add(new Location(teleStats));
                        }
                    }
                    else if(name1.equalsIgnoreCase("doors"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            if(element2.getAttributes() == null)
                            {
                                continue;
                            }
                            doorList.put(element2.getAttributeValue("name"), Integer.parseInt(element2.getAttributeValue("id")));
                        }
                    }
                    else if(name1.equalsIgnoreCase("set"))
                    {
                        npcDat.set(element1.getAttributeValue("name").trim(), element1.getAttributeValue("value").trim());
                    }
                    else if (name1.equalsIgnoreCase("drop_list"))
                    {
                        for (Element element2 : element1.getChildren())
                        {
                            DropList dropList = new DropList();
                            dropList.categories = new ArrayList<>();
                            dropList.id = npcId;
                            _drop.add(dropList);

                            final String name2 = element2.getName();
                            if (name2.equals("category"))
                            {
                                int categoryId = Integer.parseInt(element2.getAttributeValue("id"));
                                double chance = Double.parseDouble(element2.getAttributeValue("chance"));

                                DropCategory dropCategory = new DropCategory();
                                dropCategory.id = categoryId;
                                dropCategory.setChance(chance);
                                dropList.categories.add(dropCategory);
                                List<L2DropData> items = new ArrayList<>();
                                dropCategory.items = items;

                                for (Element element3 : element2.getChildren())
                                {
                                    final String name3 = element3.getName();
                                    if (name3.equals("items"))
                                    {
                                        for (Element element4 : element3.getChildren())
                                        {
                                            final String name4 = element4.getName();
                                            if (name4.equals("item"))
                                            {
                                                int itemId = Integer.parseInt(element4.getAttributeValue("id"));
                                                int min = Integer.parseInt(element4.getAttributeValue("min"));
                                                int max = Integer.parseInt(element4.getAttributeValue("max"));

                                                double chance1 = Double.parseDouble(element4.getAttributeValue("chance"));
                                                L2DropData dropData = new L2DropData();
                                                dropData.setItemId(itemId);
                                                dropData.setMinDrop(min);
                                                dropData.setMaxDrop(max);
                                                dropData.setChance(chance1);
                                                items.add(dropData);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if( _npcs.get( npcId ) != null && !custom )
                {
                    L2NpcTemplate template = _npcs.get( npcId );
                    npcDat.set( "ground_high", template.getBaseRunSpd() );
                    npcDat.set( "ground_low", template.getBaseWalkSpd() );
                    npcDat.set( "collision_radius", template.getCollisionRadius() );
                    npcDat.set( "collision_height", template.getCollisionHeight() );
                    npcDat.set( "org_hp", template.getBaseHpMax() );
                    npcDat.set( "org_mp", template.getBaseMpMax() );
                    template.updateL2CharTemplate( npcDat );
                    template.updateL2NpcTemplate( npcDat );
                    npc_skills.forEach(template::addSkill);
                    minions.forEach(template::addRaidData);
                    teachInfos.forEach(template::addTeachInfo);
                    telePositions.forEach( template::addTelePosition );
                    for(Map.Entry<String, Integer> entry : doorList.entrySet())
                    {
                        template.addDoor(entry.getKey(), entry.getValue());
                    }
                }
                else
                {
                    L2NpcTemplate template = new L2NpcTemplate( npcDat );
                    npc_skills.forEach(template::addSkill);
                    minions.forEach(template::addRaidData);
                    teachInfos.forEach(template::addTeachInfo);
                    telePositions.forEach(template::addTelePosition);
                    for(Map.Entry<String, Integer> entry : doorList.entrySet())
                    {
                        template.addDoor(entry.getKey(), entry.getValue());
                    }
                    _npcs.put( npcId, template );
                }
            }
        }
    }

    private void parseDropList(Element rootElement, L2NpcTemplate template)
    {
        for (Element element1 : rootElement.getChildren())
        {
            DropList dropList = new DropList();
            dropList.categories = new ArrayList<>();
            dropList.id = template.getNpcId();
            _drop.add(dropList);

            final String name1 = element1.getName();
            if (name1.equals("category"))
            {
                int categoryId = Integer.parseInt(element1.getAttributeValue("id"));
                double chance = Double.parseDouble(element1.getAttributeValue("chance"));

                DropCategory dropCategory = new DropCategory();
                dropCategory.id = categoryId;
                dropCategory.setChance(chance);
                dropList.categories.add(dropCategory);
                List<L2DropData> items = new ArrayList<>();
                dropCategory.items = items;

                for (Element element2 : element1.getChildren())
                {
                    final String name2 = element2.getName();
                    if (name2.equals("items"))
                    {
                        for (Element element3 : element2.getChildren())
                        {
                            final String name3 = element2.getName();
                            if (name3.equals("item"))
                            {
                                int itemId = Integer.parseInt(element3.getAttributeValue("id"));
                                int min = Integer.parseInt(element3.getAttributeValue("min"));
                                int max = Integer.parseInt(element3.getAttributeValue("max"));

                                double chance1 = Double.parseDouble(element3.getAttributeValue("chance"));
                                L2DropData dropData = new L2DropData();
                                dropData.setItemId(itemId);
                                dropData.setMinDrop(min);
                                dropData.setMaxDrop(max);
                                dropData.setChance(chance1);
                                items.add(dropData);
                            }
                        }
                    }
                }
            }
        }
    }

    public L2NpcTemplate getTemplate(int id)
    {
        return _npcs.get(id);
    }

    public L2NpcTemplate getTemplateByName(String name)
    {
        for(L2NpcTemplate npcTemplate : _npcs.values())
        {
            if(npcTemplate.getName().equalsIgnoreCase(name))
            {
                return npcTemplate;
            }
        }
        return null;
    }

    public List<L2NpcTemplate> getAllOfLevel(int... lvls)
    {
        List<L2NpcTemplate> list = new ArrayList<>();
        for(int lvl : lvls)
        {
            list.addAll(_npcs.values().stream().filter(t -> t.getLevel() == lvl).collect(Collectors.toList()));
        }
        return list;
    }

    public List<L2NpcTemplate> getAllMonstersOfLevel(int... lvls)
    {
        List<L2NpcTemplate> list = new ArrayList<>();
        for(int lvl : lvls)
        {
            list.addAll(_npcs.values().stream().filter(t -> t.getLevel() == lvl && t.isType("L2Monster")).collect(Collectors.toList()));
        }
        return list;
    }

    public List<L2NpcTemplate> getAllNpcStartingWith(String... letters)
    {
        List<L2NpcTemplate> list = new ArrayList<>();
        for(String letter : letters)
        {
            list.addAll(_npcs.values().stream().filter(t -> t.getName().startsWith(letter) && t.isType("L2Npc")).collect(Collectors.toList()));
        }
        return list;
    }

    public List<L2NpcTemplate> getAllNpcOfClassType(String... classTypes)
    {
        List<L2NpcTemplate> list = new ArrayList<>();
        for(String classType : classTypes)
        {
            list.addAll(_npcs.values().stream().filter(t -> t.isType(classType)).collect(Collectors.toList()));
        }
        return list;
    }

    public static class DropCategory
    {
        private int id;
        private double chance;
        private List<L2DropData> items;

        public DropCategory()
        {
            items = Collections.emptyList();
        }

        public int getId()
        {
            return id;
        }

        public double getChance()
        {
            return chance;
        }

        public void setChance(double chance)
        {
            this.chance = chance * 10000.0;
        }

        public List<L2DropData> getItems()
        {
            return items;
        }
    }

    public static class DropList
    {
        private int id;
        private List<DropCategory> categories;

        public DropList()
        {
            categories = Collections.emptyList();
        }

        public List<DropCategory> getCategories()
        {
            return categories;
        }

        public int getId()
        {
            return id;
        }
    }
}