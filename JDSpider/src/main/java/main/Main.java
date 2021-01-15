package main;

import database.JdbcUtils;
import parse.Parse;

import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author JwZheng
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        Scanner sc = new Scanner(System.in);
        System.out.print("起始页:");
        int start = sc.nextInt();
        System.out.print("结束页:");
        int end = sc.nextInt();
        System.out.print("是否下载图片（1-是\t0-否）：");
        boolean saveImage = sc.nextInt() == 1;

        //关闭
        sc.close();

        ExecutorService pool = Executors.newFixedThreadPool(8);

        String urlHeader = "https://search.jd.com/Search?keyword=Java&wq=Java";

        long startTime = System.currentTimeMillis();
        int startCount = JdbcUtils.getCount();

        for (int i = start; i <= end; i++) {
            //拼接每一页的URL
            String url = urlHeader + "&page=" + (2 * i - 1);
            pool.submit(new Parse(url, saveImage));
            if (i % 8 == 0) {
                Thread.sleep(5000);
            }
        }
        pool.shutdown();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

        int endCount = JdbcUtils.getCount();
        long endTime = System.currentTimeMillis();
        System.out.println("完成！" + "新增" + (endCount - startCount) + "条数据！\n" + "执行时间：" + (endTime - startTime) + "ms");
    }
}
