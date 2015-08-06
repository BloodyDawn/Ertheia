package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.world.npc.ManorSeedData;
import javolution.util.FastList;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 19.01.13
 * Time: 20:11
 */

public class ManorData extends XmlDocumentParser
{
    private static Map<Integer, ManorSeedData> _seeds = new HashMap<>();

    protected static ManorData _instance;

    private ManorData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static ManorData getInstance()
    {
        return _instance == null ? _instance = new ManorData() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _seeds.clear();
        parseFile(FilePath.MANOR_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _seeds.size() + " Seeds.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("castle"))
            {
                int castleId = Integer.parseInt(element.getAttributeValue("id"));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("crop"))
                    {
                        int cropId = Integer.parseInt(element1.getAttributeValue("id"));
                        int seedId = 0, matureId = 0, type1R = 0, type2R = 0, level = 0, limitSeeds = 0, limitCrops = 0;

                        boolean isAlt = false;

                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("seed_id"))
                            {
                                seedId = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("mature_id"))
                            {
                                matureId = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("reward1"))
                            {
                                type1R = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("reward2"))
                            {
                                type2R = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("alternative"))
                            {
                                isAlt = Integer.parseInt(element2.getAttributeValue("val")) == 1;
                            }
                            else if(name2.equalsIgnoreCase("level"))
                            {
                                level = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("limit_seed"))
                            {
                                limitSeeds = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                            else if(name2.equalsIgnoreCase("limit_crops"))
                            {
                                limitCrops = Integer.parseInt(element2.getAttributeValue("val"));
                            }
                        }

                        ManorSeedData seed = new ManorSeedData(level, cropId, matureId);
                        seed.setData(seedId, type1R, type2R, castleId, isAlt, limitSeeds, limitCrops);
                        _seeds.put(seed.getId(), seed);
                    }
                }
            }
        }
    }

    public Collection<ManorSeedData> getSeedsDataArray()
    {
        return _seeds.values();
    }

    public List<Integer> getAllCrops()
    {
        List<Integer> crops = new ArrayList<>();

        getSeedsDataArray().stream().filter(seed -> !crops.contains(seed.getCrop()) && seed.getCrop() != 0 && !crops.contains(seed.getCrop())).forEach(seed -> crops.add(seed.getCrop()));

        return crops;
    }

    public int getSeedBasicPrice(int seedId)
    {
        L2Item seedItem = ItemTable.getInstance().getTemplate(seedId);

        return seedItem != null ? seedItem.getReferencePrice() : 0;
    }

    public int getSeedBasicPriceByCrop(int cropId)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getCrop() == cropId)
            {
                return getSeedBasicPrice(seed.getId());
            }
        }
        return 0;
    }

    public int getCropBasicPrice(int cropId)
    {
        L2Item cropItem = ItemTable.getInstance().getTemplate(cropId);

        return cropItem != null ? cropItem.getReferencePrice() : 0;
    }

    public int getMatureCrop(int cropId)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getCrop() == cropId)
            {
                return seed.getMature();
            }
        }
        return 0;
    }

    /**
     * @param seedId
     * @return price which lord pays to buy one seed
     */
    public long getSeedBuyPrice(int seedId)
    {
        long buyPrice = getSeedBasicPrice(seedId);
        return buyPrice > 0 ? buyPrice : 1;
    }

    public int getSeedMinLevel(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getLevel() - 5;
        }
        return -1;
    }

    public int getSeedMaxLevel(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getLevel() + 5;
        }
        return -1;
    }

    public int getSeedLevelByCrop(int cropId)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getCrop() == cropId)
            {
                return seed.getLevel();
            }
        }
        return 0;
    }

    public int getSeedLevel(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getLevel();
        }
        return -1;
    }

    public boolean isAlternative(int seedId)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getId() == seedId)
            {
                return seed.isAlternative();
            }
        }
        return false;
    }

    public int getCropType(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getCrop();
        }
        return -1;
    }

    public int getRewardItem(int cropId, int type)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getCrop() == cropId)
            {
                return seed.getReward(type); // there can be several seeds with same crop, but reward should be the same for all
            }
        }
        return -1;
    }

    public int getRewardItemBySeed(int seedId, int type)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getReward(type);
        }
        return 0;
    }

    /**
     * @param castleId id of the castle
     * @return all crops which can be purchased by given castle
     */
    public FastList<Integer> getCropsForCastle(int castleId)
    {
        FastList<Integer> crops = new FastList<>();

        getSeedsDataArray().stream().filter(seed -> seed.getManorId() == castleId && !crops.contains(seed.getCrop())).forEach(seed -> crops.add(seed.getCrop()));
        return crops;
    }

    /**
     * @param castleId id of the castle
     * @return list of seed ids, which belongs to castle with given id
     */
    public FastList<Integer> getSeedsForCastle(int castleId)
    {
        FastList<Integer> seedsID = new FastList<>();

        getSeedsDataArray().stream().filter(seed -> seed.getManorId() == castleId && !seedsID.contains(seed.getId())).forEach(seed -> seedsID.add(seed.getId()));
        return seedsID;
    }

    /**
     * @param seedId
     * @return castle id where seed can be sowed
     */
    public int getCastleIdForSeed(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getManorId();
        }
        return 0;
    }

    public int getSeedSaleLimit(int seedId)
    {
        ManorSeedData seed = _seeds.get(seedId);

        if(seed != null)
        {
            return seed.getSeedLimit();
        }
        return 0;
    }

    public int getCropPuchaseLimit(int cropId)
    {
        for(ManorSeedData seed : getSeedsDataArray())
        {
            if(seed.getCrop() == cropId)
            {
                return seed.getCropLimit();
            }
        }
        return 0;
    }
}
