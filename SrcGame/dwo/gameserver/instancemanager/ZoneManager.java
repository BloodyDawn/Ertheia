package dwo.gameserver.instancemanager;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.world.zone.AbstractZoneSettings;
import dwo.gameserver.model.world.zone.L2WorldRegion;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.model.world.zone.form.ZoneCuboid;
import dwo.gameserver.model.world.zone.form.ZoneCylinder;
import dwo.gameserver.model.world.zone.form.ZoneNPoly;
import dwo.gameserver.model.world.zone.type.L2ArenaZone;
import dwo.gameserver.model.world.zone.type.L2OlympiadStadiumZone;
import dwo.gameserver.model.world.zone.type.L2RespawnZone;
import dwo.gameserver.model.world.zone.type.L2ZoneRespawn;
import javolution.util.FastMap;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

//TODO Вынести на хуй парсер отсюда
public class ZoneManager extends XmlDocumentParser
{
    private static final Map<String, AbstractZoneSettings> _settings = new HashMap<>();
    private final Map<Class<? extends L2ZoneType>, Map<Integer, ? extends L2ZoneType>> _classZones = new HashMap<>();
    private int _lastDynamicId = 300000;
    private List<L2ItemInstance> _debugItems;

    private Map<String, Integer> debugZoneNames = new HashMap<>();

    private ZoneManager()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static AbstractZoneSettings getSettings(String name)
    {
        return _settings.get(name);
    }

    public static ZoneManager getInstance()
    {
        return SingletonHolder._instance;
    }

    public void reload()
    {
        debugZoneNames.clear();
        // Get the world regions
        int count = 0;
        L2WorldRegion[][] worldRegions = WorldManager.getInstance().getAllWorldRegions();
        // Backup old zone settings
        for(Map<Integer, ? extends L2ZoneType> map : _classZones.values())
        {
            map.values().stream().filter(zone -> zone.getSettings() != null).forEach(zone -> _settings.put(zone.getName(), zone.getSettings()));
        }
        // Clear zones
        for(L2WorldRegion[] worldRegion : worldRegions)
        {
            for(L2WorldRegion aWorldRegion : worldRegion)
            {
                aWorldRegion.getZones().clear();
                count++;
            }
        }
        GrandBossManager.getInstance().getZones().clear();
        _log.log(Level.INFO, "Removed zones in " + count + " regions.");
        // Load the zones
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
        // Re-validate all characters in zones
        for(L2Object obj : WorldManager.getInstance().getAllVisibleObjectsArray())
        {
            if(obj instanceof L2Character)
            {
                ((L2Character) obj).revalidateZone(true);
            }
        }
        _settings.clear();
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        L2WorldRegion[][] worldRegions = WorldManager.getInstance().getAllWorldRegions();
        String zoneName;
        int zoneId, minZ, maxZ;
        String zoneType;
        String zoneShape;
        String attribute;
        List<int[]> rs = new ArrayList<>();

        attribute = rootElement.getAttributeValue("enabled");
        if(attribute != null && !Boolean.parseBoolean(attribute))
        {
            return;
        }
        
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("zone"))
            {
                attribute = element.getAttributeValue("id");
                zoneId = attribute != null ? Integer.parseInt(attribute) : _lastDynamicId++;

                attribute = element.getAttributeValue("name");
                if(attribute != null)
                {
                    zoneName = attribute;
                    if(debugZoneNames.containsKey(zoneName))
                    {
                        _log.log(Level.ERROR, "Double name for zone " + zoneName + " zoneId " + zoneId);
                    }
                    else
                    {
                        debugZoneNames.put(zoneName, zoneId);
                    }
                }
                else
                {
                    zoneName = getCurrentFile().getName() + zoneId;
                }

                minZ = Integer.parseInt(element.getAttributeValue("minZ"));
                maxZ = Integer.parseInt(element.getAttributeValue("maxZ"));

                zoneType = element.getAttributeValue("type");
                zoneShape = element.getAttributeValue("shape");

                // Create the zone
                Class<?> newZone;
                Constructor<?> zoneConstructor;
                L2ZoneType temp;
                try
                {
                    newZone = Class.forName("dwo.gameserver.model.world.zone.type.L2" + zoneType);
                    zoneConstructor = newZone.getConstructor(int.class);
                    temp = (L2ZoneType) zoneConstructor.newInstance(zoneId);
                }
                catch(Exception e)
                {
                    _log.log(Level.INFO, "ZoneData: No such zone type: " + zoneType + " in file: " + getCurrentFile().getName());
                    continue;
                }

                try
                {
                    int[][] coords;
                    rs.clear();

                    for(Element element2 : element.getChildren())
                    {
                        final String name2 = element2.getName();
                        if(name2.equalsIgnoreCase("node"))
                        {
                            final int[] point = { Integer.parseInt(element2.getAttributeValue("X")), Integer.parseInt(element2.getAttributeValue("Y")) };
                            rs.add(point);
                        }
                    }

                    coords = rs.toArray(new int[rs.size()][2]);

                    if(coords == null || coords.length == 0)
                    {
                        _log.log(Level.WARN, "ZoneData: missing data for zone: " + zoneId + " XML file: " + getCurrentFile().getName());
                        continue;
                    }

                    // Create this zone. Parsing for cuboids is a
                    // bit different than for other polygons
                    // cuboids need exactly 2 points to be defined.
                    // Other polygons need at least 3 (one per
                    // vertex)
                    if(zoneShape.equalsIgnoreCase("Cuboid"))
                    {
                        if(coords.length == 2)
                        {
                            temp.setZone(new ZoneCuboid(coords[0][0], coords[1][0], coords[0][1], coords[1][1], minZ, maxZ));
                        }
                        else
                        {
                            _log.log(Level.WARN, "ZoneData: Missing cuboid vertex in sql data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
                            continue;
                        }
                    }
                    else if(zoneShape.equalsIgnoreCase("NPoly"))
                    {
                        // nPoly needs to have at least 3 vertices
                        if(coords.length > 2)
                        {
                            int[] aX = new int[coords.length];
                            int[] aY = new int[coords.length];
                            for(int i = 0; i < coords.length; i++)
                            {
                                aX[i] = coords[i][0];
                                aY[i] = coords[i][1];
                            }
                            temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
                        }
                        else
                        {
                            _log.log(Level.WARN, "ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
                            continue;
                        }
                    }
                    else if(zoneShape.equalsIgnoreCase("Cylinder"))
                    {
                        // A Cylinder zone requires a center point
                        // at x,y and a radius
                        int zoneRad = Integer.parseInt(element.getAttributeValue("rad"));
                        if(coords.length == 1 && zoneRad > 0)
                        {
                            temp.setZone(new ZoneCylinder(coords[0][0], coords[0][1], minZ, maxZ, zoneRad));
                        }
                        else
                        {
                            _log.log(Level.WARN, "ZoneData: Bad data for zone: " + zoneId + " in file: " + getCurrentFile().getName());
                            continue;
                        }
                    }
                }
                catch(Exception e)
                {
                    _log.log(Level.ERROR, "ZoneData: Failed to load zone " + zoneId + " coordinates: " + e.getMessage(), e);
                }

                // Check for additional parameters
                for(Element element2 : element.getChildren())
                {
                    final String name2 = element2.getName();
                    if(name2.equalsIgnoreCase("stat"))
                    {
                        String name0 = element2.getAttributeValue("name");
                        String val = element2.getAttributeValue("val");

                        temp.setParameter(name0, val);
                    }
                    else if(name2.equalsIgnoreCase("spawn") && temp instanceof L2ZoneRespawn)
                    {
                        int spawnX = Integer.parseInt(element2.getAttributeValue("X"));
                        int spawnY = Integer.parseInt(element2.getAttributeValue("Y"));
                        int spawnZ = Integer.parseInt(element2.getAttributeValue("Z"));

                        int geoZ = GeoEngine.getInstance().getSpawnHeight(spawnX, spawnY, spawnZ, spawnZ);
                        if(temp.isInsideZone(spawnX, spawnY, geoZ))
                        {
                            spawnZ = geoZ;
                        }

                        String val = element2.getAttributeValue("type");
                        ((L2ZoneRespawn) temp).parseLoc(spawnX, spawnY, spawnZ, val == null ? null : val);
                    }
                    else if(name2.equalsIgnoreCase("race") && temp instanceof L2RespawnZone)
                    {
                        String race = element2.getAttributeValue("name");
                        String point = element2.getAttributeValue("point");

                        ((L2RespawnZone) temp).addRaceRespawnPoint(race, point);
                    }
                }
                if(checkId(zoneId))
                {
                    _log.log(Level.WARN, "Caution: Zone (" + zoneId + ") from file: " + getCurrentFile().getName() + " overrides previos definition.");
                }

                if(zoneName != null && !zoneName.isEmpty())
                {
                    temp.setName(zoneName);
                }

                addZone(zoneId, temp);

                // Register the zone into any world region it
                // intersects with...
                // currently 11136 test for each zone :>
                int ax, ay, bx, by;
                for(int x = 0; x < worldRegions.length; x++)
                {
                    for(int y = 0; y < worldRegions[x].length; y++)
                    {
                        ax = x - WorldManager.OFFSET_X << WorldManager.SHIFT_BY;
                        bx = x + 1 - WorldManager.OFFSET_X << WorldManager.SHIFT_BY;
                        ay = y - WorldManager.OFFSET_Y << WorldManager.SHIFT_BY;
                        by = y + 1 - WorldManager.OFFSET_Y << WorldManager.SHIFT_BY;

                        if(temp.getZone().intersectsRectangle(ax, bx, ay, by))
                        {
                            worldRegions[x][y].addZone(temp);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void load() throws JDOMException, IOException {
        _log.log(Level.INFO, "Loading zones...");
        _classZones.clear();

        long started = System.currentTimeMillis();

        parseDirectory(FilePath.ZONE_DATA);

        started = System.currentTimeMillis() - started;
        _log.log(Level.INFO, "Loaded " + _classZones.size() + " zone classes and " + getSize() + " zones in " + started / 1000 + " seconds.");
    }

    public int getSize()
    {
        int i = 0;
        for(Map<Integer, ? extends L2ZoneType> map : _classZones.values())
        {
            i += map.size();
        }
        return i;
    }

    public boolean checkId(int id)
    {
        for(Map<Integer, ? extends L2ZoneType> map : _classZones.values())
        {
            if(map.containsKey(id))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Add new zone
     * @param <T>
     * @param id
     * @param zone
     */
    public <T extends L2ZoneType> void addZone(Integer id, T zone)
    {
        //_zones.put(id, zone);
        Map<Integer, T> map = (Map<Integer, T>) _classZones.get(zone.getClass());
        if(map == null)
        {
            map = new FastMap<>();
            map.put(id, zone);
            _classZones.put(zone.getClass(), map);
        }
        else
        {
            map.put(id, zone);
        }
    }

    /**
     * @param <T>
     * @param zoneType Zone class
     * @return all zones by class type
     */
    public <T extends L2ZoneType> Collection<T> getAllZones(Class<T> zoneType)
    {
        return (Collection<T>) _classZones.get(zoneType).values();
    }

    /**
     * Get zone by ID
     * @param id
     * @return
     * @see #getZoneById(int, Class)
     */
    public L2ZoneType getZoneById(int id)
    {
        for(Map<Integer, ? extends L2ZoneType> map : _classZones.values())
        {
            if(map.containsKey(id))
            {
                return map.get(id);
            }
        }
        return null;
    }

    /**
     * @param <T>
     * @param id
     * @param zoneType
     * @return zone by ID and zone class
     */
    public <T extends L2ZoneType> T getZoneById(int id, Class<T> zoneType)
    {
        return (T) _classZones.get(zoneType).get(id);
    }

    /**
     * @param object
     * @return zones all zones from where the object is located
     */
    public List<L2ZoneType> getZones(L2Object object)
    {
        return getZones(object.getX(), object.getY(), object.getZ());
    }

    /**
     * @param object
     * @param type
     * @return zone from where the object is located by type
     */
    public <T extends L2ZoneType> T getZone(L2Object object, Class<T> type)
    {
        if(object == null)
        {
            return null;
        }
        return getZone(object.getX(), object.getY(), object.getZ(), type);
    }

    /**
     * @param x
     * @param y
     * @return zones all zones from given coordinates (plane)
     */
    public List<L2ZoneType> getZones(int x, int y)
    {
        L2WorldRegion region = WorldManager.getInstance().getRegion(x, y);
        List<L2ZoneType> temp = region.getZones().stream().filter(zone -> zone.isInsideZone(x, y)).collect(Collectors.toList());
        return temp;
    }

    /**
     * @param x
     * @param y
     * @param z
     * @return all zones from given coordinates
     */
    public List<L2ZoneType> getZones(int x, int y, int z)
    {
        L2WorldRegion region = WorldManager.getInstance().getRegion(x, y);
        List<L2ZoneType> temp = region.getZones().stream().filter(zone -> zone.isInsideZone(x, y, z)).collect(Collectors.toList());
        return temp;
    }

    /**
     * Returns zone from given coordinates
     *
     * @param x
     * @param y
     * @param z
     * @param type
     * @return zone
     */
    public <T extends L2ZoneType> T getZone(int x, int y, int z, Class<T> type)
    {
        L2WorldRegion region = WorldManager.getInstance().getRegion(x, y);
        for(L2ZoneType zone : region.getZones())
        {
            if(zone.isInsideZone(x, y, z) && type.isInstance(zone))
            {
                return (T) zone;
            }
        }
        return null;
    }

    public L2ArenaZone getArena(L2Character character)
    {
        if(character == null)
        {
            return null;
        }

        for(L2ZoneType temp : getInstance().getZones(character.getX(), character.getY(), character.getZ()))
        {
            if(temp instanceof L2ArenaZone && temp.isCharacterInZone(character))
            {
                return (L2ArenaZone) temp;
            }
        }

        return null;
    }

    public L2OlympiadStadiumZone getOlympiadStadium(L2Character character)
    {
        if(character == null)
        {
            return null;
        }

        for(L2ZoneType temp : getInstance().getZones(character.getX(), character.getY(), character.getZ()))
        {
            if(temp instanceof L2OlympiadStadiumZone && temp.isCharacterInZone(character))
            {
                return (L2OlympiadStadiumZone) temp;
            }
        }
        return null;
    }

    /**
     * For testing purposes only
     * @param <T>
     * @param obj
     * @param type
     * @return
     */
    public <T extends L2ZoneType> T getClosestZone(L2Object obj, Class<T> type)
    {
        T zone = getZone(obj, type);
        if(zone == null)
        {
            double closestdis = Double.MAX_VALUE;
            for(T temp : (Collection<T>) _classZones.get(type).values())
            {
                double distance = temp.getDistanceToZone(obj);
                if(distance < closestdis)
                {
                    closestdis = distance;
                    zone = temp;
                }
            }
        }
        return zone;
    }

    /**
     * General storage for debug items used for visualizing zones.
     * @return list of items
     */
    public List<L2ItemInstance> getDebugItems()
    {
        if(_debugItems == null)
        {
            _debugItems = new ArrayList<>();
        }
        return _debugItems;
    }

    /**
     * Remove all debug items from l2world
     */
    public void clearDebugItems()
    {
        if(_debugItems != null)
        {
            Iterator<L2ItemInstance> it = _debugItems.iterator();
            while(it.hasNext())
            {
                L2ItemInstance item = it.next();
                if(item != null)
                {
                    item.getLocationController().decay();
                }
                it.remove();
            }
        }
    }

    private static class SingletonHolder
    {
        protected static final ZoneManager _instance = new ZoneManager();
    }
}