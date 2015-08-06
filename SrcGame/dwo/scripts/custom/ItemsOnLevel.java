package dwo.scripts.custom;

import dwo.config.FilePath;
import dwo.config.events.ConfigEvents;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.holders.ItemHolder;
import dwo.gameserver.model.items.base.L2PremiumItem;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.items.itemcontainer.PcFreight;
import dwo.gameserver.model.player.mail.MailMessage;
import dwo.gameserver.model.skills.stats.StatsSet;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExGoodsInventoryChangedNoti;
import org.apache.log4j.Level;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ItemsOnLevel extends Quest
{
    private static final List<LevelContent> _levelData = new ArrayList<>();

    public ItemsOnLevel(String name, String desc)
    {
        super(name, desc);
        addEventId(HookType.ON_LEVEL_INCREASE);
        load();
    }

    public static void main(String[] args)
    {
        if(ConfigEvents.ITEMS_ON_LEVEL_ENABLE)
        {
            new ItemsOnLevel(ItemsOnLevel.class.getSimpleName(), "custom");
        }
    }

    public void load()
    {
        _levelData.clear();
        new Parser();
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _levelData.size() + " Level Surveys");
    }

    @Override
    public void onLevelIncreased(L2PcInstance player)
    {
        _levelData.stream().filter(content -> player.getLevel() >= content.getLevel()).forEach(content -> {
            if(!player.getVariablesController().get("IoL-" + content.getLevel(), Boolean.class, false))
            {
                if(content.getAward().equalsIgnoreCase("mail"))
                {
                    MailMessage msg = new MailMessage(player.getObjectId(), content.getTitle(), content.getContent(), "Подарок");
                    msg.createAttachments();
                    for(ItemHolder holder : content.getItems())
                    {
                        msg.getAttachments().addItem(ProcessType.ITEMS_ON_LEVEL, holder.getId(), holder.getCount(), null, null);
                    }
                    msg.sendMessage();
                }
                else if(content.getAward().equalsIgnoreCase("freight"))
                {
                    PcFreight freight = player.getFreight();
                    for(ItemHolder holder : content.getItems())
                    {
                        freight.addItem(ProcessType.ITEMS_ON_LEVEL, holder.getId(), holder.getCount(), null, null);
                    }
                }
                else if(content.getAward().equalsIgnoreCase("premium"))
                {
                    for(ItemHolder holder : content.getItems())
                    {
                        player.addPremiumItem(new L2PremiumItem(holder.getId(), holder.getCount(), content.getTitle(), content.getContent()));
                    }
                    player.sendPacket(new ExGoodsInventoryChangedNoti());
                }
                else if(content.getAward().equalsIgnoreCase("WareHouse"))
                {

                }
                player.getVariablesController().set("IoL-" + content.getLevel(), true);
            }
        });
    }

    @Override
    public boolean unload(boolean removeFromList)
    {
        HookManager.getInstance().removeHook(HookType.ON_LEVEL_INCREASE, this);
        _levelData.clear();
        return super.unload(removeFromList);
    }

    public static class LevelContent
    {
        private final int _level;
        private final List<ItemHolder> _items;
        private final String _title;
        private final String _content;
        private final String _award;

        public LevelContent(StatsSet set)
        {
            _level = set.getInteger("level");
            _items = new ArrayList<>();
            _title = set.getString("title");
            _content = set.getString("content");
            _award = set.getString("award");
        }

        public int getLevel()
        {
            return _level;
        }

        public boolean addItem(ItemHolder holder)
        {
            return _items.add(holder);
        }

        public List<ItemHolder> getItems()
        {
            return _items;
        }

        public String getTitle()
        {
            return _title;
        }

        public String getContent()
        {
            return _content;
        }

        public String getAward()
        {
            return _award;
        }
    }

    private class Parser extends XmlDocumentParser
    {
        public Parser()
        {
            try {
                load();
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void load() throws JDOMException, IOException {
            _levelData.clear();
            parseFile(FilePath.ITEMS_ON_LEVEL_DATA);
        }

        @Override
        protected void parseDocument(Element rootElement)
        {
            StatsSet set;
            LevelContent content;
            for(Element element : rootElement.getChildren())
            {
                final String name = element.getName();
                if(name.equalsIgnoreCase("survey"))
                {
                    set = new StatsSet();
                    final List<Attribute> attributes = element.getAttributes();
                    for(final Attribute attribute : attributes)
                    {
                        set.set(attribute.getName(), attribute.getValue());
                    }
                    content = new LevelContent(set);
                    for(Element element1 : element.getChildren())
                    {
                        final String name1 = element1.getName();
                        if(name1.equals("item"))
                        {
                            int itemId = Integer.parseInt(element1.getAttributeValue("id"));
                            int itemCount = Integer.parseInt(element1.getAttributeValue("count"));
                            content.addItem(new ItemHolder(itemId, itemCount));
                        }
                    }
                    _levelData.add(content);
                }
            }
        }
    }
}