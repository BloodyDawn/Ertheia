package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.datatables.sql.queries.Characters;
import dwo.gameserver.engine.databaseengine.FiltredPreparedStatement;
import dwo.gameserver.engine.databaseengine.L2DatabaseFactory;
import dwo.gameserver.engine.databaseengine.ThreadConnection;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Attackable;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.*;
import dwo.gameserver.model.items.CursedWeapon;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Broadcast;
import dwo.gameserver.util.database.DatabaseUtils;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CursedWeaponsManager extends XmlDocumentParser
{
    private static final Map<Integer, CursedWeapon> _cursedWeapons = new HashMap<>();

    private CursedWeaponsManager()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void announce(SystemMessage sm)
    {
        Broadcast.toAllOnlinePlayers(sm);
    }

    public static CursedWeaponsManager getInstance()
    {
        return SingletonHolder._instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _log.log(Level.INFO, "CursedWeaponsManager: Initializing...");
        _cursedWeapons.clear();
        if(!Config.ALLOW_CURSED_WEAPONS)
        {
            _log.log(Level.INFO, "CursedWeaponsManager: ALLOW_CURSED_WEAPONS = false. Loading terminated.");
            return;
        }
        parseFile(FilePath.CURSED_WEAPONS_MANAGER);
        restore();
        controlPlayers();
        _log.log(Level.INFO, "Loaded : " + _cursedWeapons.size() + " cursed weapon(s).");
    }

    @Override
    public void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("item"))
            {
                int id = Integer.parseInt(element.getAttributeValue("id"));
                int skillId = Integer.parseInt(element.getAttributeValue("skillId"));
                String name0 = element.getAttributeValue("name");

                CursedWeapon cw = new CursedWeapon(id, skillId, name0);
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("dropRate"))
                    {
                        cw.setDropRate(Double.parseDouble(element1.getAttributeValue("val")));
                    }
                    else if(name1.equalsIgnoreCase("duration"))
                    {
                        cw.setDuration(Integer.parseInt(element1.getAttributeValue("val")));
                    }
                    else if(name1.equalsIgnoreCase("durationLost"))
                    {
                        cw.setDurationLost(Integer.parseInt(element1.getAttributeValue("val")));
                    }
                    else if(name1.equalsIgnoreCase("disapearChance"))
                    {
                        cw.setDisapearChance(Integer.parseInt(element1.getAttributeValue("val")));
                    }
                    else if(name1.equalsIgnoreCase("stageKills"))
                    {
                        cw.setStageKills(Integer.parseInt(element1.getAttributeValue("val")));
                    }
                }
                _cursedWeapons.put(id, cw);
            }
        }
    }

    private void restore()
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet rset = null;
        try
        {
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection();

            statement = con.prepareStatement("SELECT itemId, charId, playerKarma, playerPkKills, nbKills, endTime FROM cursed_weapons");
            rset = statement.executeQuery();

            while(rset.next())
            {
                int itemId = rset.getInt("itemId");
                int playerId = rset.getInt("charId");
                int playerReputation = rset.getInt("playerKarma");
                int playerPkKills = rset.getInt("playerPkKills");
                int nbKills = rset.getInt("nbKills");
                long endTime = rset.getLong("endTime");

                CursedWeapon cw = _cursedWeapons.get(itemId);
                cw.setPlayerId(playerId);
                cw.setPlayerReputation(playerReputation);
                cw.setPlayerPkKills(playerPkKills);
                cw.setNbKills(nbKills);
                cw.setEndTime(endTime);
                cw.reActivate();
            }
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not restore CursedWeapons data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCSR(con, statement, rset);
        }
    }

    private void controlPlayers()
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        ResultSet rset = null;
        try
        {
            // Retrieve the L2PcInstance from the characters table of the database
            con = L2DatabaseFactory.getInstance().getConnection();

            // TODO: See comments below...
            // This entire for loop should NOT be necessary, since it is already handled by
            // CursedWeapon.endOfLife().  However, if we indeed *need* to duplicate it for safety,
            // then we'd better make sure that it FULLY cleans up inactive cursed weapons!
            // Undesired effects result otherwise, such as player with no zariche but with karma
            // or a lost-child entry in the cursedweapons table, without a corresponding one in items...
            for(CursedWeapon cw : _cursedWeapons.values())
            {
                if(cw.isActivated())
                {
                    continue;
                }

                // Do an item check to be sure that the cursed weapon isn't hold by someone
                int itemId = cw.getItemId();
                try
                {
                    statement = con.prepareStatement("SELECT owner_id FROM items WHERE item_id=?");
                    statement.setInt(1, itemId);
                    rset = statement.executeQuery();

                    if(rset.next())
                    {
                        // A player has the cursed weapon in his inventory ...
                        int playerId = rset.getInt("owner_id");
                        _log.log(Level.INFO, "PROBLEM : Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");

                        // Delete the item
                        DatabaseUtils.closeStatement(statement);
                        statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");
                        statement.setInt(1, playerId);
                        statement.setInt(2, itemId);
                        if(statement.executeUpdate() != 1)
                        {
                            _log.log(Level.WARN, "Error while deleting cursed weapon " + itemId + " from userId " + playerId);
                        }
                        DatabaseUtils.closeStatement(statement);

                        // Restore the player's old karma and pk count
                        statement = con.prepareStatement(Characters.UPDATE_CHAR_REPUTATION_PKKILLS);
                        statement.setInt(1, cw.getPlayerReputation());
                        statement.setInt(2, cw.getPlayerPkKills());
                        statement.setInt(3, playerId);
                        if(statement.executeUpdate() != 1)
                        {
                            _log.log(Level.WARN, "Error while updating reputation & pkkills for userId " + cw.getPlayerId());
                        }
                        // clean up the cursedweapons table.
                        removeFromDb(itemId);
                    }
                }
                catch(SQLException sqlE)
                {
                    _log.log(Level.ERROR, "", sqlE);
                }
                finally
                {
                    DatabaseUtils.closeDatabaseSR(statement, rset);
                }
            }
        }
        catch(Exception e)
        {
            _log.log(Level.ERROR, "Could not check CursedWeapons data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeConnection(con);
        }
    }

    public void checkDrop(L2Attackable attackable, L2PcInstance player)
    {
        synchronized(this)
        {
            if(attackable instanceof L2DefenderInstance || attackable instanceof L2RiftInvaderInstance || attackable instanceof L2GuardInstance || attackable instanceof L2GrandBossInstance || attackable instanceof L2FeedableBeastInstance || attackable instanceof L2FortCommanderInstance || attackable instanceof L2IncarnationInstance)
            {
                return;
            }

            for(CursedWeapon cw : _cursedWeapons.values())
            {
                if(cw.isActive())
                {
                    continue;
                }

                if(cw.checkDrop(attackable, player))
                {
                    break;
                }
            }
        }
    }

    public void activate(L2PcInstance player, L2ItemInstance item)
    {
        CursedWeapon cw = _cursedWeapons.get(item.getItemId());
        if(player.isCursedWeaponEquipped()) // cannot own 2 cursed swords
        {
            CursedWeapon cw2 = _cursedWeapons.get(player.getCursedWeaponEquippedId());
		    /* TODO: give the bonus level in a more appropriate manner.
                *  The following code adds "_stageKills" levels.  This will also show in the char status.
                * I do not have enough info to know if the bonus should be shown in the pk count, or if it
                * should be a full "_stageKills" bonus or just the remaining from the current count till the
                * of the current stage...
                * This code is a TEMP fix, so that the cursed weapon's bonus level can be observed with as
                * little change in the code as possible, until proper info arises.
                */
            cw2.setNbKills(cw2.getStageKills() - 1);
            cw2.increaseKills();

            // erase the newly obtained cursed weapon
            cw.setPlayer(player); // NECESSARY in order to find which inventory the weapon is in!
            cw.endOfLife(); // expire the weapon and clean up.
        }
        else
        {
            cw.activate(player, item);
        }
    }

    public void drop(int itemId, L2Character killer)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);

        cw.dropIt(killer);
    }

    public void increaseKills(int itemId)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);

        cw.increaseKills();
    }

    public int getLevel(int itemId)
    {
        CursedWeapon cw = _cursedWeapons.get(itemId);

        return cw.getLevel();
    }

    public void checkPlayer(L2PcInstance player)
    {
        if(player == null)
        {
            return;
        }

        _cursedWeapons.values().stream().filter(cw -> cw.isActivated() && player.getObjectId() == cw.getPlayerId()).forEach(cw -> {
            cw.setPlayer(player);
            cw.setItem(player.getInventory().getItemByItemId(cw.getItemId()));
            cw.giveSkill();
            player.setCursedWeaponEquippedId(cw.getItemId());
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1).addString(cw.getName()).addNumber((int) ((cw.getEndTime() - System.currentTimeMillis()) / 60000)));
        });
    }

    public int checkOwnsWeaponId(int ownerId)
    {
        for(CursedWeapon cw : _cursedWeapons.values())
        {
            if(cw.isActivated() && ownerId == cw.getPlayerId())
            {
                return cw.getItemId();
            }
        }
        return -1;
    }

    public void removeFromDb(int itemId)
    {
        ThreadConnection con = null;
        FiltredPreparedStatement statement = null;
        try
        {
            con = L2DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");
            statement.setInt(1, itemId);
            statement.executeUpdate();
        }
        catch(SQLException e)
        {
            _log.log(Level.ERROR, "CursedWeaponsManager: Failed to remove data: " + e.getMessage(), e);
        }
        finally
        {
            DatabaseUtils.closeDatabaseCS(con, statement);
        }
    }

    public void saveData()
    {
        for(CursedWeapon cw : _cursedWeapons.values())
        {
            cw.saveData();
        }
    }

    public boolean isCursed(int itemId)
    {
        return _cursedWeapons.containsKey(itemId);
    }

    public Collection<CursedWeapon> getCursedWeapons()
    {
        return _cursedWeapons.values();
    }

    public Set<Integer> getCursedWeaponsIds()
    {
        return _cursedWeapons.keySet();
    }

    public CursedWeapon getCursedWeapon(int itemId)
    {
        return _cursedWeapons.get(itemId);
    }

    public void givePassive(int itemId)
    {
        try
        {
            _cursedWeapons.get(itemId).giveSkill();
        }
        catch(Exception e)
        {
            // Ignored
        }
    }

    private static class SingletonHolder
    {
        protected static final CursedWeaponsManager _instance = new CursedWeaponsManager();
    }
}