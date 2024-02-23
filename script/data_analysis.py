# -*- coding: utf-8 -*-
import pandas as pd
import numpy as np

# 读取1.log文件
df = pd.read_csv('/tmp/applog/TestDataTrans.log', sep=' ', header=None, names=['date1','time', 'mac_address', 'dataSize', 'latency','diff'])

# 将latency列转换为数值型
# df['latency'] = pd.to_numeric(df['latency'])

# 根据dataSize进行分组，计算latency的分位值
result = df.groupby('dataSize')['latency'].quantile([0.5, 0.9, 0.98, 0.99, 0.999, 0.9995, 0.9999,1])

# 转置结果
result_transposed = result.unstack()

# 打印转置后的结果
print(result_transposed)
