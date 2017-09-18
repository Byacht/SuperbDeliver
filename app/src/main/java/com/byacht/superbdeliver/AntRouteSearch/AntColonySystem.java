package com.byacht.superbdeliver.AntRouteSearch;

import android.util.Log;

import java.util.Random;

/**
 * Created by dn on 2017/7/31.
 */

public class AntColonySystem {
    // 参数列表
    final static int  BETA = 2;
    final static int A = 1;
    final static double ROU = 0.1;
    final static double ALPHA = 0.1;
    final static double Q_0 = 0.9;
    static int N;
    private static double Lnn;
    static double tau_0;
    // 城市距离矩阵
    static double distance[][];
    // 信息素矩阵
    static double tau[][];

    AntColonySystem() {

    }

    static void CalculateDistance(int[][] city, double[][] dis) {
        // 初始化距离矩阵
        N = city.length;
//        distance = new double[N][N];
//        for (int i = 0; i < N; i++) {
//            for (int j = 0;j < N; j++) {
//                if(i == j)
//                    distance[i][j] = 0.0;
//                else {
//                    int x = city[i][0] - city[j][0];
//                    int y = city[i][1] - city[j][1];
//                    distance[i][j] = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
//                    distance[j][i] = distance[i][j];
//                }
//            }
//        }
        distance = dis;
        for (int i = 0; i < distance.length; i ++) {
            for (int j = 0; j < distance[0].length; j++) {
                Log.d("htout", "distance " + i + "<->" + j + ":" + distance[i][j]);
            }
        }

    }

    // 使用最邻近规则计算Lnn
    static void CalculateLnn() {
        double sum = 0.0;
        Boolean[] noVisited = new Boolean[N];
        int[] tour = new int[N];
        Random random = new Random();
        for (int i = 0; i < N; i++)
            noVisited[i] = true;
        int start, current, next;
        start = current = random.nextInt(N);
        noVisited[current] = false;
        for (int i = 1; i < N; i++) {
            next = ChooseNextNode(current, noVisited);
            if (next >= 0) {
                sum += distance[current][next];
                current = next;
                noVisited[current] = false;
            }
        }
        sum += distance[start][current];
        Lnn = sum;
    }

    static int ChooseNextNode(int current, Boolean[] noVisited) {
        int next = -1;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i < N; i++) {
            // 去掉已经走过的节点，从剩下的节点中选择最短的
            if (noVisited[i]) {
                if (distance[current][i] - minDist < 0) {
                    minDist = distance[current][i];
                    next = i;
                }
            }
        }
        return next;
    }

    static void InitialPheromone() {
        // 初始化信息素
        tau = new double[N][N];
        tau_0 = 1.0 / (N * Lnn);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                tau[i][j] = tau_0;
            }
        }
    }

    static double CalculatePathLength(int[] tour) {
        double sum = 0.0;
        int r, s;
        for (int i = 0; i < N - 1; i++) {
            r = tour[i];
            s = tour[i + 1];
            sum += distance[r][s];
        }
        r = tour[0];
        s = tour[N - 1];
        sum += distance[s][r];
        return sum;
    }

    // 计算当前节点到下一节点的概率
    double Transition(int r, int s) {
        // r是当前所在城市
        // s是待选择的可达城市
        if (r != s) {
            return (Math.pow(tau[r][s], A) * Math.pow(1 / distance[r][s], BETA));
        }
        else {
            return 0.0;
        }
    }

    // 局部更新规则
    void UpdateLocalPathRule(int r, int s) {
//        Log.d("htout", "r:" + r + " s:" + s);
        tau[r][s] = (1.0 - ALPHA) * tau[r][s] + ALPHA * (1.0 / (N * Lnn));
        tau[s][r] = tau[r][s];
    }

    // 全局更新规则
    static void UpdateGlobalPathRule(int[] bestTour, double globalBestLength) {
        int r, s;
        for(int i = 0; i < N - 1; i++) {
            r = bestTour[i];
            s = bestTour[i+1];
            tau[r][s] = (1.0 - ROU) * tau[r][s] + ROU * (1.0 / globalBestLength);
            tau[s][r] = tau[r][s];
        }
        r = bestTour[0];
        s = bestTour[N -1];
        tau[r][s] = (1.0 - ROU) * tau[r][s] + ROU * (1.0 / globalBestLength);
        tau[s][s] = tau[r][s];
    }

}
