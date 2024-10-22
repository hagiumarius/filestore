package com.unitedinternet.filestore;

import com.unitedinternet.filestore.config.InitializingConfig;
import com.unitedinternet.filestore.controllers.FileOperation;
import com.unitedinternet.filestore.controllers.FileOperationEvent;
import com.unitedinternet.filestore.service.CachingService;
import com.unitedinternet.filestore.service.FileCounterListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
public class FileCounterListenerTest {

    @Mock
    private CachingService cachingService;

    @InjectMocks
    FileCounterListener fileCounterListener;

    @Test
    public void testHandleCreateEvent() {
        //setup
        when(cachingService.getValue(InitializingConfig.FILES_COUNT)).thenReturn("0");

        FileOperationEvent foe = new FileOperationEvent(this, "/categories/binoculars/hunting/filestorage.txt", FileOperation.CREATED);
        fileCounterListener.handleFileOperationEvent(foe);
        verify(cachingService, times(1)).updateKey(InitializingConfig.FILES_COUNT, "1");
    }

    @Test
    public void testHandleDeleteEvent() {
        //setup
        when(cachingService.getValue(InitializingConfig.FILES_COUNT)).thenReturn("3");

        FileOperationEvent foe = new FileOperationEvent(this, "/categories/binoculars/hunting/filestorage.txt", FileOperation.DELETED);
        fileCounterListener.handleFileOperationEvent(foe);
        verify(cachingService, times(1)).updateKey(InitializingConfig.FILES_COUNT, "2");
    }

}
