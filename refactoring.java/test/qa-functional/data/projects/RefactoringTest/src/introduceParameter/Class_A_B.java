package introduceParameter;

public class Class_A_B implements Runnable{
        
    private int field = 4;
    
    public Class_A_B() {
        int x = field;
    }
       
    public void m(int param, String... varargs ) {
        String x = "ABC";
        String y = "ABC";
        int z = 12345;
    }
    
    public void run() {
        int x = 3;
    }
    
    
    class Super {
        public void m1() {
            int y = 3;
        }
    }
    
    class Sub extends Super {
        public void m1() {
            int x = 3;
        }
    }
    
    public void x() {
        int i = field;
    }
    
    public void y() {
        long i = System.currentTimeMillis();
    }
    
    class Generics<T> {
        public void genMethod() {
            T t = null;
        }
    }
    
    public void usage() {
        new Class_A_B();        
        m(1,"","");
        new Super().m1();
        new Sub().m1();
        y();
    
    }                                             
}
