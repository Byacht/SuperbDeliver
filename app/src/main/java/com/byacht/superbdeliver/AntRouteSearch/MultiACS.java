package com.byacht.superbdeliver.AntRouteSearch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Update：2018/05/10
 * devidePop()：根据排序挑选 seed
 * ACSProcess()：加入对子种群中全局最优个体更新前的检查
 * 确定目前的最终版本
 */

public class MultiACS {
	// equiton3 参数
	private static final int BETA = 2;
	private static final int ALPHA = 1;
	// 选择概率
	private static final double q_0 = 0.5;
	// 全局、局部更新的参数
	private static final double RHO = 0.1;
	// 距离相似度阈值
	private static final double SIMILARITY = 0.8;
	// 子种群个数和种群大小
	private static final int POP_NUM = 3;
	private static final int POP_SIZE_INIT = 10;
	// 迭代次数（蚁群搜索次数）
	private static int iter_max = 800;

	private int M = 30;// 蚂蚁个数
	private int N;// 城市数目
	private double Lnn;// 贪婪算法求得的路径长度
	private double tau_0;// 信息素初始值	
	private double[][] distance;// 城市间距离矩阵
	private double[][][] tau;// 信息素矩阵
	
	public class Ant implements Comparable<Ant>, Cloneable {
		int cur;// 当前所在城市
		public int[] tour;// 旅行路径
		boolean[] past;// 已访问城市
		double length;// 总距离
		
		// 构造函数，初始化数组 tour、past
		// cur设置为输入参数、length置06
		Ant(int cur) {
			this.tour = new int[N];
			this.past = new boolean[N];
			
			this.cur = cur;			
			this.tour[0] = cur;
			for (boolean item : this.past)
				item = false;
			this.past[cur] = true;
			this.length = 0.0;
		}
		
		// 打印解路径和路径长度
		void print() {
			for (int tmp : tour)
				System.out.print(tmp + " ");
			System.out.println();
			System.out.println("length: " + length);
		}
		
		/* 实现compareTo、clone接口
		 * 用于对所有蚂蚁个体进行排序 sort
		 */
		// 实现Comparable接口
		@Override
		public int compareTo(Ant o) {
			// TODO 自动生成的方法存根
			return (length - o.length >= 0) ? 1 : -1 ;
		}
		// 实现深复制接口
		@Override
		public Object clone() {
			Ant tmp = null;
			try {
				tmp = (Ant)super.clone();
			} catch(CloneNotSupportedException e) {
				e.printStackTrace();
			}
			// 主要是对数组的深复制，避免引用同一数组
			tmp.tour = new int[N];
			for (int i = 0; i < N; i++)
				tmp.tour[i] = this.tour[i];
			tmp.past = new boolean[N];
			
			return tmp;
		}
		
	}
	
	// 用最佳ant 对象存储 Lgb 和对应的路线
	private Ant[] gBest;
	
	// 多种群Ant
	private ArrayList<ArrayList<Ant>> pop;
	
	// 贪心策略（最近邻）计算Lnn
	private void calculateLnn() {
		double sum = 0.0;
		double min;
		boolean[] visited = new boolean[N];
		int start, current, next = 0;
		int i, j;
		for (boolean item : visited)
			item = false;
		
		for (i = 0; i < POP_NUM; i++)
			gBest[i] = new Ant(0);// 初始化gBest
		
		start = current = (new Random()).nextInt(N);
		visited[start] = true;
		for (i = 1; i < N; i++) {
			min = Double.MAX_VALUE;
			for (j = 0; j < N; j++) {
				if (!visited[j] && distance[current][j] < min) {
					min = distance[current][j];
					next = j;
				}
			}
			sum += min;
			current = next;
			visited[current] = true;
			
			
		}
		sum += distance[current][start];
		Lnn = sum;
			
	}
	
	/* 初始化函数
	 * 由距离矩阵获取城市数 N
	 * 计算Lnn
	 * 初始化蚁群
	 */
	private void initialize(double[][] dist) {
		distance = dist;
		N = dist.length;
		gBest = new Ant[POP_NUM];
		pop = new ArrayList<ArrayList<Ant>>();
		
		// 初始化各信息素矩阵
		// 使用同一条贪心路径
		calculateLnn();
		tau_0 = 1.0 / (N * Lnn);
		tau = new double[POP_NUM][N][N];
		int i, j, t;
		for (t = 0; t < POP_NUM; t++) {
			for (i = 0; i < N; i++) 
				for (j = 0; j < N; j++) 
					tau[t][i][j] = tau_0;			
		}
		// 初始化蚁群的起点 0
		// 每个子种群的数量初始化为10
		int start = 0;
		for (i = 0; i < POP_NUM; i++) {
			pop.add(new ArrayList<Ant>());
			for (t = 0; t < POP_SIZE_INIT; t++) {
				start = 0;
				pop.get(i).add(new Ant(start));				
			}
		}
		// 初始化最优解的路径值
		for (Ant element : gBest) {
			element.length = Double.MAX_VALUE;
		}
	}
	
	/* 相似度计算重载版本1
	 * 根据邻接矩阵计算
	 * 由于邻接矩阵是对称的，同一对路径（a->b、 b->a）在计算中重复2次
	 */
	double similarity(boolean[][] seed, boolean[][] ant) {
		int i, j;
		double res = 0.0;
		for (i = 0; i < N; i++) {
			for (j = 0; j < N; j++) {
				if (seed[i][j] && ant[i][j])
					res++;
			}
		}
		res = res / (2.0 * N);
		return res;
	}
	// 相似度计算函数重载版本2
	double similarity(int[] a, int[] b) {
		boolean[][]	m1 = new boolean[N][N];
		boolean[][] m2 = new boolean[N][N];
		int i, j;
		
		for (i = 0; i < N; i++) {
			for (j = 0; j < N; j++) {
				m1[i][j] = false;
				m2[i][j] = false;
			}
		}
		
		int curr, next;
		
		for (i = 0; i < N; i++) {
			curr = a[i];
			next = a[(i + 1) % N];
			m1[curr][next] = true;
			m1[next][curr] = true;
		}
		for (i = 0; i < N; i++) {
			curr = b[i];
			next = b[(i + 1) % N];
			m2[curr][next] = true;
			m2[next][curr] = true;
		}
		
		return similarity(m1, m2);
	}
	
	// 子种群划分
	void devidePop() {
		boolean[][] m1 = new boolean[N][N];
		boolean[][] m2 = new boolean[N][N];
		boolean[][][] seed = new boolean[POP_NUM][N][N];
		int i, j, k, l;
		int curr, next;
		double[] simDist = new double[POP_NUM];

		// 做排序
		Ant[] ants = new Ant[M];
		int count = 0;
		for (i = 0; i < POP_NUM; i++) {
			for (j = 0; j < pop.get(i).size(); j++) {
				ants[count++] = (Ant) pop.get(i).get(j).clone();
			}
		}
		Arrays.sort(ants);
		
		for (i = 0; i < POP_NUM; i++) {
			pop.get(i).clear();
		}
		// 初始化seed个体的邻接矩阵
		for (k = 0; k < POP_NUM; k++) {
			for (i = 0; i < N; i++) {
				for (j = 0; j < N; j++) {
					seed[k][i][j] = false;
				}
			}
		}
		
		// 0号个体进入第一个种群
		// 种子个体的邻接矩阵
		pop.get(0).add(ants[0]);
		for (i = 0; i < N; i++) {
			curr = ants[0].tour[i];
			next = ants[0].tour[(i + 1) % N];
			seed[0][curr][next] = true;
			seed[0][next][curr] = true;
		}
		// 对余下的蚂蚁进行划分
		int index;
		boolean isAssign;
		for (k = 1; k < M; k++) {
			index = 0;
			isAssign = false;
			for (j = 0; j < POP_NUM; j++)
				simDist[j] = 0.0;
			// 记录当前个体的邻接矩阵
			for (i = 0; i < N; i++) {
				for (j = 0; j < N; j++) {
					m2[i][j] = false;
				}
			}
			for (i = 0; i < N; i++) {
				curr = ants[k].tour[i];
				next = ants[k].tour[(i + 1) % N];
				m2[curr][next] = true;
				m2[next][curr] = true;
			}
			
			// 根据相似度进行划分
			for (l = 0; l < POP_NUM; l++) {
				// 第2、3个种群一开始为空，有seed个体进入
				if (pop.get(l).isEmpty()) {
					// 当2或3为空，但是与seed 1或2的相似度满足阈值条件时，
					// 应先直接加入pop[index]的种群中
					if (simDist[index] >= SIMILARITY) {
						isAssign = true;
						pop.get(index).add(ants[k]);
						break;
					}
					else {// 加入空子种群，成为seed
						isAssign = true;
						pop.get(l).add(ants[k]);
						// 记录seed 的邻接矩阵
						for (i = 0; i < N; i++) {
							curr = ants[k].tour[i];
							next = ants[k].tour[(i + 1) % N];
							seed[l][curr][next] = true;
							seed[l][next][curr] = true;
						}
						break;
					}
				}
				// 计算相似度
				m1 = seed[l];
				simDist[l] = similarity(m1, m2);
				if (simDist[l] >= simDist[index])
					index = l;
			}
			// 如果最后都没能加入某个种群，选择距离最近的加入
			if (!isAssign)
				pop.get(index).add(ants[k]);	
		}
	}
	
	
	// 转移方程1，轮盘赌选择
	int equition1(boolean[] past, int curr, int pop_index) {
		int i, nextCity = -1;
		double sum = 0.0;
		double[] p = new double[N];
		double max;
		for (i = 0; i < N; i++) {
			if (!past[i]) {
				p[i] = Math.pow(tau[pop_index][curr][i], ALPHA) 
						* Math.pow(1 / distance[curr][i], BETA);
				sum += p[i];
			}
			else
				p[i] = 0.0;
		}
		// 轮盘赌
		double pro = Math.random();
		max = 0.0;
		for (i = 0; i < N; i++) {
			if (!past[i]) {
				p[i] = p[i] / sum;
				max += p[i];
				if (pro <= max || (pro > 0.9999) && (max > 0.9999)) {
					nextCity = i;
					return nextCity;
				}
			}
		}
		// 启发式信息计算值太小，溢出的情况处理
		// 选择返回未访问列表中的第一个
		if (!past[nextCity]) {
			for (i = 0; i < N; i++) {
				if (past[i]) {
					nextCity = i;
					break;
				}
					
			}
		}
		return nextCity;
	}
	
	// 转移方程3，贪心策略
	int equition3(boolean[] past, int curr, int pop_index) {
		int i, nextCity = -1;
		double val, max = -Double.MAX_VALUE;
		for (i = 0; i < N; i++) {
			if (!past[i]) {
				val = Math.pow(tau[pop_index][curr][i], ALPHA) 
						* Math.pow(1 / distance[curr][i], BETA);
				if (val > max) {
					max = val;
					nextCity = i;
				}
			}
		}
		return nextCity;
	}
	
	// 全局更新公式，使用各种群的seed ant 路径更新信息素
	void globalUpdate() {
		int i, l, curr, next = -1;
		double delta;
		for (l = 0; l < POP_NUM; l++) {
			delta = 1.0 / gBest[l].length;
			for (i = 0; i < N; i++) {
				curr = gBest[l].tour[i];
				next = gBest[l].tour[(i + 1) % N];
				tau[l][curr][next] = (1 - RHO)*tau[l][curr][next] + RHO * delta;
				tau[l][next][curr] = tau[l][curr][next];
			}
		}
	}
	
	void AcsProcess() {
		int i, k, l;
		double q;
		int curr, next = 0;
		// 重新初始化蚂蚁个体的路径、访问列表
		for (l = 0; l < POP_NUM; l++) {
			for (k = 0; k < pop.get(l).size(); k++) {
				pop.get(l).get(k).tour[0] = pop.get(l).get(k).cur;
				pop.get(l).get(k).length = 0.0;
				// 所有城市均未访问
				for (i = 0; i < N; i++) {
					pop.get(l).get(k).past[i] = false;
				}
				// 当前城市已访问
				pop.get(l).get(k).past[pop.get(l).get(k).cur] = true;
			}
		}
		
		// 进行遍历
		for (i = 1; i < N; i++) {
			for (l = 0; l < POP_NUM; l++) {
				for (k = 0; k < pop.get(l).size(); k++) {
					curr = pop.get(l).get(k).cur;
					q = Math.random();
					if (q <= q_0)
						next = equition3(pop.get(l).get(k).past, curr, l);
					else
						next = equition1(pop.get(l).get(k).past, curr, l);
					pop.get(l).get(k).past[next] = true;
					pop.get(l).get(k).tour[i] = next;
				}
			}
			// 局部更新公式
			for (l = 0; l < POP_NUM; l++) {
				for (k = 0; k < pop.get(l).size(); k++) {
					curr = pop.get(l).get(k).cur;
					next = pop.get(l).get(k).tour[i];
					tau[l][curr][next] = (1 - RHO)*tau[l][curr][next] + RHO*tau_0;
					tau[l][next][curr] = tau[l][curr][next];
					pop.get(l).get(k).cur = next;
				}
			}
		}
		// 回到起点城市
		for (l = 0; l < POP_NUM; l++) 
			for (k = 0; k < pop.get(l).size(); k++)
				pop.get(l).get(k).cur = pop.get(l).get(k).tour[0];
		
		// 计算gBest
		int index;
		for (l = 0; l < POP_NUM; l++) {
			index = -1;
			for (k = 0; k < pop.get(l).size(); k++) {
				// 重新计算路径长
				pop.get(l).get(k).length = 0.0;
				for (i = 0; i < N - 1; i++) {
					curr = pop.get(l).get(k).tour[i];
					next = pop.get(l).get(k).tour[i + 1];
					pop.get(l).get(k).length += distance[curr][next];
				}
				pop.get(l).get(k).length += distance[next][pop.get(l).get(k).tour[0]];
				// 发现有小于 历史最优 个体
				if (pop.get(l).get(k).length < gBest[l].length) {
					// 检查是否与其他种群的最优重复了
					boolean isSame = false;
					for (int t = 0; t < POP_NUM; t++) {
						if (l == t)	continue;
						if (similarity(pop.get(l).get(k).tour, gBest[t].tour) >= 0.99)
							isSame = true;
					}
					if (isSame)
						continue;
					else {
						gBest[l].length = pop.get(l).get(k).length;
						index = k;
					}
				}
			}
			if (index != -1) {
				for (i = 0; i < N; i++) {
					gBest[l].tour[i] = pop.get(l).get(index).tour[i];
				}
			}
		}
	
	}

	// 对int的swap()函数
	void swap1(int[] a, int i, int j){
		int tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	// 对double的swap()函数
	void swap2(double[] a, int i, int j) {
		double tmp = a[i];
		a[i] = a[j];
		a[j] = tmp;
	}
	
	// 接口一，直接使用距离矩阵进行计算
	public int[][] acs(double[][] dist) {
		initialize(dist);
		for (int i = 0; i < iter_max; i++) {
			AcsProcess();
			globalUpdate();
			// 每隔100代进行一次划分
			if (i % 100 == 0)
				devidePop();
		}

		
		int[] index = new int[POP_NUM];
		double[] len = new double[POP_NUM];
		for (int i = 0; i < POP_NUM; i++) {
			len[i] = gBest[i].length;
			index[i] = i;
		}
		int min;
		for (int i = 0; i < POP_NUM - 1; i++) {
			min = i;
			for (int j = i + 1; j < POP_NUM; j++) {
				if (len[min] > len[j])
					min = j;
			}
			swap1(index, i, min);
			swap2(len, i, min);	
		}
		
		int[][] result = new int[POP_NUM][2 * N];
		for (int l = 0; l < POP_NUM; l++) {
			for (int i = 0; i < N; i++) {
				result[l][2*i] = gBest[index[l]].tour[i];
				result[l][2*i + 1] = gBest[index[l]].tour[(i + 1)%N];
			}
		}
		
		return result;
	}
	
	// 接口二，使用城市坐标矩阵进行计算
	public int[][] acs(int[][] city) {
		N = city.length;
		double[][] dist = new double[N][N];
		int x, y;
		for (int i = 0; i < N; i++) {
			for (int j = 0; j < N; j++) {
				if (i == j)
					dist[i][j] = 0.0;
				else {
					x = city[i][0] - city[j][0];
					y = city[i][1] - city[j][1];
					dist[i][j] = Math.sqrt((x*x + y*y));
					dist[j][i] = dist[i][j];
				}
			}
		}
		initialize(dist);
		for (int i = 0; i < iter_max; i++) {
			AcsProcess();
			globalUpdate();
			if (i % 100 == 0)
				devidePop();
		}
		
		int[] index = new int[POP_NUM];
		double[] len = new double[POP_NUM];
		for (int i = 0; i < POP_NUM; i++) {
			len[i] = gBest[i].length;
			index[i] = i;
		}
		int min;
		for (int i = 0; i < POP_NUM - 1; i++) {
			min = i;
			for (int j = i + 1; j < POP_NUM; j++) {
				if (len[min] > len[j])
					min = j;
			}
			swap1(index, i, min);
			swap2(len, i, min);	
		}
		
		int[][] result = new int[POP_NUM][2 * N];
		for (int l = 0; l < POP_NUM; l++) {
			for (int i = 0; i < N; i++) {
				result[l][2*i] = gBest[index[l]].tour[i];
				result[l][2*i + 1] = gBest[index[l]].tour[(i + 1)%N];
			}
		}
		
		return result;
		
	}
	
	public MultiACS() { }
	
}
