package controller.mc_alg;

/**
 * A <code>Vertex</code> that has a weight associated with it.
 */
public class WeightedVertex extends Vertex implements Cloneable {

    private Float weight;

    /**
     * Constructs a new <code>WeightedVertex</code> with the given position and weight. Its normal will be (0, 0, 0).
     *
     * @param x the x coordinate of the <code>WeightedVertex</code>
     * @param y the y coordinate of the <code>WeightedVertex</code>
     * @param z the z coordinate of the <code>WeightedVertex</code>
     * @param weight the weight of the <code>WeightedVertex</code>
     */
    public WeightedVertex(float x, float y, float z, Float weight) {
        super(x, y, z);
        this.weight = weight;
    }

    /**
     * Sets the weight of this <code>WeightedVertex</code> to the given value.
     *
     * @param weight the new weight
     */
    public void setWeight(Float weight) { this.weight = weight; }

    /**
     * Returns the weight of this <code>WeightedVertex</code>.
     *
     * @return the weight
     */
    public Float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return String.format("location=%s, normal=%s, weight=%s", getLocation(), getNormal(), weight);
    }

    @Override
    protected WeightedVertex clone() throws CloneNotSupportedException {
        return (WeightedVertex) super.clone();
    }
}
