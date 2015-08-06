package la2era.parsers;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: GenCloud
 * Date: 25.06.2015
 * Team: La2Era Team
 * Репарсит нпс-темплейты и дроп из подпапок (./data/npc/data, ./data/npc/droplist)
 * Преобразует нпс-темплейты с добавлением в них дропа.
 * Выходная дирректория ./data/npc/finish
 */
public class ConvDropToNPTemplate 
{
    private static Map<String, Document> npc = new HashMap<>();
    private static Map<Integer, List<Element>> npc_drop = new HashMap<>();
    private static Map<String, Document> docs = new HashMap<>();
    
    private static ConvDropToNPTemplate instance;

    private ConvDropToNPTemplate()
    {
        parseAndRefresh();
    }

    public static ConvDropToNPTemplate getInstance() {
        return instance == null ? instance = new ConvDropToNPTemplate() : instance;
    }

    public void parseAndRefresh()
    {
        try
        {
            final Collection<File> files = FileUtils.listFiles(new File("./data/npc/droplist/"), new String[] { "xml" }, true);
            for(final File file : files)
            {
                String fName = file.getName();
                Document doc;
                if(!docs.containsKey(fName))
                {
                    SAXReader _reader = new SAXReader();
                    _reader.setValidation(false);
                    doc = _reader.read(file);
                    docs.put(fName, doc);
                }
                else
                {
                    doc = docs.get(fName);
                }
                for(final Element element : doc.getRootElement().elements())
                {
                    final int id = Integer.parseInt(element.attributeValue("id"));
                    final List<Element> elem = element.elements("category");
                    npc_drop.put(id, elem);
                }
            }

            System.out.println(docs.size() + " file's drop load");

            final Collection<File> files_npc = FileUtils.listFiles(new File("./data/npc/data/"), new String[] { "xml" }, true);
            for(final File file : files_npc)
            {
                String fName = file.getName();
                Document doc;
                if(!npc.containsKey(fName))
                {
                    SAXReader _reader = new SAXReader();
                    _reader.setValidation(false);
                    doc = _reader.read(file);
                    npc.put(fName, doc);
                }
                else
                {
                    doc = npc.get(fName);
                }
                for(final Element element : doc.getRootElement().elements())
                {
                    final int id = Integer.parseInt(element.attributeValue("id"));
                    if(npc_drop.get(id) != null)
                    {
                        Element droplist = element.addElement("drop_list");
                        npc_drop.get(id).forEach(m ->
                                droplist.add(m.createCopy()));
                    }
                }
            }
            System.out.println(npc.size() + " file's npc load");
            for(final String fName1 : npc.keySet())
            {
                final Document doc1 = npc.get(fName1);
                OutputFormat of = new OutputFormat("\t", true);
                of.setOmitEncoding(false);
                of.setTrimText(true);
                of.setEncoding("UTF-8");
                XMLWriter writer = null;
                try
                {
                    writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream("./data/npc/finish/" + fName1), "UTF8"), of);
                    writer.write(doc1);
                }
                finally
                {
                    if(writer != null)
                    {
                        writer.flush();
                        writer.close();
                    }
                }
            }
        } 
        catch(IOException | DocumentException e)
        {
            e.printStackTrace();
        }
    }
}
