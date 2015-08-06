package dwo.scripts.npc.town;

import dwo.gameserver.engine.hookengine.AbstractHookImpl;
import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.engine.hookengine.impl.character.MentorHook;
import dwo.gameserver.instancemanager.HookManager;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.instance.L2PcInstance;
import dwo.gameserver.model.items.base.proptypes.ProcessType;
import dwo.gameserver.model.world.quest.Quest;
import dwo.gameserver.network.game.serverpackets.InventoryUpdate;

/**
 * L2GOD Team
 * User: ANZO
 * Date: 12.11.11
 * Time: 20:44
 */

public class MentorGuide extends Quest
{
	// Сертификаты
	private static final int CertificateOfTheWard = 33800;
	private static final int CertificateOfGraduation = 33805;
	// Помощник наставника
	private static final int MentorHelper = 33587;
	private static AbstractHookImpl _hook;

	public MentorGuide()
	{
		addAskId(MentorHelper, -10357);

		_hook = new MentorHook();
		HookManager.getInstance().addHook(HookType.ON_ENTER_WORLD, _hook);
		HookManager.getInstance().addHook(HookType.ON_DELETEME, _hook);
		HookManager.getInstance().addHook(HookType.ON_LEVEL_INCREASE, _hook);
	}

	public static void main(String[] args)
	{
		new MentorGuide();
	}

	@Override
	public String onAsk(L2PcInstance player, L2Npc npc, int ask, int reply)
	{
		if(reply == 3)
		{
			if(player.getLevel() < 85 || player.getItemsCount(CertificateOfTheWard) == 0 || !player.getVariablesController().get("menteeDone", Boolean.class, false))
			{
				return "mentoring_guide004.htm";
			}
			else
			{
				InventoryUpdate iu = new InventoryUpdate();
				iu.addRemovedItem(player.getInventory().destroyItemByItemId(ProcessType.MANUFACTURE, CertificateOfTheWard, 1, player, npc));
				iu.addItem(player.getInventory().addItem(ProcessType.MANUFACTURE, CertificateOfGraduation, 40, player, npc));
				player.sendPacket(iu);
			}
		}
		return null;
	}

	@Override
	public void onEnterWorld(L2PcInstance player)
	{
		_hook.onEnterWorld(player);
	}

	@Override
	public boolean unload()
	{
		HookManager.getInstance().removeHook(HookType.ON_ENTER_WORLD, _hook);
		HookManager.getInstance().removeHook(HookType.ON_DELETEME, _hook);
		HookManager.getInstance().removeHook(HookType.ON_LEVEL_INCREASE, _hook);
		return super.unload();
	}
}