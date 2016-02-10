package com.sangcomz.fishbun.bean;

public class MediaObject implements Comparable<MediaObject> {
    public boolean isSelected = false;
    private int id;
    private String path;
    private MediaType mediaType;
    private Long mediaTakenDateMillis;

    public MediaObject(int id, String path, MediaType mediaType, long mediaTakenDateMillis) {
        this.id = id;
        this.path = path;
        this.mediaType = mediaType;
        this.mediaTakenDateMillis = mediaTakenDateMillis;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int compareTo(MediaObject another) {
        return another.mediaTakenDateMillis.compareTo(mediaTakenDateMillis);
    }
}
