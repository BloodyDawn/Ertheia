package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.L2AccessLevel;
import dwo.gameserver.model.player.L2AdminCommandAccessRight;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.L2GameServerPacket;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AdminTable extends XmlDocumentParser
{
    private static final Map<Integer, L2AccessLevel> _accessLevels = new HashMap<>();
    private static final Map<String, L2AdminCommandAccessRight> _adminCommandAccessRights = new HashMap<>();
    private static final Map<L2PcInstance, Boolean> _gmList = new ConcurrentHashMap<>();

    protected static AdminTable _instance;

    private int _highestLevel;

    private AdminTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return AccessLevels: the one and only instance of this class<br>
     */
    public static AdminTable getInstance()
    {
        return _instance == null ? _instance = new AdminTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _accessLevels.clear();
        _adminCommandAccessRights.clear();
        parseFile(FilePath.ACCESS_LEVELS);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _accessLevels.size() + " Access Levels");
        parseFile(FilePath.ADMIN_COMMAND_ACCESS_RIGHTS);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded: " + _adminCommandAccessRights.size() + " Access Commands");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        L2AccessLevel level;
        L2AdminCommandAccessRight command;
        
        for(final Element element : rootElement.getChildren())
        {
            final String name = element.getName();            
            if(name.equalsIgnoreCase("access"))
            {
                final StatsSet set = new StatsSet();
                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                
                level = new L2AccessLevel(set);
                if(level.getLevel() > _highestLevel)
                {
                    _highestLevel = level.getLevel();
                }
                _accessLevels.put(level.getLevel(), level);
            }
            else if(name.equalsIgnoreCase("admin"))
            {
                final StatsSet set = new StatsSet();

                final List<Attribute> attributes = element.getAttributes();
                for(final Attribute attribute : attributes)
                {
                    set.set(attribute.getName(), attribute.getValue());
                }
                command = new L2AdminCommandAccessRight(set);
                _adminCommandAccessRights.put(command.getAdminCommand(), command);
            }
        }
    }

    /**
     * Returns the access level by characterAccessLevel<br>
     * <br>
     * @param accessLevelNum as int<br>
     * <br>
     * @return AccessLevel: AccessLevel instance by char access level<br>
     */
    public L2AccessLevel getAccessLevel(int accessLevelNum)
    {
        if(accessLevelNum < 0)
        {
            return _accessLevels.get(-1);
        }
        if(!_accessLevels.containsKey(accessLevelNum))
        {
            _accessLevels.put(accessLevelNum, new L2AccessLevel());
        }
        return _accessLevels.get(accessLevelNum);
    }

    public L2AccessLevel getMasterAccessLevel()
    {
        return _accessLevels.get(_highestLevel);
    }

    public boolean hasAccessLevel(int id)
    {
        return _accessLevels.containsKey(id);
    }

    public boolean hasAccess(String adminCommand, L2AccessLevel accessLevel)
    {
        L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(adminCommand);

        if(acar == null)
        {
            // Trying to avoid the spam for next time when the gm would try to use the same command
            if(accessLevel.getLevel() > 0 && accessLevel.getLevel() == _highestLevel)
            {
                acar = new L2AdminCommandAccessRight(adminCommand, true, accessLevel.getLevel());
                _adminCommandAccessRights.put(adminCommand, acar);
                _log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " auto setting accesslevel: " + accessLevel.getLevel() + " !");
            }
            else
            {
                _log.info("AdminCommandAccessRights: No rights defined for admin command " + adminCommand + " !");
                return false;
            }
        }

        return acar.hasAccess(accessLevel);
    }

    public boolean requireConfirm(String command)
    {
        L2AdminCommandAccessRight acar = _adminCommandAccessRights.get(command);
        if(acar == null)
        {
            _log.info("AdminCommandAccessRights: No rights defined for admin command " + command + '.');
            return false;
        }
        return acar.getRequireConfirm();
    }

    public List<L2PcInstance> getAllGms(boolean includeHidden)
    {
        return _gmList.entrySet().stream().filter(entry -> includeHidden || !entry.getValue()).map(Entry::getKey).collect(Collectors.toList());
    }

    public List<String> getAllGmNames(boolean includeHidden)
    {
        List<String> tmpGmList = new ArrayList<>();

        for(Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
        {
            if(!entry.getValue())
            {
                tmpGmList.add(entry.getKey().getName());
            }
            else if(includeHidden)
            {
                tmpGmList.add(entry.getKey().getName() + " (invis)");
            }
        }

        return tmpGmList;
    }

    /**
     * Add a L2PcInstance player to the Set _gmList
     * @param player
     * @param hidden
     */
    public void addGm(L2PcInstance player, boolean hidden)
    {
        _gmList.put(player, hidden);
    }

    public void deleteGm(L2PcInstance player)
    {
        _gmList.remove(player);
    }

    /**
     * GM will be displayed on clients gmlist
     * @param player
     */
    public void showGm(L2PcInstance player)
    {
        if(_gmList.containsKey(player))
        {
            _gmList.put(player, false);
        }
    }

    /**
     * GM will no longer be displayed on clients gmlist
     * @param player
     */
    public void hideGm(L2PcInstance player)
    {
        if(_gmList.containsKey(player))
        {
            _gmList.put(player, true);
        }
    }

    public boolean isGmOnline(boolean includeHidden)
    {
        for(Entry<L2PcInstance, Boolean> entry : _gmList.entrySet())
        {
            if(includeHidden || !entry.getValue())
            {
                return true;
            }
        }

        return false;
    }

    public void sendListToPlayer(L2PcInstance player)
    {
        if(isGmOnline(player.isGM()))
        {
            player.sendPacket(SystemMessageId.GM_LIST);

            for(String name : getAllGmNames(player.isGM()))
            {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.GM_C1).addString(name));
            }
        }
        else
        {
            player.sendPacket(SystemMessageId.NO_GM_PROVIDING_SERVICE_NOW);
        }
    }

    public void broadcastToGMs(L2GameServerPacket packet)
    {
        for(L2PcInstance gm : getInstance().getAllGms(true))
        {
            gm.sendPacket(packet);
        }
    }

    public void broadcastMessageToGMs(String message)
    {
        for(L2PcInstance gm : getInstance().getAllGms(true))
        {
            gm.sendMessage(message);
        }
    }
}
