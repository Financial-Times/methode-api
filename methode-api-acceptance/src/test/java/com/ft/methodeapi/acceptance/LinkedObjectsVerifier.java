package com.ft.methodeapi.acceptance;

import com.ft.methodeapi.model.LinkedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Facilitates matching contents of the list of Linked Objects, that is the list of articles.
 *
 * Created by julia.fernee on 01/09/2015.
 */
public class LinkedObjectsVerifier {

        public static List<String> mapUuid(List<LinkedObject> list) {
        List<String> uuids = new ArrayList<>();
        for(LinkedObject linked : list) {
            uuids.add(linked.getUuid());
        }
        return uuids;
    }

    public static List<String> mapType(List<LinkedObject> list) {
        List<String> type = new ArrayList<>();
        for(LinkedObject linked : list) {
            type.add(linked.getType());
        }
        return type;
    }

    public static boolean hasWorkflowStatusProperty(List<LinkedObject> list) {
        for(LinkedObject linked : list) {
            if(linked.getWorkflowStatus()==null) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasAttributesProperty(List<LinkedObject> list) {

        for(LinkedObject linked : list) {
            if(linked.getAttributes()==null) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSystemAttributesProperty(List<LinkedObject> list) {
        for(LinkedObject linked : list) {
            if(linked.getSystemAttributes()==null) {
                return false;
            }
        }
        return true;
    }

}
