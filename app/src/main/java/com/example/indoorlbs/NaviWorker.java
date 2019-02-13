package com.example.indoorlbs;

import java.util.PriorityQueue;

public class NaviWorker
{

    // 다익스트라 변수
    private int path[];
    private int beaconNum;          // 비콘 총 수량
    private boolean[] visit;        // 노드 방문 여부
    private int[] dist;             // 노드까지의 최단거리
    private int[][] ad = {
            { INF,7,9,INF,INF,14 },
            { 7,0,10,15,INF,INF },
            { 9,10,0,11,INF,2 },
            { INF,15,11,0,6,INF },
            { INF,INF,INF,6,0,9 },
            { 14,INF,2,INF,9,0 }};            // 특정 노드에서 다른 노드까지의 거리 정보

    private int[][] ad1 = {
            {0,1,2,INF,7,INF,INF,INF},
            {1,0,INF,2,INF,INF,4,INF},
            {2,INF,0,INF,INF,5,INF,INF},
            {INF,2,INF,0,INF,INF,1,INF},
            {7,INF,INF,INF,0,3,2,INF},
            {INF,INF,5,INF,3,0,INF,2},
            {INF,4,INF,1,2,INF,0,6},
            {INF,INF,INF,INF,INF,2,6,0}};

    private int[][] adReal ={
            {0, 15, INF, INF},
            {15, 0, 3, INF},
            {INF, 3, 0, 15},
            {INF, INF, 15, 0}};



    private int nV, route;             // Vertex
    static final int INF = 100000;  // 정보없는 경우 -> 무한대


    // 우선순위 큐
    private PriorityQueue<Element> q;


    // 비콘 노드 그래프 초기화
    public void init()
    {
        beaconNum   = adReal.length;


        visit       = new boolean[beaconNum];
        dist        = new int[beaconNum];
        path        = new int[beaconNum];


        temporary();
    }


    // 다익스트라 알고리즘
    public void dijkstra(int start, int dst) {

        for (int i = 0 ; i< path.length ;i++) path[i] = start;
        route = dst;

        //우선순위 큐에 처음 출발지점 정보 저장
        q = new <Element>PriorityQueue();
        dist[start] = 0;     // 자기 자신은 0으로 지정
        q.offer(new Element(start, dist[start]));


        // 큐가 없어질 때까지 반복
        while (!q.isEmpty()) {

            // 우선순위 큐는 완전 이진 트리 형태로 되어있다.
            // 큐에서 가장 짧은 노드의 큐를 Pop 한다.
            int cost = q.peek().getDistance();   // 거리 ( weight) 로서, 최상위 큐의 거리 반환
            int here = q.peek().getIndex();      // 노드 ( vertex ) 로서, 최상위 큐위 노드 반환
            q.poll();  // 최상위 큐를 꺼낸 후 삭제


            if (cost > dist[here])
                continue;


            if(visit[dst]) break;

            // 그래프 상의 모든 노드들을 탐색하며 here 노드의 주변 정보를 갱신한다.
            for (int v = 0; v < nV; v++) {

                // 1. 아직 처리가 되지 않은 노드이어야 하며 (무한루프 방지)
                // 2. here-v 간에 edge가 존재하고
                // 3. start부터 here까지의 경로가 존재하고
                // 4. 기존의 v노드까지의 최단거리 값보다 새로 계산되는 최단거리가 더 짧을 경우 최단거리 갱신

                if (!visit[here] && adReal[here][v] != INF)
                {
                    if (adReal[here][v] != INF && dist[v] > dist[here] + adReal[here][v])
                    {
                        // 최단 거리 갱신
                        // 갱신된 정보 큐에 Push
                        dist[v] = dist[here] + adReal[here][v];
                        q.offer(new Element(v, dist[v]));
                        path[v] = here; // 이전 경유를 저장해둬서 재귀로 호출
                    }
                }
            }
            // 이제 here 노드는 접근할 일이 없다. true 처리하여 진입 차단.
            visit[here] = true;
        }

        for (int i = 0 ; i < path.length ; i++){
            System.out.print(path[i]+"\t");
        }
        System.out.println();


    }

    public int[] getPath() { return path; }


    public void temporary()
    {
        nV = beaconNum;
        for (int i = 0; i<visit.length ; i++) visit[i] = false;
        for (int i = 0; i<dist.length ; i++) dist[i] = INF;
        for (int i = 0; i<path.length ; i++) path[i] = INF;
    }
}
