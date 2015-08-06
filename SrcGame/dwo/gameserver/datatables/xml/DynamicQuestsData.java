package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.holders.SpawnsHolder;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuestTemplate;
import dwo.gameserver.model.world.quest.dynamicquest.DynamicQuestTemplate.DynamicQuestDate;
import dwo.gameserver.model.world.zone.L2ZoneType;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;

/**
 * L2GOD Team
 * User: Yorie
 * Date: xx.xx.12
 * Time: xx:xx
 */

public class DynamicQuestsData extends XmlDocumentParser
{
    private static final Map<Integer, DynamicQuestTemplate> _quests = new HashMap<>();

    protected static DynamicQuestsData _instance;

    private DynamicQuestsData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static DynamicQuestsData getInstance()
    {
        return _instance == null ? _instance = new DynamicQuestsData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        parseFile(FilePath.DYNAMIC_QUEST_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _quests.size() + " dynamic quests.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("quest"))
            {
                int questId = Integer.parseInt(element.getAttributeValue("id"));
                String questName = element.getAttributeValue("name");

                int minLevel = 100;
                SpawnsHolder spawnHolder = null;
                List<DynamicQuestDate> dates = Collections.emptyList();

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();

                    switch(name1)
                    {
                        case "level": {
                            minLevel = Integer.parseInt(element1.getAttributeValue("min"));
                            break;
                        }
                        case "spawnHolder": {
                            spawnHolder = DynamicSpawnData.getInstance().getSpawnsHolder(element1.getAttributeValue("name"));
                            break;
                        }
                        case "dates": {
                            dates = new ArrayList<>();

                            for (Element element2 : element1.getChildren()) 
                            {
                                final String name2 = element2.getName();
                                if (name2.equalsIgnoreCase("date")) {
                                    String dayOfWeek = element2.getAttributeValue("day");
                                    int hour = Integer.parseInt(element2.getAttributeValue("time").split(":")[0]);
                                    int minute = Integer.parseInt(element2.getAttributeValue("time").split(":")[1]);
                                    dates.add(new DynamicQuestDate(dayOfWeek, hour, minute));
                                }
                            }
                            break;
                        }
                        case "steps": 
                        {
                            for (Element element2 : element1.getChildren()) 
                            {
                                final String name2 = element2.getName();
                                if (name2.equalsIgnoreCase("step")) 
                                {
                                    int taskId = Integer.parseInt(element2.getAttributeValue("id"));
                                    String taskName = null, taskTitle = null;
                                    int winPoints = Integer.parseInt(element2.getAttributeValue("points"));
                                    int nextTaskId = -1;
                                    int duration = Integer.parseInt(element2.getAttributeValue("duration"));
                                    boolean autostart = true;
                                    Map<Integer, Integer> points = null;
                                    List<ItemHolder> rewards = Collections.emptyList();
                                    List<ItemHolder> eliteRewards = Collections.emptyList();
                                    List<L2ZoneType> zones = Collections.emptyList();
                                    Map<String, String> dialogs = null;

                                    if (element2.getAttributeValue("name") != null) 
                                    {
                                        taskName = element2.getAttributeValue("name");
                                    }

                                    if (taskName == null || taskName.isEmpty()) 
                                    {
                                        taskName = questName;
                                    }

                                    if (element2.getAttributeValue("title") != null) 
                                    {
                                        taskTitle = element2.getAttributeValue("title");
                                    }

                                    if (taskTitle == null || taskTitle.isEmpty()) 
                                    {
                                        taskTitle = questName;
                                    }

                                    if (element2.getAttributeValue("nextTaskId") != null) 
                                    {
                                        nextTaskId = Integer.parseInt(element2.getAttributeValue("nextTaskId"));
                                    }

                                    if (element2.getAttributeValue("autostart") != null) 
                                    {
                                        autostart = Boolean.parseBoolean(element2.getAttributeValue("autostart"));
                                    }

                                    for (Element element3 : element2.getChildren())
                                    {
                                        final String name3 = element3.getName();
                                        switch (name3) 
                                        {
                                            case "zones": 
                                            {
                                                zones = new ArrayList<>();
                                                for (Element element4 : element3.getChildren()) 
                                                {
                                                    final String name4 = element4.getName();
                                                    if (name4.equalsIgnoreCase("zone")) {
                                                        zones.add(ZoneManager.getInstance().getZoneById(Integer.parseInt(element4.getAttributeValue("id"))));
                                                    }
                                                }
                                                break;
                                            }
                                            case "points": 
                                            {
                                                points = new HashMap<>();
                                                for (Element element4 : element3.getChildren())
                                                {
                                                    final String name4 = element4.getName();
                                                    if (name4.equalsIgnoreCase("npc")) {
                                                        points.put(Integer.parseInt(element4.getAttributeValue("id")), Integer.parseInt(element4.getAttributeValue("points")));
                                                    }
                                                }
                                                break;
                                            }
                                            case "rewards": 
                                            {
                                                rewards = new ArrayList<>();
                                                eliteRewards = new ArrayList<>();
                                                
                                                for (Element element4 : element3.getChildren())
                                                {
                                                    final String name4 = element4.getName();
                                                    if (name4.equalsIgnoreCase("reward")) 
                                                    {
                                                        ItemHolder item = new ItemHolder(Integer.parseInt(element4.getAttributeValue("id")), Integer.parseInt(element4.getAttributeValue("count")));
                                                        if (element4.getAttributeValue("type").equalsIgnoreCase("elite"))
                                                        {
                                                            eliteRewards.add(item);
                                                        } 
                                                        else 
                                                        {
                                                            rewards.add(item);
                                                        }
                                                    }
                                                }
                                                break;
                                            }
                                            case "dialogs": 
                                            {
                                                dialogs = new HashMap<>();

                                                for (Element element4 : element3.getChildren())
                                                {
                                                    final String name4 = element4.getName();
                                                    if (name4.equalsIgnoreCase("dialog")) 
                                                    {
                                                        dialogs.put(element4.getAttributeValue("type"), element4.getAttributeValue("name"));
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }

                                    DynamicQuestTemplate template = new DynamicQuestTemplate(questId, taskId, taskName, taskTitle, duration, minLevel, winPoints, nextTaskId, autostart, spawnHolder, zones, dates, points, rewards, eliteRewards, dialogs);
                                    _quests.put(taskId, template);
                                }
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public void reload()
    {
        _quests.clear();
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * К примеру, первый квест Орбиса можно получить перезав в качестве taskId значение 201.
     * @param taskId ID задачи.
     * @return DynamicQuestTemplate по ID задачи
     */
    public DynamicQuestTemplate getQuest(int taskId)
    {
        for(DynamicQuestTemplate template : _quests.values())
        {
            if(template.getTaskId() == taskId)
            {
                return template;
            }
        }

        return null;
    }
}