/*
 * Copyright 2000-2018 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.flow.component.treegrid.it;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.function.ValueProvider;
import com.vaadin.flow.router.Route;

import javax.swing.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.BinaryOperator;

@Route("treegrid-details-row")
public class TreeGridDetailsRowPage extends Div {

    public TreeGridDetailsRowPage() {

        BigDecimal neutral = BigDecimal.ZERO;
        BinaryOperator<BigDecimal> sum = BigDecimal::add;

        SummaryTree.Leaf<BigDecimal> node1 = new SummaryTree.Leaf<>("Eins", Arrays.asList(new BigDecimal(1), new BigDecimal(2)));

        SummaryTree.Leaf<BigDecimal> node21 = new SummaryTree.Leaf<>("2.1", Arrays.asList(new BigDecimal(10), new BigDecimal(50)));
        SummaryTree.Leaf<BigDecimal> node221 = new SummaryTree.Leaf<>("2.2.1", Arrays.asList(new BigDecimal(20), new BigDecimal(60)));
        SummaryTree.Leaf<BigDecimal> node222 = new SummaryTree.Leaf<>("2.2.2", Arrays.asList(new BigDecimal(20), new BigDecimal(60)));
        SummaryTree.SummaryNode<BigDecimal> node22 = new SummaryTree.SummaryNode<>("2.2 (multiplied)", BigDecimal.ONE, BigDecimal::multiply, node221, node222);
        SummaryTree.Leaf<BigDecimal> node23 = new SummaryTree.Leaf<>("2.3", Arrays.asList(new BigDecimal(30), new BigDecimal(70)));
        SummaryTree.SummaryNode<BigDecimal> node2 = new SummaryTree.SummaryNode<>("Zwei", neutral, sum, node21, node22, node23);

        SummaryTree.Leaf node3 = new SummaryTree.Leaf<>("Drei", Arrays.asList(new BigDecimal(100), new BigDecimal(200)));

        SummaryTree.SummaryNode<BigDecimal> summaryNode = new SummaryTree.SummaryNode<BigDecimal>("Long long long long loooooooooong name", neutral, sum, node1, node2, node3);

        SummaryTree summaryTree = new SummaryTree(summaryNode);
        summaryTree.setSizeFull();

        add(summaryTree);

        setSizeFull();
    }




    public static class SummaryTree extends VerticalLayout {

        @SuppressWarnings("WeakerAccess")
        public static abstract class Node<T>{
            private final String name;
            Node(String name){
                this.name = name;
            }

            final String getName(){
                return this.name;
            }

            abstract T getValue(int which);
            abstract int getNumberOfValues();
            abstract Collection<Node<T>> getChildren();
        }

        public static class SummaryNode<T> extends Node<T>{

            private final ArrayList<Node<T>> children = new ArrayList<>();

            private final T neutral;
            private final BinaryOperator<T> summarizer;

            @SafeVarargs
            public SummaryNode(String name, T neutral, BinaryOperator<T> summarizer, Node<T>... children) {
                super(name);

                this.neutral = neutral;
                this.summarizer = summarizer;

                this.children.addAll(Arrays.asList(children));
            }

            public T getValue(int which){
                return children.stream().map(child->child.getValue(which)).reduce(neutral, summarizer);
            }

            public Collection<Node<T>> getChildren(){
                return Collections.unmodifiableList(children);
            }

            @Override
            int getNumberOfValues() {
                if (children.isEmpty()){
                    throw new RuntimeException("No children yet, don't know about their number of values.");
                }
                else{
                    return children.get(0).getNumberOfValues();
                }
            }
        }

        public static class Leaf<T> extends Node<T>{

            private final List<T> values;

            public Leaf(String name, List<T> values) {
                super(name);
                this.values = Collections.unmodifiableList(values);
            }

            @Override
            T getValue(int which) {
                return values.get(which);
            }

            @Override
            int getNumberOfValues() {
                return values.size();
            }

            @Override
            public Collection<Node<T>> getChildren(){
                return Collections.emptyList();
            }
        }


        public <T> SummaryTree (Node<T> root){
            TreeGrid<Node<T>> treeGrid = new TreeGrid<>();
            treeGrid.setSizeFull();

            ValueProvider<Node<T>, String> vp = Node::getName;
            Grid.Column<Node<T>> column = treeGrid.addHierarchyColumn(vp);
            column.setHeader("Name");
            //column.setAutoWidth(true);

            for (int i = 0; i < root.getNumberOfValues(); i++) {
                final int j=i;
                ValueProvider<Node<T>, T> valueProvider = n->n.getValue(j);

                Grid.Column<Node<T>> col = treeGrid.addColumn(valueProvider);
                col.setHeader("Value "+j);
                //col.setAutoWidth(true);
            }

            treeGrid.setItems(Collections.singleton(root), Node::getChildren);
          //  treeGrid.expandRecursively(Collections.singleton(root), Integer.MAX_VALUE);
            //treeGrid.recalculateColumnWidths();

            treeGrid.setItemDetailsRenderer(new ComponentRenderer<>(node -> new Button(node.getName() + (node.getChildren().isEmpty()? " LEAF" : " WITH CHILDREN"))));
            //treeGrid.setItemDetailsRenderer(new ComponentRenderer<>(() -> new Button("ABACUS")));
            treeGrid.setDetailsVisible(root, true);

            Button button = new Button("setDetailsVisible(root, true) and setDetailsVisibleOnClick(true)");
            button.addClickListener( evt -> {
                treeGrid.setDetailsVisible(root, true);
                treeGrid.setDetailsVisibleOnClick(true);
            });

            add(treeGrid, button);

        }

    }
}
