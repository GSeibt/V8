package controller.mc_alg;

public class WeightedVertex extends Vertex implements Cloneable {

    private Float weight;

    public WeightedVertex(float x, float y, float z, Float weight) {
        super(x, y, z);
        this.weight = weight;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) { this.weight = weight; }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s, weight=%s", getLocation(), getNormal(), weight);
    }

    @Override
    protected WeightedVertex clone() throws CloneNotSupportedException {
        return (WeightedVertex) super.clone();
    }
}
