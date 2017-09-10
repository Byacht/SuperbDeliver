package com.byacht.superbdeliver.AntRouteSearch;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by dn on 2017/7/31.
 */

public class ACS {
    // 蚂蚁个数
    private final static int M = 3;
    private int[][] C;
    private final static int MAX_TIMES = 500;

    public int[] ACSalgorithm(int[][] city, double[][] distance) {
        C = city;
        AntColonySystem antColonySystem = new AntColonySystem();
        InitializeParameter(city, distance);
        int n = antColonySystem.N;
        Ant[] ants = new Ant[n];
        Random random = new Random();
        // 随机初始化起点城市
        for (int i = 0; i < M; i++) {
            ants[i] = new Ant();
            ants[i].start = random.nextInt(n);
        }
        // 全局最优路径
        int[] globalTour = new int[n];
        // 全局最优路径
        double Lgb = Double.MAX_VALUE;
        // 算法进行
        for (int m = 0; m < MAX_TIMES; m++) {
            // 最多迭代500次
            // 局部最优路径
            int[] localPath = new int[n];
            // 局部最优长度
            double Llb = Double.MAX_VALUE;
            // 当前路径长度
            double tourPath;
            // 索引记录
            int index = -1;
            // 初始化未赋值参数
            for (int k = 0; k < M; k++) {
                ants[k].count = 0;
                ants[k].tour[0] = ants[k].start;
                for (int i = 0; i < n; i++) {
                    ants[k].J[i] = true;
                }
                ants[k].Move(ants[k].start);
            }
            int nextCity;
            for (int i = 1; i < n; i++) {
                for (int k = 0; k < M; k++) {
                    nextCity = ants[k].Choose();
                    // 局部更新
                    ants[k].UpdateLocalPathRule(ants[k].current, nextCity);
                    ants[k].Move(nextCity);
                }
            }

            // tsp结束，回到起点城市
            for (int k = 0; k < M; k++) {
                ants[k].current = ants[k].tour[0];
            }

            // 计算当前最优路径值
            for (int k = 0; k < M; k++) {
                tourPath = ants[k].CalculatePathLength(ants[k].tour);
                if (tourPath < Llb) {
                    index = k;
                    Llb = tourPath;
                    localPath = ants[k].tour;
                }
            }
            // 全局比较
            if (Llb - Lgb < 0) {
                globalTour = localPath;
                Lgb = Llb;
            }
            AntColonySystem.UpdateGlobalPathRule(globalTour, Lgb);
        }
        ArrayList<Pair> tour = new ArrayList<Pair>();
        int points[] = new int[2 * n];
        int r, s;
        s = globalTour[1];
        for(int i = 0; i < n - 1; i++)  {
            r = globalTour[i];
            s = globalTour[i + 1];
            Point from = new Point(C[r][0], C[r][1]);
            Point to = new Point(C[s][0], C[s][1]);
            Pair object = new Pair(from, to);
            tour.add(object);
            points[2 * i] = r;
            points[2 * i + 1] = s;
        }
        // 从最后的城市回到起始城市
        r = globalTour[0];
        Point from  = new Point(C[s][0], C[s][1]);
        Point to = new Point(C[r][0], C[r][1]);
        tour.add(new Pair(from, to));
        return points;
    }

    void InitializeParameter(int[][] city, double[][] distance) {
        AntColonySystem.CalculateDistance(city, distance);
        AntColonySystem.CalculateLnn();
        AntColonySystem.InitialPheromone();
    }
    class Pair {
        Point from;
        Point to;
        Pair(Point r, Point s) {
            from = r;
            to = s;
        }
    }
}
