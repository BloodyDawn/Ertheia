package  dwo.gameserver.engine.documentengine;

import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.type.L2ArmorType;
import dwo.gameserver.model.items.base.type.L2WeaponType;
import dwo.gameserver.model.player.base.PlayerState;
import dwo.gameserver.model.player.base.Race;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.base.conditions.*;
import dwo.gameserver.model.skills.base.conditions.ConditionGameTime.CheckGameTime;
import dwo.gameserver.model.skills.base.funcs.*;
import dwo.gameserver.model.skills.effects.EffectTemplate;
import dwo.gameserver.model.skills.stats.Env;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.zone.TargetPosition;
import dwo.gameserver.util.crypt.datapack.CryptUtil;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author mkizub
 */

public abstract class XmlDocumentBase
{
    public static final Logger _log = LogManager.getLogger(XmlDocumentBase.class);
    private static final boolean debagMul = false;
    private final File _file;
    protected Map<String, String[]> _tables;

    protected XmlDocumentBase(File pFile)
    {
        _file = pFile;
        _tables = new FastMap<>();
    }

    public Document parse()
    {
        Document doc;
        try
        {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            doc = factory.newDocumentBuilder().parse(CryptUtil.decryptOnDemand(_file), _file.getAbsolutePath());
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Error loading file " + _file + " current id: " + getCurrentId(), e);
            return null;
        }
        try
        {
            parseDocument(doc);
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Error in file " + _file + " CurrentId: " + getCurrentId(), e);
            return null;
        }
        return doc;
    }

    protected abstract int getCurrentId();

    protected abstract void parseDocument(Document doc);

    protected abstract StatsSet getStatsSet();

    protected abstract String getTableValue(String name);

    protected abstract String getTableValue(String name, int idx);

    protected void resetTable()
    {
        _tables = new FastMap<>();
    }

    protected void setTable(String name, String[] table)
    {
        _tables.put(name, table);
    }

    protected void parseTemplate(Node n, Object template)
    {
        Condition condition = null;
        n = n.getFirstChild();
        if(n == null)
        {
            return;
        }

        if("cond".equalsIgnoreCase(n.getNodeName()))
        {
            condition = parseCondition(n.getFirstChild(), template);
            Node msg = n.getAttributes().getNamedItem("msg");
            Node msgId = n.getAttributes().getNamedItem("msgId");
            if(condition != null && msg != null)
            {
                condition.setMessage(msg.getNodeValue());
            }
            else if(condition != null && msgId != null)
            {
                condition.setMessageId(Integer.decode(getValue(msgId.getNodeValue(), null)));
                Node addName = n.getAttributes().getNamedItem("addName");
                if(addName != null && Integer.decode(getValue(msgId.getNodeValue(), null)) > 0)
                {
                    condition.addName();
                }
            }
            n = n.getNextSibling();
        }

        for(; n != null; n = n.getNextSibling())
        {
            if("add".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Add", condition);
            }
            else if("sub".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Sub", condition);
            }
            else if("mul".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Mul", condition);
            }
            else if("div".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Div", condition);
            }
            else if("set".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Set", condition);
            }
            else if("enchant".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "Enchant", condition);
            }
            else if("enchanthp".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantHp", condition);
            }
            else if("enchantrunspd".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantRunSpd", condition);
            }
            else if("enchantaccevas".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantAccEvas", condition);
            }
            else if("enchantmatk".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantMAtk", condition);
            }
            else if("enchantpatk".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantPAtk", condition);
            }
            else if("enchantpmcritatk".equalsIgnoreCase(n.getNodeName()))
            {
                attachFunc(n, template, "EnchantPMcritAtk", condition);
            }
            else if("effect".equalsIgnoreCase(n.getNodeName()))
            {
                if(template instanceof EffectTemplate)
                {
                    throw new RuntimeException("Nested effects");
                }
                attachEffect(n, template, condition);
            }
        }
    }

    protected void attachFunc(Node n, Object template, String name, Condition attachCond)
    {
        Stats stat = Stats.valueOfXml(n.getAttributes().getNamedItem("stat").getNodeValue());
        String order = n.getAttributes().getNamedItem("order").getNodeValue();
        Lambda lambda = getLambda(n, template);
        int ord = Integer.decode(getValue(order, template));
        Condition applayCond = parseCondition(n.getFirstChild(), template);
        FuncTemplate ft = new FuncTemplate(attachCond, applayCond, name, stat, ord, lambda);
        if(template instanceof L2Item)
        {
            ((L2Item) template).attach(ft);
        }
        else if(template instanceof L2Skill)
        {
            ((L2Skill) template).attach(ft);
        }
        else if(template instanceof EffectTemplate)
        {
            ((EffectTemplate) template).attach(ft);
        }
    }

    protected void attachLambdaFunc(Node n, Object template, LambdaCalc calc)
    {
        String name = n.getNodeName();
        StringBuilder sb = new StringBuilder(name);
        sb.setCharAt(0, Character.toUpperCase(name.charAt(0)));
        name = sb.toString();
        Lambda lambda = getLambda(n, template);
        FuncTemplate ft = new FuncTemplate(null, null, name, null, calc.funcs.length, lambda);
        calc.addFunc(ft.getFunc(new Env(), calc));
    }

    protected void attachEffect(Node n, Object template, Condition attachCond)
    {
        NamedNodeMap attrs = n.getAttributes();
        StatsSet set = new StatsSet();
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node att = attrs.item(i);
            set.set(att.getNodeName(), getValue(att.getNodeValue(), template));
        }
        StatsSet parameters = parseParameters(n.getFirstChild(), template);
        Lambda lambda = getLambda(n, template);
        Condition applayCond = parseCondition(n.getFirstChild(), template);
        EffectTemplate effectTemplate = new EffectTemplate(attachCond, applayCond, lambda, set, parameters);
        parseTemplate(n, effectTemplate);
        if(template instanceof L2Item)
        {
            ((L2Item) template).attach(effectTemplate);
        }
        else if(template instanceof L2Skill)
        {
            L2Skill sk = (L2Skill) template;
            if(set.getInteger("self", 0) == 1)
            {
                sk.attachSelf(effectTemplate);
            }
            else if(sk.isPassive())
            {
                sk.attachPassive(effectTemplate);
            }
            else
            {
                sk.attach(effectTemplate);
            }
        }
    }

    protected Condition parseCondition(Node n, Object template)
    {
        while(n != null && n.getNodeType() != Node.ELEMENT_NODE)
        {
            n = n.getNextSibling();
        }
        if(n == null)
        {
            return null;
        }
        if("and".equalsIgnoreCase(n.getNodeName()))
        {
            return parseLogicAnd(n, template);
        }
        if("or".equalsIgnoreCase(n.getNodeName()))
        {
            return parseLogicOr(n, template);
        }
        if("not".equalsIgnoreCase(n.getNodeName()))
        {
            return parseLogicNot(n, template);
        }
        if("player".equalsIgnoreCase(n.getNodeName()))
        {
            return parsePlayerCondition(n, template);
        }
        if("target".equalsIgnoreCase(n.getNodeName()))
        {
            return parseTargetCondition(n, template);
        }
        if("clan".equalsIgnoreCase(n.getNodeName()))
        {
            return parseClanCondition(n, template);
        }
        if("skill".equalsIgnoreCase(n.getNodeName()))
        {
            return parseSkillCondition(n);
        }
        if("using".equalsIgnoreCase(n.getNodeName()))
        {
            return parseUsingCondition(n);
        }
        if("game".equalsIgnoreCase(n.getNodeName()))
        {
            return parseGameCondition(n);
        }
        return null;
    }

    protected Condition parseLogicAnd(Node n, Object template)
    {
        ConditionLogicAnd cond = new ConditionLogicAnd();
        for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if(n.getNodeType() == Node.ELEMENT_NODE)
            {
                cond.add(parseCondition(n, template));
            }
        }
        if(cond.conditions == null || cond.conditions.length == 0)
        {
            _log.log(Level.ERROR, "Empty <and> condition in " + _file);
        }
        return cond;
    }

    protected Condition parseLogicOr(Node n, Object template)
    {
        ConditionLogicOr cond = new ConditionLogicOr();
        for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if(n.getNodeType() == Node.ELEMENT_NODE)
            {
                cond.add(parseCondition(n, template));
            }
        }
        if(cond.conditions == null || cond.conditions.length == 0)
        {
            _log.log(Level.ERROR, "Empty <or> condition in " + _file);
        }
        return cond;
    }

    protected Condition parseLogicNot(Node n, Object template)
    {
        for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if(n.getNodeType() == Node.ELEMENT_NODE)
            {
                return new ConditionLogicNot(parseCondition(n, template));
            }
        }
        _log.log(Level.ERROR, "Empty <not> condition in " + _file);
        return null;
    }

    protected Condition parsePlayerCondition(Node n, Object template)
    {
        Condition cond = null;
        byte[] forces = new byte[2];
        NamedNodeMap attrs = n.getAttributes();
        String temp = "";
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node a = attrs.item(i);
            temp = a.getNodeName();
            if("races".equalsIgnoreCase(a.getNodeName()))
            {
                String[] racesVal = a.getNodeValue().split(",");
                Race[] races = new Race[racesVal.length];
                for(int r = 0; r < racesVal.length; r++)
                {
                    if(racesVal[r] != null)
                    {
                        races[r] = Race.valueOf(racesVal[r]);
                    }
                }
                cond = joinAnd(cond, new ConditionPlayerRace(races));
            }
            else if("level".equalsIgnoreCase(a.getNodeName()))
            {
                int lvl = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerLevel(lvl));
            }
            else if("dualClassLevel".equalsIgnoreCase(a.getNodeName()))
            {
                int lvl = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerDualClassLevel(lvl));
            }
            else if("levelRange".equalsIgnoreCase(a.getNodeName()))
            {
                String[] range = getValue(a.getNodeValue(), template).split(";");
                if(range.length == 2)
                {
                    int[] lvlRange = new int[2];
                    lvlRange[0] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
                    lvlRange[1] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
                    cond = joinAnd(cond, new ConditionPlayerLevelRange(lvlRange));
                }
            }
            else if("resting".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RESTING, val));
            }
            else if("flying".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FLYING, val));
            }
            else if("moving".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.MOVING, val));
            }
            else if("running".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.RUNNING, val));
            }
            else if("standing".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.STANDING, val));
            }
            else if("fighting".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FIGHTING, val));
            }
            else if("behind".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.BEHIND, val));
            }
            else if("front".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.FRONT, val));
            }
            else if("chaotic".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.CHAOTIC, val));
            }
            else if("olympiad".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerState(PlayerState.OLYMPIAD, val));
            }
            else if("ishero".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerIsHero(val));
            }
            else if("transformationId".equalsIgnoreCase(a.getNodeName()))
            {
                int id = Integer.parseInt(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerTransformationId(id));
            }
            else if("hp".equalsIgnoreCase(a.getNodeName()))
            {
                int hp = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerHp(hp));
            }
            else if("mp".equalsIgnoreCase(a.getNodeName()))
            {
                int hp = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerMp(hp));
            }
            else if("cp".equalsIgnoreCase(a.getNodeName()))
            {
                int cp = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerCp(cp));
            }
            else if("grade".equalsIgnoreCase(a.getNodeName()))
            {
                int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerGrade(expIndex));
            }
            else if("pkCount".equalsIgnoreCase(a.getNodeName()))
            {
                int expIndex = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerPkCount(expIndex));
            }
            else if("siegezone".equalsIgnoreCase(a.getNodeName()))
            {
                int value = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionSiegeZone(value, true));
            }
            else if("siegeside".equalsIgnoreCase(a.getNodeName()))
            {
                int value = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerSiegeSide(value));
            }
            else if("battle_force".equalsIgnoreCase(a.getNodeName()))
            {
                forces[0] = Byte.decode(getValue(a.getNodeValue(), null));
            }
            else if("spell_force".equalsIgnoreCase(a.getNodeName()))
            {
                forces[1] = Byte.decode(getValue(a.getNodeValue(), null));
            }
            else if("charges".equalsIgnoreCase(a.getNodeName()))
            {
                int value = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerCharges(value));
            }
            else if("souls".equalsIgnoreCase(a.getNodeName()))
            {
                int value = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerSouls(value));
            }
            else if("summonExists".equalsIgnoreCase(a.getNodeName()))
            {
                boolean value = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerSummonExists(value));
            }
            else if("weight".equalsIgnoreCase(a.getNodeName()))
            {
                int weight = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerWeight(weight));
            }
            else if("invSize".equalsIgnoreCase(a.getNodeName()))
            {
                int size = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerInvSize(size));
            }
            else if("isClanLeader".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerIsClanLeader(val));
            }
            else if("pledgeClass".equalsIgnoreCase(a.getNodeName()))
            {
                int pledgeClass = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerPledgeClass(pledgeClass));
            }
            else if("clanHall".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerHasClanHall(array));
            }
            else if("fort".equalsIgnoreCase(a.getNodeName()))
            {
                int fort = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerHasFort(fort));
            }
            else if("castle".equalsIgnoreCase(a.getNodeName()))
            {
                int castle = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerHasCastle(castle));
            }
            else if("sex".equalsIgnoreCase(a.getNodeName()))
            {
                int sex = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionPlayerSex(sex));
            }
            else if("flyMounted".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerFlyMounted(val));
            }
            else if("vehicleMounted".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerVehicleMounted(val));
            }
            else if("landingZone".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerLandingZone(val));
            }
            else if("isInSiege".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerIsInSiege(val));
            }
            else if("active_effect_id".equalsIgnoreCase(a.getNodeName()))
            {
                int effect_id = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id));
            }
            else if("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName()))
            {
                String val = getValue(a.getNodeValue(), template);
                int effect_id = Integer.decode(getValue(val.split(",")[0], template));
                int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
                cond = joinAnd(cond, new ConditionPlayerActiveEffectId(effect_id, effect_lvl));
            }
            else if("active_skill_id".equalsIgnoreCase(a.getNodeName()))
            {
                int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id));
            }
            else if("item_equipped".equalsIgnoreCase(a.getNodeName()))
            {
                List<Integer> itemIds = new FastList<>(1);
                if(a.getNodeValue().indexOf(';') < 0)
                {
                    Integer.parseInt(getValue(a.getNodeValue(), template));
                }
                else
                {
                    String[] ids = a.getNodeValue().split(";");
                    for(String id : ids)
                    {
                        itemIds.add(Integer.parseInt(id));
                    }
                }
                cond = joinAnd(cond, new ConditionPlayerItemEquipped(itemIds));
            }
            else if("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName()))
            {
                String val = getValue(a.getNodeValue(), template);
                int skill_id = Integer.decode(getValue(val.split(",")[0], template));
                int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
                cond = joinAnd(cond, new ConditionPlayerActiveSkillId(skill_id, skill_lvl));
            }
            else if("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerClassIdRestriction(array));
            }
            else if("class_level".equalsIgnoreCase(a.getNodeName()))
            {
                int val = Integer.parseInt(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerClassLevel(val));
            }
            else if("subclass".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerSubclass(val));
            }
            else if("awakened".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerAwakened(val));
            }
            else if("instanceId".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerInstanceId(array));
            }
            else if("agathionId".equalsIgnoreCase(a.getNodeName()))
            {
                int agathionId = Integer.decode(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerAgathionId(agathionId));
            }
            else if("cloakStatus".equalsIgnoreCase(a.getNodeName()))
            {
                int val = Integer.parseInt(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerCloakStatus(val));
            }
            else if("hasPet".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerHasPet(array));
            }
            else if("servitorNpcId".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerServitorNpcId(array));
            }
            else if("npcIdRadius".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                int npcId = 0;
                int radius = 0;
                if(st.countTokens() > 1)
                {
                    npcId = Integer.decode(getValue(st.nextToken().trim(), null));
                    radius = Integer.decode(getValue(st.nextToken().trim(), null));
                }
                cond = joinAnd(cond, new ConditionPlayerRangeFromNpc(npcId, radius));
            }
            else if("canSweep".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionPlayerCanSweep(Boolean.parseBoolean(a.getNodeValue())));
            }
            else if("insideZoneId".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionPlayerInsideZoneId(array));
            }
            else if("summonPoints".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionPlayerSummonPoints(Integer.valueOf(a.getNodeValue())));
            }
            else if("canTransform".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionPlayerCanTransform(val));
            }
            else if("reputation".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionPlayerReputation(Integer.valueOf(a.getNodeValue())));
            }
            else if("fame".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionPlayerFame(Integer.valueOf(a.getNodeValue())));
            }
            else if("onChaosFestival".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionPlayerChaosFestival(Boolean.valueOf(a.getNodeValue())));
            }
        }

        if(forces[0] + forces[1] > 0)
        {
            cond = joinAnd(cond, new ConditionForceBuff(forces));
        }

        if(cond == null)
        {
            _log.log(Level.ERROR, "Unrecognized <player> condition: " + temp + " in " + _file);
        }
        return cond;
    }

    protected Condition parseTargetCondition(Node n, Object template)
    {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node a = attrs.item(i);
            if("aggro".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionTargetAggro(val));
            }
            else if("siegezone".equalsIgnoreCase(a.getNodeName()))
            {
                int value = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionSiegeZone(value, false));
            }
            else if("level".equalsIgnoreCase(a.getNodeName()))
            {
                int lvl = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionTargetLevel(lvl));
            }
            else if("levelRange".equalsIgnoreCase(a.getNodeName()))
            {
                String[] range = getValue(a.getNodeValue(), template).split(";");
                if(range.length == 2)
                {
                    int[] lvlRange = new int[2];
                    lvlRange[0] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[0]);
                    lvlRange[1] = Integer.decode(getValue(a.getNodeValue(), template).split(";")[1]);
                    cond = joinAnd(cond, new ConditionTargetLevelRange(lvlRange));
                }
            }
            else if("playable".equalsIgnoreCase(a.getNodeName()))
            {
                cond = joinAnd(cond, new ConditionTargetPlayable());
            }
            else if("class_id_restriction".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionTargetClassIdRestriction(array));
            }
            else if("active_effect_id".equalsIgnoreCase(a.getNodeName()))
            {
                int effect_id = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id));
            }
            else if("active_effect_id_lvl".equalsIgnoreCase(a.getNodeName()))
            {
                String val = getValue(a.getNodeValue(), template);
                int effect_id = Integer.decode(getValue(val.split(",")[0], template));
                int effect_lvl = Integer.decode(getValue(val.split(",")[1], template));
                cond = joinAnd(cond, new ConditionTargetActiveEffectId(effect_id, effect_lvl));
            }
            else if("active_skill_id".equalsIgnoreCase(a.getNodeName()))
            {
                int skill_id = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id));
            }
            else if("active_skill_id_lvl".equalsIgnoreCase(a.getNodeName()))
            {
                String val = getValue(a.getNodeValue(), template);
                int skill_id = Integer.decode(getValue(val.split(",")[0], template));
                int skill_lvl = Integer.decode(getValue(val.split(",")[1], template));
                cond = joinAnd(cond, new ConditionTargetActiveSkillId(skill_id, skill_lvl));
            }
            else if("abnormal".equalsIgnoreCase(a.getNodeName()))
            {
                int abnormalId = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionTargetAbnormal(abnormalId));
            }
            else if("mindistance".equalsIgnoreCase(a.getNodeName()))
            {
                int distance = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionMinDistance(distance * distance));
            }
            // used for npc race
            else if("race_id".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionTargetRaceId(array));
            }
            // used for pc race
            else if("races".equalsIgnoreCase(a.getNodeName()))
            {
                String[] racesVal = a.getNodeValue().split(",");
                Race[] races = new Race[racesVal.length];
                for(int r = 0; r < racesVal.length; r++)
                {
                    if(racesVal[r] != null)
                    {
                        races[r] = Race.valueOf(racesVal[r]);
                    }
                }
                cond = joinAnd(cond, new ConditionTargetRace(races));
            }
            else if("using".equalsIgnoreCase(a.getNodeName()))
            {
                int mask = 0;
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    for(L2WeaponType wt : L2WeaponType.values())
                    {
                        if(wt.toString().equals(item))
                        {
                            mask |= wt.mask();
                            break;
                        }
                    }
                    for(L2ArmorType at : L2ArmorType.values())
                    {
                        if(at.toString().equals(item))
                        {
                            mask |= at.mask();
                            break;
                        }
                    }
                }
                cond = joinAnd(cond, new ConditionTargetUsesWeaponKind(mask));
            }
            else if("npcId".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                ArrayList<Integer> array = new ArrayList<>(st.countTokens());
                while(st.hasMoreTokens())
                {
                    String item = st.nextToken().trim();
                    array.add(Integer.decode(getValue(item, null)));
                }
                cond = joinAnd(cond, new ConditionTargetNpcId(array));
            }
            else if("npcType".equalsIgnoreCase(a.getNodeName()))
            {
                String[] values = getValue(a.getNodeValue(), template).trim().split(",");

                List<Class<? extends L2Object>> types = new FastList<>();
                Class<? extends L2Object> type;

                for(String aValuesSplit : values)
                {
                    type = L2Object.getGameObjectTypeByName(aValuesSplit);

                    if(type == null)
                    {
                        throw new IllegalArgumentException("Instance type not recognized: " + aValuesSplit);
                    }

                    types.add(type);
                }

                cond = joinAnd(cond, new ConditionTargetNpcType(types));
            }
            else if("hp".equalsIgnoreCase(a.getNodeName()))
            {
                int hp = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionTargetHp(hp));
            }
            else if("weight".equalsIgnoreCase(a.getNodeName()))
            {
                int weight = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionTargetWeight(weight));
            }
            else if("invSize".equalsIgnoreCase(a.getNodeName()))
            {
                int size = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionTargetInvSize(size));
            }
            else if("position".equalsIgnoreCase(a.getNodeName()))
            {
                TargetPosition position = TargetPosition.valueOf(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionTargetPosition(position));
            }
            else if("canTransform".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.valueOf(a.getNodeValue());
                cond = joinAnd(cond, new ConditionTargetCanTransform(val));
            }
        }
        if(cond == null)
        {
            _log.log(Level.ERROR, "Unrecognized <target> condition in " + _file);
        }
        return cond;
    }

    protected Condition parseClanCondition(Node n, Object template)
    {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();
        String temp = "";
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node a = attrs.item(i);
            temp = a.getNodeName();
            if("fame".equalsIgnoreCase(a.getNodeName()))
            {
                int fame = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionClanFame(fame));
            }
            else if("level".equalsIgnoreCase(a.getNodeName()))
            {
                int level = Integer.decode(getValue(a.getNodeValue(), template));
                cond = joinAnd(cond, new ConditionClanLevel(level));
            }
        }

        if(cond == null)
        {
            _log.log(Level.ERROR, "Unrecognized <clan> condition: " + temp + " in " + _file);
        }
        return cond;
    }

    protected Condition parseSkillCondition(Node n)
    {
        NamedNodeMap attrs = n.getAttributes();
        Stats stat = Stats.valueOfXml(attrs.getNamedItem("stat").getNodeValue());
        return new ConditionSkillStats(stat);
    }

    protected Condition parseUsingCondition(Node n)
    {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node a = attrs.item(i);
            if("kind".equalsIgnoreCase(a.getNodeName()))
            {
                int mask = 0;
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ",");
                while(st.hasMoreTokens())
                {
                    int old = mask;
                    String item = st.nextToken().trim();
                    if(ItemTable._weaponTypes.containsKey(item))
                    {
                        mask |= ItemTable._weaponTypes.get(item).mask();
                    }

                    if(ItemTable._armorTypes.containsKey(item))
                    {
                        mask |= ItemTable._armorTypes.get(item).mask();
                    }

                    if(old == mask)
                    {
                        _log.log(Level.INFO, "[parseUsingCondition=\"kind\"] Unknown item type name: " + item);
                    }
                }
                cond = joinAnd(cond, new ConditionUsingItemType(mask));
            }
            else if("skill".equalsIgnoreCase(a.getNodeName()))
            {
                int id = Integer.parseInt(a.getNodeValue());
                cond = joinAnd(cond, new ConditionUsingSkill(id));
            }
            else if("slotitem".equalsIgnoreCase(a.getNodeName()))
            {
                StringTokenizer st = new StringTokenizer(a.getNodeValue(), ";");
                int id = Integer.parseInt(st.nextToken().trim());
                int slot = Integer.parseInt(st.nextToken().trim());
                int enchant = 0;
                if(st.hasMoreTokens())
                {
                    enchant = Integer.parseInt(st.nextToken().trim());
                }
                cond = joinAnd(cond, new ConditionSlotItemId(slot, id, enchant));
            }
            else if("weaponChange".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionChangeWeapon(val));
            }
        }
        if(cond == null)
        {
            _log.log(Level.ERROR, "Unrecognized <using> condition in " + _file);
        }
        return cond;
    }

    protected Condition parseGameCondition(Node n)
    {
        Condition cond = null;
        NamedNodeMap attrs = n.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++)
        {
            Node a = attrs.item(i);
            if("skill".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionWithSkill(val));
            }
            if("night".equalsIgnoreCase(a.getNodeName()))
            {
                boolean val = Boolean.parseBoolean(a.getNodeValue());
                cond = joinAnd(cond, new ConditionGameTime(CheckGameTime.NIGHT, val));
            }
            if("chance".equalsIgnoreCase(a.getNodeName()))
            {
                int val = Integer.decode(getValue(a.getNodeValue(), null));
                cond = joinAnd(cond, new ConditionGameChance(val));
            }
        }
        if(cond == null)
        {
            _log.log(Level.ERROR, "Unrecognized <game> condition in " + _file);
        }
        return cond;
    }

    protected void parseTable(Node n)
    {
        NamedNodeMap attrs = n.getAttributes();
        String name = attrs.getNamedItem("name").getNodeValue();
        if(name.charAt(0) != '#')
        {
            throw new IllegalArgumentException("Table name must start with #");
        }
        StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
        List<String> array = new ArrayList<>(data.countTokens());
        while(data.hasMoreTokens())
        {
            array.add(data.nextToken());
        }
        setTable(name, array.toArray(new String[array.size()]));
    }

    protected void parseBeanSet(Node n, StatsSet set, Integer level)
    {
        String name = n.getAttributes().getNamedItem("name").getNodeValue().trim();
        String value = n.getAttributes().getNamedItem("val").getNodeValue().trim();
        char ch = value.isEmpty() ? ' ' : value.charAt(0);
        if(ch == '#' || ch == '-' || Character.isDigit(ch))
        {
            set.set(name, String.valueOf(getValue(value, level)));
        }
        else
        {
            set.set(name, value);
        }
    }

    protected void setExtractableSkillData(StatsSet set, String value)
    {
        set.set("capsuled_items_skill", value);
    }

    protected Lambda getLambda(Node n, Object template)
    {
        Node nval = n.getAttributes().getNamedItem("val");
        if(nval != null)
        {
            String val = nval.getNodeValue();
            if(val.charAt(0) == '#')
            {
                if(debagMul)
                {
                    double value = Double.parseDouble(getTableValue(val));
                    if(n.getNodeName().equalsIgnoreCase("mul") && value <= 0.0)
                    {
                        _log.warn("MUL operation with zero val in skillid: " + getCurrentId());
                    }
                }
                return new LambdaConst(Double.parseDouble(getTableValue(val)));
            }
            else if(val.charAt(0) == '$')
            {
                if(val.equalsIgnoreCase("$player_level"))
                {
                    return new LambdaStats(LambdaStats.StatsType.PLAYER_LEVEL);
                }
                if(val.equalsIgnoreCase("$target_level"))
                {
                    return new LambdaStats(LambdaStats.StatsType.TARGET_LEVEL);
                }
                if(val.equalsIgnoreCase("$player_max_hp"))
                {
                    return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_HP);
                }
                if(val.equalsIgnoreCase("$player_max_mp"))
                {
                    return new LambdaStats(LambdaStats.StatsType.PLAYER_MAX_MP);
                }
                // try to find value out of item fields
                StatsSet set = getStatsSet();
                String field = set.getString(val.substring(1));
                if(field != null)
                {
                    return new LambdaConst(Double.parseDouble(getValue(field, template)));
                }
                // failed
                throw new IllegalArgumentException("Unknown value " + val);
            }
            else
            {
                if(debagMul)
                {
                    double value = Double.parseDouble(val);
                    if(n.getNodeName().equalsIgnoreCase("mul") && value <= 0.0)
                    {
                        _log.warn("MUL operation with zero val in skillid: " + getCurrentId());
                    }
                }

                return new LambdaConst(Double.parseDouble(val));
            }
        }
        LambdaCalc calc = new LambdaCalc();
        n = n.getFirstChild();
        while(n != null && n.getNodeType() != Node.ELEMENT_NODE)
        {
            n = n.getNextSibling();
        }
        if(n == null || !"val".equals(n.getNodeName()))
        {
            throw new IllegalArgumentException("Value not specified");
        }

        for(n = n.getFirstChild(); n != null; n = n.getNextSibling())
        {
            if(n.getNodeType() != Node.ELEMENT_NODE)
            {
                continue;
            }
            attachLambdaFunc(n, template, calc);
        }

        return calc;
    }

    protected String getValue(String value, Object template)
    {
        // is it a table?
        if(value.charAt(0) == '#')
        {
            if(template instanceof L2Skill)
            {
                return getTableValue(value);
            }
            else if(template instanceof Integer)
            {
                return getTableValue(value, (Integer) template);
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        return value;
    }

    protected Condition joinAnd(Condition cond, Condition c)
    {
        if(cond == null)
        {
            return c;
        }
        if(cond instanceof ConditionLogicAnd)
        {
            ((ConditionLogicAnd) cond).add(c);
            return cond;
        }
        ConditionLogicAnd and = new ConditionLogicAnd();
        and.add(cond);
        and.add(c);
        return and;
    }

    /**
     * Parse effect's parameters.
     * @param n the node to start the parsing
     * @param template the effect template
     * @return the list of parameters if any, {@code null} otherwise
     */
    private StatsSet parseParameters(Node n, Object template)
    {
        StatsSet parameters = null;
        while(n != null)
        {
            // Parse all parameters.
            if(n.getNodeType() == Node.ELEMENT_NODE && "param".equals(n.getNodeName()))
            {
                if(parameters == null)
                {
                    parameters = new StatsSet();
                }
                NamedNodeMap params = n.getAttributes();
                for(int i = 0; i < params.getLength(); i++)
                {
                    Node att = params.item(i);
                    parameters.set(att.getNodeName(), getValue(att.getNodeValue(), template));
                }
            }
            n = n.getNextSibling();
        }
        return parameters;
    }
}