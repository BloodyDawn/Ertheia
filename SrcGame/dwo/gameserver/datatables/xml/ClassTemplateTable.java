package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.templates.L2PcTemplate;
import dwo.gameserver.model.player.PlayerLvlUpData;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassInfo;
import dwo.gameserver.model.skills.stats.StatsSet;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * L2GOD Team
 * User: Yorie, ANZO
 * Date: 16.06.12
 * Time: 17:55
 */

public class ClassTemplateTable extends XmlDocumentParser
{
    // Информация о классе (клиентское\сереверное имя)
    private static final Map<ClassId, ClassInfo> _classInfoData = new HashMap<>();

    // Набор всех темлейтов игроков
    private static final Map<ClassId, L2PcTemplate> _classTemplates = new HashMap<>();

    protected static ClassTemplateTable _instance;

    private ClassTemplateTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ClassTemplateTable getInstance()
    {
        return _instance == null ? _instance = new ClassTemplateTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _classInfoData.clear();
        _classTemplates.clear();
        parseDirectory(FilePath.CLASS_TEMPLATES_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + _classInfoData.size() + " Class info data's.");
        _log.info(getClass().getSimpleName() + ": Loaded " + _classTemplates.size() + " Class templates.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            String className;
            String classServerName;
            int classId, classParentId = -1, classLevel, raceId,
                    str = 0, con = 0, dex = 0, _int = 0, wit = 0, men = 0,
                    pAtk = 0, pDef = 0, mAtk = 0, mDef = 0, pSpd = 0, mSpd = 0, pCritRate = 0, runSpd = 0;
            
            double collisionRM = 0.0, collisionHM = 0.0, collisionRF = 0.0, collisionHF = 0.0;
            
            Map<Integer, PlayerLvlUpData> baseHpMpCp = new HashMap<>();
            StatsSet set;
            final String name = element.getName();
            if(name.equalsIgnoreCase("classtemplate"))
            {
                className = element.getAttributeValue("name");
                classServerName = element.getAttributeValue("serverName");
                classId = Integer.parseInt(element.getAttributeValue("classId"));
                classLevel = Integer.parseInt(element.getAttributeValue("classLevel"));
                if(element.getAttributeValue("parentClassId") != null)
                {
                    classParentId = Integer.parseInt(element.getAttributeValue("parentClassId"));
                }
                raceId = Integer.parseInt(element.getAttributeValue("raceId"));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("basevalues"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            String atr;
                            final String name2 = element2.getName();
                            if(element2.getAttributes() != null)
                            {
                                 atr = element2.getAttributeValue("value");
                            }
                            else
                            {
                                continue;
                            }

                            switch(name2)
                            {
                                case "str":
                                    str = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "con":
                                    con = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "dex":
                                    dex = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "int":
                                    _int = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "wit":
                                    wit = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "men":
                                    men = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "pAtk":
                                    pAtk = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "mAtk":
                                    mAtk = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "pDef":
                                    pDef = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "mDef":
                                    mDef = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "pSpd":
                                    pSpd = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "mSpd":
                                    mSpd = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "pCritRate":
                                    pCritRate = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "runSpd":
                                    runSpd = Integer.parseInt(element2.getAttributeValue(atr));
                                    break;
                                case "collisionRM":
                                    collisionRM = Float.parseFloat(element2.getAttributeValue(atr));
                                    break;
                                case "collisionHM":
                                    collisionHM = Float.parseFloat(element2.getAttributeValue(atr));
                                    break;
                                case "collisionRF":
                                    collisionRF = Float.parseFloat(element2.getAttributeValue(atr));
                                    break;
                                case "collisionHF":
                                    collisionHF = Float.parseFloat(element2.getAttributeValue(atr));
                                    break;
                            }
                        }
                    }
                    else if(name1.equalsIgnoreCase("lvlupvalues"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("lvl_add"))
                            {
                                if(element2.getAttributes() != null)
                                {
                                    int level = Integer.parseInt(element2.getAttributeValue("lvl"));
                                    double hp = Float.parseFloat(element2.getAttributeValue("hp"));
                                    double mp = Float.parseFloat(element2.getAttributeValue("mp"));
                                    double cp = Float.parseFloat(element2.getAttributeValue("cp"));
                                    baseHpMpCp.put(level, new PlayerLvlUpData(hp, mp, cp));
                                }
                            }
                        }
                    }

                    // Ложим инфу о классах (имя серверное и клиентское)
                    _classInfoData.put(ClassId.getClassId(classId), new ClassInfo(ClassId.getClassId(classId), className, classServerName, classParentId >= 0 ? ClassId.getClassId(classParentId) : null));

                    // Генерим темплейты для игроков и ложим в меп
                    set = new StatsSet();
                    set.set("classId", classId);
                    set.set("className", classServerName);
                    set.set("raceId", raceId);
                    set.set("str", str);
                    set.set("con", con);
                    set.set("dex", dex);
                    set.set("int", _int);
                    set.set("wit", wit);
                    set.set("men", men);

                    set.set("org_hp_regen", 2);
                    set.set("org_mp_regen", 0.9);
                    set.set("base_physical_attack", pAtk);
                    set.set("base_defend", pDef);
                    set.set("base_magic_attack", mAtk);
                    set.set("base_magic_defend", mDef);
                    set.set("classBaseLevel", classLevel);
                    set.set("base_attack_speed", pSpd);
                    set.set("baseMAtkSpd", mSpd);
                    set.set("base_critical", pCritRate / 10);
                    set.set("baseMCritRate", 34); /* добавить еще один параметр базовый магКритРейт в chartemplate */
                    set.set("ground_low", runSpd);
                    set.set("ground_high", 0);
                    set.set("baseShldDef", 0);
                    set.set("baseShldRate", 0);
                    set.set("base_attack_range", 40);

                    set.set("spawnX", -114534);
                    set.set("spawnY", 260040);
                    set.set("spawnZ", -1200);

                    set.set("collision_radius", collisionRM);
                    set.set("collision_height", collisionHM);
                    set.set("collision_radius_female", collisionRF);
                    set.set("collision_height_female", collisionHF);

                    L2PcTemplate ct = new L2PcTemplate(CharTemplateTable.getInstance().getTemplate(ClassId.getClassId(classId)), set, baseHpMpCp);
                    _classTemplates.put(ClassId.getClassId(classId), ct);
                }
            }
        }
    }

    /**
     * @return полный лист классов с их информацией
     */
    public Map<ClassId, ClassInfo> getClassList()
    {
        return _classInfoData;
    }

    /**
     * @param classId ClassID класса
     * @return the class info related to the given {@code classId}.
     */
    public ClassInfo getClass(ClassId classId)
    {
        return _classInfoData.get(classId);
    }

    /**
     * @param classId the class Id as integer.
     * @return the class info related to the given {@code classId}.
     */
    public ClassInfo getClass(int classId)
    {
        ClassId id = ClassId.getClassId(classId);
        return id != null ? _classInfoData.get(id) : null;
    }

    /**
     * @param classServName the server side class name.
     * @return the class info related to the given {@code classServName}.
     */
    public ClassInfo getClass(String classServName)
    {
        for(ClassInfo classInfo : _classInfoData.values())
        {
            if(classInfo.getClassServName().equals(classServName))
            {
                return classInfo;
            }
        }
        return null;
    }

    public L2PcTemplate getTemplate(ClassId classId)
    {

        return _classTemplates.get(classId);
    }

    public L2PcTemplate getTemplate(int classId)
    {
        return _classTemplates.get(ClassId.getClassId(classId));
    }
}