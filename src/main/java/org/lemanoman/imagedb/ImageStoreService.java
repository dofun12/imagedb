package org.lemanoman.imagedb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ImageStoreService {
    @Value("${imagedb.upload.path}")
    private String path;

    final ObjectMapper mapper = new ObjectMapper();

    @Value("${imagedb.metadata.path}")
    private String metadata;

    public boolean deleteTemporaryImage(String id){
        final var imageDb = getMetadata(id);
        if(imageDb==null){
            return false;
        }
        if(!imageDb.isTemporary()){
            return false;
        }

        File file = new File(imageDb.getPath());
        if(!file.exists()){
            return false;
        }

        return file.delete();
    }

    public ImageMetadataDto getMetadata(String id){
        File baseDir = new File(this.metadata);
        File temporaryBaseDir = getTemporaryMetadataDir(baseDir);
        File metadataJsonTemporary = new File(temporaryBaseDir, id);
        if(metadataJsonTemporary.exists()){
            try {
                return mapper.readValue(metadataJsonTemporary, ImageMetadataDto.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        File metadataJson = new File(baseDir, id);
        if(metadataJson.exists()){
            try {
                return mapper.readValue(metadataJson, ImageMetadataDto.class);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;



    }


    public List<ImageMetadataDto> findAllTemporary(){
        List<ImageMetadataDto> list = new ArrayList<>();
        File baseDir = new File(this.metadata);
        File temporaryDir = getTemporaryMetadataDir(baseDir);
        if(!temporaryDir.exists()|| temporaryDir.list()==null){
            return list;
        }

        for(String id:temporaryDir.list()){
            if(id==null){
                continue;
            }
            final var metadata = getMetadata(id);
            if(metadata==null){
                continue;
            }
            list.add(metadata);
        }
        return list;
    }
    public List<ImageMetadataDto> findAllPersistent(){
        List<ImageMetadataDto> list = new ArrayList<>();
        File baseDir = new File(this.metadata);
        if(!baseDir.exists()|| baseDir.list()==null){
            return list;
        }

        for(String id:baseDir.list()){
            if(id==null){
                continue;
            }
            final var metadata = getMetadata(id);
            if(metadata==null){
                continue;
            }
            list.add(metadata);
        }
        return list;
    }

    public void storeMetadata(String id, String filepath, boolean temporary){
        File baseDir = new File(this.metadata);
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }
        File temporaryDir = getTemporaryMetadataDir(baseDir);
        if(!temporaryDir.exists()){
            temporaryDir.mkdirs();
        }
        File outputBaseDir = baseDir;
        if(temporary){
            outputBaseDir = temporaryDir;
        }
        ObjectNode metadataJsonNode = mapper.createObjectNode();
        metadataJsonNode.put("id", id);
        metadataJsonNode.put("path", filepath);
        metadataJsonNode.put("temporary", temporary);
        metadataJsonNode.put("added", new Date().getTime());
        try {
            mapper.writeValue(new File(outputBaseDir, id), metadataJsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private File getTemporaryMetadataDir(File baseDir){
        return new File(baseDir, "tmp");
    }
}
