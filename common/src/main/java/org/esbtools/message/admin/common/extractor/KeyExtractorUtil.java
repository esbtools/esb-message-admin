/*
 Copyright 2015 esbtools Contributors and/or its affiliates.

 This file is part of esbtools.

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.esbtools.message.admin.common.extractor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.esbtools.message.admin.model.MetadataField;
import org.esbtools.message.admin.model.MetadataType;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


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

    private static final Logger LOGGER=LoggerFactory.getLogger(KeyExtractorUtil.class);
    private String hash;
    private Map<String, List<XPathExpression>> expressions;

    public KeyExtractorUtil(List<MetadataField> searchKeys, String argHash) {
        expressions = new HashMap<>();
        hash = argHash;

        XPath xpath = XPathFactory.newInstance().newXPath();
        for (MetadataField searchKey : searchKeys) {

            for (MetadataField path : searchKey.getChildren()) {
                if (path.getType() == MetadataType.XPATH) {
                    loadXPathExpressions(xpath, searchKey, path);
                }
            }
        }
    }

    private final void loadXPathExpressions(XPath xpath, MetadataField searchKey, MetadataField path) {
        if (!expressions.containsKey(searchKey.getValue())) {
            expressions.put(searchKey.getValue(), new LinkedList<XPathExpression>());
        }
        XPathExpression expr;
        try {
            expr = xpath.compile(path.getValue());
            expressions.get(searchKey.getValue()).add(expr);
            LOGGER.info("adding key: {} with path: {}", searchKey.getValue(), path.getValue());
        } catch (XPathExpressionException e) {
            LOGGER.warn("XPATH: {} is invalid. Ignoring it!", path.getValue(), e);
        }
    }

    public String getHash() {
        return hash;
    }

    private void addToMap(Map<String, List<String>> keysMap, String key, String value) {
        if (!keysMap.containsKey(key)) {
            keysMap.put(key, new LinkedList<String>());
        }
        keysMap.get(key).add(value);
    }

    public Map<String, List<String>> getEntriesFromPayload(String payload) throws KeyExtractorException {

        try {
            return getEntriesFromXPaths(payload);
        } catch (Exception e) {
            throw new KeyExtractorException(e);
        }

    }

    private Map<String, List<String>> getEntriesFromXPaths(String payload) throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        Map<String, List<String>> keysMap = new HashMap<>();

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(new ByteArrayInputStream(payload.getBytes("UTF-8")));

        Iterator<String> iter = expressions.keySet().iterator();

        while (iter.hasNext()) {

            String key = iter.next();
            for (XPathExpression valuePath : expressions.get(key)) {
                getXPathMatches(keysMap, doc, key, valuePath);
            }
        }
        return keysMap;
    }

    private void getXPathMatches(Map<String, List<String>> keysMap, Document doc, String key, XPathExpression valuePath) throws XPathExpressionException {
        valuePath.evaluate(doc);
        NodeList result;
        try {
            result = (NodeList) valuePath.evaluate(doc, XPathConstants.NODESET);

            for (int index = 0; index < result.getLength(); index++) {
                Node node = result.item(index);
                String value;
                if(node.hasChildNodes()) {
                    value = node.getFirstChild().getNodeValue();
                } else {
                    value = node.getNodeValue();
                }
                addToMap(keysMap, key, value);
                LOGGER.info("found key: {} with value: {}", key, value);
            }
        } catch (XPathExpressionException e) {
            LOGGER.error("Error occurred in Xpath evaluation", e);
            addToMap(keysMap, key, valuePath.evaluate(doc));
        }
    }
}