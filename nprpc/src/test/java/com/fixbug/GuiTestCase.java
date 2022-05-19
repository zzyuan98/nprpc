package com.fixbug;

/**
 *
 * 事件回调机制的例子
 *
 * 模拟界面类
 *
 * 接收用户发起的事件
 * 显示处理进度
 * 处理完成后显示结果
 *
 *
 * 需求1：下载完成后需要显示
 * 需求2：下载过程中显示下载进度
 * 在代码某处：a.该干什么事情了 b.这个事情怎么做 =》在次数直接调用一个函数就可以完成了
 *
 * @author zzyuan
 * @create 2022-04-05 16:40
 *
 */
public class GuiTestCase implements INotifyCallBack{

    public static void main(String[] args) {
        GuiTestCase guiTestCase = new GuiTestCase();
        guiTestCase.downLoadFile("我要学java");

    }

    private DownLoad downLoad;

    public GuiTestCase() {
        downLoad = new DownLoad(this);
    }

    /**
     *
     * 下载文件
     * @param file
     */
    public void downLoadFile(String file){
        System.out.println("begin start download file : " + file);
        downLoad.start(file);

    }

    /**
     * 显示下载进度的方法
     * @param file
     * @param process
     */
    public void progress(String file , int process){
        System.out.println("download file: " + file + " progress : " + process + "%");
    }


    /**
     * 文件下载完成了
     * @param file
     */
    public void result(String file){
        System.out.println("download file ：" + file + " over. ");
    }

}

//把需要上报的事件都定义在接口中
interface INotifyCallBack{
    void progress(String file , int progress);
    void result(String file);
}


/**
 * 负责下载的类
 */
class DownLoad{

    private INotifyCallBack cb;

    public DownLoad(INotifyCallBack cb){
        this.cb = cb;
    }


    /**
     * 底层的下载任务方法
     * @param file
     */
    public void start(String file){
        int count = 0;
        while (count <= 100){

            try {
                cb.progress(file,count);
                Thread.sleep(2000);
                count += 20;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cb.result(file);
    }

}