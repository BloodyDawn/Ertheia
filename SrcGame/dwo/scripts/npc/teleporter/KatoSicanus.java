package dwo.scripts.npc.teleporter;

import dwo.gameserver.instancemanager.GrandBossManager;
import dwo.gameserver.instancemanager.QuestManager;
import dwo.gameserver.instancemanager.ZoneManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Summon;
import dwo.gameserver.model.actor.instance.L2GrandBossInstance;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.player.formation.group.L2Party;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.zone.type.L2BossZone;
import dwo.gameserver.network.game.components.SystemMessageId;
import dwo.gameserver.network.game.serverpackets.SystemMessage;
import dwo.gameserver.util.Rnd;
import dwo.gameserver.util.Util;
import dwo.scripts.ai.individual.raidbosses.Lindvior;

/**
 * User: GenCloud
 * Date: 06.01.2015
 * Team: DWO
 */
public class KatoSicanus extends Quest
{
    // НПЦ
    private static final int KATO_SICANUS = 33881;
    private static final int LINDVIOR_RB = 25899;
    private final static int ZONE_ID = 12015;

    public KatoSicanus()
    {
        addTeleportRequestId(KATO_SICANUS);
    }

    public static void main(String[] args)
    {
        new KatoSicanus();
    }

    @Override
    public String onTeleportRequest(L2Npc npc, L2PcInstance player)
    {
        if(Lindvior.getInstance() != null)
        {
            int status = GrandBossManager.getInstance().getBossStatus(LINDVIOR_RB);

            if(status == 1 || status == 2)
            {
                return "33881-2.htm";
            }
            else if(status == 3)
            {
                return "33881-1.htm";
            }
            else if(status == 0)
            {
                if(!checkConditions(player))
                {
                    return "";
                }
                if (player.isGM())
                {
                    player.teleToLocation(46740 + Rnd.get(500), -28619 + Rnd.get(500), -1405);
                    L2GrandBossInstance lindvior = GrandBossManager.getInstance().getBoss(LINDVIOR_RB);
                    QuestManager.getInstance().getQuest(Lindvior.class).notifyEvent("waiting", lindvior, player);
                }
                else
                {
                    for(L2PcInstance member : player.getParty().getMembers())
                    {
                        L2BossZone zone = ZoneManager.getInstance().getZoneById(ZONE_ID, L2BossZone.class);

                        if (zone != null)
                        {
                            zone.allowPlayerEntry(member, 15);
                        }

                        member.teleToLocation(46740 + Rnd.get(500), -28619 + Rnd.get(500), -1405);

                        if (!member.getPets().isEmpty())
                        {
                            for (L2Summon pet : member.getPets())
                            {
                                pet.teleToLocation(46740 + Rnd.get(500), -28619 + Rnd.get(500), -1405);
                            }
                        }
                        
                        L2GrandBossInstance lindvior = GrandBossManager.getInstance().getBoss(LINDVIOR_RB);
                        QuestManager.getInstance().getQuest(Lindvior.class).notifyEvent("waiting", lindvior, member);
                    }
                }
            }
        }
        return null;
    }


    private boolean checkConditions(L2PcInstance player)
    {
        if (player.isGM())
        {
            return true;
        }
        if(player.getParty() == null)
        {
            player.sendPacket(SystemMessageId.NOT_IN_PARTY_CANT_ENTER);
            return false;
        }
        L2Party party = player.getParty();
        if(!party.isInCommandChannel())
        {
            party.broadcastPacket(SystemMessageId.NOT_IN_COMMAND_CHANNEL_CANT_ENTER);
            return false;
        }
        if(party.getCommandChannel().getMembers().size() < 49 || party.getCommandChannel().getPartyCount() < 7)
        {
            party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_ENOUGH_PEOPLE_TO_ENTER_INSTANCE_ZONE_NEED_S1).addNumber(49));
            return false;
        }
        if(party.getCommandChannel().getMembers().size() > 112 || party.getCommandChannel().getPartyCount() > 16)
        {
            party.getCommandChannel().broadcastMessage(SystemMessageId.CANNOT_ENTER_MAX_ENTRANTS);
            return false;
        }
        if(party.getCommandChannel().getLeader() != player)
        {
            party.getCommandChannel().broadcastMessage(SystemMessageId.ONLY_FOR_ALLIANCE_CHANNEL_LEADER);
            return false;
        }

        for(L2PcInstance member : party.getCommandChannel().getMembers())
        {
            if (member == null || member.getLevel() < 99)
            {
                party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_LEVEL_REQUIREMENT_NOT_SUFFICIENT).addPcName(member));
                return false;
            }

            if (!Util.checkIfInRange(1000, player, member, true))
            {
                party.getCommandChannel().broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.C1_IS_IN_LOCATION_THAT_CANNOT_BE_ENTERED).addPcName(member));
                return false;
            }
        }
        return true;
    }
}
