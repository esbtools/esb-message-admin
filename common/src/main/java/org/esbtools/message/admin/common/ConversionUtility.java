/**
 * Copyright (c) 2006 Red Hat, Inc.
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of
 * Red Hat, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Red Hat.
 */
package org.esbtools.message.admin.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.esbtools.message.admin.common.orm.EsbMessageEntity;
import org.esbtools.message.admin.common.orm.EsbMessageHeaderEntity;
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

public class ConversionUtility {

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

   public static List<List<String>> getCriteria(JSONArray matchCriteria) {

       List<List<String>> result = new ArrayList<>();
       for(int i=0; i<matchCriteria.size(); i++) {
           List<String> criterion = new ArrayList<String>();
           JSONObject matchCritierion = (JSONObject) matchCriteria.get(i);
           for(Entry<String,String> entry : (Set<Entry<String,String>>)matchCritierion.entrySet()) {
               criterion.add(entry.toString().toLowerCase());
           }
           result.add(criterion);
       }
       return result;
   }

   public static List<EsbMessageEntity> convertFromEsbMessageArray(EsbMessage[] esbMessages) {

        if (esbMessages==null) {
            return null;
        }
        ArrayList<EsbMessageEntity> list=new ArrayList<EsbMessageEntity>(esbMessages.length);
        for(EsbMessage esbMessage:esbMessages)
            list.add(convertFromEsbMessage(esbMessage));
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

}
