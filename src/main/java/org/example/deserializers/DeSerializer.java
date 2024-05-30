package org.example.deserializers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Generic DeSerializer for object & list of objects
 * @param <T> pojo of deserialize to
 */
public interface DeSerializer<T> {
    Logger LOGGER = Logger.getLogger(DeSerializer.class);
    ObjectMapper objectMapper = new ObjectMapper();
    default List<T> deserializeToList(String json, Class<T> clazz){
        try {
            JsonNode jsonNodes = objectMapper.readTree(json);
            List<T> objects = new ArrayList<>();
            if (jsonNodes.isArray()) {
                for(JsonNode cloudTrailEvent : jsonNodes) {
                    T cloudTrailEventObj  = objectMapper.treeToValue(cloudTrailEvent, clazz);
                    objects.add(cloudTrailEventObj);
                }
                return objects;
            } else {
                throw new IllegalArgumentException("Input json String should be a List of elements of type : " + clazz.getName());
            }
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing Object of type : " + clazz.getName() + ", error encountered :" + e);
            throw new RuntimeException(e);
        }
    }
    default T deserializeToObject(String json, Class<T> clazz){
        try {
            JsonNode jsonNode = objectMapper.readTree(json);
            if (jsonNode.isArray()) {
                throw new IllegalArgumentException("Input json string should be a single element of type: "+ clazz.getName());
            }
            return objectMapper.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error parsing Object of type : " + clazz.getName() + ", error encountered :" + e);
            throw new RuntimeException(e);
        }
    }
}
