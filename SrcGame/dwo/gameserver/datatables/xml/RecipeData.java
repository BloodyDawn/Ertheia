package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemChanceHolder;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.ItemTable;
import dwo.gameserver.model.items.base.instance.L2ItemInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.Inventory;
import dwo.gameserver.model.player.L2ManufactureItem;
import dwo.gameserver.model.player.L2RecipeList;
import dwo.gameserver.model.player.PlayerPrivateStoreType;
import dwo.gameserver.model.skills.base.L2Skill;
import dwo.gameserver.model.skills.stats.Stats;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.ItemList;
import dwo.gameserver.network.game.serverpackets.StatusUpdate;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeBookItemList;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeItemMakeInfo;
import dwo.gameserver.network.game.serverpackets.packet.recipeshop.RecipeShopItemInfo;
import dwo.gameserver.taskmanager.manager.AttackStanceTaskManager;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeData extends XmlDocumentParser
{
    private static final Map<Integer, L2RecipeList> _lists = new HashMap<>();

    protected static RecipeData instance;

    private RecipeData()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static RecipeData getInstance()
    {
        return instance == null ? instance = new RecipeData() : instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _lists.clear();
        parseFile(FilePath.RECIPE_DATA);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _lists.size() + " recipes.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        List<ItemHolder> recipeIndegrientList = new ArrayList<>();
        List<ItemChanceHolder> recipeProductList = new ArrayList<>();

        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("item"))
            {
                String att;
                recipeIndegrientList.clear();
                recipeProductList.clear();
                int id;
                StatsSet set = new StatsSet();

                att = element.getAttributeValue("id");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing id for recipe item, skipping");
                    continue;
                }
                id = Integer.parseInt(att);
                set.set("id", id);

                att = element.getAttributeValue("recipeId");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing recipeId for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("recipeId", Integer.parseInt(att));

                att = element.getAttributeValue("name");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing name for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("recipeName", att);

                att = element.getAttributeValue("craftLevel");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing level for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("craftLevel", Integer.parseInt(att));

                att = element.getAttributeValue("type");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing type for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("isDwarvenRecipe", att.equalsIgnoreCase("dwarven"));

                att = element.getAttributeValue("mpConsume");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing mpConsume for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("mpConsume", Integer.parseInt(att));

                att = element.getAttributeValue("successRate");
                if(att == null)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Missing successRate for recipe item id: " + id + ", skipping");
                    continue;
                }
                set.set("successRate", Integer.parseInt(att));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("ingredient"))
                    {
                        recipeIndegrientList.add(new ItemHolder(Integer.parseInt(element1.getAttributeValue("id")),
                                Integer.parseInt(element1.getAttributeValue("count"))));
                    }
                    else if(name1.equalsIgnoreCase("production"))
                    {
                        recipeProductList.add(new ItemChanceHolder(Integer.parseInt(element1.getAttributeValue("id")),
                                Integer.parseInt(element1.getAttributeValue("count")),
                                Integer.parseInt(element1.getAttributeValue("chance"))));
                    }
                }

                set.set("successRate", Integer.parseInt(att));

                L2RecipeList recipeList = new L2RecipeList(set);
                recipeIndegrientList.forEach(recipeList::addRecipe);
                recipeProductList.forEach(recipeList::addProduct);
                _lists.put(id, recipeList);
            }
        }
    }

    public L2RecipeList getRecipeList(int listId)
    {
        return _lists.get(listId);
    }

    public L2RecipeList getRecipeByItemId(int itemId)
    {
        for(L2RecipeList find : _lists.values())
        {
            if(find.getRecipeId() == itemId)
            {
                return find;
            }
        }
        return null;
    }

    public void requestBookOpen(L2PcInstance player, boolean isDwarvenCraft)
    {
        synchronized(this)
        {
            RecipeBookItemList response = new RecipeBookItemList(isDwarvenCraft, player.getMaxMp());
            response.addRecipes(isDwarvenCraft ? player.getRecipeController().getDwarvenRecipeBook().values() : player.getRecipeController().getCommonRecipeBook().values());
            player.sendPacket(response);
        }
    }

    public void requestManufactureItem(L2PcInstance manufacturer, int recipeListId, L2PcInstance player)
    {
        synchronized(this)
        {
            L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

            if(recipeList == null)
            {
                return;
            }

            if(!manufacturer.getRecipeController().hasRecipe(recipeList) && !manufacturer.getRecipeController().hasRecipe(recipeList))
            {
                Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
                return;
            }

            RecipeItemMaker maker = new RecipeItemMaker(manufacturer, recipeList, player);
            if(maker._isValid)
            {
                maker.run();
            }
        }
    }

    public void requestMakeItem(L2PcInstance player, int recipeListId)
    {
        synchronized(this)
        {
            if(AttackStanceTaskManager.getInstance().hasAttackStanceTask(player) || player.isInDuel())
            {
                player.sendPacket(SystemMessageId.CANT_OPERATE_PRIVATE_STORE_DURING_COMBAT);
                return;
            }

            L2RecipeList recipeList = getValidRecipeList(player, recipeListId);

            if(recipeList == null)
            {
                return;
            }

            if(!player.getRecipeController().hasRecipe(recipeList) && !player.getRecipeController().hasRecipe(recipeList))
            {
                Util.handleIllegalPlayerAction(player, "Warning!! Character " + player.getName() + " of account " + player.getAccountName() + " sent a false recipe id.", Config.DEFAULT_PUNISH);
                return;
            }

            RecipeItemMaker maker = new RecipeItemMaker(player, recipeList, player);
            if(maker._isValid)
            {
                maker.run();
            }
        }
    }

    private L2RecipeList getValidRecipeList(L2PcInstance player, int id)
    {
        L2RecipeList recipeList = getRecipeList(id);

        if(recipeList == null || recipeList.getRecipes().length == 0)
        {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_AUTHORIZED_REGISTER_RECIPE));
            player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
            return null;
        }
        return recipeList;
    }

    private class RecipeItemMaker implements Runnable
    {
        protected final L2RecipeList _recipeList;
        protected final L2PcInstance _player; // "crafter"
        protected final L2PcInstance _target; // "customer"
        protected final L2Skill _skill;
        protected final int _skillId;
        protected final int _skillLevel;
        protected boolean _isValid;
        protected List<ItemHolder> _items;
        protected long _price;
        protected int _totalItems;
        protected int _materialsRefPrice;

        public RecipeItemMaker(L2PcInstance pPlayer, L2RecipeList pRecipeList, L2PcInstance pTarget)
        {
            _player = pPlayer;
            _target = pTarget;
            _recipeList = pRecipeList;

            _isValid = false;
            _skillId = _recipeList.isDwarvenRecipe() ? L2Skill.SKILL_CREATE_DWARVEN : L2Skill.SKILL_CREATE_COMMON;
            _skillLevel = _player.getSkillLevel(_skillId);
            _skill = _player.getKnownSkill(_skillId);

            _player.setPrivateStoreType(PlayerPrivateStoreType.MANUFACTURE);

            if(_player.isAlikeDead() || _player.isDead())
            {
                _player.sendActionFailed();
                abort();
                return;
            }

            if(_target.isAlikeDead() || _target.isDead())
            {
                _target.sendActionFailed();
                abort();
                return;
            }

            if(_target.isProcessingTransaction())
            {
                _target.sendActionFailed();
                abort();
                return;
            }

            if(_player.isProcessingTransaction())
            {
                _player.sendActionFailed();
                abort();
                return;
            }

            // validate recipe list
            if(_recipeList.getRecipes().length == 0)
            {
                _player.sendActionFailed();
                abort();
                return;
            }

            // validate skill level
            if(_recipeList.getLevel() > _skillLevel)
            {
                _player.sendActionFailed();
                abort();
                return;
            }

            // check that customer can afford to pay for creation services
            if(!_player.equals(_target))
            {
                for(L2ManufactureItem temp : _player.getCreateList().getList())
                {
                    if(temp.getRecipeId() == _recipeList.getId()) // find recipe for item we want manufactured
                    {
                        _price = temp.getCost();
                        if(_target.getAdenaCount() < _price) // check price
                        {
                            _target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                            abort();
                            return;
                        }
                        break;
                    }
                }
            }

            // make temporary items
            if((_items = listItems(false)) == null)
            {
                abort();
                return;
            }

            // calculate reference price
            for(ItemHolder i : _items)
            {
                _materialsRefPrice += ItemTable.getInstance().getTemplate(i.getId()).getReferencePrice() * i.getCount();
                _totalItems += i.getCount();
            }

            updateMakeInfo(true);
            updateCurMp();
            updateCurLoad();

            _player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
            _isValid = true;
        }

        @Override
        public void run()
        {
            if(!Config.IS_CRAFTING_ENABLED)
            {
                _target.sendMessage("Создание предметов отключено Администрацией.");
                abort();
                return;
            }

            if(_player == null || _target == null)
            {
                _log.log(Level.WARN, getClass().getSimpleName() + ": player or target == null (disconnected?), aborting" + _target + _player);
                abort();
                return;
            }

            if(!_player.isOnline() || !_target.isOnline())
            {
                _log.log(Level.WARN, getClass().getSimpleName() + ": player or target is not online, aborting " + _target + _player);
                abort();
                return;
            }
            finishCrafting();
        }

        private void finishCrafting()
        {
            // Забираем ману
            if(_player.getCurrentMp() < _recipeList.getMpConsume())
            {
                _player.sendPacket(SystemMessageId.NOT_ENOUGH_MP);
                abort();
                return;
            }

            _player.getStatus().reduceMp(_recipeList.getMpConsume());

            // first take adena for manufacture
            if(!_target.equals(_player) && _price > 0) // customer must pay for services
            {
                // attempt to pay for item
                L2ItemInstance adenatransfer = _target.transferItem(ProcessType.RECIPE, _target.getInventory().getAdenaInstance().getObjectId(), _price, _player.getInventory(), _player);

                if(adenatransfer == null)
                {
                    _target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
                    abort();
                    return;
                }
            }

            int minLuc = _player.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().min().getLuc();
            int maxLuc = _player.getBaseTemplate().getBaseCharTemplate().getDefaultAttributes().max().getLuc();
            int luc = Math.min(maxLuc, Math.max(minLuc, _player.getBaseTemplate().getBaseLUC() + _player.getHennaStatLUC()));

            if((_items = listItems(true)) == null) // this line actually takes materials from inventory
            { // handle possible cheaters here
                // (they click craft then try to get rid of items in order to get free craft)
            }

            else if(Rnd.getChance(_recipeList.getRecipeSuccessRate() * Rnd.get(luc)))
            {
                rewardPlayer(); // and immediately puts created item in its place
                updateMakeInfo(true);
            }
            else
            {
                if(_target.equals(_player))
                {
                    _target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED);
                }
                else
                {
                    _player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_C1_AT_S3_ADENA_FAILED).addString(_target.getName()).addItemName(_recipeList.getProducts().getFirst().getId()).addItemNumber(_price));
                    _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_FAILED_TO_CREATE_S2_FOR_S3_ADENA).addString(_player.getName()).addItemName(_recipeList.getProducts().getFirst().getId()).addItemNumber(_price));
                }
                updateMakeInfo(false);
            }
            // update load and mana bar of craft window
            updateCurMp();
            updateCurLoad();
            _player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
            _target.sendPacket(new ItemList(_target, false));
        }

        private void updateMakeInfo(boolean success)
        {
            if(_target.equals(_player))
            {
                _target.sendPacket(new RecipeItemMakeInfo(_recipeList.getId(), _target, success));
            }
            else
            {
                _target.sendPacket(new RecipeShopItemInfo(_player, _recipeList.getId()));
            }
        }

        private void updateCurLoad()
        {
            StatusUpdate su = new StatusUpdate(_target);
            su.addAttribute(StatusUpdate.CUR_LOAD, _target.getCurrentLoad());
            _target.sendPacket(su);
        }

        private void updateCurMp()
        {
            StatusUpdate su = new StatusUpdate(_target);
            su.addAttribute(StatusUpdate.CUR_MP, (int) _target.getCurrentMp());
            _target.sendPacket(su);
        }

        private List<ItemHolder> listItems(boolean remove)
        {
            ItemHolder[] recipes = _recipeList.getRecipes();
            Inventory inv = _target.getInventory();
            List<ItemHolder> materials = new ArrayList<>();

            for(ItemHolder recipe : recipes)
            {
                long quantity = recipe.getCount();

                if(recipe.getCount() > 0)
                {
                    L2ItemInstance item = inv.getItemByItemId(recipe.getId());
                    long itemQuantityAmount = item == null ? 0 : item.getCount();

                    // check materials
                    if(itemQuantityAmount < quantity)
                    {
                        _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE).addItemName(recipe.getId()).addItemNumber(quantity - itemQuantityAmount));
                        abort();
                        return null;
                    }

                    // make new temporary object, just for counting purposes
                    ItemHolder temp = new ItemHolder(item.getItemId(), quantity);
                    materials.add(temp);
                }
            }

            if(remove)
            {
                for(ItemHolder tmp : materials)
                {
                    inv.destroyItemByItemId(ProcessType.MANUFACTURE, tmp.getId(), tmp.getCount(), _target, _player);

                    if(tmp.getCount() > 1)
                    {
                        _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(tmp.getId()).addItemNumber(tmp.getCount()));
                    }
                    else
                    {
                        _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(tmp.getId()));
                    }
                }
            }
            return materials;
        }

        private void abort()
        {
            updateMakeInfo(false);
            _player.setPrivateStoreType(PlayerPrivateStoreType.NONE);
        }

        private void rewardPlayer()
        {
            int itemId = -1, itemCount = -1;

            // Считаем продукты рецепта
            double rndNum = 100 * Rnd.nextDouble(), chance, chanceFrom = 0;
            for(ItemChanceHolder product : _recipeList.getProducts())
            {
                chance = product.getChance();
                if(rndNum >= chanceFrom && rndNum <= chance + chanceFrom)
                {
                    itemId = product.getId();
                    itemCount = product.getCount();

                    // TODO: Возможно, при дабл крафте должно быть какое-либо сообщение
                    if((int) _player.calcStat(Stats.CRAFT_MASTERY, 0, null, null) > 0 && Rnd.getChance(1))
                    {
                        itemCount <<= 1;
                    }
                    break;
                }
                chanceFrom += chance;
            }

            // Выдаем предмет
            _target.getInventory().addItem(ProcessType.MANUFACTURE, itemId, itemCount, _target, _player);

            HookManager.getInstance().notifyEvent(HookType.ON_ITEM_CRAFTED, null, itemId, _player);

            if(_target != _player)
            {
                // inform manufacturer of earned profit
                if(itemCount == 1)
                {
                    _player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_C1_FOR_S3_ADENA).addString(_target.getName()).addItemName(itemId).addItemNumber(_price));
                    _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_FOR_S3_ADENA).addString(_player.getName()).addItemName(itemId).addItemNumber(_price));
                }
                else
                {
                    _player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_C1_FOR_S4_ADENA).addString(_target.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(_price));
                    _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_CREATED_S2_S3_S_FOR_S4_ADENA).addString(_player.getName()).addNumber(itemCount).addItemName(itemId).addItemNumber(_price));
                }
            }

            if(itemCount > 1)
            {
                _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(itemCount));
            }
            else
            {
                _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId));
            }
            updateMakeInfo(true); // success
        }
    }
}