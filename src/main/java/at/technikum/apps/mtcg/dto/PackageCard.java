package at.technikum.apps.mtcg.dto;

public class PackageCard {
    private String id;
    private String name;
    private int damage;

    PackageCard() {
    }

    PackageCard(String id, String name, int damage) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }
}
