package ua.kernel.dabbd.triggers.functions.util;

import org.apache.flink.util.Collector;

import java.util.List;

public class TestCollector<T> implements Collector<T> {

        public final List<T> list;

        public TestCollector(List<T> list) {
            this.list = list;
        }

        @Override
        public void collect(T record) {
            list.add(record);
        }

        @Override
        public void close() {}

    }