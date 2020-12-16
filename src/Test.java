package sandbox.jfr;

import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordingStream;

import java.time.Duration;

/**
 * /Users/jz/.jabba/jdk/adopt@1.15.0-1/Contents/Home/bin/java -javaagent:/Users/jz/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.5981.155/IntelliJ IDEA 2020.3 EAP.app/Contents/lib/idea_rt.jar=60060:/Users/jz/Library/Application Support/JetBrains/Toolbox/apps/IDEA-U/ch-0/203.5981.155/IntelliJ IDEA 2020.3 EAP.app/Contents/bin -Dfile.encoding=UTF-8 -classpath /Users/jz/code/jfr-sandbox/out/production/jfr-sandbox:/Users/jz/.ivy2/cache/org.scala-lang/scala-reflect/jars/scala-reflect-2.13.3.jar:/Users/jz/.ivy2/cache/org.scala-lang/scala-library/jars/scala-library-2.13.3.jar sandbox.jfr.Test
 * jdk.NativeMethodSample {
 *   startTime = 13:39:47.023
 *   sampledThread = "Thread-0" (javaThreadId = 14)
 *   state = "STATE_RUNNABLE"
 *   stackTrace = [
 *     java.lang.Runtime.availableProcessors()
 *     sandbox.jfr.Test.nativeMethod() line: 46
 *     sandbox.jfr.Test.foo() line: 41
 *     java.lang.Thread.run() line: 832
 *     ...
 *   ]
 * }
 */
public class Test {

  final static String NATIVE_EVENT = "jdk.NativeMethodSample";

  static volatile boolean alive = true;

  public static void main(String[] args) throws Exception {
    Thread t = new Thread(Test::foo);
    t.setDaemon(true);
    t.start();
    try (RecordingStream rs = new RecordingStream()) {
      rs.enable(NATIVE_EVENT).withPeriod(Duration.ofMillis(1));
      rs.onEvent(NATIVE_EVENT, e -> {
        RecordedThread thread = e.getValue("sampledThread");
        if (thread != null && thread.getJavaThreadId() == t.getId() && alive) {
          assert (e.getStackTrace().getFrames().stream().anyMatch(x -> !x.isJavaFrame() && x.getMethod().getName().equals("availableProcessors")));
          System.out.println(e);
          alive = false;
          rs.close();
        }
      });
      rs.start();
    }

  }

  public static void foo() {
    nativeMethod();
  }

  public static void nativeMethod() {
    while (alive) {
      Runtime.getRuntime().availableProcessors();
    }
  }
}

