package org.lemanoman.imagedb;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

    private ImageStoreService imageStoreService;

    public ScheduledTasks(ImageStoreService imageStoreService){
        this.imageStoreService = imageStoreService;
    }
    @Scheduled(fixedRate = 60000)
    public void reportCurrentTime() {
        for(ImageMetadataDto item: imageStoreService.findAllTemporary()){
            if(item==null){
                continue;
            }
            if(!item.isTemporary()){
                continue;
            }
            long timeNow = new Date().getTime();
            if((timeNow-item.getAdded())<3600000){
                continue;
            }
            this.imageStoreService.deleteTemporaryImage(item.getId());
            log.info("Deletando arquivo temporario {}", item.getId());
        }

    }
}