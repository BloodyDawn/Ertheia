package dwo.gameserver.model.world.zone;

import dwo.config.Config;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.datatables.xml.SpawnTable;
import dwo.gameserver.model.actor.*;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.type.L2DerbyTrackZone;
import dwo.gameserver.model.world.zone.type.L2PeaceZone;
import dwo.gameserver.util.RunnableImpl;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public final class L2WorldRegion
{
    private static Logger _log = LogManager.getLogger(L2WorldRegion.class);
    private final Map<Integer, L2Playable> _allPlayable;
    private final Map<Integer, L2Object> _visibleObjects;
    private final List<L2WorldRegion> _surroundingRegions;
    private final int _tileX;
    private final int _tileY;
    private final List<L2ZoneType> _zones;
    private boolean _active = false;
    private ScheduledFuture<?> _neighborsTask = null;

    public L2WorldRegion(int pTileX, int pTileY)
    {
        _allPlayable = new ConcurrentHashMap<>();
        _visibleObjects = new ConcurrentHashMap<>();
        _surroundingRegions = new ArrayList<>();

        _tileX = pTileX;
        _tileY = pTileY;
        _active = Config.GRIDS_ALWAYS_ON;
        _zones = new ArrayList<>();
    }

    public List<L2ZoneType> getZones()
    {
        return _zones;
    }

    public void addZone(L2ZoneType zone)
    {
        _zones.add(zone);
    }

    public void removeZone(L2ZoneType zone)
    {
        _zones.remove(zone);
    }

    public void revalidateZones(L2Character character)
    {
        if (character.isTeleporting()) {
            return;
        }
        getZones().stream().filter(zone -> zone != null).forEach(zone -> zone.revalidateInZone(character));
    }

    public void removeFromZones(L2Character character)
    {
        getZones().stream().filter(zone -> zone != null).forEach(zone -> zone.removeCharacter(character));
    }

    public boolean checkIfInPeaceZone(int x, int y, int z)
    {
        for (L2ZoneType e : getZones()) {
            if (((e instanceof L2DerbyTrackZone)) || ((e instanceof L2PeaceZone))) {
                if (e.isInsideZone(x, y, z)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void onDeath(L2Character character)
    {
        getZones().stream().filter(zone -> zone != null).forEach(zone -> zone.onDieInside(character));
    }

    public void onRevive(L2Character character)
    {
        getZones().stream().filter(zone -> zone != null).forEach(zone -> zone.onReviveInside(character));
    }

    private void switchAI(boolean activate)
    {
        if (!activate)
        {
            Collection<L2Object> visibleObjects = _visibleObjects.values();
            for (L2Object object : visibleObjects) {
                if ((object instanceof L2Attackable))
                {
                    L2Attackable mob = (L2Attackable)object;
                    mob.setTarget(null);
                    mob.stopMove(null);
                    mob.stopAllEffects();
                    mob.clearAggroList();
                    mob.getAttackByList().clear();
                    mob.getKnownList().removeAllKnownObjects();
                    
                    if (mob.hasAI())
                    {
                        mob.getAI().setIntention(CtrlIntention.AI_INTENTION_IDLE);
                        mob.getAI().stopAITask();
                    }
                }
                else if ((object instanceof L2Vehicle))
                {
                    ((L2Vehicle)object).getKnownList().removeAllKnownObjects();
                }
            }
        }
        else
        {
            Collection<L2Object> vObj = _visibleObjects.values();
            for (L2Object o : vObj) {
                if ((o instanceof L2Attackable)) {
                    ((L2Attackable)o).getStatus().startHpMpRegeneration();
                } else if ((o instanceof L2Npc)) {
                    ((L2Npc)o).startRandomAnimationTimer();
                }
            }
        }
    }

    public boolean isActive()
    {
        return _active;
    }

    public void setActive(boolean value)
    {
        if (_active == value) {
            return;
        }
        _active = value;

        switchAI(value);
    }

    public boolean areNeighborsEmpty()
    {
        if ((isActive()) && (!_allPlayable.isEmpty())) {
            return false;
        }
        for (L2WorldRegion neighbor : _surroundingRegions) {
            if ((neighbor.isActive()) && (!neighbor._allPlayable.isEmpty())) {
                return false;
            }
        }
        return true;
    }

    private void startActivation()
    {
        setActive(true);
        synchronized (this)
        {
            if (_neighborsTask != null)
            {
                _neighborsTask.cancel(true);
                _neighborsTask = null;
            }
            _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(true), 1000 * Config.GRID_NEIGHBOR_TURNON_TIME);
        }
    }

    private void startDeactivation()
    {
        synchronized (this)
        {
            if (_neighborsTask != null)
            {
                _neighborsTask.cancel(true);
                _neighborsTask = null;
            }
            _neighborsTask = ThreadPoolManager.getInstance().scheduleGeneral(new NeighborsTask(false), 1000 * Config.GRID_NEIGHBOR_TURNOFF_TIME);
        }
    }

    public void addVisibleObject(L2Object object)
    {
        if (object == null) {
            return;
        }
        if ((object.getLocationController().getWorldRegion() != this)) throw new AssertionError();

        _visibleObjects.put(object.getObjectId(), object);
        if ((object instanceof L2Playable))
        {
            _allPlayable.put(object.getObjectId(), (L2Playable) object);
            if ((_allPlayable.size() == 1) && (!Config.GRIDS_ALWAYS_ON)) {
                startActivation();
            }
        }
    }

    public void removeVisibleObject(L2Object object)
    {
        if (object == null) {
            return;
        }
        if ((!((object.getLocationController().getWorldRegion() == this) || (object.getLocationController().getWorldRegion() == null))))
            throw new AssertionError();

        _visibleObjects.remove(object.getObjectId());
        if ((object instanceof L2Playable))
        {
            _allPlayable.remove(object.getObjectId());
            if ((_allPlayable.isEmpty()) && (!Config.GRIDS_ALWAYS_ON)) {
                startDeactivation();
            }
        }
    }

    public void addSurroundingRegion(L2WorldRegion region)
    {
        _surroundingRegions.add(region);
    }

    public List<L2WorldRegion> getSurroundingRegions()
    {
        return _surroundingRegions;
    }

    public Map<Integer, L2Playable> getVisiblePlayable()
    {
        return _allPlayable;
    }

    public Map<Integer, L2Object> getVisibleObjects()
    {
        return _visibleObjects;
    }

    public void deleteVisibleNpcSpawns()
    {
        _log.log(Level.INFO, "Deleting all visible NPC's in Region: " + toString());
        Collection<L2Object> vNPC = _visibleObjects.values();
        vNPC.stream().filter(obj -> (obj instanceof L2Npc)).forEach(obj -> {
            L2Npc target = (L2Npc) obj;
            target.getLocationController().delete();
            L2Spawn spawn = target.getSpawn();
            if (spawn != null) {
                spawn.stopRespawn();
                SpawnTable.getInstance().deleteSpawn(spawn);
            }
            _log.log(Level.INFO, "Removed NPC " + target.getObjectId());
        });
        _log.log(Level.INFO, "All visible NPC's deleted in Region: " + toString());
    }

    public String toString()
    {
        return "[" + _tileX + "_" + _tileY + "]";
    }

    public class NeighborsTask extends RunnableImpl
    {
        private final boolean _isActivating;

        public NeighborsTask(boolean isActivating)
        {
            _isActivating = isActivating;
        }

        @Override
        public void runImpl() throws Exception 
        {
            if (_isActivating)
            {
                for (L2WorldRegion neighbor : getSurroundingRegions()) {
                    neighbor.setActive(true);
                }
            }
            else
            {
                if (areNeighborsEmpty()) {
                    setActive(false);
                }
                getSurroundingRegions().stream().filter(L2WorldRegion::areNeighborsEmpty).forEach(neighbor -> neighbor.setActive(false));
            }
        }
    }
}
