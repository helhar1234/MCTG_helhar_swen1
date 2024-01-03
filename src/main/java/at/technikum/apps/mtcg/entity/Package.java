package at.technikum.apps.mtcg.entity;

public class Package {
    private String id;
    private int price;

    public Package(){}

    Package(String id, int price){
        this.id = id;
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }
}
