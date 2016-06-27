package com.ft.methodeapi.service.methode;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.eidosmedia.wa.render.EomDbHelper;
import com.eidosmedia.wa.render.WebObject;
import com.ft.methodeapi.model.EomFile;
import com.ft.methodeapi.model.LinkedObject;
import com.ft.methodeapi.service.methode.connection.MethodeObjectFactory;
import com.google.common.base.Function;

public class MethodeFileRepositoryTest {
  private static final String LIST_UUID = UUID.randomUUID().toString();
  private static final String LIST_TYPE = "EOM::WebContainer";
  
  private MethodeFileRepository repository;
  private MethodeObjectFactory clientFactory = mock(MethodeObjectFactory.class);
  private MethodeObjectFactory testClientFactory = mock(MethodeObjectFactory.class);
  private EomDbHelper eomDbHelper = mock(EomDbHelper.class);
  
  @Before
  public void setUp() {
    Function<EOM.Session,EomDbHelper> f = new Function<EOM.Session,EomDbHelper>() {
      @Override
      public EomDbHelper apply(EOM.Session ignored) {
        return eomDbHelper;
      }
    };
    
    repository = new MethodeFileRepository.Builder()
        .withClientMethodeObjectFactory(clientFactory)
        .withTestClientMethodeObjectFactory(testClientFactory)
        .withEomDbSupplier(f)
        .build();
  }
  
  private WebObject mockListItem(String uuid) throws Exception {
    EOM.File methodeFile = mock(EOM.File.class);
    WebObject listItem = mock(WebObject.class);
    when(listItem.getUuid()).thenReturn(uuid);
    when(listItem.getEomFile()).thenReturn(methodeFile);
    
    return listItem;
  }
  
  @Test
  public void thatListCanBeRead() throws Exception {
    EOM.File methodeFile = mock(EOM.File.class);
    when(methodeFile.get_type_name()).thenReturn(LIST_TYPE);
    when(methodeFile.get_attributes()).thenReturn("");
    
    String uuidForListItem1 = UUID.randomUUID().toString();
    WebObject listItem1 = mockListItem(uuidForListItem1);
    
    String uuidForListItem2 = UUID.randomUUID().toString();
    WebObject listItem2 = mockListItem(uuidForListItem2);
    
    Map<String, WebObject[]> zones = Collections.singletonMap("zone1", new WebObject[] {listItem1, listItem2});
    
    WebObject webObject = mock(WebObject.class);
    when(webObject.getEomFile()).thenReturn(methodeFile);
    when(webObject.getLinked()).thenReturn(zones);
    
    when(eomDbHelper.getWebObjectByUuid(LIST_UUID)).thenReturn(webObject);
    
    EomFile actual = repository.findFileByUuid(LIST_UUID).get();
    
    assertThat(actual.getUuid(), equalTo(LIST_UUID));
    List<LinkedObject> linkedObjects = actual.getLinkedObjects();
    assertThat(linkedObjects.size(), equalTo(2));
    assertThat(linkedObjects.get(0).getUuid(), equalTo(uuidForListItem1));
    assertThat(linkedObjects.get(1).getUuid(), equalTo(uuidForListItem2));
    
    verify(methodeFile)._release();
  }
  
  @Test
  public void thatListWithUnreadableItemsCanBeRead() throws Exception {
    EOM.File methodeFile = mock(EOM.File.class);
    when(methodeFile.get_type_name()).thenReturn(LIST_TYPE);
    when(methodeFile.get_attributes()).thenReturn("");
    
    String uuidForListItem1 = UUID.randomUUID().toString();
    WebObject listItem1 = mockListItem(uuidForListItem1);
    
    String uuidForListItem3 = UUID.randomUUID().toString();
    WebObject listItem3 = mockListItem(uuidForListItem3);
    
    Map<String, WebObject[]> zones = Collections.singletonMap("zone1", new WebObject[] {listItem1, null, listItem3});
    
    WebObject webObject = mock(WebObject.class);
    when(webObject.getEomFile()).thenReturn(methodeFile);
    when(webObject.getLinked()).thenReturn(zones);
    
    when(eomDbHelper.getWebObjectByUuid(LIST_UUID)).thenReturn(webObject);
    
    EomFile actual = repository.findFileByUuid(LIST_UUID).get();
    
    assertThat(actual.getUuid(), equalTo(LIST_UUID));
    List<LinkedObject> linkedObjects = actual.getLinkedObjects();
    assertThat(linkedObjects.size(), equalTo(2));
    assertThat(linkedObjects.get(0).getUuid(), equalTo(uuidForListItem1));
    assertThat(linkedObjects.get(1).getUuid(), equalTo(uuidForListItem3));
    
    verify(methodeFile)._release();
  }
}
