package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.soulcrystal.SoulCrystal;
import dwo.gameserver.model.items.soulcrystal.SoulCrystalAbsorbType;
import dwo.gameserver.model.items.soulcrystal.SoulCrystalLevelingInfo;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 02.06.12
 * Time: 17:42
 */

public class SoulCrystalData extends XmlDocumentParser
{
    private final Map<Integer, SoulCrystal> _soulCrystals = new HashMap<>();
    private final Map<Integer, Map<Integer, SoulCrystalLevelingInfo>> _npcLevelingInfos = new HashMap<>();

    protected static SoulCrystalData instance;

    private SoulCrystalData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static SoulCrystalData getInstance()
    {
        return instance == null ? instance = new SoulCrystalData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _soulCrystals.clear();
        _npcLevelingInfos.clear();
        parseFile(FilePath.SOUL_CRYSTALS_UPGRADE_DATA);
        _log.log(Level.INFO, "[EnhanceYourWeapon] Loaded " + _soulCrystals.size() + " Soul Crystal data.");
        _log.log(Level.INFO, "[EnhanceYourWeapon] Loaded " + _npcLevelingInfos.size() + " npc Leveling info data.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("crystal"))
            {
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("item"))
                    {
                        String att = element1.getAttributeValue("itemId");
                        if(att == null)
                        {
                            _log.log(Level.ERROR, "[EnhanceYourWeapon] Missing itemId in Crystal List, skipping");
                            continue;
                        }
                        int itemId = Integer.parseInt(element1.getAttributeValue("itemId"));

                        att = element1.getAttributeValue("level");
                        if(att == null)
                        {
                            _log.log(Level.ERROR, "[EnhanceYourWeapon] Missing level in Crystal List itemId: " + itemId + ", skipping");
                            continue;
                        }
                        int level = Integer.parseInt(element1.getAttributeValue("level"));

                        att = element1.getAttributeValue("leveledItemId");
                        if(att == null)
                        {
                            _log.log(Level.ERROR, "[EnhanceYourWeapon] Missing leveledItemId in Crystal List itemId: " + itemId + ", skipping");
                            continue;
                        }
                        int leveledItemId = Integer.parseInt(element1.getAttributeValue("leveledItemId"));

                        _soulCrystals.put(itemId, new SoulCrystal(level, itemId, leveledItemId));
                    }
                }
            }
            else if(name.equalsIgnoreCase("npc"))
            {
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("item"))
                    {
                        String att = element1.getAttributeValue("npcId");
                        if(att == null)
                        {
                            _log.log(Level.ERROR, "[EnhanceYourWeapon] Missing npcId in NPC List, skipping");
                            continue;
                        }
                        int npcId = Integer.parseInt(att);

                        Map<Integer, SoulCrystalLevelingInfo> temp = new HashMap<>();

                        for(Element element2 : element1.getChildren())
                        {
                            boolean isSkillNeeded = false;
                            int chance = 5;
                            SoulCrystalAbsorbType soulAbsorbType = SoulCrystalAbsorbType.LAST_HIT;

                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("detail"))
                            {
                                att = element2.getAttributeValue("absorbType");
                                if(att != null)
                                {
                                    soulAbsorbType = Enum.valueOf(SoulCrystalAbsorbType.class, att);
                                }

                                att = element2.getAttributeValue("chance");
                                if(att != null)
                                {
                                    chance = Integer.parseInt(att);
                                }

                                att = element2.getAttributeValue("skill");
                                if(att != null)
                                {
                                    isSkillNeeded = Boolean.parseBoolean(att);
                                }

                                String att1 = element2.getAttributeValue("maxLevel");
                                String att2 = element2.getAttributeValue("levelList");
                                if(att1 == null && att2 == null)
                                {
                                    _log.log(Level.ERROR, "[EnhanceYourWeapon] Missing maxlevel/levelList in NPC List npcId: " + npcId + ", skipping");
                                    continue;
                                }
                                SoulCrystalLevelingInfo infoSoulCrystal = new SoulCrystalLevelingInfo(soulAbsorbType, isSkillNeeded, chance);
                                if(att1 != null)
                                {
                                    int maxLevel = Integer.parseInt(att1);
                                    for(int i = 0; i <= maxLevel; i++)
                                    {
                                        temp.put(i, infoSoulCrystal);
                                    }
                                }
                                else
                                {
                                    StringTokenizer st = new StringTokenizer(att2, ",");
                                    int tokenCount = st.countTokens();
                                    for(int i = 0; i < tokenCount; i++)
                                    {
                                        Integer value = Integer.decode(st.nextToken().trim());
                                        if(value == null)
                                        {
                                            _log.log(Level.ERROR, "[EnhanceYourWeapon] Bad Level value!! npcId: " + npcId + " token: " + i);
                                            value = 0;
                                        }
                                        temp.put(value, infoSoulCrystal);
                                    }
                                }
                            }
                        }

                        if(temp.isEmpty())
                        {
                            _log.log(Level.ERROR, "[EnhanceYourWeapon] No leveling info for npcId: " + npcId + ", skipping");
                            continue;
                        }
                        _npcLevelingInfos.put(npcId, temp);
                    }
                }
            }
        }
    }

    public Map<Integer, SoulCrystal> getSoulCrystal()
    {
        return _soulCrystals;
    }

    public Map<Integer, Map<Integer, SoulCrystalLevelingInfo>> getNpcLevelInfo()
    {
        return _npcLevelingInfos;
    }
}