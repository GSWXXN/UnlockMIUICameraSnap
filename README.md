<img src="./doc/CameraSnap.svg" width="200" alt="icon">

# UnlockMIUICameraSnap
[![Xposed](https://img.shields.io/badge/-Xposed-green?style=flat&logo=Android&logoColor=white)](#)
[![GitHub](https://img.shields.io/github/license/GSWXXN/UnlockMIUICameraSnap)](https://github.com/GSWXXN/UnlockMIUICameraSnap/blob/main/LICENSE)
[![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/GSWXXN/UnlockMIUICameraSnap?label=version)](https://github.com/Xposed-Modules-Repo/com.gswxxn.camerasnap/releases)
[![GitHub all releases](https://img.shields.io/github/downloads/Xposed-Modules-Repo/com.gswxxn.camerasnap/total?label=Downloads)](https://github.com/Xposed-Modules-Repo/com.gswxxn.camerasnap/releases)

解锁/补全 MIUI 街拍模式

## 测试环境

> 小米12S Ultra  
> Android 13  
> MIUI 14

## 模块功能
1. 为所有机型/相机板本开放街拍模式
2. 补全录像模式

## 使用方法

1. 在Xposed管理器(LSPosed)中激活模块
2. 作用域勾选相机
3. 强制停止相机

## 已知问题
* 录像途中可能会被打断重新录制, 经过调查后发现是系统向相机发送了音量下按键被抬起事件. 由于问题发生在系统内, 与相机无关, 所以暂时不打算解决.

## 无法使用

请先检查模块是否正常激活，并且作用域是否勾选。
如果排查后仍有错误，请提交issue，并附上 LSPosed 的日志.
酷安[@迷璐](http://www.coolapk.com/u/1189245)


## 致谢
使用 [Yuki Hook API](https://github.com/fankes/YukiHookAPI) 构建模块  
使用 [DexKit](https://github.com/LuckyPray/DexKit) 查找被混淆的方法