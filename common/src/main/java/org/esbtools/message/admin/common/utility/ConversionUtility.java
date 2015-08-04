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
package org.esbtools.message.admin.common.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.esbtools.message.admin.common.config.Configuration;
import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.common.orm.EsbMessageHeaderEntity;
import org.esbtools.message.admin.common.orm.EsbMessageSensitiveInfoEntity;
import org.esbtools.message.admin.common.orm.MetadataEntity;
import org.esbtools.message.admin.model.EsbMessage;
import org.esbtools.message.admin.model.Header;
import org.esbtools.message.admin.model.MetadataField;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 *
 * @author ykoer
 */

public final class ConversionUtility {

    private ConversionUtility() {

    }

    /**
     * Convert an EsbMessage to an EsbMessageEntity
     */
    public static EsbMessageEntity convertFromEsbMessage(EsbMessage esbMessage) {

        EsbMessageEntity eme = new EsbMessageEntity();
        eme.setErrorQueue(esbMessage.getErrorQueue());
        eme.setMessageId(esbMessage.getMessageId());
        eme.setTimestamp(esbMessage.getTimestamp());
        eme.setMessageGuid(esbMessage.getMessageGuid());
        eme.setMessageType(esbMessage.getMessageType());
        eme.setSourceQueue(esbMessage.getSourceQueue());
        eme.setSourceLocation(esbMessage.getSourceLocation());
        eme.setSourceSystem(esbMessage.getSourceSystem());
        eme.setServiceName(esbMessage.getServiceName());
        eme.setErrorComponent(esbMessage.getErrorComponent());
        eme.setErrorMessage(esbMessage.getErrorMessage());
        eme.setErrorDetails(esbMessage.getErrorDetails());
        eme.setErrorType(esbMessage.getErrorType());
        eme.setOccurrenceCount(esbMessage.getOccurrenceCount());
        eme.setPayload(esbMessage.getPayload());

        List<EsbMessageHeaderEntity> headers = eme.getErrorHeaders();

        if (headers == null) {
            headers = new ArrayList<EsbMessageHeaderEntity>();
        }

        if (esbMessage.getHeaders() != null) {
            for (Header header : esbMessage.getHeaders()) {
                headers.add(new EsbMessageHeaderEntity(eme, header.getType(), header.getName(), header.getValue()));
            }
        }
        eme.setErrorHeaders(headers);

        return eme;
    }

    /**
     * Convert an EsbMessageEntity to an EsbMessage.
     */
    public static EsbMessage convertToEsbMessage(EsbMessageEntity ee) {

        EsbMessage esbMessage = null;
        if (ee != null) {
            esbMessage = new EsbMessage();
            esbMessage.setId(ee.getId());
            esbMessage.setErrorQueue(ee.getErrorQueue());
            esbMessage.setMessageId(ee.getMessageId());
            esbMessage.setTimestamp(ee.getTimestamp());
            esbMessage.setMessageGuid(ee.getMessageGuid());
            esbMessage.setMessageType(ee.getMessageType());
            esbMessage.setSourceQueue(ee.getSourceQueue());
            esbMessage.setSourceLocation(ee.getSourceLocation());
            esbMessage.setSourceSystem(ee.getSourceSystem());
            esbMessage.setServiceName(ee.getServiceName());
            esbMessage.setErrorComponent(ee.getErrorComponent());
            esbMessage.setErrorMessage(ee.getErrorMessage());
            esbMessage.setErrorDetails(ee.getErrorDetails());
            esbMessage.setErrorType(ee.getErrorType());
            esbMessage.setOccurrenceCount(ee.getOccurrenceCount());
            esbMessage.setPayload(ee.getPayload());

            List<EsbMessageHeaderEntity> headers = ee.getErrorHeaders();
            if (headers != null) {
                List<Header> headerList = new ArrayList<Header>();
                for (EsbMessageHeaderEntity header : headers) {
                    headerList.add(new Header(header.getType(), header.getName(), header.getValue()));
                }
                esbMessage.setHeaders(headerList);
            }
        }
        return esbMessage;
    }

    public static Map<String, String> getMap(JSONObject jsonMap) {
        Map<String,String> map = new HashMap<>();
        for(Entry<String,String> entry : (Set<Entry<String,String>>)jsonMap.entrySet()) {
            map.put(entry.getKey(),entry.getValue());
        }
        return map;
    }

    public static List<Configuration> getConfigurations(JSONArray jsonConfigurations) {

        List<Configuration> result = new ArrayList<>();
        for(int i=0; i<jsonConfigurations.size(); i++) {
            Configuration conf = new Configuration();
            JSONObject jsonConfiguration = (JSONObject) jsonConfigurations.get(i);
            conf.setMatchCriteriaMap(getMap((JSONObject) jsonConfiguration.get("matchCriteria")));
            conf.setConfigurationMap(getMap((JSONObject) jsonConfiguration.get("configuration")));
            result.add(conf);
        }
        return result;
    }

    public static List<EsbMessageEntity> convertFromEsbMessageArray(EsbMessage[] esbMessages) {

        if (esbMessages==null) {
            return null;
        }
        ArrayList<EsbMessageEntity> list=new ArrayList<EsbMessageEntity>(esbMessages.length);
        for(EsbMessage esbMessage:esbMessages) {
            list.add(convertFromEsbMessage(esbMessage));
        }

        return list;
    }

    public static EsbMessage[] convertToEsbMessageArray(List<EsbMessageEntity> l) {

        if (l==null) {
            return null;
        }

        EsbMessage[] esbMessages = new EsbMessage[l.size()];
        for(int i=0; i<l.size(); i++) {
            esbMessages[i]=convertToEsbMessage(l.get(i));
        }
        return esbMessages;
    }

    public static MetadataField convertToMetadataField(MetadataEntity entity) {
        MetadataField result = new MetadataField(entity.getType(), entity.getName(), entity.getValue());
        result.setId(entity.getId());
        return result;
    }

    public static List<MetadataField> convertToMetadataFields(List<MetadataEntity> entities) {
        List<MetadataField> result = new ArrayList<MetadataField>(entities.size());
        for (MetadataEntity entity : entities) {
            result.add(convertToMetadataField(entity));
        }
        return result;
    }

    public static List<EsbMessageSensitiveInfoEntity> convertToEsbMessageSensitiveInfo(EncryptionUtil encrypter, EsbMessageEntity eme, List<String> sensitiveInfo) {
        List<EsbMessageSensitiveInfoEntity> result = new ArrayList<EsbMessageSensitiveInfoEntity>();
        if(sensitiveInfo!=null) {
            for(String text: sensitiveInfo) {
                result.add(new EsbMessageSensitiveInfoEntity(eme, encrypter.encrypt(text)));
            }
        }
        return result;
    }

}
