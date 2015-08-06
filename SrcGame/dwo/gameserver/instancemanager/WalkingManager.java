package dwo.gameserver.instancemanager;

import dwo.config.FilePath;
import dwo.gameserver.Announcements;
import dwo.gameserver.ThreadPoolManager;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.actor.L2Character;
import dwo.gameserver.model.actor.L2Npc;
import dwo.gameserver.model.actor.L2Object;
import dwo.gameserver.model.actor.ai.CtrlIntention;
import dwo.gameserver.model.world.npc.L2NpcWalkerNode;
import dwo.gameserver.model.world.npc.L2WalkRoute;
import dwo.gameserver.model.world.npc.spawn.L2Spawn;
import dwo.gameserver.model.world.zone.Location;
import dwo.gameserver.network.game.components.NpcStringId;
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
import java.util.concurrent.ScheduledFuture;

public class WalkingManager extends XmlDocumentParser
{
	//Repeat style: 0 - go back, 1 - go to first point (circle style), 2 - teleport to first point (conveyor style), 3 - random walking between points.
	private static final byte REPEAT_GO_BACK = 0;
	private static final byte REPEAT_GO_FIRST = 1;
	private static final byte REPEAT_TELE_FIRST = 2;
	private static final byte REPEAT_RANDOM = 3;
	private static final byte REPEAT_DELETE = 4;
	/**
	 * NPC ждет, пока игрок приблизится к нему и после этого начинает перемещение в другую точку.
	 * По завершении передвижения NPC исчезает.
	 * Если истекается время, заданное в delay для текущей точки, то NPC перестает ждать игрока и перемещается в следующую точку.
	 */
	private static final byte REPEAT_GUIDE = 5;

	private Map<Integer, L2WalkRoute> _routes = new HashMap<>(); //all available routes
	private Map<Integer, WalkInfo> _activeRoutes = new HashMap<>(); //each record represents NPC, moving by predefined route from _routes, and moving progress

	protected WalkingManager()
	{
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

	public static WalkingManager getInstance()
	{
		return SingletonHolder._instance;
	}

	@Override
	public void load() throws JDOMException, IOException {
		_routes.clear();
		parseFile(FilePath.WALKING_MANAGER);
		_log.log(Level.INFO, "WalkingManager: loaded " + _routes.size() + " walking routes.");
	}

	@Override
	protected void parseDocument(Element rootElement)
	{
		for(Element element : rootElement.getChildren())
		{
            final String name = element.getName();
			if(name.equals("route"))
			{
				boolean debug = false;
				int routeId = Integer.parseInt(element.getAttributeValue("id"));
				boolean repeat = Boolean.parseBoolean(element.getAttributeValue("repeat"));
				String repeatStyle = element.getAttributeValue("repeatStyle");
				byte repeatType;

				if(repeatStyle.equalsIgnoreCase("back"))
				{
					repeatType = REPEAT_GO_BACK;
				}
				else if(repeatStyle.equalsIgnoreCase("cycle"))
				{
					repeatType = REPEAT_GO_FIRST;
				}
				else if(repeatStyle.equalsIgnoreCase("conveyor"))
				{
					repeatType = REPEAT_TELE_FIRST;
				}
				else if(repeatStyle.equalsIgnoreCase("random"))
				{
					repeatType = REPEAT_RANDOM;
				}
				else if(repeatStyle.equalsIgnoreCase("delete"))
				{
					repeatType = REPEAT_DELETE;
				}
				else
				{
					repeatType = repeatStyle.equalsIgnoreCase("guide") ? REPEAT_GUIDE : -1;
				}

				boolean animationWhenArrived = element.getAttributeValue("animationWhenArrived") != null && Boolean.parseBoolean(element.getAttributeValue("animationWhenArrived"));

				List<L2NpcWalkerNode> list = new ArrayList<>();
				for(Element element1 : element.getChildren())
				{
                    final String name1 = element1.getName();
					if(name1.equals("point"))
					{
						int x = Integer.parseInt(element1.getAttributeValue("X"));
						int y = Integer.parseInt(element1.getAttributeValue("Y"));
						int z = Integer.parseInt(element1.getAttributeValue("Z"));
						int delay = Integer.parseInt(element1.getAttributeValue("delay"));

						String chatString = null;
						NpcStringId npcString = null;
						String node = element1.getAttributeValue("string");
						if(node != null)
						{
							chatString = node;
						}
						else
						{
							node = element1.getAttributeValue("npcStringId");
							if(node != null)
							{
								npcString = NpcStringId.getNpcStringId(Integer.parseInt(node));
								if(npcString == null)
								{
									_log.log(Level.WARN, "WalkingManager: Unknown npcStringId " + node + '.');
									continue;
								}
							}
						}
						list.add(new L2NpcWalkerNode(0, npcString, chatString, x, y, z, delay, Boolean.parseBoolean(element1.getAttributeValue("run")), animationWhenArrived));
					}
					else if(name1.equals("stat"))
					{
						String name0 = element1.getAttributeValue("name");
						String val = element1.getAttributeValue("val");

						if(name0.equalsIgnoreCase("debug"))
						{
							debug = Boolean.parseBoolean(val);
						}
					}
				}
				L2WalkRoute newRoute = new L2WalkRoute(routeId, list, repeat, false, repeatType);
				newRoute.setDebug(debug);
				_routes.put(routeId, newRoute);
			}
		}
	}

	public boolean isRegistered(L2Npc npc)
	{
		return _activeRoutes.containsKey(npc.getObjectId());
	}

	public void startMoving(L2Npc npc, int routeId)
	{
		if(_routes.containsKey(routeId) && npc != null && !npc.isDead()) //check, if these route and NPC present
		{
			if(_activeRoutes.containsKey(npc.getObjectId()))
			{
				//Announcements.getInstance().announceToAll("Here_1!");
				if(_activeRoutes.containsKey(npc.getObjectId()) && (npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE))
				{
					//Announcements.getInstance().announceToAll("Here_2!");
					WalkInfo walk = _activeRoutes.get(npc.getObjectId());
					//Announcements.getInstance().announceToAll("X = " + Integer.toString(npc.getX()) + ", Y = " +  Integer.toString(npc.getY()) + ", node = " + Integer.toString(walk._currentNode));

					//Prevent call simultaneosly from scheduled task and onArrived() or temporarily stop walking for resuming in future
					if(walk._blocked || walk._suspended)
					{
						return;
					}

					//Announcements.getInstance().announceToAll("Continue move!");
					walk._blocked = true;
					//Check this first, within the bounds of random moving, we have no conception of "first" or "last" node
					if(walk.getRoute().getRepeatType() == REPEAT_RANDOM && walk._nodeArrived)
					{
						int newNode = walk._currentNode;

						while(newNode == walk._currentNode)
						{
							newNode = Rnd.get(walk.getRoute().getNodesCount());
						}

						walk._currentNode = newNode;
						walk._nodeArrived = false;
					}

					else if(walk.isAtLastNode()) //Last node arrived
					{
						if(!walk.getRoute().repeatWalk())
						{
							cancelMoving(npc, false);
							return;
						}

						if(walk.getRoute().repeatWalk() && walk.getRoute().getRepeatType() == REPEAT_DELETE || walk.getRoute().getRepeatType() == REPEAT_GUIDE)
						{
							cancelMoving(npc, true);
							return;
						}

						switch(walk.getRoute().getRepeatType())
						{
							case REPEAT_GO_BACK:
								walk._forward = false;
								walk._currentNode -= 2;
								break;
							case REPEAT_GO_FIRST:
								walk._currentNode = 0;
								break;
							case REPEAT_TELE_FIRST:
								npc.teleToLocation(npc.getSpawn().getLocx(), npc.getSpawn().getLocy(), npc.getSpawn().getLocz());
								walk._currentNode = 0;
								break;
							case REPEAT_DELETE:
							case REPEAT_GUIDE:
								cancelMoving(npc, true);
								break;
						}
					}

					else if(walk._currentNode == -1) //First node arrived, when direction is first <-- last
					{
						walk._currentNode = 1;
						walk._forward = true;
					}

					L2NpcWalkerNode node = walk.getCurrentNode();
					npc.setIsRunning(node.getRunning());

					if(walk.getRoute().debug())
					{
						Announcements.getInstance().announceToAll("Continue to node " + Integer.toString(walk._currentNode));
					}

					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
					walk._blocked = false;
				}
			}
			else
			{
				//only if not already moved / not engaged in battle... should not happens if called on spawn
				if(npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE || npc.getAI().getIntention() == CtrlIntention.AI_INTENTION_IDLE)
				{
					WalkInfo walk = new WalkInfo(routeId);
					//walk._lastActionTime = System.currentTimeMillis();
					L2NpcWalkerNode node = walk.getCurrentNode();

					if(!npc.isInsideRadius(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 3000, true, false)) //too far from first point, decline further operations
					{
						return;
					}

					//Announcements.getInstance().announceToAll("Start to move!");
					npc.setIsRunning(node.getRunning());
					npc.getAI().setIntention(CtrlIntention.AI_INTENTION_MOVE_TO, new Location(node.getMoveX(), node.getMoveY(), node.getMoveZ(), 0));
					walk._walkCheckTask = ThreadPoolManager.getInstance().scheduleAiAtFixedRate(() -> startMoving(npc, routeId), 60000, 60000); //start walk check task, for resuming walk after fight

					npc.getKnownList().startTrackingTask();

					_activeRoutes.put(npc.getObjectId(), walk); //register route
				}
				else //try a bit later
				{
					ThreadPoolManager.getInstance().scheduleGeneral(() -> startMoving(npc, routeId), 60000);
				}
			}
		}
	}

	public void cancelMoving(L2Npc npc, boolean delete)
	{
		if(_activeRoutes.containsKey(npc.getObjectId()))
		{
			_activeRoutes.get(npc.getObjectId())._walkCheckTask.cancel(true);
			_activeRoutes.remove(npc.getObjectId());
			npc.getKnownList().stopTrackingTask();
			if(delete)
			{
				npc.getLocationController().delete();
			}
		}
	}

	public void resumeMoving(L2Npc npc)
	{
		if(!_activeRoutes.containsKey(npc.getObjectId()))
		{
			return;
		}

		WalkInfo walk = _activeRoutes.get(npc.getObjectId());
		walk._suspended = false;
		startMoving(npc, walk.getRoute().getId());
	}

	public void stopMoving(L2Npc npc, boolean suspend)
	{
		if(!_activeRoutes.containsKey(npc.getObjectId()))
		{
			return;
		}

		WalkInfo walk = _activeRoutes.get(npc.getObjectId());
		walk._suspended = suspend;
		npc.stopMove(null);
		npc.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
	}

	public void onArrived(L2Npc npc)
	{
		if(_activeRoutes.containsKey(npc.getObjectId()))
		{
			WalkInfo walk = _activeRoutes.get(npc.getObjectId());

			//Opposite should not happen... but happens sometime
			if(walk._currentNode >= 0 && walk._currentNode < walk.getRoute().getNodesCount())
			{
				L2NpcWalkerNode node = walk.getRoute().getNodeList().get(walk._currentNode);
				if(node.getMoveX() == npc.getX() && node.getMoveY() == npc.getY())
				{
					if(walk.getRoute().debug())
					{
						Announcements.getInstance().announceToAll("Arrived to node " + Integer.toString(walk._currentNode));
					}

					walk._nodeArrived = true;
					if(walk.getRoute().getRepeatType() != REPEAT_RANDOM)
					{
						if(walk._forward)
						{
							walk._currentNode++;
						}
						else
						{
							walk._currentNode--;
						}
					}

					int delay;

					if(walk.isAtLastNode())
					{
						delay = walk.getRoute().getLastNode().getDelay();
					}
					else
					{
						delay = walk._currentNode < 0 ? walk.getRoute().getNodeList().get(0).getDelay() : walk.getCurrentNode().getDelay();
					}

					walk._blocked = true; //prevents to be ran from walk check task, if there is delay in this node.
					if(walk.getRoute().getRepeatType() == REPEAT_GUIDE)
					{
						L2Object target = npc.getTarget();
						if(target instanceof L2Character)
						{
							npc.setHeading(Util.calculateHeadingFrom(npc, target));
						}
						ThreadPoolManager.getInstance().scheduleGeneral(new WaitPlayerTask(npc, walk, delay), 1000);
					}
					else
					{
						ThreadPoolManager.getInstance().scheduleGeneral(new ArrivedTask(npc, walk), 100 + delay * 1000L);
					}
				}
			}
		}
	}

	public void onDeath(L2Npc npc)
	{
		if(npc != null)
		{
			L2Spawn spawn = npc.getSpawn();
			if(spawn != null)
			{
				if(spawn.getRespawnDelay() == 0)
				{
					if(_activeRoutes.containsKey(npc.getObjectId()))
					{
						cancelMoving(npc, false);
					}
				}
			}
		}
	}

	private static class SingletonHolder
	{
		protected static final WalkingManager _instance = new WalkingManager();
	}

	private class WalkInfo
	{
		private ScheduledFuture<?> _walkCheckTask;
		private boolean _blocked;
		private boolean _suspended;
		private boolean _nodeArrived;
		private int _currentNode;
		private boolean _forward = true; //Determines first --> last or first <-- last direction
		private int _routeId;

		public WalkInfo(int routeId)
		{
			_routeId = routeId;
		}

		private L2WalkRoute getRoute()
		{
			return _routes.get(_routeId);
		}

		private L2NpcWalkerNode getCurrentNode()
		{
			return getRoute().getNodeList().get(_currentNode);
		}

		private boolean isAtLastNode()
		{
			return _currentNode >= getRoute().getNodesCount();
		}
	}

	private class ArrivedTask implements Runnable
	{
		WalkInfo _walk;
		L2Npc _npc;

		public ArrivedTask(L2Npc npc, WalkInfo walk)
		{
			_npc = npc;
			_walk = walk;
		}

		@Override
		public void run()
		{
			_walk._blocked = false;
			startMoving(_npc, _walk.getRoute().getId());
		}
	}

	private class WaitPlayerTask implements Runnable
	{
		private L2Npc _npc;
		private WalkInfo _walk;
		private int _waitTime;

		WaitPlayerTask(L2Npc npc, WalkInfo walk, int waitTime)
		{
			_npc = npc;
			_walk = walk;
			_waitTime = waitTime;
		}

		@Override
		public void run()
		{
			if(_walk.isAtLastNode())
			{
				cancelMoving(_npc, true);
			}
			L2Object followTarget = _npc.getTarget();
			// Если игрок в радиусе 200 или истекло время ожидания, начинаем перемещение
			if(followTarget == null || _npc.isInsideRadius(followTarget, 350, false, false) || _waitTime <= 0)
			{
				ThreadPoolManager.getInstance().executeTask(new ArrivedTask(_npc, _walk));
			}
			else
			{
				// Повторяем проверку через 1 секунду
				ThreadPoolManager.getInstance().scheduleGeneral(new WaitPlayerTask(_npc, _walk, _waitTime - 1), 1000);
			}
		}
	}
}