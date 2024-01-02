package at.technikum.apps.mtcg.entity;

public class UserData {
    private String name;
    private String bio;
    private String image;

    public UserData(){

    }

    UserData(String name, String bio, String image){
        this.name = name;
        this.bio = bio;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
