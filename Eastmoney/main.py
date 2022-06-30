import re
import time
import datetime

import pandas as pd
from selenium import webdriver

import xlwings as xw
from selenium.webdriver.common.by import By


def crawl():
    browser = webdriver.Chrome()
    browser.get("https://data.eastmoney.com/kzz/")
    time.sleep(2)

    dfPage = pd.DataFrame([[]])

    # 获取第一页的表格
    data = browser.page_source
    data_table = pd.read_html(data)  # 使用PANDAS读取页面源代码的所有表格
    dfPage = pd.concat([dfPage, data_table[1]], ignore_index=True)

    # 获取页数
    page_sum_patten = '<a href="javascript:" ;="" target="_self" data-page=".*?">(.*?)</a> '
    data_a = re.findall(page_sum_patten, data)
    page_sum = int(''.join(data_a))

    # # 点击下一页，并获取表格数据
    # for i in range(page_sum - 1):
    #     # 自动翻页
    #     p_data_a = '<div class="pagerbox">(.*?)</div>'
    #     data_a = re.findall(p_data_a, data)
    #     data_a_str = ''.join(data_a)
    #     page_a_num = str(data_a_str.count("</a>"))  # page_a_num为统计出来的</a>的数量
    #     browser.find_element(By.XPATH, '//*[@id="dataview"]/div[3]/div[1]/a[' + page_a_num + ']').click()
    #     time.sleep(2)  # 页面拉取数据需要时间，注意延迟
    #
    #     # 重新获取源码数据
    #     data = browser.page_source
    #     data_table = pd.read_html(data)  # 利用pandas获取其中的表格，注意结果里面包含有多个表格，是列表形式
    #     dfPage = pd.concat([dfPage, data_table[1]], ignore_index=True)

    browser.quit()  # 关闭浏览器

    # 数据清洗
    dfPage = dfPage.dropna(axis=0)  # 删除空行
    dfPage = dfPage.reset_index()  # 重建索引
    dfPage.drop(columns='index', inplace=True)  # 删除原来非索引的列

    # 重命名列名
    # column_Rename = dict()
    # for column in dfPage.columns.values:
    #     column_Rename.update({column: column[0]})
    # dfPage.rename(columns=column_Rename, inplace=True)
    dfPage.droplevel(0)
    dfPage.drop(columns='相关', axis=1, inplace=True)  # 删除无用信息列
    dfPage.drop(columns='申购上限(万元)', axis=1, inplace=True)  # 删除无用信息列

    today = (str(datetime.datetime.now()).split(' ')[0]).replace('-', '')  # 取得今天日期,并从datetime类型转换成字符串类型,去掉日期中的'-'
    dfPage.to_excel(today + '东方财富网可转债数据.xlsx')


# 按间距中的绿色按钮以运行脚本。
if __name__ == '__main__':
    crawl()

# 访问 https://www.jetbrains.com/help/pycharm/ 获取 PyCharm 帮助
