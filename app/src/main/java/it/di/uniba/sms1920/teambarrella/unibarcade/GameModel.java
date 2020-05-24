package it.di.uniba.sms1920.teambarrella.unibarcade;

public class GameModel {

    private int image;
    private String title;
    private String desc;

    public GameModel(int image, String title, String desc) {
        this.image = image;
        this.title = title;
        this.desc = desc;
    }

    public int getImage() {
        return image;
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }
}
