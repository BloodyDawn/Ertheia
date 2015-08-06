package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.base.type.L2EtcItemType;
import dwo.gameserver.model.world.npc.L2PetData;
import dwo.gameserver.model.world.npc.L2PetLevelData;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PetDataTable extends XmlDocumentParser
{
    private static final Map<Integer, L2PetData> _petTable = new HashMap<>();

    protected static PetDataTable instance;

    private PetDataTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static PetDataTable getInstance()
    {
        return instance == null ? instance = new PetDataTable() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _petTable.clear();
        parseDirectory(FilePath.PET_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _petTable.size() + " Pets.");
    }

    @Override
    public void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("pet"))
            {
                int npcId = Integer.parseInt(element.getAttributeValue("id"));
                L2PetData data = new L2PetData();
                
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element.getName();
                    if(name1.equalsIgnoreCase("set"))
                    {
                        String type = element1.getAttributeValue("name");
                        switch(type)
                        {
                            case "food":
                                String[] values = element1.getAttributeValue("val").split(";");
                                int[] food = new int[values.length];
                                for(int i = 0; i < values.length; i++)
                                {
                                    food[i] = Integer.parseInt(values[i]);
                                }
                                data.setFood(food);
                                break;
                            case "load":
                                data.setLoad(Integer.parseInt(element.getAttributeValue("val")));
                                break;
                            case "hungry_limit":
                                data.setHungryLimit(Integer.parseInt(element.getAttributeValue("val")));
                                break;
                            case "sync_level":
                                data.setSyncLevel(Integer.parseInt(element.getAttributeValue("val")));
                                break;
                        }
                    }
                    else if(name1.equalsIgnoreCase("skills"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("skill"))
                            {
                                int skillId = Integer.parseInt(element.getAttributeValue("skillId"));
                                int skillLvl = Integer.parseInt(element.getAttributeValue("skillLvl"));
                                int minLvl = Integer.parseInt(element.getAttributeValue("minLvl"));
                                data.addNewSkill(skillId, skillLvl, minLvl);
                            }
                        }
                    }
                    else if(name1.equalsIgnoreCase("stats"))
                    {
                        for(Element element2 : element1.getChildren())
                        {
                            final String name2 = element2.getName();
                            if(name2.equalsIgnoreCase("stat"))
                            {
                                int level = Integer.parseInt(element.getAttributeValue("level"));
                                L2PetLevelData stat = new L2PetLevelData();
                                for(Element element3 : element2.getChildren())
                                {
                                    final String name3 = element3.getName();
                                    if(name3.equalsIgnoreCase("set"))
                                    {
                                        String type = element3.getAttributeValue("name");
                                        String value = element3.getAttributeValue("val");
                                        switch(type)
                                        {
                                            case "max_meal":
                                                stat.setPetMaxFeed(Integer.parseInt(value));
                                                break;
                                            case "exp":
                                                stat.setPetMaxExp(Long.parseLong(value));
                                                break;
                                            case "get_exp_type":
                                                stat.setOwnerExpTaken(Integer.parseInt(value));
                                                break;
                                            case "consume_meal_in_battle":
                                                stat.setPetFeedBattle(Integer.parseInt(value));
                                                break;
                                            case "consume_meal_in_normal":
                                                stat.setPetFeedNormal(Integer.parseInt(value));
                                                break;
                                            case "patk":
                                                stat.setPetPAtk(Float.parseFloat(value));
                                                break;
                                            case "pdef":
                                                stat.setPetPDef(Float.parseFloat(value));
                                                break;
                                            case "matk":
                                                stat.setPetMAtk(Float.parseFloat(value));
                                                break;
                                            case "mdef":
                                                stat.setPetMDef(Float.parseFloat(value));
                                                break;
                                            case "hp":
                                                stat.setPetMaxHP(Float.parseFloat(value));
                                                break;
                                            case "mp":
                                                stat.setPetMaxMP(Float.parseFloat(value));
                                                break;
                                            case "hpreg":
                                                stat.setPetRegenHP(Float.parseFloat(value));
                                                break;
                                            case "mpreg":
                                                stat.setPetRegenMP(Float.parseFloat(value));
                                                break;
                                            case "soulshot_count":
                                                stat.setPetSoulShot((short) Integer.parseInt(value));
                                                break;
                                            case "spiritshot_count":
                                                stat.setPetSpiritShot((short) Integer.parseInt(value));
                                                break;
                                        }
                                    }
                                }
                                data.addNewStat(stat, level);
                            }
                        }
                    }
                }
                _petTable.put(npcId, data);
            }
        }
    }

    public L2PetLevelData getPetLevelData(int petID, int petLevel)
    {
        return _petTable.get(petID).getPetLevelData(petLevel);
    }

    public L2PetData getPetData(int petID)
    {
        if(!_petTable.containsKey(petID))
        {
            _log.log(Level.INFO, "Missing pet data for npcid: " + petID);
            return null;
        }

        return _petTable.get(petID);
    }

    public int getPetMinLevel(int petID)
    {
        return _petTable.get(petID).getMinLevel();
    }

    public static boolean isWolf(int npcId)
    {
        return npcId == 12077;
    }

    public static boolean isEvolvedWolf(int npcId)
    {
        return npcId == 16030 || npcId == 16037 || npcId == 16025 || npcId == 16041 || npcId == 16042;
    }

    public static boolean isSinEater(int npcId)
    {
        return npcId == 12564;
    }

    public static boolean isHatchling(int npcId)
    {
        return npcId > 12310 && npcId < 12314;
    }

    public static boolean isStrider(int npcId)
    {
        return npcId > 12525 && npcId < 12529 || npcId > 16037 && npcId < 16041 || npcId == 16068;
    }

    /*
     * Pets stuffs
     */

    public static boolean isWyvern(int npcId)
    {
        return npcId == 12621;
    }

    public static boolean isBaby(int npcId)
    {
        return npcId > 12779 && npcId < 12783;
    }

    public static boolean isImprovedBaby(int npcId)
    {
        return npcId > 16033 && npcId < 16037;
    }

    public static boolean isCucuruTime(int npcId)
    {
        return npcId == 13330;
    }

    public static boolean isPetFood(int itemId)
    {
        switch(itemId)
        {
            case 2515:
            case 4038:
            case 5168:
            case 5169:
            case 6316:
            case 7582:
            case 9668:
            case 10425:
                return true;
            default:
                return false;
        }
    }

    public static boolean isPetItem(int itemId)
    {
        L2Item item = ItemTable.getInstance().getTemplate(itemId);
        return item != null && item.getItemType() == L2EtcItemType.PET_COLLAR;
    }

    public static int[] getPetItemsByNpc(int npcId)
    {
        switch(npcId)
        {
            case 12077:// Wolf
                return new int[]{
                        2375
                };
            case 16025:// Great Wolf
                return new int[]{
                        9882
                };
            case 16030:// Black Wolf
                return new int[]{
                        10163
                };
            case 16037:// White Great Wolf
                return new int[]{
                        10307
                };
            case 16041:// Fenrir
                return new int[]{
                        10426
                };
            case 16042:// White Fenrir
                return new int[]{
                        10611
                };
            case 12564:// Sin Eater
                return new int[]{
                        4425
                };

            case 12311:// hatchling of wind
            case 12312:// hatchling of star
            case 12313:// hatchling of twilight
                return new int[]{
                        3500, 3501, 3502
                };

            case 12526:// wind strider
            case 12527:// Star strider
            case 12528:// Twilight strider
            case 16038: // red strider of wind
            case 16039: // red strider of star
            case 16040: // red strider of dusk
            case 16068: // Guardian Strider
                return new int[]{
                        4422, 4423, 4424, 10308, 10309, 10310, 14819
                };

            case 12621:// Wyvern
                return new int[]{
                        8663
                };

            case 12780:// Baby Buffalo
            case 12782:// Baby Cougar
            case 12781:// Baby Kookaburra
                return new int[]{
                        6648, 6649, 6650
                };

            case 16034:// Improved Baby Buffalo
            case 16036:// Improved Baby Cougar
            case 16035:// Improved Baby Kookaburra
                return new int[]{
                        10311, 10312, 10313
                };

            // unknown item id.. should never happen
            default:
                return new int[]{
                        0
                };
        }
    }

    public static boolean isMountable(int npcId)
    {
        return npcId == 12526 // wind strider
                || npcId == 12527 // star strider
                || npcId == 12528 // twilight strider
                || npcId == 12621 // wyvern
                || npcId == 16037 // Great Snow Wolf
                || npcId == 16041 // Fenrir Wolf
                || npcId == 16042 // White Fenrir Wolf
                || npcId == 16038 // Red Wind Strider
                || npcId == 16039 // Red Star Strider
                || npcId == 16040 // Red Twilight Strider
                || npcId == 16068; // Guardian Strider
    }
}