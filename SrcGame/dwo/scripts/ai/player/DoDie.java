package dwo.scripts.ai.player;

import dwo.gameserver.engine.hookengine.HookType;
import dwo.gameserver.model.world.quest.Quest;

public class DoDie extends Quest
{
	public DoDie(String name, String desc)
	{
		super(name, desc);
		addEventId(HookType.ON_DIE);
	}

	public static void main(String[] args)
	{
		new DoDie("DoDie", "ai");
	}
}