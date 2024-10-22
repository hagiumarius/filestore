package com.unitedinternet.filestore;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.controllers.FileOperation;
import com.unitedinternet.filestore.controllers.FileOperationEvent;
import com.unitedinternet.filestore.service.CachingService;
import com.unitedinternet.filestore.service.RegexFilesListener;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
public class RegexFileListenerTest {

    @Mock
    private CachingService cachingService;

    @InjectMocks
    RegexFilesListener regexFileListener;

    @Test
    public void testHandleCreateEvent() {
        //setup
        when(cachingService.exists(InitializingConfig.REGEX_LIST)).thenReturn(true);
        when(cachingService.getElementsFromSet(InitializingConfig.REGEX_LIST)).thenReturn(Sets.set("^.*ST.*$"));

        FileOperationEvent foe = new FileOperationEvent(this, "/categories/binoculars/hunting/test.txt", FileOperation.CREATED);
        regexFileListener.handleFileOperationEvent(foe);
        verify(cachingService, times(1)).addToList("^.*ST.*$", "/categories/binoculars/hunting/test.txt");
    }

    @Test
    public void testHandleDeleteEvent() {
        //setup
        when(cachingService.exists(InitializingConfig.REGEX_LIST)).thenReturn(true);
        when(cachingService.getElementsFromSet(InitializingConfig.REGEX_LIST)).thenReturn(Sets.set("^.*ST.*$"));

        FileOperationEvent foe = new FileOperationEvent(this, "/categories/binoculars/hunting/test.txt", FileOperation.DELETED);
        regexFileListener.handleFileOperationEvent(foe);
        verify(cachingService, times(1)).removeFromList("^.*ST.*$", "/categories/binoculars/hunting/test.txt");
    }

}
