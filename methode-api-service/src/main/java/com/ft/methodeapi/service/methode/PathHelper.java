package com.ft.methodeapi.service.methode;

public class PathHelper {

    public static boolean folderIsAncestor(String[] ancestor, String[] descendant) {
        if (ancestor.length > descendant.length) {
            return false;
        }
        for (int i = 0; i < ancestor.length; i++) {
            if (!ancestor[i].equals(descendant[i])) {
                return false;
            }
        }
        return true;
    }

}
