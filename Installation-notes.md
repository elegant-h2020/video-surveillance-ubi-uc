# Opencv Installation for the apache flink  surveillance-system example

* The basic installation guides: [install-opencv in ubuntu](https://www.360learntocode.com/2020/05/install-opencv-in-linuxubuntu-for-java.html)
and ["how to install opencv in ubuntu"](https://linuxize.com/post/how-to-install-opencv-on-ubuntu-18-04/)

* Despite downolading from source or building the library a major issue can be 
the "Cmake" part

* As the second article implies after executing the command 	
"cmake -DBUILD_SHARED_LIBS=OFF .." one should see  ant and JNI been configured

* If thats not the case, you might need to set the JAVA_HOME and ANT_HOME
inside /etc/profile (some articles imply to add them inside /etc/enviroment
which also works). Article that helped [building-native-libraries-not-working/](https://answers.opencv.org/question/188595/building-native-libraries-not-working/)
[how to build opencv with java](https://stackoverflow.com/questions/17386551/how-to-build-opencv-with-java-under-linux-using-command-linegonna-use-it-in-ma)

* Also in case that you have set these and still cmake does not find JAVA 
  [reinstalling both Ant and jdk helped](https://askubuntu.com/questions/961779/ant-for-java-not-showing-as-installed-but-it-is-installed)

* By using the mj<number of cores> command, the *jar and *so files are
  produced, which can be used for intelij

* In order to be able to use the OpenCV library, the *so files
should be moved in the java library path this is not related to Intelij been
able to perform autocomplete actions (https://stackoverflow.com/questions/37901505/java-opencv-from-maven?noredirect=1&lq=1)

* After adding the *so file under /usr/lib  the Classpathnotfound was solved

* In order to change the opencv jar https://stackoverflow.com/questions/57160735/how-to-use-opencv-build-opencv-and-opencv-contrib-in-a-maven-project
