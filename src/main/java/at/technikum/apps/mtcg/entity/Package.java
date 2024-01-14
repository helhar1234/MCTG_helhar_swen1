package at.technikum.apps.mtcg.entity;

public class Package {
    private String id;
    private int price;

    private boolean sold;

    public Package() {
    }

    public Package(String id, int price, boolean sold) {
        this.id = id;
        this.price = price;
        this.sold = sold;
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

    public boolean isSold() {
        return sold;
    }

    public void setSold(boolean sold) {
        this.sold = sold;
    }
}
