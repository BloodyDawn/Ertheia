package dwo.scripts.ai.zone;

import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.model.world.quest.QuestState;
import dwo.gameserver.model.world.zone.L2ZoneType;
import dwo.gameserver.network.game.serverpackets.packet.ex.ExStartScenePlayer;
import dwo.scripts.quests._10320_ToTheCentralSquare;

/**
 * User: GenCloud
 * Date: 20.03.2015
 * Team: La2Era Team
 */
public class TalkingIslandZone extends Quest
{
    // Зоны
    private static final int ZONE_ID = 400114;

    public TalkingIslandZone()
    {
        super();
        addEnterZoneId(ZONE_ID);
    }

    public static void main(String[] args)
    {
        new TalkingIslandZone();
    }

    @Override
    public String onEnterZone(L2Character character, L2ZoneType zone)
    {
        L2PcInstance player = character.getActingPlayer();
        QuestState qs = player.getQuestState(_10320_ToTheCentralSquare.class);

        if (player != null && player.getVariablesController().get("TI_PRESENTATION") == null)
        {
            if (qs != null && qs.getCond() == 1)
            {
                player.showQuestMovie(ExStartScenePlayer.SCENE_MUSEUM_EXIT_2);
            }
            else
            {
                player.showQuestMovie(ExStartScenePlayer.SCENE_MUSEUM_EXIT_1);
            }
            
            player.getVariablesController().set("TI_PRESENTATION", 1);
        }
        return super.onEnterZone(character, zone);
    }

    @Override
    public String onExitZone(L2Character character, L2ZoneType zone)
    {
        return super.onExitZone(character, zone);
    }
}
