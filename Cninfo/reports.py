#!/usr/bin/env python
import re
import sys
from datetime import date

import requests
import os
import time

import logging
from selenium import webdriver

import pythoncom
import tushare as ts
import pandas as pd
import matplotlib.pyplot as plt
import xlwings as xw

from selenium.webdriver import ActionChains
from selenium.webdriver.common.by import By


class CnInfoReports:

    def __init__(self):
        self.headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:96.0) Gecko/20100101 Firefox/96.0',
            'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
            'X-Requested-With': 'XMLHttpRequest',
            'Origin': 'http://www.cninfo.com.cn',
            'Referer': 'http://www.cninfo.com.cn/new/commonUrl/pageOfSearch?url=disclosure/list/search&lastPage=index',
        }
        self.app = xw.App(visible=False)
        # 目前有效的沪深股票信息
        self.all_shsz_stock = self.get_stock_json('http://www.cninfo.com.cn/new/data/szse_stock.json', '沪深')
        # 目前有效的港股信息
        self.all_hk_stock = self.get_stock_json('http://www.cninfo.com.cn/new/data/hke_stock.json', '港股')
        # 公告
        self.query_all_url = 'http://www.cninfo.com.cn/new/fulltextSearch?notautosubmit=&keyWord='
        self.query_url = 'http://www.cninfo.com.cn/new/disclosure/stock?stockCode={}&orgId={}#companyProfile'

    # 获取最新的股票代码信息，用于筛选用户输入
    def get_stock_json(self, url: str, name: str = '沪深') -> dict:
        stock_json = requests.get(url, headers=self.headers).json()
        stockList = stock_json['stockList']
        print(f'成功更新{len(stockList)}条{name}股票信息')
        stockDict = {each['code']: each for each in stockList}
        return stockDict

    # 筛选用户输入的代码
    def remove_invalid_stock(self, stock_list: list):
        valid_shsz_stock = []
        valid_hk_stock = []
        for stock in stock_list:
            if len(stock) == 6:
                try:
                    this_stock = self.all_shsz_stock[stock]
                    valid_shsz_stock.append((this_stock['orgId'], re.sub('\*', '', this_stock['zwjc']), stock))
                except KeyError:
                    print(f'【{stock}】 证券代码无效，跳过')
                    continue
            elif len(stock) == 5:
                try:
                    this_stock = self.all_hk_stock[stock]
                    valid_hk_stock.append((this_stock['orgId'], re.sub('\*', '', this_stock['zwjc']), stock))
                except KeyError:
                    print(f'【{stock} 】 证券代码无效，跳过')
                    continue
            else:
                print(f'【{stock}】 请确保代码为六位数字（A股）或五位数字（港股）')

        return valid_shsz_stock, valid_hk_stock

    # 获取历史行情数据，并画图分析，保存到excel中
    def history_data(self, stock_tuple: tuple, seDate: str) -> None:
        splits = seDate.split('~')
        orgId, name, code = stock_tuple
        stock_info = ts.get_hist_data(code, start=splits[0], end=splits[1])

        stock_table = pd.DataFrame()
        print(f'【{code}】 开始查询历史行情')

        for current_date in stock_info.index:
            current_k_line = stock_info.loc[current_date]

            current_stock_info = {
                '日期': pd.to_datetime(current_date),
                '开盘价': current_k_line.open,
                '收盘价': current_k_line.close,
                '涨跌额': current_k_line.price_change,
                '股价涨跌幅(%)': current_k_line.p_change,
                '成交量(手)': current_k_line.volume,
                '换手率': current_k_line.turnover,
                'MA5': current_k_line.ma5,
                'MA10': current_k_line.ma10,
                'MA20': current_k_line.ma20,
            }
            stock_table = pd.concat([stock_table, pd.DataFrame(current_stock_info, index=[1])], ignore_index=True)

        stock_table = stock_table.set_index('日期')
        order = ['开盘价', '收盘价', '涨跌额', '股价涨跌幅(%)', '成交量(手)', '换手率', 'MA5', 'MA10', 'MA20']
        stock_table = stock_table[order]

        wb = self.app.books.add()
        sht = wb.sheets.add("历史行情")
        sht.range("A:A").api.NumberFormat = "@"
        sht.range('A1').value = stock_table

        # 数据可视化，并将图片导入到Excel当中
        fig = plt.figure(figsize=(12, 6))
        plt.rcParams['font.sans-serif'] = ['SimHei']
        plt.rcParams['axes.unicode_minus'] = False

        # 绘制第一个折线图：股价涨跌幅(%)
        plt.plot(stock_table.index, stock_table['开盘价'].apply(lambda x: abs(x)), label='开盘价', color='red')
        plt.legend(loc='upper left')  # 设置图例位置

        plt.twinx()
        plt.plot(stock_table.index, stock_table['收盘价'].apply(lambda x: abs(x)), label='收盘价', color='blue')
        plt.legend(loc='best')

        # 设置图片标题，自动调整x坐标轴刻度的角度并展示图片
        plt.title(name)  # 设置标题
        plt.gcf().autofmt_xdate()  # 自动调整x坐标轴刻度的角度

        # 把图片放到excel中
        sht.pictures.add(fig, name='图1', update=True, left=500)

        if not os.path.exists('data/' + code + '_' + name):
            os.makedirs('data/' + code + '_' + name)

        path = 'data/' + code + '_' + name + '/' + code + '_' + name + '_历史行情.xlsx'
        wb.save(path)
        wb.close()
        print(f'【{code}】 查询完毕，已保存到{path}')

    # 下载指定日期的报告，默认只下载一页，设置page为99999可全部下载
    def download_report(self, stock_tuple: tuple, page=1, seDate='2021-09-01~2022-01-01'):
        orgId, name, code = stock_tuple
        splits = seDate.split('~')

        options = webdriver.ChromeOptions()
        # 隐藏浏览器界面
        options.add_argument('headless')
        browser = webdriver.Chrome(options=options)

        browser.get(self.query_all_url + name)
        time.sleep(3)
        browser.find_element(by=By.XPATH,
                             value='//form[@id="calendar"]/div[1]/span[1]/div[1]/div[1]/input[1]').send_keys(splits[0])
        browser.find_element(by=By.XPATH,
                             value='//form[@id="calendar"]/div[1]/span[1]/div[1]/div[1]/input[2]').send_keys(splits[1])

        action = ActionChains(browser)
        action.move_by_offset(200, 100).click().perform()
        time.sleep(5)
        data = browser.find_element(by=By.XPATH,
                                    value="//div[@id='fulltext-search']/div[1]/div[1]/div[2]").get_attribute(
            'innerHTML')

        p_count = '<span class="total-box" style="">共(.*?)条'
        count = re.findall(p_count, data)[0]
        pages = int(int(count) / 10)
        pages = min(page - 1, pages)

        # 1.自动翻页获取源码源代码
        datas = [data]
        # 默认下载 2 页
        for i in range(pages):
            browser.find_element(by=By.XPATH,
                                 value='//*[@id="fulltext-search"]/div/div[1]/div[2]/div[4]/div[2]/div/button[2]').click()
            time.sleep(2)
            data = browser \
                .find_element(by=By.XPATH,
                              value="//div[@id='fulltext-search']/div[1]/div[1]/div[2]") \
                .get_attribute('innerHTML')
            datas.append(data)
            time.sleep(1)
        alldata = "".join(datas)
        browser.quit()

        # 2.编写正则表达式
        p_title = '<span title="" class="r-title">(.*?)</span>'
        p_href = '<a target="_blank" href="(.*?)" data-id='
        p_date = '<span class="time">(.*?)</span>'
        title = re.findall(p_title, alldata)
        href = re.findall(p_href, alldata)
        date = re.findall(p_date, alldata, re.S)

        table = pd.DataFrame()

        # 3.清洗数据
        for i in range(len(title)):
            info = {
                'title': re.sub('<.*?>', '', title[i]),
                'date': date[i].strip().split(' ')[0],
                'url': 'http://www.cninfo.com.cn' + re.sub('amp;', '', href[i]),
            }
            info = pd.DataFrame(info, index=[1])
            table = pd.concat([table, info], ignore_index=True)

        table = table.set_index('date')
        table = table[['title', 'url']]

        wb = self.app.books.add()
        sht = wb.sheets.add("公告列表")
        sht.range("A:A").api.NumberFormat = "@"
        sht.range('A1').value = table

        if not os.path.exists('data/' + code + '_' + name):
            os.makedirs('data/' + code + '_' + name)
        path = 'data/' + code + '_' + name + '/' + code + '_' + name + '_公告列表.xlsx'

        wb.save(path)
        wb.close()

        # 4、下载PDF文件
        path = os.getcwd() + "\\data\\" + code + '_' + name + "\\"
        url_list = table['url'].tolist()
        print(f'【{code}】 获取到{len(url_list)}条公告，开始下载')
        self.download(url_list, path, code)

    def download(self, url_list: list, path: str, code):
        options = webdriver.ChromeOptions()
        prefs = {'profile.default_content_settings.popups': 0, 'download.default_directory': path,
                 "profile.default_content_setting_values.automatic_downloads": 1}
        options.add_experimental_option('prefs', prefs)
        # 隐藏浏览器界面
        options.add_argument('headless')
        browser = webdriver.Chrome(options=options)
        sleep_time = 3
        for index, url in enumerate(url_list):
            browser.get(url)
            time.sleep(sleep_time)
            try:
                browser.find_element(by=By.XPATH,
                                     value='//*[@id="noticeDetail"]/div/div[1]/div[3]/div[1]/button').click()
                print(f'【{code}】 ({index + 1}/{len(url_list)})下载完成，{url}')
                time.sleep(sleep_time)
            except:
                print(f'【{code}】 ({index + 1}/{len(url_list)})下载失败，{url}')
                sleep_time = sleep_time + 1 if sleep_time <= 4 else sleep_time
                pass
        browser.quit()

    def real_time_data(self, stock_tuple_list: list):
        options = webdriver.ChromeOptions()
        # 隐藏浏览器界面
        options.add_argument('headless')
        browser = webdriver.Chrome(options=options)
        table = pd.DataFrame()

        for stock in stock_tuple_list:
            row = pd.DataFrame(self.real_time_data_item(stock, browser), index=[1])
            table = pd.concat([table, row], join='outer', ignore_index=True)
        browser.quit()
        table = table.set_index('名称')

        wb = self.app.books.add()
        sht = wb.sheets.add("实时数据")
        sht.range("B:B").api.NumberFormat = "@"
        sht.range("C:C").api.NumberFormat = "@"
        sht.range('A1').value = table

        if not os.path.exists('data/'):
            os.makedirs('data/')
        wb.save(f'data/{str(date.today())}_实时数据.xlsx')
        wb.close()

    def real_time_data_item(self, stock_tuple: tuple, browser: webdriver.Chrome):
        orgId, name, code = stock_tuple
        print(f'【{code}】 开始查询实时数据')
        browser.get(self.query_url.format(code, orgId))
        time.sleep(5)

        ele = browser.find_element(by=By.XPATH, value='//div[@class="news-header"]')
        price = ele.find_element(by=By.XPATH, value='./div[@class="news-header-left"]/div[2]/div[1]').text
        up_and_down_value = ele.find_element(by=By.XPATH,
                                             value='./div[@class="news-header-left"]/div[2]/div[2]/div[1]/div[1]').text
        up_and_down_range = ele.find_element(by=By.XPATH,
                                             value='./div[@class="news-header-left"]/div[2]/div[2]/div[1]/div[2]').text
        date_time = ele.find_element(by=By.XPATH,
                                     value='./div[@class="news-header-left"]/div[2]/div[3]/span[@class="date"]').text

        ele_list = browser.find_elements(by=By.XPATH,
                                         value='//div[@class="news-header"]/div[@class="right-item-father"]/div[@class="right-item"]')
        table_row = {
            '代码': code,
            '名称': name,
            '日期': date_time,
            '价格': price,
            '涨跌额': up_and_down_value,
            '涨跌幅': up_and_down_range
        }

        key_patten = '<span style="float: left;">(.*?)</span>'
        value_patten = '<span style="float: right;">(.*?)</span>'
        for ele in ele_list:
            data = ele.get_attribute('innerHTML')
            key = re.findall(key_patten, data)[0]
            value = re.findall(value_patten, data)[0]
            table_row[key] = value

        return table_row

    def run(self):
        seDate = ''
        stock_str = ''
        stock_list = []
        stock_file_path = ''
        flag = '0'
        while flag != '1' and flag != '2':
            flag = input("请选择输入模式：1、控制台输入；2、文件读取")
            if flag == '2':
                while stock_file_path == '':
                    stock_file_path = input("请输入文件路径，示例：stock.txt")
                    try:
                        with open(stock_file_path, "r") as f:
                            stock_list = f.read().splitlines()
                    except FileNotFoundError:
                        print("文件不存在，请重新输入")
                        stock_file_path = ''

        while stock_str == '' and len(stock_list) == 0:
            stock_str = input("请输入股票代码，示例：000001,000002")
            stock_list = stock_str.split(',')

        dateRex = "([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))[~]([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3})-(((0[13578]|1[02])-(0[1-9]|[12][0-9]|3[01]))|((0[469]|11)-(0[1-9]|[12][0-9]|30))|(02-(0[1-9]|[1][0-9]|2[0-8])))"
        while seDate == '':
            seDate = input('请输入起始日期，示例：2020-09-01~2021-01-01')
            if not re.match(dateRex, seDate):
                print("日期格式不正确，请重新输入")
                seDate = ''

        valid_shsz_stock, valid_hk_stock = self.remove_invalid_stock(stock_list)
        valid_stock = valid_shsz_stock + valid_hk_stock

        if len(valid_hk_stock) == 0 and len(valid_shsz_stock) == 0:
            print(f'获取到 0 条有效股票代码，请重新输入')
            self.run()
            return

        pythoncom.CoInitialize()
        self.real_time_data(valid_shsz_stock)

        for stock in valid_shsz_stock:
            self.history_data(stock, seDate)

        for stock in valid_stock:
            self.download_report(stock)

        self.app.quit()
        pythoncom.CoUninitialize()



if __name__ == '__main__':
    logger = logging.getLogger('CnInfoReports')
    logger.setLevel(logging.DEBUG)
    CnInfoReports().run()
    sys.exit()
