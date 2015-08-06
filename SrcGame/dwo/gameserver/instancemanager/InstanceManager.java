package dwo.gameserver.instancemanager;

import dwo.config.FilePath;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2DoorTemplate;
import dwo.gameserver.model.world.Instance;
import dwo.gameserver.util.database.DatabaseUtils;
import javolution.util.FastList;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author evill33t, GodKratos
 */

public class InstanceManager extends XmlDocumentParser
{
    private static final FastMap<Integer, Instance> _instanceList = new FastMap<>();
    // InstanceId Names
    private static final Map<Integer, String> _instanceIdNames = new FastMap<>();
    private static final String ADD_INSTANCE_TIME = "INSERT INTO character_instance_time (charId,instanceId,time) values (?,?,?) ON DUPLICATE KEY UPDATE time=?";
    private static final String RESTORE_INSTANCE_TIMES = "SELECT instanceId,time FROM character_instance_time WHERE charId=?";
    private static final String DELETE_INSTANCE_TIME = "DELETE FROM character_instance_time WHERE charId=? AND instanceId=?";
    private final FastMap<Integer, InstanceWorld> _instanceWorlds = new FastMap<>();
    private final Map<Integer, Map<Integer, Long>> _playerInstanceTimes = new FastMap<>();
    private int _dynamic = 300000;

    private InstanceManager()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static InstanceManager getInstance()
    {
        return SingletonHolder._instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _instanceIdNames.clear();
        _instanceList.clear();
        parseFile(FilePath.INSTANCE_NAMES_DATA);
        _log.info(getClass().getSimpleName() + ": Loaded " + _instanceIdNames.size() + " instance names.");
        createWorld();
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("instance"))
            {
                _instanceIdNames.put(Integer.parseInt(element.getAttributeValue("id")), element.getAttributeValue("name"));
            }
        }
    }

    public long getInstanceTime(int playerObjId, int id)
    {
        if(!_playerInstanceTimes.containsKey(playerObjId))
        {
            restoreInstanceTimes(playerObjId);
        }
        if(_playerInstanceTimes.get(playerObjId).containsKey(id))
        {
            return _playerInstanceTimes.get(playerObjId).get(id);
        }
        return -1;
    }

    /**
     * @param playerObjId
     * @return
     */
    public Map<Integer, Long> getAllInstanceTimes(int playerObjId)
    {
        if(!_playerInstanceTimes.containsKey(playerObjId))
        {
            restoreInstanceTimes(playerObjId);
        }
        return _playerInstanceTimes.get(playerObjId);
    }

    /**
     * @param playerObjId
     * @param id
     * @param time
     */
    public void setInstanceTime(int playerObjId, int id, long time)
    {
        if(!_playerInstanceTimes.containsKey(playerObjId))
        {
            restoreInstanceTimes(playerObjId);
        }
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(ADD_INSTANCE_TIME);
            statement.setInt(1, playerObjId);
            statement.setInt(2, id);
            statement.setLong(3, time);
            statement.setLong(4, time);
            statement.execute();
            _playerInstanceTimes.get(playerObjId).put(id, time);

        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not insert character instance time data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }

    /**
     * @param playerObjId
     * @param id
     */
    public void deleteInstanceTime(int playerObjId, int id)
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(DELETE_INSTANCE_TIME);
            statement.setInt(1, playerObjId);
            statement.setInt(2, id);
            statement.execute();
            _playerInstanceTimes.get(playerObjId).remove(id);
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not delete character instance time data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }

    public void restoreInstanceTimes(int playerObjId)
    {
        if(_playerInstanceTimes.containsKey(playerObjId))
        {
            return; // already restored
        }

        _playerInstanceTimes.put(playerObjId, new FastMap<>());

        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet rset = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(RESTORE_INSTANCE_TIMES);
            statement.setInt(1, playerObjId);
            rset = statement.executeQuery();

            while(rset.next())
            {
                int id = rset.getInt("instanceId");
                long time = rset.getLong("time");
                if(time < System.currentTimeMillis())
                {
                    deleteInstanceTime(playerObjId, id);
                }
                else
                {
                    _playerInstanceTimes.get(playerObjId).put(id, time);
                }
            }
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not delete character instance time data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCSR(con, statement, rset);
        }
    }

    /**
     * @param id
     * @return
     */
    public String getInstanceIdName(int id)
    {
        if(_instanceIdNames.containsKey(id))
        {
            return _instanceIdNames.get(id);
        }
        return "Неизвестное измерение";
    }

    public void addWorld(InstanceWorld world)
    {
        _instanceWorlds.put(world.instanceId, world);
    }

    public InstanceWorld getWorld(int instanceId)
    {
        return _instanceWorlds.get(instanceId);
    }

    public InstanceWorld getPlayerWorld(L2PcInstance player)
    {
        for(InstanceWorld temp : _instanceWorlds.values())
        {
            if(temp == null)
            {
                continue;
            }
            // check if the player have a World Instance where he/she is allowed to enter
            if(temp.allowed.contains(player.getObjectId()))
            {
                return temp;
            }
        }
        return null;
    }

    /**
     * Возвращает инстанс игрока с заданным типом, либо null, если инстанс отсутствует, либо не подходит по типу.
     *
     * @param character Игрок или NPC в инстансе.
     * @param <T> Класс, унаследованный от InstanceWorld.
     * @return
     */
    public <T extends InstanceWorld> T getInstanceWorld(L2Character character, Class<T> type)
    {
        InstanceWorld world = null;
        try
        {
            world = type.cast(getWorld(character.getInstanceId()));
        }
        catch(Exception e)
        {
        }

        return world == null ? null : type.cast(world);
    }

    private void createWorld()
    {
        Instance themultiverse = new Instance(-1);
        themultiverse.setName("multiverse");
        _instanceList.put(-1, themultiverse);
        _log.log(Level.INFO, "Multiverse Instance created");

        Instance universe = new Instance(0);
        universe.setName("universe");
        _instanceList.put(0, universe);
        _log.log(Level.INFO, "Universe Instance created");
    }

    public void destroyInstance(int instanceId)
    {
        if(instanceId <= 0)
        {
            return;
        }
        Instance temp = _instanceList.get(instanceId);
        if(temp != null)
        {
            temp.removeNpcs();
            temp.removePlayers();
            temp.removeDoors();
            temp.cancelTimer();
            temp.cancelTasks();
            _instanceList.remove(instanceId);
            if(_instanceWorlds.containsKey(instanceId))
            {
                _instanceWorlds.remove(instanceId);
            }
        }
    }

    public Instance getInstance(int instanceid)
    {
        return _instanceList.get(instanceid);
    }

    public Map<Integer, Instance> getInstances()
    {
        return _instanceList;
    }

    public List<L2DoorInstance> getAllDoorRelatedInstancedDoors(L2DoorTemplate template)
    {
        List<L2DoorInstance> list = null;
        for(L2DoorTemplate tTemp : template.getDoorTemplatesInSameBlocks())
        {
            for(Instance temp : _instanceList.values())
            {
                if(temp == null)
                {
                    continue;
                }

                L2DoorInstance door = temp.getDoor(tTemp.getId());
                // check if the player is in any active instance
                if(door != null)
                {
                    if(list == null)
                    {
                        list = new ArrayList<>();
                    }

                    list.add(door);
                }
            }
        }
        return list;
    }

    public int getPlayerInstance(int objectId)
    {
        for(Instance temp : _instanceList.values())
        {
            if(temp == null)
            {
                continue;
            }
            // check if the player is in any active instance
            if(temp.containsPlayer(objectId))
            {
                return temp.getId();
            }
        }
        // 0 is default instance aka the world
        return 0;
    }

    public int createInstance()
    {
        _dynamic = 1;
        while(getInstance(_dynamic) != null)
        {
            _dynamic++;
            if(_dynamic == Integer.MAX_VALUE)
            {
                _log.log(Level.WARN, "InstanceManager: More then " + (Integer.MAX_VALUE - 300000) + " instances created");
                _dynamic = 300000;
            }
        }
        Instance instance = new Instance(_dynamic);
        _instanceList.put(_dynamic, instance);
        return _dynamic;
    }

    public boolean createInstance(int id)
    {
        if(getInstance(id) != null)
        {
            return false;
        }

        Instance instance = new Instance(id);
        _instanceList.put(id, instance);
        return true;
    }

    public boolean createInstanceFromTemplate(int id, String template)
    {
        if(getInstance(id) != null)
        {
            return false;
        }
        Instance instance = new Instance(id);
        _instanceList.put(id, instance);
        instance.loadInstanceTemplate(template);
        return true;
    }

    /**
     * Create a new instance with a dynamic instance id based on a template (or null)
     *
     * @param template xml file
     * @return
     */
    public int createDynamicInstance(String template)
    {
        while(getInstance(_dynamic) != null)
        {
            _dynamic++;
            if(_dynamic == Integer.MAX_VALUE)
            {
                _log.log(Level.WARN, "InstanceManager: More then " + (Integer.MAX_VALUE - 300000) + " instances created");
                _dynamic = 300000;
            }
        }
        Instance instance = new Instance(_dynamic);
        _instanceList.put(_dynamic, instance);
        if(template != null)
        {
            instance.loadInstanceTemplate(template);
        }
        return _dynamic;
    }

    /**
     * Checks whether instance of given {@code instanceId} exists.
     *
     * @param instanceId
     *            instance id
     * @return true if instance exists, otherwise false
     */
    public boolean instanceExist(int instanceId)
    {
        return _instanceList.get(instanceId) != null;
    }

    public static class InstanceWorld
    {
        public int instanceId;
        public int templateId = -1;
        public List<Integer> allowed = new FastList<>();
        public volatile int status;
    }

    private static class SingletonHolder
    {
        protected static final InstanceManager _instance = new InstanceManager();
    }
}