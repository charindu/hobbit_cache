package cache;

import java.util.*;

public class HobbitCache<K, T> implements IHobbitCacheable<K, T>{

    private HashMap<K, T> cacheMap;

    protected class HobbitCacheObject {
        public long lastAccessed = System.currentTimeMillis();
        public T value;

        protected HobbitCacheObject(T value) {
            this.value = value;
        }
    }

    public HobbitCache( final long timeInterval, int max) {
        cacheMap = new HashMap<K, T>(max);

        if ( timeInterval > 0) {

            Thread t = new Thread(() -> {
                while (true) {
                    try {
                        Thread.sleep(timeInterval * 1000);
                    } catch (InterruptedException ex) {
                    }
                }
            });

            t.setDaemon(true);
            t.start();
        }
    }

    // PUT method
    public void put(K key, T value) {
        synchronized (cacheMap) {
            cacheMap.put(key, value);
        }
    }

    // GET method
    @SuppressWarnings("unchecked")
    public T get(K key) {
        synchronized (cacheMap) {

            Optional<HobbitCacheObject> hobbitCacheObjectOpt = Optional.ofNullable((HobbitCacheObject) cacheMap.get(key));
            if(hobbitCacheObjectOpt.isPresent()){
                hobbitCacheObjectOpt.get().lastAccessed = System.currentTimeMillis();
                return (T) hobbitCacheObjectOpt.get().value;
            }else {
                return null;
            }

        }
    }

    // EVICT method
    public void evict(K key) {
        synchronized (cacheMap) {
            cacheMap.remove(key);
        }
    }

    // CLEANUP method
    public void cleanup() {

        ArrayList<K> deleteKey;
        synchronized (cacheMap) {
            Iterator<?> itr = cacheMap.entrySet().iterator();
            deleteKey = new ArrayList<>((cacheMap.size() / 2) + 1);
            K key = null;
            while (itr.hasNext()) {
                Map.Entry pair = (Map.Entry)itr.next();
                key = (K) pair.getKey();
                Optional<T> hobbitCacheObjectOpt = Optional.ofNullable((T) pair.getValue());
                if (hobbitCacheObjectOpt.isPresent()) {
                    deleteKey.add(key);
                }
            }
        }

        for (K key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
            Thread.yield();
        }

    }

    //SIZE of the cacheMap
    public int size() {
        synchronized (cacheMap) {
            return cacheMap.size();
        }
    }
}
