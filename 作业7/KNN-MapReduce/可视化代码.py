import matplotlib.pyplot as plt
from pylab import *
import numpy as np
mpl.rcParams['font.sans-serif'] = ['SimHei']

# names = ['5', '10', '15', '20', '25']
# x = [1,2,3,5,7,10,15,20]
# euclid = [1,0.977777,0.9555555,0.86666666,0.86666666,0.82222222,0.77777777,0.711111111]
# manhattan = [1,0.9333333,0.8888888,0.8666666,0.84444444,0.84444444,0.733333333,0.68888888]
# chebyshev = [1,0.9333333,0.9333333,0.888888,0.8666666,0.8,0.7777777,0.71111111]
# #plt.plot(x, y, 'ro-')
# #plt.plot(x, y1, 'bo-')
# #pl.xlim(-1, 11)  # 限定横轴的范围
# #pl.ylim(-1, 110)  # 限定纵轴的范围
# plt.plot(x, euclid, marker='o', mec='r', mfc='w',label=u'欧氏距离')
# plt.plot(x, manhattan, marker='*', ms=10,label=u'曼哈顿距离')
# plt.plot(x, chebyshev, label=u'切比雪夫距离')
# plt.legend()  # 让图例生效
# plt.xticks(x, rotation=45)
# plt.margins(0)
# plt.subplots_adjust(bottom=0.15)
# plt.xlabel(u"k值") #X轴标签
# plt.ylabel("Accuracy") #Y轴标签
# plt.title("k-Accuracy") #标题
#
# plt.show()

sepal_length = [5,5.5,4.9,4.4,5.1,5,4.5,4.4,5,5.1,4.8,5.1,4.6,5.3,5,6,6.7,6.3,
                5.6,5.5,5.5,6.1,5.8,5,5.6,5.7,5.7,6.2,5.1,5.7,7.7,6.3,6.4,
                6,6.9,6.7,6.9,5.8,6.8,6.7,6.7,6.3,6.5,6.2,5.9]
sepal_width = [3.2,3.5,3.6,3,3.4,3.5,2.3,3.2,3.5,3.8,3,3.8,3.2,3.7,3.3,3.4,3.1,2.3,3,2.5,2.6,3,2.6,2.3,
               2.7,3,2.9,2.9,2.5,2.8,3,3.4,3.1,3,3.1,3.1,3.1,2.7,3.2,3.3,3,2.5,3,3.4,3]
petal_length = [1.2,1.3,1.4,1.3,1.5,1.3,1.3,1.3,1.6,1.9,1.4,1.6,1.4,1.5,1.4,4.5,4.7,4.4,4.1,4,4.4,
                4.6,4,3.3,4.2,4.2,4.2,4.3,3,4.1,6.1,5.6,5.5,4.8,5.4,5.6,5.1,5.1,5.9,5.7,5.2,5,5.2,5.4,5.1]
petal_width = [0.2,0.2,0.1,0.2,0.2,0.3,0.3,0.2,0.6,0.4,0.3,0.2,0.2,0.2,0.2,1.6,1.5,1.3,1.3,1.3,1.2,1.4,1.2,1,1.3,
               1.2,1.3,1.3,1.1,1.3,2.3,2.4,1.8,1.8,2.1,2.4,2.3,1.9,2.3,2.5,2.3,1.9,2,2.3,1.8]
sepal_width = np.array(sepal_width)
sepal_length = np.array(sepal_length)
petal_length = np.array(petal_length)
petal_width = np.array(petal_width)
sepal_width_min = sepal_width.min()
sepal_width_max = sepal_width.max()
sepal_length_min = sepal_length.min()
sepal_length_max = sepal_length.max()

N,M = 500,500
t1 = np.linspace(sepal_width_min,sepal_width_max,N)
t2 = np.linspace(sepal_length_min,sepal_length_max,M)

x1, x2 = np.meshgrid(t1, t2)                    # 生成网格采样点
x_test = np.stack((x1.flat, x2.flat), axis=1)

cm_light = mpl.colors.ListedColormap(['#77E0A0', '#FF8080', '#A0A0FF'])
cm_dark = mpl.colors.ListedColormap(['g', 'r', 'b'])

label_predicted = ["setosa"]*15 + ["versicolor"] * 15 + ["virginica"] * 3 + ["versicolor"] + ["virginica"] * 2 +\
                  ["versicolor"] * 2 + ["virginica"] * 3 + ["versicolor"] * 2 + ["virginica","versicolor"]
true_label = ["setosa"] * 15 + ["versicolor"] * 15 + ["virginica"] * 15
colors = []
for i in true_label:
    if i == "setosa":
        colors.append("red")
    elif i == "versicolor":
        colors.append("yellow")
    elif i == "virginica":
        colors.append("blue")
plt.subplot(4,4,1)
plt.scatter(sepal_length[0:15],sepal_length[0:15],c="red",s=8)
plt.scatter(sepal_length[15:30],sepal_length[15:30],c="yellow",s=8)
plt.scatter(sepal_length[30:45],sepal_length[30:45],c="blue",s=8)
# plt.xlabel("sepal length")
plt.ylabel("sepal length")

plt.subplot(4,4,5)
plt.scatter(sepal_length[0:15],sepal_width[0:15],c="red",s=8)
plt.scatter(sepal_length[15:30],sepal_width[15:30],c="yellow",s=8)
plt.scatter(sepal_length[30:45],sepal_width[30:45],c="blue",s=8)
# plt.xlabel("sepal length")
plt.ylabel("sepal width")

plt.subplot(4,4,9)
plt.scatter(sepal_length[0:15],petal_length[0:15],c="red",s=8)
plt.scatter(sepal_length[15:30],petal_length[15:30],c="yellow",s=8)
plt.scatter(sepal_length[30:45],petal_length[30:45],c="blue",s=8)
plt.ylabel("petal length")

plt.subplot(4,4,13)
plt.scatter(sepal_length[0:15],petal_width[0:15],c="red",s=8)
plt.scatter(sepal_length[15:30],petal_width[15:30],c="yellow",s=8)
plt.scatter(sepal_length[30:45],petal_width[30:45],c="blue",s=8)
plt.xlabel("sepal length")
plt.ylabel("petal width")

plt.subplot(4,4,2)
plt.scatter(sepal_width[0:15],sepal_length[0:15],c="red",s=8)
plt.scatter(sepal_width[15:30],sepal_length[15:30],c="yellow",s=8)
plt.scatter(sepal_width[30:45],sepal_length[30:45],c="blue",s=8)

plt.subplot(4,4,6)
plt.scatter(sepal_width[0:15],sepal_width[0:15],c="red",s=8)
plt.scatter(sepal_width[15:30],sepal_width[15:30],c="yellow",s=8)
plt.scatter(sepal_width[30:45],sepal_width[30:45],c="blue",s=8)

plt.subplot(4,4,10)
plt.scatter(sepal_width[0:15],petal_length[0:15],c="red",s=8)
plt.scatter(sepal_width[15:30],petal_length[15:30],c="yellow",s=8)
plt.scatter(sepal_width[30:45],petal_length[30:45],c="blue",s=8)

plt.subplot(4,4,14)
plt.scatter(sepal_width[0:15],petal_width[0:15],c="red",s=8)
plt.scatter(sepal_width[15:30],petal_width[15:30],c="yellow",s=8)
plt.scatter(sepal_width[30:45],petal_width[30:45],c="blue",s=8)
plt.xlabel("sepal width")

plt.subplot(4,4,3)
plt.scatter(petal_length[0:15],sepal_length[0:15],c="red",s=8)
plt.scatter(petal_length[15:30],sepal_length[15:30],c="yellow",s=8)
plt.scatter(petal_length[30:45],sepal_length[30:45],c="blue",s=8)

plt.subplot(4,4,7)
plt.scatter(petal_length[0:15],sepal_width[0:15],c="red",s=8)
plt.scatter(petal_length[15:30],sepal_width[15:30],c="yellow",s=8)
plt.scatter(petal_length[30:45],sepal_width[30:45],c="blue",s=8)

plt.subplot(4,4,11)
plt.scatter(petal_length[0:15],petal_length[0:15],c="red",s=8)
plt.scatter(petal_length[15:30],petal_length[15:30],c="yellow",s=8)
plt.scatter(petal_length[30:45],petal_length[30:45],c="blue",s=8)

plt.subplot(4,4,15)
plt.scatter(petal_length[0:15],petal_width[0:15],c="red",s=8)
plt.scatter(petal_length[15:30],petal_width[15:30],c="yellow",s=8)
plt.scatter(petal_length[30:45],petal_width[30:45],c="blue",s=8)
plt.xlabel("petal length")

plt.subplot(4,4,4)
plt.scatter(petal_width[0:15],sepal_length[0:15],c="red",s=8)
plt.scatter(petal_width[15:30],sepal_length[15:30],c="yellow",s=8)
plt.scatter(petal_width[30:45],sepal_length[30:45],c="blue",s=8)

plt.subplot(4,4,8)
plt.scatter(petal_width[0:15],sepal_width[0:15],c="red",s=8)
plt.scatter(petal_width[15:30],sepal_width[15:30],c="yellow",s=8)
plt.scatter(petal_width[30:45],sepal_width[30:45],c="blue",s=8)

plt.subplot(4,4,12)
plt.scatter(petal_width[0:15],petal_length[0:15],c="red",s=8)
plt.scatter(petal_width[15:30],petal_length[15:30],c="yellow",s=8)
plt.scatter(petal_width[30:45],petal_length[30:45],c="blue",s=8)

plt.subplot(4,4,16)
plt.scatter(petal_width[0:15],petal_width[0:15],c="red",s=8)
plt.scatter(petal_width[15:30],petal_width[15:30],c="yellow",s=8)
plt.scatter(petal_width[30:45],petal_width[30:45],c="blue",s=8)
plt.xlabel("petal width")

plt.legend(labels=["setosa","versicolor","virginica"],loc='lower right',fontsize=8)
plt.show()
print(x_test)





















