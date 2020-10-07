package cache;

public interface IHobbitCacheable<K, T> {

    void put(K key, T value);

    T get(K key);

    void evict(K key);

    void cleanup();

    int size();
}
