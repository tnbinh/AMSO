/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myUtils.analysis;
import java.lang.management.*;
/**
 *
 * @author tranbinh
 */
public class CPUTime {


/** Get CPU time in nanoseconds. */
public long getCpuTime( ) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    return bean.isCurrentThreadCpuTimeSupported( ) ?
        bean.getCurrentThreadCpuTime( ) : 0L;
}

/** Get user time in nanoseconds. */
public long getUserTime( ) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    return bean.isCurrentThreadCpuTimeSupported( ) ?
        bean.getCurrentThreadUserTime( ) : 0L;
}

/** Get system time in nanoseconds. */
public long getSystemTime( ) {
    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
    return bean.isCurrentThreadCpuTimeSupported( ) ?
        (bean.getCurrentThreadCpuTime() - bean.getCurrentThreadUserTime( )) : 0L;
}

public static void main(String[] args) throws Exception {

    CPUTime a = new CPUTime();
    long startCPUTimeNano = a.getCpuTime();
    long startUserTimeNano   = a.getUserTime( );
    long start = System.nanoTime();

        for(int i =0; i< 1E6; i++ ){
            System.out.printf("My nbr is: %d\n", i);
        }

        long taskUserTimeNano    = a.getUserTime( ) - startUserTimeNano;
        long taskCPUTimeNano  = a.getCpuTime() - startCPUTimeNano;

              System.out.printf("startSystemTimeNano: %s ns, startUserTimeNano: %s ns, "
                + "start real time nano: %s ns\n", startCPUTimeNano, startUserTimeNano,
                 start);

        System.out.printf("taskUserTimeNano: %s ns, taskSystemTimeNano: %s ns, "
                + "real time nano: %s ns\n", taskUserTimeNano, taskCPUTimeNano,
                 (System.nanoTime() - start));

        System.out.printf("taskUserTimeNano: %s ns, taskSystemTimeNano: %s ns, "
                + "real time nano: %s ns\n", a.getUserTime(), a.getSystemTime(),
                 (System.nanoTime() - start));

    }

}
