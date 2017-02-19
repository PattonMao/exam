# exam
 用maven构建的工程。先启动ServerApp，在启动CarApp。行进线路直接用代码模拟List构成，没有采用读取文件的方式。
CarActor代表一辆月球车，读取线路数据后，向Server发送数据。数据交互使用akka的actor。
