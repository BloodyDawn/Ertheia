package dwo.gameserver.datatables.xml;

import dwo.config.Config;
import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.world.npc.L2NpcWalkerNode;
import dwo.gameserver.network.game.components.NpcStringId;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NpcWalkerRoutesData extends XmlDocumentParser
{
	private static final Map<Integer, List<L2NpcWalkerNode>> _routes = new HashMap<>();

    protected static NpcWalkerRoutesData instance;

    private NpcWalkerRoutesData()
	{
		if(Config.ALLOW_NPC_WALKERS)
		{
            try {
                load();
            } catch (JDOMException | IOException e) {
                e.printStackTrace();
            }
        }
	}

	public static NpcWalkerRoutesData getInstance()
	{
		return instance == null ? instance = new NpcWalkerRoutesData() : instance;
	}

	@Override
	public void load() throws JDOMException, IOException {
		_routes.clear();
		parseFile(FilePath.NPC_WALKER_ROUTES_TABLE);
		_log.log(Level.INFO, "NpcWalkerRoutesTable: Loaded " + _routes.size() + " Npc Walker Routes.");
	}

	@Override
	protected void parseDocument(Element rootElement)
	{
		List<L2NpcWalkerNode> list;
		for(Element element : rootElement.getChildren())
		{
            final String name = element.getName();
			if(name.equals("walker"))
			{
				list = new ArrayList<>();
				int npcId = Integer.parseInt(element.getAttributeValue("npcId"));
				for(Element element1 : element.getChildren())
				{
                    final String name1 = element1.getName();
					if(name1.equals("route"))
					{
						int id = Integer.parseInt(element1.getAttributeValue("id"));
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
							node = element1.getAttributeValue("npcString");
							if(node != null)
							{
								npcString = NpcStringId.getNpcStringId(node);
								if(npcString == null)
								{
									_log.log(Level.WARN, "NpcWalkerRoutersTable: Unknown npcstring '" + node + '.');
									continue;
								}
							}
							else
							{
								node = element1.getAttributeValue("npcStringId");
								if(node != null)
								{
									npcString = NpcStringId.getNpcStringId(Integer.parseInt(node));
									if(npcString == null)
									{
										_log.log(Level.WARN, "NpcWalkerRoutersTable: Unknown npcstring ID '" + node + '.');
										continue;
									}
								}
							}
						}
						list.add(new L2NpcWalkerNode(id, npcString, chatString, x, y, z, delay, Boolean.parseBoolean(element1.getAttributeValue("run")), false));
					}
				}
				((ArrayList<L2NpcWalkerNode>) list).trimToSize();
				_routes.put(npcId, list);
			}
		}
	}

	public List<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		return _routes.get(id);
	}
}