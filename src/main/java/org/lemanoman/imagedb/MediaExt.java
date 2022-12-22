package org.lemanoman.imagedb;

public class MediaExt {
    private String ext;
    private String contentType;

    public static MediaExt build(String ext, String contentType){
        return new MediaExt(ext, contentType);
    }

    public MediaExt(String ext, String contentType) {
        this.ext = ext;
        this.contentType = contentType;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
