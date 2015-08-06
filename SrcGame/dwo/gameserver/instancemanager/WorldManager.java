package dwo.gameserver.instancemanager;

import dwo.config.Config;
import dwo.gameserver.datatables.sql.CharNameTable;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.L2Playable;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.instance.L2PetInstance;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.util.StringUtil;
import dwo.gameserver.util.arrays.L2TIntObjectHashMap;
import dwo.gameserver.util.geometry.Point3D;
import gnu.trove.procedure.TObjectProcedure;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public final class WorldManager
{
    public static final int GRACIA_MAX_X = -166168;
    public static final int GRACIA_MAX_Z = 6105;
    public static final int GRACIA_MIN_Z = -895;
    public static final int SHIFT_BY = 12;
    private static final int TILE_SIZE = 32768;
    public static final int MAP_MIN_X = (Config.WORLD_X_MIN - 20) * TILE_SIZE;
    public static final int OFFSET_X = Math.abs(MAP_MIN_X >> 12);
    public static final int MAP_MAX_X = (Config.WORLD_X_MAX - 19) * TILE_SIZE;
    private static final int REGIONS_X = (MAP_MAX_X >> 12) + OFFSET_X;
    public static final int MAP_MIN_Y = (Config.WORLD_Y_MIN - 18) * TILE_SIZE;
    public static final int OFFSET_Y = Math.abs(MAP_MIN_Y >> 12);
    public static final int MAP_MAX_Y = (Config.WORLD_Y_MAX - 17) * TILE_SIZE;
    private static final int REGIONS_Y = (MAP_MAX_Y >> 12) + OFFSET_Y;
    private static Logger _log = LogManager.getLogger(WorldManager.class);
    private final L2TIntObjectHashMap<L2PcInstance> _allPlayers;
    private final L2TIntObjectHashMap<L2Object> _allObjects;
    private final Map<Integer, FastList<L2PetInstance>> _petsInstance;
    private L2WorldRegion[][] _worldRegions;

    private WorldManager()
    {
        _allPlayers = new L2TIntObjectHashMap<>();
        _allObjects = new L2TIntObjectHashMap<>();
        _petsInstance = new ConcurrentHashMap<>();

        initRegions();
    }

    public static WorldManager getInstance()
    {
        return SingletonHolder._instance;
    }

    public void storeObject(L2Object object)
    {
        if (_allObjects.containsKey(object.getObjectId()))
        {
            _log.log(Level.WARN, "--------[L2World] object: " + object + " already exist in OID map!--------");
            _log.log(Level.WARN, "New object: " + StringUtil.getTraceString(Thread.currentThread().getStackTrace()));
            _log.log(Level.WARN, "----------------- Previous Put -----------------");
            _log.log(Level.WARN, "Previous: " + _allObjects.get(object.getObjectId()));
            _log.log(Level.WARN, "---------------------- End ---------------------");
            return;
        }
        _allObjects.put(object.getObjectId(), object);
    }

    public void removeObject(L2Object object)
    {
        _allObjects.remove(object.getObjectId());
    }

    public void removeObjects(List<L2Object> list)
    {
        list.stream().filter(o -> o != null).forEach(o -> {
            _allObjects.remove(o.getObjectId());
        });
    }

    public L2Object findObject(int oID)
    {
        return _allObjects.get(oID);
    }

    public final L2Object[] getAllVisibleObjectsArray()
    {
        return _allObjects.values(new L2Object[0]);
    }

    public final int getAllVisibleObjectsCount()
    {
        return _allObjects.size();
    }

    public L2TIntObjectHashMap<L2PcInstance> getAllPlayers()
    {
        return _allPlayers;
    }

    public final L2PcInstance[] getAllPlayersArray()
    {
        return _allPlayers.values(new L2PcInstance[0]);
    }

    public final boolean forEachPlayer(TObjectProcedure<L2PcInstance> proc)
    {
        return _allPlayers.forEachValue(proc);
    }

    public int getAllPlayersCount()
    {
        return _allPlayers.size();
    }

    public L2PcInstance getPlayer(String name)
    {
        return getPlayer(CharNameTable.getInstance().getIdByName(name));
    }

    public L2PcInstance getPlayer(int playerObjId)
    {
        return _allPlayers.get(playerObjId);
    }

    public L2Object getObject(int objectId)
    {
        return _allObjects.get(objectId);
    }

    public FastList<L2PetInstance> getPets(int ownerId)
    {
        return _petsInstance.get(ownerId);
    }

    public FastList<L2PetInstance> addPet(L2PetInstance pet)
    {
        FastList<L2PetInstance> temp = getPets(pet.getOwner().getObjectId());
        if (temp == null) {
            temp = new FastList<>();
        }
        temp.add(pet);
        return _petsInstance.put(pet.getOwner().getObjectId(), temp);
    }

    public void removePet(L2PetInstance pet)
    {
        FastList<L2PetInstance> temp = _petsInstance.get(pet.getOwner().getObjectId());
        temp.remove(pet);
        this._petsInstance.put(pet.getOwner().getObjectId(), temp);
    }

    public void addVisibleObject(L2Object object, L2WorldRegion newRegion)
    {
        L2PcInstance tmp;
        if (object.isPlayer())
        {
            L2PcInstance player = object.getActingPlayer();
            if (!player.isTeleporting())
            {
                tmp = getPlayer(player.getObjectId());
                if (tmp != null)
                {
                    _log.log(Level.WARN, "Duplicate character!? Closing both characters (" + player.getName() + ")");
                    player.logout();
                    tmp.logout();
                    return;
                }
                addToAllPlayers(player);
            }
        }
        if (!newRegion.isActive()) {
            return;
        }
        List<L2Object> visibles = getVisibleObjects(object, 2000);
        if (Config.DEBUG) {
            _log.log(Level.DEBUG, "objects in range:" + visibles.size());
        }
        visibles.stream().filter(visible -> visible != null).forEach(visible -> 
        {
            visible.getKnownList().addKnownObject(object);
            object.getKnownList().addKnownObject(visible);
        });
    }

    public void addToAllPlayers(L2PcInstance cha)
    {
        this._allPlayers.put(cha.getObjectId(), cha);
    }

    public void removeFromAllPlayers(L2PcInstance cha)
    {
        this._allPlayers.remove(cha.getObjectId());
    }

    public void removeVisibleObject(L2Object object, L2WorldRegion oldRegion)
    {
        if (object == null) {
            return;
        }
        if (oldRegion != null)
        {
            oldRegion.removeVisibleObject(object);
            for (L2WorldRegion reg : oldRegion.getSurroundingRegions()) {
                reg.getVisibleObjects().values().stream().filter(obj -> obj != null).forEach(obj -> {
                    obj.getKnownList().removeKnownObject(object);
                    object.getKnownList().removeKnownObject(obj);
                });
            }
            object.getKnownList().removeAllKnownObjects();
            if (object.isPlayer())
            {
                L2PcInstance player = object.getActingPlayer();
                if (!player.isTeleporting()) {
                    removeFromAllPlayers(player);
                }
            }
        }
    }

    public List<L2Object> getVisibleObjects(L2Object object)
    {
        L2WorldRegion reg = object.getLocationController().getWorldRegion();
        if (reg == null) {
            return null;
        }
        List<L2Object> result = new ArrayList<>();
        for (L2WorldRegion regi : reg.getSurroundingRegions()) {
            result.addAll(regi.getVisibleObjects().values().stream().filter(_object -> (_object != null) && (!_object.equals(object)) && (_object.isVisible())).map(_object -> _object).collect(Collectors.toList()));
        }
        return result;
    }

    public List<L2Object> getVisibleObjects(L2Object object, int radius)
    {
        if ((object == null) || (!object.isVisible())) {
            return new ArrayList<>();
        }
        int x = object.getX();
        int y = object.getY();
        int sqRadius = radius * radius;


        List<L2Object> result = new ArrayList<>();
        for (L2WorldRegion regi : object.getLocationController().getWorldRegion().getSurroundingRegions()) {
            regi.getVisibleObjects().values().stream().filter(_object -> (_object != null) && (!_object.equals(object))).forEach(_object -> {
                int x1 = _object.getX();
                int y1 = _object.getY();

                double dx = x1 - x;
                double dy = y1 - y;
                if (dx * dx + dy * dy < sqRadius) {
                    result.add(_object);
                }
            });
        }
        return result;
    }

    public List<L2Playable> getVisiblePlayable(L2Object object)
    {
        L2WorldRegion reg = object.getLocationController().getWorldRegion();
        if (reg == null) {
            return null;
        }
        List<L2Playable> result = new ArrayList<>();
        for (L2WorldRegion regi : reg.getSurroundingRegions()) 
        {
            result.addAll(regi.getVisiblePlayable().values().stream().filter(_object -> (_object != null) && (!_object.equals(object)) && (_object.isVisible())).map(_object -> _object).collect(Collectors.toList()));
        }
        return result;
    }

    public L2WorldRegion getRegion(Point3D point)
    {
        return this._worldRegions[((point.getX() >> 12) + OFFSET_X)][((point.getY() >> 12) + OFFSET_Y)];
    }

    public L2WorldRegion getRegion(int x, int y)
    {
        return this._worldRegions[((x >> 12) + OFFSET_X)][((y >> 12) + OFFSET_Y)];
    }

    public L2WorldRegion[][] getAllWorldRegions()
    {
        return this._worldRegions;
    }

    private boolean validRegion(int x, int y)
    {
        return (x >= 0) && (x <= REGIONS_X) && (y >= 0) && (y <= REGIONS_Y);
    }

    private void initRegions()
    {
        this._worldRegions = new L2WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];
        for (int i = 0; i <= REGIONS_X; i++) {
            for (int j = 0; j <= REGIONS_Y; j++) {
                this._worldRegions[i][j] = new L2WorldRegion(i, j);
            }
        }
        for (int x = 0; x <= REGIONS_X; x++) {
            for (int y = 0; y <= REGIONS_Y; y++) {
                for (int a = -1; a <= 1; a++) {
                    for (int b = -1; b <= 1; b++) {
                        if (validRegion(x + a, y + b)) {
                            this._worldRegions[(x + a)][(y + b)].addSurroundingRegion(this._worldRegions[x][y]);
                        }
                    }
                }
            }
        }
        _log.log(Level.INFO, "L2World: (" + REGIONS_X + " by " + REGIONS_Y + ") World Region Grid set up.");
    }

    public void deleteVisibleNpcSpawns()
    {
        _log.log(Level.INFO, "Deleting all visible NPC's.");
        for (int i = 0; i <= REGIONS_X; i++) {
            for (int j = 0; j <= REGIONS_Y; j++) {
                this._worldRegions[i][j].deleteVisibleNpcSpawns();
            }
        }
        _log.log(Level.INFO, "All visible NPC's deleted.");
    }

    private static class SingletonHolder
    {
        protected static final WorldManager _instance = new WorldManager();
    }
}
