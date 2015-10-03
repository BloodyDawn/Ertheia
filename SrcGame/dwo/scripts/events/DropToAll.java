package dwo.scripts.events;

import dwo.gameserver.datatables.xml.NpcTable;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.actor.templates.L2NpcTemplate;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.util.Rnd;

/**
 * Created with IntelliJ IDEA.
 * User: Eugene Chipachenko
 * Date: 08.09.2015
 * Time: 20:14
 */
public class DropToAll extends Quest
    {
      // Gold Einhasad 4356
      // Silver Shilen 4357
      // Coin of Luck 4037
      // Mouse Coin 10639
      private static final int ITEM_ID = 10639;

      public DropToAll()
      {
        super();
        System.out.println( "Event: Drop to all" );
        for( L2NpcTemplate npcTemplate : NpcTable.getInstance().getAllMonstersOfLevel( 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99 ) )
        {
          this.addEventId( npcTemplate.getNpcId(), Quest.QuestEventType.ON_KILL );
        }
      }

    public static void main( final String[] args )
    {
      new DropToAll();
    }

    @Override
    public String onKill( final L2Npc npc, final L2PcInstance killer, final boolean isPet )
    {
      if( npc.getLevel() >= 85 && npc.getLevel() <= 95 && Rnd.getChance( 50 ) )
      {
        killer.addItem( ProcessType.DROP, ITEM_ID, Rnd.get( 5, 10 ), killer, true );
      }
    else if( npc.getLevel() > 95 && npc.getLevel() < 100 && Rnd.getChance( 70 ) )
    {
      killer.addItem( ProcessType.DROP, ITEM_ID, Rnd.get( 10, 15 ), killer, true );
    }
    return null;
  }
}
