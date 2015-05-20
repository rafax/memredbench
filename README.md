# Comparison of Redis and Memcached performance

Uses Jedis for Redis client and Shade for Memcached.
Run using java -server -Xmx3g -jar memredbench-assembly-1.0.jar 5

## Local env (MacBook Pro (15-inch, Mid 2012), 16GB ram, i7) Redis 3.0.1 Memcached 1.4.24
```
Run sequential
Starting redis
Took 9547 ms throughput 5237/s avg 0.19094 ms
Starting memcached
Took 16723 ms throughput 2989/s avg 0.33446 ms
Run parallel
Starting redis
Took 2982 ms throughput 16767/s avg 0.05964 ms
Starting memcached
Took 2581 ms throughput 19372/s avg 0.05162 ms
```

## AWS instance (m3.xlarge, Ubuntu 14.04) Redis 3.0.1 Memcached 1.4.14
```
Run sequential
Starting redis
Took 6067 ms throughput 8241/s avg 0.12134 ms
Starting memcached
Took 14041 ms throughput 3560/s avg 0.28082 ms
Run parallel
Starting redis
Took 2243 ms throughput 22291/s avg 0.04486 ms
Starting memcached
Took 3579 ms throughput 13970/s avg 0.07158 ms
```

## AWS instance (m3.xlarge, Ubuntu 14.04) with Elasticache backends -> cache.m3.medium Redis 2.8.19 Memcached 1.4.14
```
Run sequential
Starting redis
Took 22626 ms throughput 2209/s avg 0.45252 ms
Starting memcached
Took 32487 ms throughput 1539/s avg 0.64974 ms
Run parallel
Starting redis
Took 16960 ms throughput 2948/s avg 0.3392 ms
Starting memcached
Took 5435 ms throughput 9199/s avg 0.1087 ms
```