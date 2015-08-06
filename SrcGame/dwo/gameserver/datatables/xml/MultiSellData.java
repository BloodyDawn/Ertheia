package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.L2Item;
import dwo.gameserver.model.items.itemcontainer.PcInventory;
import dwo.gameserver.model.items.multisell.Entry;
import dwo.gameserver.model.items.multisell.Ingredient;
import dwo.gameserver.model.items.multisell.ListContainer;
import dwo.gameserver.model.items.multisell.PreparedListContainer;
import dwo.gameserver.model.player.formation.clan.L2Clan;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.MultiSellList;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.File;
import java.io.IOException;

public class MultiSellData extends XmlDocumentParser
{
    public static final int PAGE_SIZE = 40;

    public static final int PC_BANG_POINTS = -100;
    public static final int CLAN_REPUTATION = -200;
    public static final int FAME = -300;

    private final TIntObjectHashMap<ListContainer> _entries = new TIntObjectHashMap<>();

    private static MultiSellData instance;

    private MultiSellData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static MultiSellData getInstance()
    {
        return instance == null ? instance = new MultiSellData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _entries.clear();

        File[] fileList = FilePath.MULTISELL_DIR.listFiles();
        for(File f : fileList)
        {
            if(f.isFile())
            {
                parseFile(f);
            }
            else if(f.isDirectory())
            {
                if(f.getName().equals("custom"))
                {
                    File customDir = new File(f, Config.CUSTOM_DATA_DIRECTORY);
                    if(customDir.exists())
                    {
                        parseDirectory(customDir);
                    }
                }
                else
                {
                    parseDirectory(f);
                }
            }
        }

        verify();
        _log.log(Level.INFO, "MultiSell: Loaded " + _entries.size() + " lists.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        int entryId = 1;
        ListContainer list = new ListContainer();

        if (rootElement.getAttributeValue("taxFree") != null) {
            list.setApplyTaxes(Boolean.parseBoolean(rootElement.getAttributeValue("taxFree")));
        }
        if (rootElement.getAttributeValue("keepEnchant") != null) {
            list.setKeepEnchant(Boolean.parseBoolean(rootElement.getAttributeValue("keepEnchant")));
        }
        if (rootElement.getAttributeValue("allowAugmentedItems") != null) {
            list.setAllowAugmentedItems(Boolean.parseBoolean(rootElement.getAttributeValue("allowAugmentedItems")));
        }
        if (rootElement.getAttributeValue("allowElementalItems") != null) {
            list.setAllowElementalItems(Boolean.parseBoolean(rootElement.getAttributeValue("allowElementalItems")));
        }
        if (rootElement.getAttributeValue("showAll") != null) {
            list.setShowAll(Boolean.parseBoolean(rootElement.getAttributeValue("showAll")));
        }
        if (rootElement.getAttributeValue("chanceBuy") != null) {
            list.setChanceBuy(Boolean.parseBoolean(rootElement.getAttributeValue("chanceBuy")));
        }
        if (rootElement.getAttributeValue("npcCheck") != null) {
            list.setNpcRequied(Boolean.parseBoolean(rootElement.getAttributeValue("npcCheck")));
        }
        
        for(Element element : rootElement.getChildren())
        {
            final String name =  element.getName();
            if(name.equalsIgnoreCase("item"))
            {
                list.getEntries().add(parseEntry(element, entryId++));
            }
        }

        int listId = Integer.parseInt(getCurrentFile().getName().replace(".xml", ""));
        list.setListId(listId);
        _entries.put(listId, list);
    }

    private Entry parseEntry(Element rootElement, int entryId)
    {
        Entry entry = new Entry(entryId);

        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            int id, enchantLvl, chance;
            long count;
            
            if(name.equalsIgnoreCase("ingredient"))
            {
                id = Integer.parseInt(element.getAttributeValue("id"));
                count = Long.parseLong(element.getAttributeValue("count"));
                enchantLvl = Integer.valueOf(element.getAttributeValue("enchantLvl", "0"));
                entry.addIngredient(new Ingredient(id, count, enchantLvl, 0));
            }
            else if(name.equalsIgnoreCase("production"))
            {
                id = Integer.parseInt(element.getAttributeValue("id"));
                count = Long.parseLong(element.getAttributeValue("count"));
                enchantLvl = Integer.valueOf(element.getAttributeValue("enchantLvl", "0"));
                chance = Integer.parseInt(element.getAttributeValue("chance", "100"));
                entry.addProduct(new Ingredient(id, count, enchantLvl, chance));
            }
        }

        // Проверка на дюпы
        entry.getIngredients().stream().filter(ing -> ing.getItemId() == PcInventory.ADENA_ID && entry.getIngredients().size() == 1).forEach(ing -> 
        {
            for(Ingredient pro : entry.getProducts())
            {
                L2Item item = ItemTable.getInstance().getTemplate(pro.getItemId());

                if(item != null && item.getReferencePrice() / 2.0 > ing.getItemCount() && pro.getChance() >= 100 && !getCurrentFile().getPath().contains("gm_shop"))
                {
                    _log.warn("Find DUPE in multisell " + getCurrentFile().getName() + ". Product ID: " + pro.getItemId() + ". Price difference is " + (item.getReferencePrice() / 2.0 - ing.getItemCount()) + " Adena.");
                    _log.warn("ATTENTION: Price for product: " + pro.getItemId() + " was setted to normalized item price for preventing DUPE!");
                    ing.setItemCount(item.getReferencePrice());
                }
            }
        });

        return entry;
    }

    public void separateAndSend(int listId, L2PcInstance player, L2Npc npc)
    {
        ListContainer template = _entries.get(listId);
        if(template == null)
        {
            _log.log(Level.WARN, "[MultiSell] can't find list id: " + listId + " requested by player: " + player.getName() + ", npcId:" + (npc != null ? npc.getNpcId() : 0));
            return;
        }

        PreparedListContainer list = new PreparedListContainer(template, template.isShowAll(), player, npc);
        int index = 0;
        do
        {
            // send list at least once even if size = 0
            player.sendPacket(new MultiSellList(list, index));
            index += PAGE_SIZE;
        }
        while(index < list.getEntries().size());

        player.setMultiSell(list);
    }

    private void verify()
    {
        ListContainer list;
        TIntObjectIterator<ListContainer> iter = _entries.iterator();
        while(iter.hasNext())
        {
            iter.advance();
            list = iter.value();

            for(Entry ent : list.getEntries())
            {
                for(Ingredient ing : ent.getIngredients())
                {
                    if(!verifyIngredient(ing))
                    {
                        _log.log(Level.WARN, "[MultiSell] can't find ingredient with itemId: " + ing.getItemId() + " in list: " + list.getListId());
                    }
                }
                for(Ingredient ing : ent.getProducts())
                {
                    if(!verifyIngredient(ing))
                    {
                        _log.log(Level.WARN, "[MultiSell] can't find product with itemId: " + ing.getItemId() + " in list: " + list.getListId());
                    }
                }
            }
        }
    }

    private boolean verifyIngredient(Ingredient ing)
    {
        switch(ing.getItemId())
        {
            case CLAN_REPUTATION:
            case FAME:
            case PC_BANG_POINTS:
                return true;
            default:
                if(ing.getTemplate() != null)
                {
                    return true;
                }
        }

        return false;
    }


    public static boolean checkSpecialIngredient(int id, long amount, L2PcInstance player)
    {
        switch(id)
        {
            case CLAN_REPUTATION:
                if(player.getClan() == null)
                {
                    player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER);
                    break;
                }
                if((player.getClanPrivileges() & L2Clan.CP_CL_TROOPS_FAME) != L2Clan.CP_CL_TROOPS_FAME)
                {
                    player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
                    break;
                }
                if(player.getClan().getReputationScore() < amount)
                {
                    player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    break;
                }
                return true;
            case FAME:
                if(player.getFame() < amount)
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_FAME_POINTS);
                    break;
                }
                return true;
            case PC_BANG_POINTS:
                if(player.getPcBangPoints() < amount)
                {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                    break;
                }
                return true;
        }
        return false;
    }

    public static boolean getSpecialIngredient(int id, long amount, L2PcInstance player)
    {
        switch(id)
        {
            case CLAN_REPUTATION:
                player.getClan().takeReputationScore((int) amount, true);
                player.getClan().broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.C1_PURCHASED_CLAN_ITEM_REDUCING_S2_REPU_POINTS).addCharName(player).addItemNumber(amount));
                return true;
            case FAME:
                player.setFame(player.getFame() - (int) amount);
                player.sendUserInfo();
                return true;
            case PC_BANG_POINTS:
                player.setPcBangPoints(player.getPcBangPoints() - (int) amount);
                return true;
        }
        return false;
    }

    public static void addSpecialProduct(int id, long amount, L2PcInstance player)
    {
        switch(id)
        {
            case CLAN_REPUTATION:
                player.getClan().addReputationScore((int) amount, true);
                break;
            case FAME:
                player.setFame((int) (player.getFame() + amount));
                player.sendUserInfo();
                break;
            case PC_BANG_POINTS:
                player.setPcBangPoints(player.getPcBangPoints() + (int) amount);
                break;
        }
    }
}