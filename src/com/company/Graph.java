package com.company;

import java.util.*;
import java.util.function.Consumer;

public interface Graph {
    /**
     * Кол-во вершин в графе
     * @return
     */
    int vertexCount();

    /**
     * Кол-во ребер в графе
     * @return
     */
    int edgeCount();

    /**
     * Добавление ребра между вершинами с номерами v1 и v2
     * @param v1
     * @param v2
     */
    void addAdge(int v1, int v2);

    /**
     * Удаление ребра/ребер между вершинами с номерами v1 и v2
     * @param v1
     * @param v2
     */
    void removeAdge(int v1, int v2);

    /**
     * @param v Номер вершины, смежные с которой необходимо найти
     * @return Объект, поддерживающий итерацию по номерам связанных с v вершин
     */
    Iterable<Integer> adjacencies(int v);

    /**
     * Проверка смежности двух вершин
     * @param v1
     * @param v2
     * @return
     */
    default boolean isAdj(int v1, int v2) {
        for (Integer adj : adjacencies(v1)) {
            if (adj == v2) {
                return true;
            }
        }
        return false;
    }

    /**
     * Поиск в глубину, реализованный рекурсивно
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    default void dfsRecursionImpl(int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[vertexCount()];

        class Inner {
            void visit(Integer curr) {
                visitor.accept(curr);
                visited[curr] = true;
                for (Integer v : adjacencies(curr)) {
                    if (!visited[v]) {
                        visit(v);
                    }
                }
            }
        }
        new Inner().visit(from);
    }

    /**
     * Поиск в глубину, реализованный с помощью стека
     * (не совсем "правильный"/классический, т.к. "в глубину" реализуется только "план" обхода, а не сам обход)
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    default void dfsStackImpl(int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[vertexCount()];
        Stack<Integer> stack = new Stack<Integer>();
        stack.push(from);
        visited[from] = true;
        while (!stack.empty()) {
            Integer curr = stack.pop();
            visitor.accept(curr);
            for (Integer v : adjacencies(curr)) {
                if (!visited[v]) {
                    stack.push(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в ширину, реализованный с помощью очереди
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @param visitor Посетитель
     */
    default void bfsQueueImpl(int from, Consumer<Integer> visitor) {
        boolean[] visited = new boolean[vertexCount()];
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        visited[from] = true;
        while (queue.size() > 0) {
            Integer curr = queue.remove();
            visitor.accept(curr);
            for (Integer v : adjacencies(curr)) {
                if (!visited[v]) {
                    queue.add(v);
                    visited[v] = true;
                }
            }
        }
    }

    /**
     * Поиск в глубину в виде итератора
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    default Iterable<Integer> dfs(int from) {
        return new Iterable<Integer>() {
            private Stack<Integer> stack = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                stack = new Stack<>();
                stack.push(from);
                visited = new boolean[Graph.this.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! stack.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = stack.pop();
                        for (Integer adj : Graph.this.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                stack.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    /**
     * Поиск в ширину в виде итератора
     * (начальная вершина также включена)
     * @param from Вершина, с которой начинается поиск
     * @return Итератор
     */
    default Iterable<Integer> bfs(int from) {
        return new Iterable<Integer>() {
            private Queue<Integer> queue = null;
            private boolean[] visited = null;

            @Override
            public Iterator<Integer> iterator() {
                queue = new LinkedList<>();
                queue.add(from);
                visited = new boolean[Graph.this.vertexCount()];
                visited[from] = true;

                return new Iterator<Integer>() {
                    @Override
                    public boolean hasNext() {
                        return ! queue.isEmpty();
                    }

                    @Override
                    public Integer next() {
                        Integer result = queue.remove();
                        for (Integer adj : Graph.this.adjacencies(result)) {
                            if (!visited[adj]) {
                                visited[adj] = true;
                                queue.add(adj);
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }

    default String toDot() {
        StringBuilder sb = new StringBuilder();
        String nl = System.getProperty("line.separator");
        boolean isDigraph = this instanceof Digraph;
        sb.append(isDigraph ? "digraph" : "strict graph").append(" {").append(nl);
        for (int v1 = 0; v1 < vertexCount(); v1++) {
            int count = 0;
            for (Integer v2 : this.adjacencies(v1)) {
                sb.append(String.format("  %d %s %d", v1, (isDigraph ? "->" : "--"), v2)).append(nl);
                count++;
            }
            if (count == 0) {
                sb.append(v1).append(nl);
            }
        }
        sb.append("}").append(nl);

        return sb.toString();
    }

    default ArrayList<String> dfsRecursionFindAll(int from, int to) {
        boolean[] visited = new boolean[vertexCount()];
        ArrayList<String> ways = new ArrayList<String>();

        class Inner {
            String way = "";
            void visit(Integer curr) {
                visited[curr] = true;
                way += curr;
                if(curr == to){
                    ways.add(way);
                    visited[curr] = false;
                    way = way.substring(0, way.length()-1);
                    return;
                }
                for (Integer v : adjacencies(curr)) {
                    if (!visited[v]) {
                        visit(v);
                    }
                }
                visited[curr] = false;
                way = way.substring(0, way.length()-1);
            }
        }
        new Inner().visit(from);
        return ways;
    }

    default ArrayList<Integer> bfsQueueFindAll(int from, int to) {
        Boolean[] visited = new Boolean[vertexCount()];
        //List<List<Integer>> indOfWay = new ArrayList<>();
        ArrayList<Integer>[] indOfWay = new ArrayList[vertexCount()];
        for(int i = 0; i < indOfWay.length; i++){
            indOfWay[i] = new ArrayList<>();
        }
        Queue<Integer> queue = new LinkedList<>();
        List<Boolean[]> ways = new ArrayList<>();
        List<Integer> result = new ArrayList<>();

        queue.add(from);
        visited[from]= true;
        Boolean[] way0 = new Boolean[vertexCount()];
        way0[from] = true;
        ways.add(way0);
        indOfWay[from].add(0);

        while (queue.size() > 0) {
            Integer curr = queue.remove();
            // visitor.accept(curr);

            for ( int i = 0; i < indOfWay[curr].size(); i++){
                boolean flag = true;
                int ind = indOfWay[curr].get(i);
                Boolean[] wayCopy = ways.get(i).clone();

                for (Integer v : adjacencies(curr)) {
                    if(ways.get(ind)[v] != null){
                        // indOfWay[v].remove(ind);
                        continue;
                    }
                    if (flag) {
                        flag = false;
                        ways.get(ind)[v] = true;
                        indOfWay[v].add(ind);
                    } else {
                        Boolean[] wayi = wayCopy.clone();
                        wayi[v] = true;
                        ways.add(wayi);
                        indOfWay[v].add(ways.size() - 1);
                    }
                    if (v == to){
                        result.add(indOfWay[v].get(indOfWay[v].size() - 1));
                    }else {
                        if (visited[v] == null) {
                            queue.add(v);
                            visited[v] = true;
                        }
                    }
                }
            }


        }
        ArrayList<Integer> vres = new ArrayList<>();
        if (result.size() > 1){
            for( int i = 0; i < ways.get(0).length; i++){
                for (int j = 1; j < result.size(); j++){
                    if (ways.get(result.get(0))[i] == null){
                        break;
                    }
                    if (ways.get(result.get(j))[i] == null){
                        break;
                    }
                    vres.add(i);
                }

            }
        }
        else if (result.size() == 1 ){
            vres.add(-1);
        }
        else vres.add(-2);
        return vres;
    }

    default ArrayList<Integer> bfsQueueDelete(int from, int to) {
        ArrayList<Integer> result = new ArrayList<>();
        ArrayList<Integer> way = new ArrayList<>();
        ArrayList<Integer> firstWay = bfsFindWay(to, from, -1);

        int delete;
        if (firstWay != null){
            for(int i = 1; i < firstWay.size() - 1; i++){
                delete = firstWay.get(i);
                way = bfsFindWay(from, to, delete);
                    if (way.isEmpty()) result.add(delete);

            }
            if (result.isEmpty()) result.add(-2);
        }
        else result.add(-1);
        return result;
    }

    default ArrayList<Integer> bfsFindWay(int from, int to, int delete) {

        boolean[] visited = new boolean[vertexCount()];
        if (delete != -1) visited[delete] = true;
        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(from);
        Integer[] pr = new Integer[vertexCount()];
        pr[from] = -1;
        visited[from] = true;

        ArrayList<Integer> way = new ArrayList<>();

        while (queue.size() > 0) {
            Integer curr = queue.remove();

            for (Integer v : adjacencies(curr)) {
                if (v == to){
                    pr[v] = curr;
                    queue.clear();
                    break;
                }
                if (!visited[v]) {
                    pr[v] = curr;
                    queue.add(v);
                    visited[v] = true;
                }
            }
        }
       if (pr[to] != null) {
           for (int v = to; v != -1; v = pr[v] ) way.add(0, v);
       }
       return way;
    }


}
