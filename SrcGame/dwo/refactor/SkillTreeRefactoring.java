package dwo.refactor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import dwo.gameserver.model.player.base.ClassId;

public class SkillTreeRefactoring {

    public static void main(String[] args) throws JDOMException, IOException {
        File in = new File("/home/alf/Desktop/L2WT-Ertheia/Data/game/data/stats/player/skillTrees/classSkillTree/classSkillTree.xml");



        SAXBuilder builder = new SAXBuilder();
        Document document = builder.build(in);
        Element rootElement = document.getRootElement();
        for (Element element : rootElement.getChildren()) {
            final String name = element.getName();
            if (name.equalsIgnoreCase("skillTree")) {
                Integer classId = Integer.parseInt(element.getAttributeValue("classId"));
                String className = ClassId.getClassId(classId).name();

                element.setAttribute("name", className);
                File out = new File(in.getParent(), "class_skill_tree/[" + classId + "] " + className + ".xml");

                Element list = new Element("list");
                list.addContent(element.clone());

                Document doc = new Document(list);

                XMLOutputter xmlOutput = new XMLOutputter();
                xmlOutput.setFormat(Format.getPrettyFormat().setIndent("\t"));
                xmlOutput.output(doc, new FileWriter(out));
            }
        }
    }
}
