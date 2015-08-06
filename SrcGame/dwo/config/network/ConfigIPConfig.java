/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package dwo.config.network;

import dwo.config.Config;
import org.apache.log4j.Level;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;

/**
 * @author L0ngh0rn
 */
public class ConfigIPConfig extends Config
{
	private static final String path = IPCONFIG_CONFIG;

	public static void loadConfig()
	{
		_log.log(Level.INFO, "Loading: " + path);
		try
		{
			File file = new File(path);
			Document doc = null;
			ArrayList<String> subnets = new ArrayList<>(5);
			ArrayList<String> hosts = new ArrayList<>(5);
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setIgnoringComments(true);
			doc = factory.newDocumentBuilder().parse(file);

			for(Node n = doc.getFirstChild(); n != null; n = n.getNextSibling())
			{
				NamedNodeMap attrs;
				Node att;

				if("gameserver".equalsIgnoreCase(n.getNodeName()))
				{
					for(Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
					{
						if("define".equalsIgnoreCase(d.getNodeName()))
						{
							attrs = d.getAttributes();

							att = attrs.getNamedItem("subnet");
							if(att == null)
							{
								continue;
							}

							subnets.add(att.getNodeValue());

							att = attrs.getNamedItem("address");
							if(att == null)
							{
								continue;
							}

							hosts.add(att.getNodeValue());

							if(hosts.size() != subnets.size())
							{
								throw new Error("Failed to Load " + path + " File - subnets does not match server addresses.");
							}
						}
					}

					attrs = n.getAttributes();

					att = attrs.getNamedItem("address");
					if(att == null)
					{
						throw new Error("Failed to Load " + path + " File - default server address is missing.");
					}

					subnets.add("0.0.0.0/0");
					hosts.add(att.getNodeValue());
				}
			}
			GAME_SERVER_SUBNETS = subnets.toArray(new String[subnets.size()]);
			GAME_SERVER_HOSTS = hosts.toArray(new String[hosts.size()]);
		}
		catch(Exception e)
		{
			throw new Error("Failed to Load " + path + " File.", e);
		}
	}
}
