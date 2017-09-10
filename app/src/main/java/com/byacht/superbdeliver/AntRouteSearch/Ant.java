package com.byacht.superbdeliver.AntRouteSearch;

/**
 * Created by dn on 2017/7/31.
 */

public class Ant extends AntColonySystem {
    // 布尔型数组记录已经到达过的城市编号
    Boolean J[];
    // 记录tsp路径的城市编号
    int tour[];
    // 初始城市和当前城市编号
    int start, current;
    // 已经过的城市数目
    int count;
    Ant() {
        super();
        J = new Boolean[N];
        tour = new int[N];
    }
    void Move(int nextCity) {
        current = nextCity;
        J[current] = false;
        tour[count++] = current;
    }

    int Choose() {
        int nextCity = -1;
        double q = Math.random();
        // 若q <= Q_0，则按先验经验转移，否则按概率转移
        if (q <= Q_0) {
            double probability = -1.0;
            for (int i = 0; i < N; i++) {
                if(J[i]) {
                    double prob = Transition(current, i);
                    if (prob > probability) {
                        probability = prob;
                        nextCity = i;
                    }
                }
            }
        }
        else {
            // 按概率转移，使用轮盘赌选择
            double p = Math.random();
            double sum = 0.0;
            double probability = 0.0;
            for (int i = 0; i < N; i++) {
                if (J[i]) {
                    sum += Transition(current, i);
                }
            }
            for (int i = 0; i < N; i++) {
                if (J[i] && sum > 0) {
                    probability += Transition(current, i) / sum;
                    if (probability >= p || (p > 0.9999) && (probability > 0.9999)) {
                        nextCity = i;
                        break;
                    }
                }
            }
        }
        return nextCity;
    }
}
