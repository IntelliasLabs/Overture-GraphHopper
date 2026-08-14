package com.graphhopper.reader.overture.road.segment.spliter;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collector;

class ListIfNotEmptyOtherNull {

    static <T> Collector<T, ?, List<T>> toListIfNotEmptyOtherNull() {
        return Collector.of(
                ArrayList<T>::new,
                List::add,
                (l, r) -> {
                    l.addAll(r);
                    return l;
                },
                l -> l.isEmpty() ? Collections.emptyList() : l
        );
    }

}
