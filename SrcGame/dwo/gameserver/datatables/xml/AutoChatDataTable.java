package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.ChatType;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.npc.spawn.SpawnListener;
import dwo.gameserver.network.game.components.NpcStringId;
import dwo.gameserver.network.game.serverpackets.NS;
import dwo.gameserver.util.Rnd;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class AutoChatDataTable extends XmlDocumentParser implements SpawnListener
{
    private static final long DEFAULT_CHAT_DELAY = 10000 + Rnd.get(10000);
    protected static AutoChatDataTable _instance;
    protected final Map<Integer, AutoChatInstance> _registeredChats = new HashMap<>();

    protected AutoChatDataTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }
    
    public static AutoChatDataTable getInstance()
    {
        return _instance == null ? _instance = new AutoChatDataTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _registeredChats.clear();
        parseFile(FilePath.AUTO_CHAT_DATA);
        L2Spawn.addSpawnListener(this);
        _log.log(Level.INFO, getClass().getSimpleName() + ": Loaded " + _registeredChats.size() + " NPC Auto Chats.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("group"))
            {
                final int npcId = Integer.parseInt(element.getAttributeValue("npcId"));
                final long delay = Long.parseLong(element.getAttributeValue("delay"));

                List<Integer> chatText = new ArrayList<>();
                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("text"))
                    {
                        chatText.add(Integer.parseInt(element1.getAttributeValue("val")));
                    }
                }
                registerGlobalChat(npcId, chatText.toArray(new Integer[chatText.size()]), delay);
            }
        }
    }

    public void reload()
    {
        _registeredChats.values().stream().filter(aci -> aci != null).forEach(aci -> 
        {
            if(aci._chatTask != null)
            {
                aci._chatTask.cancel(true);
            }
            removeChat(aci);
        });

        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public int size()
    {
        return _registeredChats.size();
    }

    /**
     * @param npcId
     * @param chatTexts
     * @param chatDelay (-1 = default delay)
     * @return the associated auto chat instance.
     */
    public AutoChatInstance registerGlobalChat(int npcId, Integer[] chatTexts, long chatDelay)
    {
        return registerChat(npcId, null, chatTexts, chatDelay);
    }

    /**
     * Registers a NON globally-active auto chat for the given NPC instance, and adds to the currently
     * assigned chat instance for this NPC ID, otherwise creates a new instance if
     * a previous one is not found.
     * @param npcInst
     * @param chatTexts
     * @param chatDelay (-1 = default delay)
     * @return the associated auto chat instance.
     */
    public AutoChatInstance registerChat(L2Npc npcInst, Integer[] chatTexts, long chatDelay)
    {
        return registerChat(npcInst.getNpcId(), npcInst, chatTexts, chatDelay);
    }

    private AutoChatInstance registerChat(int npcId, L2Npc npcInst, Integer[] chatTexts, long chatDelay)
    {
        AutoChatInstance chatInst;

        if(chatDelay < 0)
        {
            chatDelay = DEFAULT_CHAT_DELAY;
        }

        chatInst = _registeredChats.containsKey(npcId) ? _registeredChats.get(npcId) : new AutoChatInstance(npcId, chatTexts, chatDelay, npcInst == null);

        if(npcInst != null)
        {
            chatInst.addChatDefinition(npcInst);
        }

        _registeredChats.put(npcId, chatInst);

        return chatInst;
    }

    /**
     * Removes and cancels ALL auto chat definition for the given NPC ID,
     * and removes its chat instance if it exists.
     *
     * @param npcId int
     * @return boolean removedSuccessfully
     */
//    public boolean removeChat(int npcId)
//    {
//        AutoChatInstance chatInst = _registeredChats.get(npcId);
//
//        return removeChat(chatInst);
//    }

    /**
     * Removes and cancels ALL auto chats for the given chat instance.
     *
     * @param chatInst AutoChatInstance
     * @return boolean removedSuccessfully
     */
    public boolean removeChat(AutoChatInstance chatInst)
    {
        if(chatInst == null)
        {
            return false;
        }

        _registeredChats.remove(chatInst.getNPCId());
        chatInst.setActive(false);
        return true;
    }

    /**
     * Returns the associated auto chat instance either by the given NPC ID
     * or object ID.
     *
     * @param id
     * @param byObjectId
     * @return AutoChatInstance chatInst
     */
//    public AutoChatInstance getAutoChatInstance(int id, boolean byObjectId)
//    {
//        if(!byObjectId)
//        {
//            return _registeredChats.get(id);
//        }
//        for(AutoChatInstance chatInst : _registeredChats.values())
//        {
//            if(chatInst.getChatDefinition(id) != null)
//            {
//                return chatInst;
//            }
//        }
//
//        return null;
//    }

    /**
     * Sets the active state of all auto chat instances to that specified,
     * and cancels the scheduled chat task if necessary.
     *
     * @param isActive
     */
    public void setAutoChatActive(boolean isActive)
    {
        for(AutoChatInstance chatInst : _registeredChats.values())
        {
            chatInst.setActive(isActive);
        }
    }

    /**
     * Used in conjunction with a SpawnListener, this method is called every time
     * an NPC is spawned in the world.
     * <BR><BR>
     * If an auto chat instance is set to be "global", all instances matching the registered
     * NPC ID will be added to that chat instance.
     */
    @Override
    public void npcSpawned(L2Npc npc)
    {
        synchronized(_registeredChats)
        {
            if(npc == null)
            {
                return;
            }

            int npcId = npc.getNpcId();

            if(_registeredChats.containsKey(npcId))
            {
                AutoChatInstance chatInst = _registeredChats.get(npcId);

                if(chatInst != null && chatInst.isGlobal())
                {
                    chatInst.addChatDefinition(npc);
                }
            }
        }
    }

    /**
     * Auto Chat Instance
     * Manages the auto chat instances for a specific registered NPC ID.
     *
     * @author Tempy
     */
    public class AutoChatInstance
    {
        protected int _npcId;
        protected ScheduledFuture<?> _chatTask;
        private Integer[] _defaultTexts;		private long _defaultDelay = DEFAULT_CHAT_DELAY;
        private boolean _defaultRandom;
        private boolean _globalChat;
        private boolean _isActive;
        private Map<Integer, AutoChatDefinition> _chatDefinitions = new HashMap<>();

        protected AutoChatInstance(int npcId, Integer[] chatTexts, long chatDelay, boolean isGlobal)
        {
            _defaultTexts = chatTexts;
            _npcId = npcId;
            _defaultDelay = chatDelay;
            _globalChat = isGlobal;
            setActive(true);
        }

        protected AutoChatDefinition getChatDefinition(int objectId)
        {
            return _chatDefinitions.get(objectId);
        }

        protected Collection<AutoChatDefinition> getChatDefinitions()
        {
            return _chatDefinitions.values();
        }

        /**
         * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID,
         * and launches the scheduled chat task.
         * <BR>
         * Returns the object ID for the NPC instance, with which to refer
         * to the created chat definition.
         * <BR>
         * <B>Note</B>: Uses pre-defined default values for texts and chat delays from the chat instance.
         *
         * @param npcInst
         * @return int objectId
         */
        public int addChatDefinition(L2Npc npcInst)
        {
            return addChatDefinition(npcInst, null, 0);
        }

        /**
         * Defines an auto chat for an instance matching this auto chat instance's registered NPC ID,
         * and launches the scheduled chat task.
         * <BR>
         * Returns the object ID for the NPC instance, with which to refer
         * to the created chat definition.
         *
         * @param npcInst
         * @param chatTexts
         * @param chatDelay
         * @return int objectId
         */
        public int addChatDefinition(L2Npc npcInst, Integer[] chatTexts, long chatDelay)
        {
            int objectId = npcInst.getObjectId();
            AutoChatDefinition chatDef = new AutoChatDefinition(this, npcInst, chatTexts, chatDelay);
            chatDef.setRandomChat(true);
            _chatDefinitions.put(objectId, chatDef);
            return objectId;
        }

        /**
         * Removes a chat definition specified by the given object ID.
         *
         * @param objectId
         * @return boolean removedSuccessfully
         */
//        public boolean removeChatDefinition(int objectId)
//        {
//            if(!_chatDefinitions.containsKey(objectId))
//            {
//                return false;
//            }
//
//            AutoChatDefinition chatDefinition = _chatDefinitions.get(objectId);
//            chatDefinition.setActive(false);
//
//            _chatDefinitions.remove(objectId);
//
//            return true;
//        }

        /**
         * Tests if this auto chat instance is active.
         *
         * @return boolean isActive
         */
        public boolean isActive()
        {
            return _isActive;
        }

        /**
         * Sets the activity of ALL auto chat definitions handled by this chat instance.
         *
         * @param activeValue
         */
        public void setActive(boolean activeValue)
        {
            if(_isActive == activeValue)
            {
                return;
            }

            _isActive = activeValue;

            if(!_globalChat)
            {
                for(AutoChatDefinition chatDefinition : _chatDefinitions.values())
                {
                    chatDefinition.setActive(activeValue);
                }
                return;
            }

            if(_isActive)
            {
                AutoChatRunner acr = new AutoChatRunner(_npcId, -1);
                _chatTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, _defaultDelay, _defaultDelay);
            }
            else
            {
                _chatTask.cancel(false);
            }
        }

        /**
         * Tests if this auto chat instance applies to
         * ALL currently spawned instances of the registered NPC ID.
         *
         * @return boolean isGlobal
         */
        public boolean isGlobal()
        {
            return _globalChat;
        }

        /**
         * Tests if random order is the DEFAULT for new chat definitions.
         *
         * @return boolean isRandom
         */
        public boolean isDefaultRandom()
        {
            return _defaultRandom;
        }

        public void setDefaultRandom(boolean randValue)
        {
            _defaultRandom = randValue;
        }

        /**
         * Tests if the auto chat definition given by its object ID is set to be random.
         *
         * @return boolean isRandom
         */
//        public boolean isRandomChat(int objectId)
//        {
//            return _chatDefinitions.containsKey(objectId) && _chatDefinitions.get(objectId).isRandomChat();
//        }

        /**
         * @return the ID of the NPC type managed by this auto chat instance.
         */
        public int getNPCId()
        {
            return _npcId;
        }

        /**
         * Returns the number of auto chat definitions stored for this instance.
         *
         * @return int definitionCount
         */
//        public int getDefinitionCount()
//        {
//            return _chatDefinitions.size();
//        }

        /**
         * Returns a list of all NPC instances handled by this auto chat instance.
         *
         * @return L2NpcInstance[] npcInsts
         */
//        public L2Npc[] getNPCInstanceList()
//        {
//            Collection<AutoChatDefinition> values = _chatDefinitions.values();
//            ArrayList<L2Npc> npcInsts = new ArrayList<>(values.size());
//
//            npcInsts.addAll(values.stream().map(chatDefinition -> chatDefinition._npcInstance).collect(Collectors.toList()));
//
//            return npcInsts.toArray(new L2Npc[npcInsts.size()]);
//        }

        /**
         * A series of methods used to get and set default values for new chat definitions.
         */
        public long getDefaultDelay()
        {
            return _defaultDelay;
        }

        public Integer[] getDefaultTexts()
        {
            return _defaultTexts;
        }

//        public void setDefaultChatDelay(long delayValue)
//        {
//            _defaultDelay = delayValue;
//        }

//        public void setDefaultChatTexts(Integer[] textsValue)
//        {
//            _defaultTexts = textsValue;
//        }

        /**
         * Sets a specific chat delay for the specified auto chat definition given by its object ID.
         *
         * @param objectId
         * @param delayValue
         */
//        public void setChatDelay(int objectId, long delayValue)
//        {
//            AutoChatDefinition chatDef = getChatDefinition(objectId);
//
//            if(chatDef != null)
//            {
//                chatDef.setChatDelay(delayValue);
//            }
//        }

        /**
         * Sets a specific set of chat texts for the specified auto chat definition given by its object ID.
         *
         * @param objectId
         * @param textsValue
         */
//        public void setChatTexts(int objectId, Integer[] textsValue)
//        {
//            AutoChatDefinition chatDef = getChatDefinition(objectId);
//
//            if(chatDef != null)
//            {
//                chatDef.setChatTexts(textsValue);
//            }
//        }

        /**
         * Sets specifically to use random chat order for the auto chat definition given by its object ID.
         *
         * @param objectId
         * @param randValue
         */
//        public void setRandomChat(int objectId, boolean randValue)
//        {
//            AutoChatDefinition chatDef = getChatDefinition(objectId);
//
//            if(chatDef != null)
//            {
//                chatDef.setRandomChat(randValue);
//            }
//        }

        /**
         * Auto Chat Definition
         * Stores information about specific chat data for an instance of the NPC ID
         * specified by the containing auto chat instance.
         * <BR>
         * Each NPC instance of this type should be stored in a subsequent AutoChatDefinition class.
         *
         * @author Tempy
         */
        private class AutoChatDefinition
        {
            protected int _chatIndex;
            protected L2Npc _npcInstance;

            protected AutoChatInstance _chatInstance;

            private long _chatDelay;
            private Integer[] _chatTexts;
            private boolean _isActiveDefinition;
            private boolean _randomChat;

            protected AutoChatDefinition(AutoChatInstance chatInst, L2Npc npcInst, Integer[] chatTexts, long chatDelay)
            {
                _npcInstance = npcInst;

                _chatInstance = chatInst;
                _randomChat = chatInst.isDefaultRandom();

                _chatDelay = chatDelay;
                _chatTexts = chatTexts;

                // If global chat isn't enabled for the parent instance,
                // then handle the chat task locally.
                if(!chatInst.isGlobal())
                {
                    setActive(true);
                }
            }

            protected AutoChatDefinition(AutoChatInstance chatInst, L2Npc npcInst)
            {
                this(chatInst, npcInst, null, -1);
            }

            protected Integer[] getChatTexts()
            {
                return _chatTexts != null ? _chatTexts : _chatInstance.getDefaultTexts();
            }

//            void setChatTexts(Integer[] textsValue)
//            {
//                _chatTexts = textsValue;
//            }

            private long getChatDelay()
            {
                return _chatDelay > 0 ? _chatDelay : _chatInstance.getDefaultDelay();
            }

//            void setChatDelay(long delayValue)
//            {
//                _chatDelay = delayValue;
//            }

//            private boolean isActive()
//            {
//                return _isActiveDefinition;
//            }

            void setActive(boolean activeValue)
            {
                if(_isActiveDefinition == activeValue)
                {
                    return;
                }

                if(activeValue)
                {
                    AutoChatRunner acr = new AutoChatRunner(_npcId, _npcInstance.getObjectId());
                    _chatTask = getChatDelay() == 0 ? ThreadPoolManager.getInstance().scheduleGeneral(acr, 5) : ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(acr, getChatDelay(), getChatDelay());
                }
                else
                {
                    _chatTask.cancel(false);
                }

                _isActiveDefinition = activeValue;
            }

            boolean isRandomChat()
            {
                return _randomChat;
            }

            void setRandomChat(boolean randValue)
            {
                _randomChat = randValue;
            }
        }

        /**
         * Auto Chat Runner
         * Represents the auto chat scheduled task for each chat instance.
         *
         * @author Tempy
         */
        private class AutoChatRunner implements Runnable
        {
            private int _runnerNpcId;
            private int _objectId;

            protected AutoChatRunner(int pNpcId, int pObjectId)
            {
                _runnerNpcId = pNpcId;
                _objectId = pObjectId;
            }

            @Override
            public void run()
            {
                synchronized(this)
                {
                    AutoChatInstance chatInst = _registeredChats.get(_runnerNpcId);
                    Collection<AutoChatDefinition> chatDefinitions = new ArrayList<>();

                    if(chatInst.isGlobal())
                    {
                        chatDefinitions = chatInst.getChatDefinitions();
                    }
                    else
                    {
                        AutoChatDefinition chatDef = chatInst.getChatDefinition(_objectId);

                        if(chatDef == null)
                        {
                            _log.log(Level.WARN, "AutoChatHandler: Auto chat definition is NULL for NPC ID " + _npcId + '.');
                            return;
                        }
                        chatDefinitions.add(chatDef);
                    }

                    for(AutoChatDefinition chatDef : chatDefinitions)
                    {
                        try
                        {
                            L2Npc chatNpc = chatDef._npcInstance;
                            List<L2PcInstance> nearbyPlayers = new ArrayList<>();

                            for(L2Character character : chatNpc.getKnownList().getKnownCharactersInRadius(1500))
                            {
                                if(!character.isPlayer())
                                {
                                    continue;
                                }
                                nearbyPlayers.add((L2PcInstance) character);
                            }

                            int maxIndex = chatDef.getChatTexts().length;
                            int lastIndex = Rnd.get(maxIndex);

                            Integer text;

                            if(!chatDef.isRandomChat())
                            {
                                lastIndex = chatDef._chatIndex + 1;

                                if(lastIndex == maxIndex)
                                {
                                    lastIndex = 0;
                                }

                                chatDef._chatIndex = lastIndex;
                            }

                            text = chatDef.getChatTexts()[lastIndex];

                            if(text == null)
                            {
                                return;
                            }

                            NS cs = new NS(chatNpc.getObjectId(), ChatType.NPC_ALL, chatNpc.getNpcId(), NpcStringId.getNpcStringId(text));

                            for(L2PcInstance nearbyPlayer : nearbyPlayers)
                            {
                                nearbyPlayer.sendPacket(cs);
                            }
                        }
                        catch(Exception e)
                        {
                            _log.log(Level.ERROR, "", e);
                            return;
                        }
                    }
                }
            }
        }



    }
}