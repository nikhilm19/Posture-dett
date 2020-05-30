package com.example.posturead;

public class ImagesListData{
    private String description,url;

    public ImagesListData(String description, String url) {
        this.description = description;
        this.url = url;
    }

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){

        this.url=url;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

}
