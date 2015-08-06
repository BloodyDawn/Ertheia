package dwo.gameserver.engine.geodataengine.door;

import dwo.config.FilePath;
import dwo.gameserver.engine.databaseengine.idfactory.IdFactory;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.engine.geodataengine.GeoEngine;
import dwo.gameserver.instancemanager.InstanceManager;
import dwo.gameserver.instancemanager.clanhall.ClanHallManager;
import dwo.gameserver.model.actor.instance.L2DoorInstance;
import dwo.gameserver.model.actor.templates.L2DoorTemplate;
import dwo.gameserver.model.world.residence.clanhall.ClanHall;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DoorGeoEngine extends XmlDocumentParser
{
    private static DoorGeoEngine _instance;
    private final DoorGeoRegion[][] _doorRegions;
    private final Map<Integer, L2DoorInstance> _doorsByTemplateId;
    private final Map<Integer, L2DoorTemplate> _templatesByTemplateId;
    private final Map<Integer, List<L2DoorTemplate>> _templatesByGeoRegion;
    private L2DoorInstance[] _doors;
    private L2DoorTemplate[] _templates;

    private DoorGeoEngine()
    {
        _doorRegions = new DoorGeoRegion[19][17];
        _doorsByTemplateId = new HashMap<>();
        _templatesByTemplateId = new HashMap<>();
        _templatesByGeoRegion = new HashMap<>();
    }

    public static void init()
    {
        _instance = new DoorGeoEngine();
        try {
            _instance.load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static DoorGeoEngine getInstance()
    {
        return _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _doorsByTemplateId.clear();
        _templatesByTemplateId.clear();
        _templatesByGeoRegion.clear();

        parseFile(FilePath.DOOR_DATA);
        _log.log(Level.INFO, "DoorGeoEngine: Loaded " + _templatesByTemplateId.size() + " doors.");

        initializeTemplates();
        _log.log(Level.INFO, "DoorGeoEngine: Door templates initialized.");

        long time = System.currentTimeMillis();
        reloadGeoRegions();
        _log.log(Level.INFO, "DoorGeoEngine: Computed/Copied all needed Geo- Regions/Blocks/Cells in " + (System.currentTimeMillis() - time) + "ms.");

        initializeDoors();
        _log.log(Level.INFO, "DoorGeoEngine: Doors initialized.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("door"))
            {
                L2DoorTemplate template = new L2DoorTemplate(element);

                if(_templatesByTemplateId.put(template.getId(), template) == null)
                {
                    int regionOffset = GeoEngine.getRegionOffset2(template.getGeoRegion()[0], template.getGeoRegion()[1]);
                    List<L2DoorTemplate> list = _templatesByGeoRegion.get(regionOffset);
                    if(list == null)
                    {
                        list = new ArrayList<>();
                        _templatesByGeoRegion.put(regionOffset, list);
                    }
                    list.add(template);
                }
            }
        }

        _templates = _templatesByTemplateId.values().toArray(new L2DoorTemplate[_templatesByTemplateId.size()]);
    }

    /**
     * @param castleId ID Замка
     * @return двери, принадлежащие заданному замку
     */
    public L2DoorInstance[] getCastleDoors(int castleId)
    {
        List<L2DoorInstance> doors = new ArrayList<>(64);
        for(L2DoorInstance door : _doors)
        {
            if(door.getDoorTemplate().getCastleId() == castleId)
            {
                doors.add(door);
            }
        }
        return doors.toArray(new L2DoorInstance[doors.size()]);
    }

    /**
     * @param fortId ID Форта
     * @return двери, принадлежащие заданному Форту
     */
    public L2DoorInstance[] getFortDoors(int fortId)
    {
        List<L2DoorInstance> doors = new ArrayList<>(64);
        for(L2DoorInstance door : _doors)
        {
            if(door.getDoorTemplate().getFortId() == fortId)
            {
                doors.add(door);
            }
        }
        return doors.toArray(new L2DoorInstance[doors.size()]);
    }

    /**
     * @param doorId ID двери
     * @return Инстанс двери
     */
    public L2DoorInstance getDoor(int doorId)
    {
        return _doorsByTemplateId.get(doorId);
    }

    /**
     * @return все существующие двери
     */
    public L2DoorInstance[] getDoors()
    {
        return _doors;
    }

    public void reloadGeoRegions()
    {
        for(byte regionX = 10; regionX < 29; regionX++)
        {
            for(byte regionY = 10; regionY < 27; regionY++)
            {
                _doorRegions[regionX - 10][regionY - 10] = new DoorGeoRegion(regionX, regionY, this);
            }
        }
    }

    public void initializeTemplates()
    {
        _templates = _templatesByTemplateId.values().toArray(new L2DoorTemplate[_templatesByTemplateId.size()]);

        for(L2DoorTemplate template : _templates)
        {
            template.computeCells();
        }

        for(L2DoorTemplate template : _templates)
        {
            template.computeDoorTemplatesInSameBlocks(this);
        }
    }

    public void initializeDoors()
    {
        if(!_doorsByTemplateId.isEmpty())
        {
            throw new RuntimeException("I won`t allow that now");
        }

        for(L2DoorTemplate template : _templates)
        {
            L2DoorInstance instance = new L2DoorInstance(IdFactory.getInstance().getNextId(), template);
            _doorsByTemplateId.put(template.getId(), instance);
        }

        _doors = _doorsByTemplateId.values().toArray(new L2DoorInstance[_doorsByTemplateId.size()]);

        for(L2DoorInstance door : _doors)
        {
            // Двери замков контролируются самим замком
            if(door.getDoorTemplate().getCastleId() != -1)
            {
                continue;
            }

            // Двери фортов контролируются самим фортом
            if(door.getDoorTemplate().getFortId() != -1)
            {
                continue;
            }

            door.getLocationController().spawn(door.getX(), door.getY(), door.getZ());

            if(door.getDoorTemplate().getClanHallId() != -1)
            {
                ClanHall clanhall = ClanHallManager.getInstance().getClanHallById(door.getDoorTemplate().getClanHallId());
                clanhall.getDoors().add(door);
                door.setClanHall(clanhall);
            }
        }
    }

    public boolean isDoorInSameBlock(int geoX, int geoY, List<L2DoorTemplate> templates)
    {
        for(L2DoorTemplate template : templates)
        {
            if(template.isInSameBlock(geoX, geoY))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param geoX геокоордината по X
     * @param geoY геокоордината по Y
     * @return квадрат, в котором расположена дверь
     */
    public DoorGeoRegion getDoorGeoRegion(int geoX, int geoY)
    {
        int regionX = GeoEngine.getRegionXY(geoX);
        int regionY = GeoEngine.getRegionXY(geoY);
        return _doorRegions[regionX][regionY];
    }

    /**
     * @param doorId ID двери
     * @param store сохранять ли её в общем массиве?
     * @return новый инстанс двери
     */
    public L2DoorInstance newInstance(int doorId, boolean store)
    {
        return newInstance(doorId, 0, store);
    }

    /**
     * @param doorId ID двери
     * @param instanceId ID временной зоны
     * @param store сохранять ли её в общем массиве?
     * @return новый инстанс двери в заданной временной зоне
     */
    public L2DoorInstance newInstance(int doorId, int instanceId, boolean store)
    {
        L2DoorTemplate template = _templatesByTemplateId.get(doorId);
        if(template == null)
        {
            throw new RuntimeException();
        }

        L2DoorInstance door = new L2DoorInstance(IdFactory.getInstance().getNextId(), template);
        door.getInstanceController().setInstanceId(instanceId);

        if(store)
        {
            if(instanceId == 0)
            {
                _doorsByTemplateId.put(doorId, door);
                for(int i = _doors.length; i-- > 0; )
                {
                    if(_doors[i].getDoorId() == doorId)
                    {
                        _doors[i] = door;
                    }
                }
            }
        }
        return door;
    }

    /**
     * Апдейт статов существующей двери
     * @param door инстанс двери
     */
    public void updateDoor(L2DoorInstance door)
    {
        if(door == null)
        {
            return;
        }

        updateDoorTemplate(door.getDoorTemplate(), door.getInstanceId());
    }

    /**
     * Апдейт темплейта двери
     * @param template L2DoorTemplate двери
     * @param instanceId ID временной зоны
     */
    public void updateDoorTemplate(L2DoorTemplate template, int instanceId)
    {
        if(template == null)
        {
            return;
        }

        List<L2DoorInstance> doors = new ArrayList<>();
        List<L2DoorInstance> instanceDoors = InstanceManager.getInstance().getAllDoorRelatedInstancedDoors(template);
        L2DoorTemplate[] templates = template.getDoorTemplatesInSameBlocks();

        L2DoorInstance door;
        for(L2DoorTemplate temp : templates)
        {
            door = _doorsByTemplateId.get(temp.getId());

            if(door != null)
            {
                doors.add(door);
            }
        }

        if(instanceDoors != null)
        {
            doors.addAll(instanceDoors);
        }

        for(int[] block : template.getBlocks())
        {
            DoorGeoRegion region = getDoorGeoRegion(block[0], block[1]);
            if(region == null)
            {
                continue;
            }

            region.updateCells(block[0], block[1], doors);
        }
    }

    /**
     * @return все существующие шаблоны дверей
     */
    public L2DoorTemplate[] getAllDoorTemplates()
    {
        return _templates;
    }

    /**
     * @param regionX квадрат по X
     * @param regionY квадрат по Y
     * @return все двери в заданном квадрате
     */
    public List<L2DoorTemplate> getAllDoorsInRegion(int regionX, int regionY)
    {
        return _templatesByGeoRegion.get(GeoEngine.getRegionOffset2(regionX, regionY));
    }

    /**
     * @param geoX координата по X
     * @param geoY координата по Y
     * @param height высота
     * @param instanceId ID временной зоны
     * @return маску для заданной ячейки (0xFFF0 | (0xF & cell))
     */
    public short getCell(int geoX, int geoY, int height, int instanceId)
    {
        int regionX = GeoEngine.getRegionXY(geoX);
        int regionY = GeoEngine.getRegionXY(geoY);

        DoorGeoRegion region = _doorRegions[regionX][regionY];
        if(region == null)
        {
            _log.log(Level.ERROR, "No such region: " + regionX + ", " + regionY);
            return -1;
        }
        return region.getCell(geoX, geoY, (short) height, instanceId);
    }

    public short getCellBeyond(int geoX, int geoY, int height, int instanceId)
    {
        int regionX = GeoEngine.getRegionXY(geoX);
        int regionY = GeoEngine.getRegionXY(geoY);

        DoorGeoRegion region = _doorRegions[regionX][regionY];

        if(region == null)
        {
            _log.log(Level.WARN, "No such region: " + regionX + ", " + regionY);
            return -1;
        }

        return region.getCellBeyond(geoX, geoY, (short) height, instanceId);
    }
}