package com.ft.methodeapi.service.methode;

import static com.ft.methodeapi.service.methode.PathHelper.folderIsAncestor;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PathHelperTest {

    @Test
    public void folderIsNotAncestorOfEmptyPath() {
        assertFalse(folderIsAncestor(new String[]{"asdf"}, new String[]{}));
    }

    @Test
    public void folderIsNotAncestorIfPathsDiffer() {
        assertFalse(folderIsAncestor(new String[]{"asdf", "foo"}, new String[]{"asdf", "bar"}));
    }

    @Test
    public void folderIsAncestorOfSelf() {
        final String[] path = {"asdf", "foo"};
        assertTrue(folderIsAncestor(path, path));
    }

    @Test
    public void folderIsAncestorOfChild() {
        final String[] path = {"asdf", "foo"};
        final String[] child = {"asdf", "foo", "bar"};
        assertTrue(folderIsAncestor(path, child));
    }

}
