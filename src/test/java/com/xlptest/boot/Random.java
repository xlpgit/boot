//package com.xlptest.boot;
//
//import org.apache.commons.math3.linear.Array2DRowRealMatrix;
//import org.apache.commons.math3.linear.RealMatrix;
//import org.apache.commons.math3.random.*;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Vector;
//
///**
// * @author twjitm
// */
//public class Random {
//    public static List<Vector> Sample2D(float width, float height, float r, int k = 30)
//    {
//        return Sample2D((int)DateTime.Now.Ticks, width, height, r, k);
//    }
//
//    public static List<Vector2> Sample2D(int seed, float width, float height, float r, int k = 30)
//    {
//        // STEP 0
//
//        // 维度，平面就是2维
//        int n = 2;
//
//        // 计算出合理的cell大小
//        // cell是一个正方形，为了保证每个cell内部不可能出现多个点，那么cell内的任意点最远距离不能大于r
//        // 因为cell内最长的距离是对角线，假设对角线长度是r，那边长就是下面的cell_size
//        var cell_size = r / Math.Sqrt(n);
//
//        // 计算出有多少行列的cell
//        var cols = (int)Math.Ceiling(width / cell_size);
//        var rows = (int)Math.Ceiling(height / cell_size);
//
//        // cells记录了所有合法的点
//        var cells = new List<Vector2>();
//
//        // grids记录了每个cell内的点在cells里的索引，-1表示没有点
//        var grids = new int[rows, cols];
//        for (var i = 0; i < rows; ++i) {
//            for (var j = 0; j < cols; ++j) {
//                grids[i, j] = -1;
//            }
//        }
//
//        // STEP 1
//        var random = new Random(seed);
//
//        // 随机选一个起始点
//        var x0 = new Vector2(random.Range(width), random.Range(height));
//        var col = (int)Math.Floor(x0.x / cell_size);
//        var row = (int)Math.Floor(x0.y / cell_size);
//
//        var x0_idx = cells.Count;
//        cells.Add(x0);
//        grids[row, col] = x0_idx;
//
//        var active_list = new List<int>();
//        active_list.Add(x0_idx);
//
//        // STEP 2
//        while (active_list.Count > 0) {
//            // 随机选一个待处理的点xi
//            var xi_idx = active_list[random.Range(active_list.Count)]; // 区间是[0,1)，不用担心溢出。
//            var xi = cells[xi_idx];
//            var found = false;
//
//            // 以xi为中点，随机找与xi距离在[r,2r)的点xk，并判断该点的合法性
//            // 重复k次，如果都找不到，则把xi从active_list中去掉，认为xi附近已经没有合法点了
//            for (var i = 0; i < k; ++i) {
//                var dir = random.insideUnitCircle();
//                var xk = xi + (dir.normalized * r + dir * r); // [r,2r)
//                if (xk.x < 0 || xk.x >= width || xk.y < 0 || xk.y >= height) {
//                    continue;
//                }
//
//                col = (int)Math.Floor(xk.x / cell_size);
//                row = (int)Math.Floor(xk.y / cell_size);
//
//                if (grids[row, col] != -1) {
//                    continue;
//                }
//
//                // 要判断xk的合法性，就是要判断附近有没有点与xk的距离小于r
//                // 由于cell的边长小于r，所以只测试xk所在的cell的九宫格是不够的（考虑xk正好处于cell的边缘的情况）
//                // 正确做法是以xk为中心，做一个边长为2r的正方形，测试这个正方形覆盖到的所有cell
//                var ok = true;
//                var min_r = (int)Math.Floor((xk.y - r) / cell_size);
//                var max_r = (int)Math.Floor((xk.y + r) / cell_size);
//                var min_c = (int)Math.Floor((xk.x - r) / cell_size);
//                var max_c = (int)Math.Floor((xk.x + r) / cell_size);
//                for (var or = min_r; or <= max_r; ++or) {
//                    if (or < 0 || or >= rows) {
//                        continue;
//                    }
//
//                    for (var oc = min_c; oc <= max_c; ++oc) {
//                        if (oc < 0 || oc >= cols) {
//                            continue;
//                        }
//
//                        var xj_idx = grids[or, oc];
//                        if (xj_idx != -1) {
//                            var xj = cells[xj_idx];
//                            var dist = (xj - xk).magnitude;
//                            if (dist < r) {
//                                ok = false;
//                                goto end_of_distance_check;
//                            }
//                        }
//                    }
//                }
//
//                if (ok) {
//                    var xk_idx = cells.Count;
//                    cells.Add(xk);
//
//                    grids[row, col] = xk_idx;
//                    active_list.Add(xk_idx);
//
//                    found = true;
//                    break;
//                }
//            }
//
//            if (!found) {
//                active_list.Remove(xi_idx);
//            }
//        }
//
//        return cells;
//    }
//}
