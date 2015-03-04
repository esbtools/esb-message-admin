package org.esbtools.message.admin.common.extractor;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * The KeyExtractor is a xpath based ValueExtractor implementation. 
 * It evaluates a list of xpath operations against the given payload 
 * and puts the value along with the name into map. 
 * The purpose is to enrich the payload with additional metadata so users 
 * can search by them.
 * 
 * @author vrjain
 * @author ykoer
 *
 */
public class KeyExtractorUtil {

    private static final Logger log = Logger.getLogger(KeyExtractorUtil.class.getName());
    private Map<String, List<XPathExpression>> expressions;

    public KeyExtractorUtil(List<MetadataField> searchKeys) {
        expressions = new HashMap<String, List<XPathExpression>>();

        XPath xpath = XPathFactory.newInstance().newXPath();
        for (MetadataField searchKey : searchKeys) {

            for (MetadataField path : searchKey.getChildren()) {
                Map<String, List<XPathExpression>> expressionMap = null;
                if (path.getType() == MetadataType.XPATH) {
                    expressionMap = expressions;
                }

                if (expressionMap != null) {
                    if (!expressionMap.containsKey(searchKey.getValue())) {
                        expressionMap.put(searchKey.getValue(), new LinkedList<XPathExpression>());
                    }
                    XPathExpression expr;
                    try {
                        expr = xpath.compile(path.getValue());
                        expressionMap.get(searchKey.getValue()).add(expr);
                        log.info("adding key:" + searchKey.getValue() + " with path:" + path.getValue());
                    } catch (XPathExpressionException e) {
                        log.warning("XPATH: " + path.getValue() + " is invalid. Ignoring it!");
                    }
                }

            }
        }
    }

    private void addToMap(Map<String, List<String>> keysMap, String key, String value) {
        if (!keysMap.containsKey(key)) {
            keysMap.put(key, new LinkedList<String>());
        }
        keysMap.get(key).add(value);
    }

    public Map<String, List<String>> getEntriesFromPayload(String payload) throws KeyExtractorException {
        Map<String, List<String>> keysMap = new HashMap<String, List<String>>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory
                .newInstance();
        try {

            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(payload)));

            Iterator<String> iter = expressions.keySet().iterator();

            while (iter.hasNext()) {

                String key = iter.next();
                for (XPathExpression valuePath : expressions.get(key)) {
                    valuePath.evaluate(doc);
                    NodeList result;
                    try {
                        result = (NodeList) valuePath.evaluate(doc, XPathConstants.NODESET);

                        for (int index = 0; index < result.getLength(); index++) {
                            Node node = result.item(index);
                            String value = node.getFirstChild().getNodeValue();
                            addToMap(keysMap, key, value);
                            log.info("found key:" + key + "with value:" + value);
                        }
                    } catch (XPathExpressionException e) {
                        addToMap(keysMap, key, valuePath.evaluate(doc));
                    }
                }
            }

        } catch (Exception e) {
            throw new KeyExtractorException(e);
        }

        return keysMap;
    }
}