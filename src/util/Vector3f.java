package util;

/**
 * A vector in 3D space.
 */
public class Vector3f implements Cloneable {

    private float x;
    private float y;
    private float z;

    /**
     * Constructs a new <code>Vector</code> from the given coordinates.
     *
     * @param x
     *         the x-coordinate
     * @param y
     *         the y-coordinate
     * @param z
     *         the z-coordinate
     */
    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Returns a new <code>Vector</code> representing <code>this</code> normalized.
     *
     * @return the new normalized <code>Vector</code>
     */
    public Vector3f normalized() {
        float length = length();

        return new Vector3f(x / length, y / length, z / length);
    }

    /**
     * Returns the length of the vector.
     *
     * @return the length
     */
    public float length() {
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * Returns a new <code>Vector</code> representing <code>this</code> <code>Vector</code> rotated around the given
     * <code>axis</code> by the given <code>angle</code>.
     *
     * @param axis
     *         the rotation axis
     * @param angle
     *         the angle of rotation
     *
     * @return the new rotated <code>Vector</code>
     */
    public Vector3f rotate(Vector3f axis, float angle) {
        float sinAngle = (float) Math.sin(-angle);
        float cosAngle = (float) Math.cos(-angle);

        return this.cross(axis.mul(sinAngle)).add( //Rotation on local X
                (this.mul(cosAngle)).add( //Rotation on local Z
                        axis.mul(this.dot(axis.mul(1 - cosAngle))))); //Rotation on local Y
    }

    /**
     * Returns the dot-product of this <code>Vector</code> with the given <code>Vector r</code>.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the dot-product
     */
    public float dot(Vector3f r) {
        return x * r.getX() + y * r.getY() + z * r.getZ();
    }

    /**
     * Returns a new <code>Vector</code> representing the cross product of this <code>Vector</code> with the given
     * <code>Vector r</code>.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the cross-product
     */
    public Vector3f cross(Vector3f r) {
        float x_ = y * r.getZ() - z * r.getY();
        float y_ = z * r.getX() - x * r.getZ();
        float z_ = x * r.getY() - y * r.getX();

        return new Vector3f(x_, y_, z_);
    }

    /**
     * Returns a new <code>Vector</code> representing the sum of <code>this</code> <code>Vector</code> and the given
     * one.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the sum of the two vectors
     */
    public Vector3f add(Vector3f r) {
        return new Vector3f(x + r.getX(), y + r.getY(), z + r.getZ());
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public void setXYZ(float x, float y, float z) {
        setX(x);
        setY(y);
        setZ(z);
    }

    /**
     * Returns a new <code>Vector</code> representing the product of <code>this</code> <code>Vector</code> and the
     * given scalar.
     *
     * @param scalar
     *         the scalar
     *
     * @return the product of the <code>Vector</code> and the scalar
     */
    public Vector3f mul(float scalar) {
        return new Vector3f(x * scalar, y * scalar, z * scalar);
    }

    /**
     * Returns a new <code>Vector</code> representing the sum of <code>this</code> <code>Vector</code> and the given
     * scalar.
     *
     * @param scalar
     *         the scalar
     *
     * @return the sum of the <code>Vector</code> and the scalar
     */
    public Vector3f add(float scalar) {
        return new Vector3f(x + scalar, y + scalar, z + scalar);
    }

    /**
     * Returns a new <code>Vector</code> representing the difference of <code>this</code> <code>Vector</code> and the
     * given one.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the difference of the two vectors
     */
    public Vector3f sub(Vector3f r) {
        return new Vector3f(x - r.getX(), y - r.getY(), z - r.getZ());
    }

    /**
     * Returns a new <code>Vector</code> representing the difference of <code>this</code> <code>Vector</code> and the
     * given scalar.
     *
     * @param scalar
     *         the scalar
     *
     * @return the difference of the <code>Vector</code> and the scalar
     */
    public Vector3f sub(float scalar) {
        return new Vector3f(x - scalar, y - scalar, z - scalar);
    }

    /**
     * Returns a new <code>Vector</code> representing the product of <code>this</code> <code>Vector</code> and the
     * given one.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the product of the two vectors
     */
    public Vector3f mul(Vector3f r) {
        return new Vector3f(x * r.getX(), y * r.getY(), z * r.getZ());
    }

    /**
     * Returns a new <code>Vector</code> representing the quotient of <code>this</code> <code>Vector</code> and the
     * given one.
     *
     * @param r
     *         the other <code>Vector</code>
     *
     * @return the quotient of the two vectors
     */
    public Vector3f div(Vector3f r) {
        return new Vector3f(x / r.getX(), y / r.getY(), z / r.getZ());
    }

    /**
     * Returns a new <code>Vector</code> representing the quotient of <code>this</code> <code>Vector</code> and the
     * given scalar.
     *
     * @param scalar
     *         the scalar
     *
     * @return the quotient of the <code>Vector</code> and the scalar
     */
    public Vector3f div(float scalar) {
        return new Vector3f(x / scalar, y / scalar, z / scalar);
    }

    /**
     * Returns a new <code>Vector</code> representing the absolute value of this <code>Vector</code>.
     *
     * @return the absolute <code>Vector</code>
     */
    public Vector3f abs() {
        return new Vector3f(Math.abs(x), Math.abs(y), Math.abs(z));
    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        result = 31 * result + (z != +0.0f ? Float.floatToIntBits(z) : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Vector3f vector3f = (Vector3f) o;

        if (Float.compare(vector3f.x, x) != 0) {
            return false;
        }
        if (Float.compare(vector3f.y, y) != 0) {
            return false;
        }
        if (Float.compare(vector3f.z, z) != 0) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("(%s %s %s)", x, y, z);
    }

    @Override
    public Vector3f clone() throws CloneNotSupportedException {
        return (Vector3f) super.clone();
    }
}
