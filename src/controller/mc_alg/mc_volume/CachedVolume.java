package controller.mc_alg.mc_volume;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import controller.DCMImage;

public class CachedVolume implements MCVolume {

    private class CacheMap extends LinkedHashMap<Integer, float[][]> {

        private final int maxEntries;

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

    private float[][] get(int z) {

        if (!cache.containsKey(z)) {
            cache.put(z, images.get(z).getImageRaster());
        }

        return cache.get(z);
    }

    @Override
    public float density(int x, int y, int z) {

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
