package com.vaadin.flow.component.grid.it;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.Route;

@Route("changing-data-size")
public class ChangingDataSizePage extends Div {

    private List<String> items;
    private boolean modifiedList;

    public ChangingDataSizePage() {
        Grid<String> grid = new Grid<>();
        grid.addColumn(s -> s);

        items = IntStream.range(0, 133).mapToObj(idx -> "Item " + idx)
                .collect(Collectors.toList());

        CallbackDataProvider<String, Void> dataProvider = DataProvider
                .<String> fromCallbacks(this::getItems, this::count);
        grid.setDataProvider(dataProvider);
        add(grid);
    }

    private Stream<String> getItems(Query<String, Void> query) {
        if (query.getOffset() >= 100 && !modifiedList) {
            items.remove(items.size() - 1);
            modifiedList = true;
        }
        List<String> subList = items.subList(query.getOffset(),
                query.getOffset() + Math.min(items.size() - query.getOffset(),
                        query.getLimit()));
        System.out.println("Query --> Offset: " + query.getOffset() + " Limit: "
                + query.getLimit() + " --> Returned " + subList.size()
                + " items");
        return subList.stream();
    }

    private int count(Query<String, Void> query) {
        System.out.println("Count --> Offset: " + query.getOffset() + " Limit: "
                + query.getLimit() + " --> Returned " + items.size());
        return items.size();
    }

}
