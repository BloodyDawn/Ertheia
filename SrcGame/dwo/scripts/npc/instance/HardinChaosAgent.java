package dwo.scripts.npc.instance;

import dwo.gameserver.cache.HtmCache;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.base.ClassId;
import dwo.gameserver.model.player.base.ClassLevel;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Util;
import org.apache.log4j.Level;

/**
 * User: GenCloud
 * Date: 05.04.2015
 * Team: La2Era Team
 * TODO: дописать уловия, уточнить забираються скилы или нет, уточнить работу самого хардина "где, когда и что"
 */
public class HardinChaosAgent extends Quest
{
    private static final int АгентХаосаХардин = 33870;
    //private int var;

    public HardinChaosAgent()
    {
        super();
        addFirstTalkId(АгентХаосаХардин);
        addAskId(АгентХаосаХардин, 33870);
    }

    public static void main(String[] args)
    {
        new HardinChaosAgent();
    }

    @Override
    public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
    {
        ClassId classId = player.getClassId();
        String content;

        if (ask == 33870)
        {
            if (reply == 1)
            {
                return npc.getNpcId() + "-reawake.htm";
            }
            
            if (reply == 2)
            {   
                content = HtmCache.getInstance().getHtm(player.getLang(), "default/33870-reawake_list.htm");
                StringBuilder cl = new StringBuilder();

                for (ClassId clId : ClassId.values())
                {
                    if(player.getClassId().level() != ClassLevel.AWAKEN.ordinal())
                    {
                        continue;
                    }
                    
                    if (!clId.isOfType2(classId.getType2()))
                    {
                        continue;
                    }

                    cl.append("<button value=\"");
                    cl.append(clId.getName());
                    cl.append("\" action=\"bypass -h menu_select?ask=33870&reply=");
                    cl.append(String.valueOf(clId.getId()));
                    cl.append("\" width=\"200\" height=\"31\" back=\"L2UI_CT1.HtmlWnd_DF_Awake_Down\" fore=\"L2UI_CT1.HtmlWnd_DF_Awake\"><br>");
                }

                content = content.replace("<?class_list?>", cl.toString());
                return content;
            }
            
            if (reply >= 148 && reply <= 181)
            {
                ClassId reAwakeId = ClassId.values()[reply];

                final int classToAwakening = Util.getAwakenRelativeClass(classId.getId());
                
                if (classToAwakening < 0)
                {
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Error while getAwakenRelativeClass()! Player: [" + player.getName() + "] trying to awake from ID: [" + classId + "] SubclassIndex: [" + (player.isSubClassActive() ? player.getSubclass().getClassIndex() + "]" : "no subclass]"));
                    return null;
                }
                
                if(!reAwakeId.isOfType2(classId.getType2())) 
                {                    
                    _log.log(Level.ERROR, getClass().getSimpleName() + ": Player: [" + player.getName() + "] trying to awake from ID: [" + classId + "] SubclassIndex: [" + (player.isSubClassActive() ? player.getSubclass().getClassIndex() + "]" : "no subclass]"));
                    return null;
                }

                if(player.getLevel() >= 85 && (!player.isSubClassActive() || player.getSubclass().isDualClass()) && player.getClassId().level() == ClassLevel.THIRD.ordinal()) 
                {
                    player.getVariablesController().set("CHAOS_REAWAKE", 1);

                    player.setClassId(reAwakeId.getId());

                    if(player.isSubClassActive())
                    {
                        player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClassId());
                    }
                    else
                    {
                        player.setBaseClassId(player.getActiveClassId());
                    }
                    
                    doAwake(player);
                }
                else
                {
                    _log.log(Level.WARN, "Player " + player.getName() + " trying cheating with Awakening!");
                }
            }
        }
        
        return super.onAsk(player, npc, ask, reply);
    }

    @Override
    public String onFirstTalk(L2Npc npc, L2PcInstance player)
    {        
        return "33870.htm";
    }

    public void doAwake(L2PcInstance player)
    {
        player.rewardSkills();
        player.broadcastUserInfo();
//        deleteNonAwakedSkills(player);
//        deleteRestrictedItems(player);
//        giveItems(player);
//        sendSocialAction(player);
    }

//    private boolean checkCond(L2PcInstance player)
//    {
//        var = player.getVariablesController().get("CHAOS_REAWAKE", Integer.class, 0);
//
//        if(player.getClassId().level() != ClassLevel.AWAKEN.ordinal())
//        {
//            showHtmlFile(player, "default/33870-no.htm", false);
//            return false;
//        }
//
//        if(var != 1)
//        {
//            if(player.isBaseClassActive())
//            {
//                if(ItemFunctions.getItemCount(player, AwakeningManagerInstance.CHAOS_ESSENCE) == 0)
//                {
//                    showChatWindow(player, "default/" + getNpcId() + "-no_already_reawakened.htm");
//                    return false;
//                }
//            }
//            else if(player.isDualClassActive())
//            {
//                if(ItemFunctions.getItemCount(player, AwakeningManagerInstance.CHAOS_ESSENCE) > 0 || ItemFunctions.getItemCount(player, AwakeningManagerInstance.CHAOS_ESSENCE_DUAL_CLASS) == 0)
//                {
//                    showChatWindow(player, "default/" + getNpcId() + "-no_already_reawakened.htm");
//                    return false;
//                }
//            }
//            else
//            {
//                showChatWindow(player, "default/" + getNpcId() + "-no_already_reawakened.htm");
//                return false;
//            }
//        }
//
//        if(player.getServitors().length > 0)
//        {
//            showChatWindow(player, "default/" + getNpcId() + "-no_summon.htm");
//            return false;
//        }
//
//        return true;
//    }
}
