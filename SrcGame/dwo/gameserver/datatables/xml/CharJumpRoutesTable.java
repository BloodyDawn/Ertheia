package dwo.gameserver.datatables.xml;

import dwo.config.FilePath;
import dwo.gameserver.engine.documentengine.XmlDocumentParser;
import dwo.gameserver.model.holders.JumpHolder;
import dwo.gameserver.model.player.jump.L2JumpNode;
import dwo.gameserver.model.player.jump.L2JumpType;
import dwo.gameserver.model.world.zone.Location;
import org.apache.log4j.Level;
import org.jdom2.Element;
import org.jdom2.JDOMException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * L2GOD Team
 * User: Bacek, Keiichi, ANZO
 * @correct: GenCloud
 * Date: 04.05.11
 * Time: 12:00
 */

public class CharJumpRoutesTable extends XmlDocumentParser
{
    private final Map<Integer, List<L2JumpNode>> _jumpRoutes = new HashMap<>();

    protected static CharJumpRoutesTable _instance;

    private CharJumpRoutesTable()
    {
        try {
            load();
        } catch (JDOMException | IOException e) {
            e.printStackTrace();
        }
    }

    public static CharJumpRoutesTable getInstance()
    {
        return _instance == null ? _instance = new CharJumpRoutesTable() : _instance;
    }

    @Override
    public void load() throws JDOMException, IOException {
        _jumpRoutes.clear();
        parseFile(FilePath.JUMP_ROUTES);
        _log.log(Level.INFO, "Loaded " + _jumpRoutes.size() + " Jump Routes.");
    }

    @Override
    protected void parseDocument(Element rootElement)
    {
        for(Element element : rootElement.getChildren())
        {
            final String name = element.getName();
            if(name.equalsIgnoreCase("jump"))
            {
                List<L2JumpNode> list = new ArrayList<>();

                final int jmpId = Integer.parseInt(element.getAttributeValue("jmpId"));

                for(Element element1 : element.getChildren())
                {
                    final String name1 = element1.getName();
                    if(name1.equalsIgnoreCase("route"))
                    {
                        List<JumpHolder> Jump = new ArrayList<>();
                        int id = Integer.parseInt(element1.getAttributeValue("id"));
                        L2JumpType type = L2JumpType.valueOf(element1.getAttributeValue("type"));

                        String[] coordinates = element1.getAttributeValue("coordinates").split(";");
                        Location loc;
                        for(String coordinate : coordinates)
                        {
                            loc = new Location(Integer.parseInt(coordinate.split(",")[0]), Integer.parseInt(coordinate.split(",")[1]), Integer.parseInt(coordinate.split(",")[2]));
                            int num = Integer.parseInt(coordinate.split(",")[3]);
                            if(num > 0)
                            {
                                num -= 1;
                            }

                            Jump.add(new JumpHolder(num, loc));
                        }
                        list.add(new L2JumpNode(id, type, Jump));
                    }
                }
                _jumpRoutes.put(jmpId, list);
            }
        }
    }

    public List<L2JumpNode> getJumpId(int id)
    {
        return _jumpRoutes.get(id);
    }
}