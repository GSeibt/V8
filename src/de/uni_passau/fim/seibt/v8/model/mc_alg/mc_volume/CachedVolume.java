package de.uni_passau.fim.seibt.v8.model.mc_alg.mc_volume;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.uni_passau.fim.seibt.v8.controller.DCMImage;

/**
 * A <code>MCVolume</code> that takes data from an internal FIFO cache of slices.
 * This implementation will call {@link de.uni_passau.fim.seibt.v8.controller.DCMImage#reset()} when the slice is
 * removed from the cache.
 */
public class CachedVolume implements MCVolume {

    /**
     * <code>LinkedHashMap</code> used to maintain the cache.
     */
    private class CacheMap extends LinkedHashMap<Integer, float[][]> {

        private final int maxEntries;

        /**
         * Constructs a new <code>CacheMap</code> that will drop its eldest entry when its size exceeds
         * <code>maxEntries</code>.
         *
         * @param maxEntries the maximum number of entries for this <code>Map</code>
         */
        private CacheMap(int maxEntries) {
            this.maxEntries = maxEntries;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<Integer, float[][]> eldest) {
            boolean remove = size() > maxEntries;

            if (remove) {
                images.get(eldest.getKey()).reset();
            }

            return remove;
        }
    }

    private List<DCMImage> images;
    private Map<Integer, float[][]> cache;
    private int xSize;
    private int ySize;

    /**
     * Constructs a new <code>CachedVolume</code> backed by the given <code>List</code> of <code>DCMImage</code>s.
     * The cache for this <code>CachedVolume</code> will contain at most <code>maxSize</code> image raster.
     *
     * @param images the <code>DCMImage</code>s to take data from
     * @param maxSize the maximum size of the data cache
     */
    public CachedVolume(List<DCMImage> images, int maxSize) {
        this.images = new ArrayList<>(images);
        this.cache = new CacheMap(maxSize);

        float[][] firstSlice;
        if (images.isEmpty()) {
            this.xSize = 0;
            this.ySize = 0;
        } else {
            firstSlice = get(0);
            this.xSize = firstSlice[0].length;
            this.ySize = firstSlice.length;
        }
    }

    /**
     * Returns the slice with index z from the cache.
     *
     * @param z the slice index
     * @return the slice
     */
    private float[][] get(int z) {

        if (!cache.containsKey(z)) {
            cache.put(z, images.get(z).getImageRaster());
        }

        return cache.get(z);
    }

    @Override
    public float value(int x, int y, int z) {

        if (z < 0 || z >= zSize()) {
            return 0f;
        }

        if (y < 0 || y >= ySize()) {
            return 0f;
        }

        if (x < 0 || x >= xSize()) {
            return 0f;
        }

        return get(z)[y][x];
    }

    @Override
    public int xSize() {
        return xSize;
    }

    @Override
    public int ySize() {
        return ySize;
    }

    @Override
    public int zSize() {
        return images.size();
    }
}
